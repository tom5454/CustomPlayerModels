package com.tom.cpm.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.renderer.texture.NativeImage;

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
		NativeImage ni = NativeImage.read(f);
		Image i = new Image(ni.getWidth(), ni.getHeight());
		for(int y = 0;y<ni.getHeight();y++) {
			for(int x = 0;x<ni.getWidth();x++) {
				int rgb = ni.getPixelRGBA(x, y);
				int a = (rgb >> 24 & 255);
				int b = (rgb >> 16 & 255);
				int g = (rgb >> 8 & 255);
				int r = (rgb & 255);
				i.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return i;
	}

	@Override
	public void write(Image img, File f) throws IOException {
		createFromBufferedImage(img).writeToFile(f);
	}

	@Override
	public void write(Image img, OutputStream f) throws IOException {
		WriteCallback wc = new WriteCallback(Channels.newChannel(f));
		NativeImage ni = createFromBufferedImage(img);

		try {
			if (!STBImageWrite.stbi_write_png_to_func(wc, 0L, ni.getWidth(), ni.getHeight(), ni.format().components(), MemoryUtil.memByteBuffer(ni.pixels, ni.size), 0)) {
				throw new IOException("Could not write image: " + STBImage.stbi_failure_reason());
			}
		} finally {
			wc.free();
		}

		wc.throwIfException();
	}

	@Override
	public Vec2i getSize(InputStream din) throws IOException {
		ByteBuffer byteBufferIn = null;
		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			byteBufferIn = TextureUtil.readResource(din);
			((Buffer)byteBufferIn).rewind();
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
				int a = (rgb >> 24 & 255);
				int r = (rgb >> 16 & 255);
				int g = (rgb >> 8 & 255);
				int b = (rgb & 255);
				ni.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
			}
		}
		return ni;
	}

	static class WriteCallback extends STBIWriteCallback {
		private final WritableByteChannel output;
		private IOException exception;

		private WriteCallback(WritableByteChannel p_i49388_1_) {
			this.output = p_i49388_1_;
		}

		@Override
		public void invoke(long p_invoke_1_, long p_invoke_3_, int p_invoke_5_) {
			ByteBuffer bytebuffer = getData(p_invoke_3_, p_invoke_5_);

			try {
				this.output.write(bytebuffer);
			} catch (IOException ioexception) {
				this.exception = ioexception;
			}

		}

		public void throwIfException() throws IOException {
			if (this.exception != null) {
				throw this.exception;
			}
		}
	}
}
