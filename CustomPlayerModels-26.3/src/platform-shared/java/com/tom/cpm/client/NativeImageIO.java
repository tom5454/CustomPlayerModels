package com.tom.cpm.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spng.SPNG;
import org.lwjgl.util.spng.spng_ihdr;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;

public class NativeImageIO implements IImageIO {

	@Override
	public Image read(File f) throws IOException {
		try (FileInputStream fi = new FileInputStream(f)){
			return read(fi);
		}
	}

	@Override
	public Image read(InputStream f) throws IOException {
		try (NativeImage ni = NativeImage.read(f)) {
			Image i = new Image(ni.getWidth(), ni.getHeight());
			for(int y = 0;y<ni.getHeight();y++) {
				for(int x = 0;x<ni.getWidth();x++) {
					int rgb = ni.getPixel(x, y);
					i.setRGB(x, y, rgb);
				}
			}
			return i;
		}
	}

	@Override
	public void write(Image img, File f) throws IOException {
		try (NativeImage i = createFromBufferedImage(img)) {
			i.writeToFile(f);
		}
	}

	@Override
	public void write(Image img, OutputStream f) throws IOException {
		try (Arena arena = Arena.ofConfined();
				MemoryStack stack = MemoryStack.stackPush();
				WritableByteChannel channel = Channels.newChannel(f);
				SPNGContext spng = new SPNGContext()) {
			int width = img.getWidth();
			int height = Math.min(img.getHeight(), Integer.MAX_VALUE / width / 4);

			int numPixels = width * height;
			MemorySegment rgbaBuffer = arena.allocate((long) numPixels * 4, 1);
			for (int i = 0; i < numPixels; i++) {
				int argb = img.getData()[i];

				byte a = (byte) ((argb >> 24) & 0xFF);
				byte r = (byte) ((argb >> 16) & 0xFF);
				byte g = (byte) ((argb >> 8) & 0xFF);
				byte b = (byte) (argb & 0xFF);

				long offset = i * 4L;
				rgbaBuffer.set(ValueLayout.JAVA_BYTE, offset, r);      // R
				rgbaBuffer.set(ValueLayout.JAVA_BYTE, offset + 1, g);  // G
				rgbaBuffer.set(ValueLayout.JAVA_BYTE, offset + 2, b);  // B
				rgbaBuffer.set(ValueLayout.JAVA_BYTE, offset + 3, a);  // A
			}

			WriteCallback writer = new WriteCallback(channel);
			spng.setStream(writer, arena);
			spng.setHeader(stack, width, height);
			spng.write(rgbaBuffer);
			writer.throwIfException();
		}
	}

	@Override
	public Vec2i getSize(InputStream din) throws IOException {
		ByteBuffer byteBufferIn = null;
		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			byteBufferIn = TextureUtil.readResource(din);
			byteBufferIn.rewind();
			IntBuffer intbuffer = memorystack.mallocInt(1);
			IntBuffer intbuffer1 = memorystack.mallocInt(1);
			IntBuffer intbuffer2 = memorystack.mallocInt(1);
			if(!STBImage.stbi_info_from_memory(byteBufferIn, intbuffer, intbuffer1, intbuffer2)) {
				throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
			}
			return new Vec2i(intbuffer.get(0), intbuffer1.get(0));
		} finally {
			MemoryUtil.memFree(byteBufferIn);
		}
	}

	public static NativeImage createFromBufferedImage(Image texture) {
		NativeImage ni = new NativeImage(texture.getWidth(), texture.getHeight(), false);
		for(int y = 0;y<texture.getHeight();y++) {
			for(int x = 0;x<texture.getWidth();x++) {
				int rgb = texture.getRGB(x, y);
				ni.setPixel(x, y, rgb);
			}
		}
		return ni;
	}

	private static class WriteCallback {
		private static final Linker LINKER = Linker.nativeLinker();
		private static final FunctionDescriptor CALLBACK_DESC = FunctionDescriptor.of(
				ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_BYTE), ValueLayout.JAVA_LONG
				);
		private static final MethodHandle CALLBACK_FN = findCallbackFn();
		private final WritableByteChannel output;
		private IOException exception;

		private static MethodHandle findCallbackFn() {
			try {
				return MethodHandles.lookup()
						.findVirtual(
								WriteCallback.class, "invoke", MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class, MemorySegment.class, long.class)
								);
			} catch (Exception var1) {
				throw new IllegalStateException(var1);
			}
		}

		private WriteCallback(final WritableByteChannel output) {
			this.output = output;
		}

		public MemorySegment createUpcall(final Arena arena) {
			return LINKER.upcallStub(CALLBACK_FN.bindTo(this), CALLBACK_DESC, arena);
		}

		@SuppressWarnings("unused")
		public int invoke(final MemorySegment ctx, final MemorySegment user, final MemorySegment dest, final long length) {
			ByteBuffer dataBuf = dest.reinterpret(length).asByteBuffer();

			try {
				this.output.write(dataBuf);
				return SPNG.SPNG_OK;
			} catch (IOException var8) {
				this.exception = var8;
				return SPNG.SPNG_IO_ERROR;
			}
		}

		public void throwIfException() throws IOException {
			if (this.exception != null) {
				throw this.exception;
			}
		}
	}

	private static class SPNGContext implements AutoCloseable {
		private long context;

		public SPNGContext() {
			context = SPNG.spng_ctx_new(SPNG.SPNG_CTX_ENCODER);
			if (this.context == 0L) {
				throw new OutOfMemoryError("Failed to allocate SPNG context");
			}
		}

		public void write(MemorySegment rgbaBuffer) throws IOException {
			checkClosed();
			checkSpngError("write image", SPNG.nspng_encode_image(context, rgbaBuffer.address(), rgbaBuffer.byteSize(), SPNG.SPNG_FMT_PNG, SPNG.SPNG_ENCODE_FINALIZE));
		}

		public void setHeader(MemoryStack stack, int width, int height) throws IOException {
			checkClosed();
			spng_ihdr header = spng_ihdr.calloc(stack).width(width).height(height).color_type(SPNG.SPNG_COLOR_TYPE_TRUECOLOR_ALPHA).bit_depth((byte)8);
			checkSpngError("set header", SPNG.spng_set_ihdr(context, header));
		}

		public void setStream(WriteCallback writer, Arena arena) throws IOException {
			checkClosed();
			MemorySegment writerUpcall = writer.createUpcall(arena);
			checkSpngError("set output", SPNG.nspng_set_png_stream(context, writerUpcall.address(), 0L));
		}

		private static void checkSpngError(final String operation, final int result) throws IOException {
			if (result != 0) {
				throw new IOException("SPNG operation '" + operation + "' failed: " + SPNG.spng_strerror(result) + " (" + result + ")");
			}
		}

		private void checkClosed() {
			if (this.context == 0L) {
				throw new IllegalStateException("SPNGContext has already been closed");
			}
		}

		@Override
		public void close() throws IOException {
			if (this.context != 0L) {
				long ptr = this.context;
				this.context = 0L;
				SPNG.spng_ctx_free(ptr);
			}
		}
	}
}
