package com.tom.cpm.shared.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Function;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;

public class IOHelper implements DataInput, DataOutput, Closeable {
	private static final int DIV = Short.MAX_VALUE / Vec3f.MAX_POS;
	private DataInputStream din;
	private DataOutputStream dout;
	private ByteArrayOutputStream baos;
	private byte[] dataIn;

	public IOHelper(DataInputStream din) {
		this.din = din;
	}

	public IOHelper(DataOutputStream dout) {
		this.dout = dout;
	}

	public IOHelper(InputStream din) {
		this.din = new DataInputStream(din);
	}

	public IOHelper(OutputStream dout) {
		this.dout = new DataOutputStream(dout);
		if(dout instanceof ByteArrayOutputStream)baos = (ByteArrayOutputStream) dout;
	}

	public IOHelper(byte[] data) {
		this(new FastByteArrayInputStream(data));
		this.dataIn = data;
	}

	public IOHelper() {
		this(new ByteArrayOutputStream());
	}

	public IOHelper(String b64) {
		this(Base64.getDecoder().decode(b64));
	}

	public int read() throws IOException {
		return din.read();
	}

	@Override
	public final void readFully(byte[] b) throws IOException {
		din.readFully(b);
	}

	@Override
	public void close() throws IOException {
		if(din != null)din.close();
		if(dout != null)dout.close();
	}

	@Override
	public final void readFully(byte[] b, int off, int len) throws IOException {
		din.readFully(b, off, len);
	}

	@Override
	public final boolean readBoolean() throws IOException {
		return din.readBoolean();
	}

	@Override
	public final byte readByte() throws IOException {
		return din.readByte();
	}

	@Override
	public final int readUnsignedByte() throws IOException {
		return din.readUnsignedByte();
	}

	@Override
	public final short readShort() throws IOException {
		return din.readShort();
	}

	@Override
	public final int readUnsignedShort() throws IOException {
		return din.readUnsignedShort();
	}

	@Override
	public final char readChar() throws IOException {
		return din.readChar();
	}

	@Override
	public final int readInt() throws IOException {
		return din.readInt();
	}

	@Override
	public final long readLong() throws IOException {
		return din.readLong();
	}

	@Override
	public final float readFloat() throws IOException {
		return din.readFloat();
	}

	@Override
	public final double readDouble() throws IOException {
		return din.readDouble();
	}

	@Override
	public final String readUTF() throws IOException {
		int i = readVarInt();
		if(i < 0)throw new IOException();
		if(i == 0)return "";
		byte[] s = new byte[i];
		readFully(s);
		return new String(s, StandardCharsets.UTF_8);
	}

	@Override
	public void write(int b) throws IOException {
		dout.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		dout.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dout.write(b, off, len);
	}

	@Override
	public final void writeBoolean(boolean v) throws IOException {
		dout.writeBoolean(v);
	}

	@Override
	public final void writeByte(int v) throws IOException {
		dout.writeByte(v);
	}

	@Override
	public final void writeShort(int v) throws IOException {
		dout.writeShort(v);
	}

	@Override
	public final void writeChar(int v) throws IOException {
		dout.writeChar(v);
	}

	@Override
	public final void writeInt(int v) throws IOException {
		dout.writeInt(v);
	}

	@Override
	public final void writeLong(long v) throws IOException {
		dout.writeLong(v);
	}

	@Override
	public final void writeFloat(float v) throws IOException {
		dout.writeFloat(v);
	}

	@Override
	public final void writeDouble(double v) throws IOException {
		dout.writeDouble(v);
	}

	@Override
	public final void writeBytes(String s) throws IOException {
		dout.writeBytes(s);
	}

	@Override
	public final void writeChars(String s) throws IOException {
		dout.writeChars(s);
	}

	@Override
	public final void writeUTF(String s) throws IOException {
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		writeVarInt(b.length);
		write(b);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return din.skipBytes(n);
	}

	@Override
	@Deprecated
	public String readLine() throws IOException {
		return din.readLine();
	}

	public void writeVarInt(int toWrite) throws IOException {
		while ((toWrite & -128) != 0) {
			dout.writeByte(toWrite & 127 | 128);
			toWrite >>>= 7;
		}

		dout.writeByte(toWrite);
	}

	public int readVarInt() throws IOException {
		int i = 0;
		int j = 0;
		byte b0;

		do {
			b0 = din.readByte();
			i |= (b0 & 127) << j++ * 7;

			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		}
		while ((b0 & 128) == 128);

		return i;
	}

	public Vec3f readVec3ub() throws IOException {
		Vec3f v = new Vec3f();
		v.x = din.read() / 10f;
		v.y = din.read() / 10f;
		v.z = din.read() / 10f;
		return v;
	}

	public void writeVec3ub(Vec3f v) throws IOException {
		dout.write(MathHelper.clamp((int) (v.x * 10), 0, 255));
		dout.write(MathHelper.clamp((int) (v.y * 10), 0, 255));
		dout.write(MathHelper.clamp((int) (v.z * 10), 0, 255));
	}

	public Vec3f readVec3b() throws IOException {
		Vec3f v = new Vec3f();
		v.x = din.readByte() / 10f;
		v.y = din.readByte() / 10f;
		v.z = din.readByte() / 10f;
		return v;
	}

	public void writeVec3b(Vec3f v) throws IOException {
		dout.writeByte(MathHelper.clamp((int) (v.x * 10), Byte.MIN_VALUE, Byte.MAX_VALUE));
		dout.writeByte(MathHelper.clamp((int) (v.y * 10), Byte.MIN_VALUE, Byte.MAX_VALUE));
		dout.writeByte(MathHelper.clamp((int) (v.z * 10), Byte.MIN_VALUE, Byte.MAX_VALUE));
	}

	public Vec3f readAngle() throws IOException {
		Vec3f v = new Vec3f();
		v.x = (float) (din.readShort() / 65535f * 2 * Math.PI);
		v.y = (float) (din.readShort() / 65535f * 2 * Math.PI);
		v.z = (float) (din.readShort() / 65535f * 2 * Math.PI);
		return v;
	}

	public void writeAngle(Vec3f v) throws IOException {
		dout.writeShort(MathHelper.clamp((int) (v.x / 360f * 65535), 0, 65535));
		dout.writeShort(MathHelper.clamp((int) (v.y / 360f * 65535), 0, 65535));
		dout.writeShort(MathHelper.clamp((int) (v.z / 360f * 65535), 0, 65535));
	}

	public Vec3f readVec6b() throws IOException {
		Vec3f v = new Vec3f();
		v.x = readFloat2();
		v.y = readFloat2();
		v.z = readFloat2();
		return v;
	}

	public void writeVec6b(Vec3f v) throws IOException {
		writeFloat2(v.x);
		writeFloat2(v.y);
		writeFloat2(v.z);
	}

	public float readFloat2() throws IOException {
		return din.readShort() / (float) DIV;
	}

	public void writeFloat2(float f) throws IOException {
		dout.writeShort(MathHelper.clamp((int) (f * DIV), Short.MIN_VALUE, Short.MAX_VALUE));
	}

	public IOHelper readNextBlock() throws IOException {
		int size = readVarInt();
		if(size > 1024*1024 || size < 0)throw new IOException();
		byte[] dt = new byte[size];
		readFully(dt);
		return new IOHelper(dt);
	}

	public IOHelper writeNextBlock() {
		return new IOHelper(new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				writeVarInt(count);
				writeTo(dout);
			}
		});
	}

	public Vec2i read2s() throws IOException {
		return new Vec2i(din.readShort(), din.readShort());
	}

	public void write2s(Vec2i v) throws IOException {
		dout.writeShort(v.x);
		dout.writeShort(v.y);
	}

	public DataInputStream getDin() {
		return din;
	}

	public DataOutputStream getDout() {
		return dout;
	}

	public <T extends Enum<T>> void writeEnum(T val) throws IOException {
		writeByte(val.ordinal());
	}

	public <T extends Enum<T>> T readEnum(T[] values) throws IOException {
		int id = readByte();
		return id >= values.length || id < 0 ? null : values[id];
	}

	public void writeUUID(UUID uuid) throws IOException {
		writeLong(uuid.getLeastSignificantBits());
		writeLong(uuid.getMostSignificantBits());
	}

	public UUID readUUID() throws IOException {
		long l = readLong();
		long m = readLong();
		return new UUID(m, l);
	}

	public <T extends Enum<T>, B> B readObjectBlock(T[] values, ObjectReader<T, B> reader) throws IOException {
		T v = readEnum(values);
		IOHelper b = readNextBlock();
		if(v == null)return null;
		else return reader.read(v, b);
	}

	public <T extends Enum<T>, B extends ObjectBlock<T>> void writeObjectBlock(B val) throws IOException {
		writeEnum(val.getType());
		try (IOHelper h = writeNextBlock()) {
			val.write(h);
		}
	}

	public <T extends Enum<T>, B> void writeObjectBlock(B val, Function<B, T> type, ObjectWriter<B> writer) throws IOException {
		T t = type.apply(val);
		writeEnum(t);
		try (IOHelper h = writeNextBlock()) {
			writer.write(val, h);
		}
	}

	public void reset() throws IOException {
		din.reset();
	}

	public ImageBlock readImage() throws IOException {
		return new ImageBlock(this);
	}

	public void writeImage(Image img) throws IOException {
		try(IOHelper h = writeNextBlock()) {
			img.storeTo(h.getDout());
		}
	}

	public void writeBlock(IOHelper to) throws IOException {
		if(dataIn != null) {
			to.writeVarInt(dataIn.length);
			to.write(dataIn);
			return;
		}
		if(baos == null)throw new IOException("Not a byte array backed io handler");
		to.writeVarInt(baos.size());
		baos.writeTo(to.dout);
	}

	public void writeByteArray(byte[] dataIn) throws IOException {
		writeVarInt(dataIn.length);
		write(dataIn);
	}

	public byte[] readByteArray() throws IOException {
		int i = readVarInt();
		if(i < 0)throw new IOException();
		if(i == 0)return new byte[0];
		byte[] s = new byte[i];
		readFully(s);
		return s;
	}

	public String toB64() throws IOException {
		if(baos == null)throw new IOException("Not a byte array backed io handler");
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	public byte[] toBytes() throws IOException {
		if(baos == null)throw new IOException("Not a byte array backed io handler");
		return baos.toByteArray();
	}

	public IOHelper flip() throws IOException {
		return new IOHelper(toBytes());
	}

	public int size() throws IOException {
		if(dataIn != null)return dataIn.length;
		if(baos != null)return baos.size();
		throw new IOException("Not a byte array backed io handler");
	}

	@FunctionalInterface
	public static interface ObjectReader<T, R> {
		R read(T type, IOHelper block) throws IOException;
	}

	public static interface ObjectWriter<B> {
		void write(B data, IOHelper out) throws IOException;
	}

	public static interface ObjectBlock<T extends Enum<T>> {
		void write(IOHelper h) throws IOException;
		T getType();
	}

	public static class ImageBlock {
		private IOHelper buf;
		private Image image;
		private int w, h;

		public ImageBlock(IOHelper io) throws IOException {
			buf = io.readNextBlock();
			if(buf.dataIn.length != 0) {
				Vec2i size = ImageIO.getSize(buf.getDin());
				w = size.x;
				h = size.y;
				buf.reset();
			}
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}

		public void doReadImage() throws IOException {
			if(buf.dataIn.length != 0) {
				image = Image.loadFrom(buf.getDin());
				w = image.getWidth();
				h = image.getHeight();
			}
		}

		public Image getImage() {
			return image;
		}
	}
}
