package com.tom.cpm.web.client.java.zip;

import java.util.Arrays;

import com.jcraft.jzlib.GZIPException;
import com.jcraft.jzlib.ZStream;

import elemental2.dom.DomGlobal;

@SuppressWarnings("deprecation")
public class Inflater {

	private final ZStreamRef zsRef;
	private byte[] buf = defaultBuf;
	private int off, len;
	private boolean finished;
	private boolean needDict;
	private long bytesRead;
	private long bytesWritten;

	private static final byte[] defaultBuf = new byte[0];

	/**
	 * Creates a new decompressor. If the parameter 'nowrap' is true then
	 * the ZLIB header and checksum fields will not be used. This provides
	 * compatibility with the compression format used by both GZIP and PKZIP.
	 * <p>
	 * Note: When using the 'nowrap' option it is also necessary to provide
	 * an extra "dummy" byte as input. This is required by the ZLIB native
	 * library in order to support certain optimizations.
	 *
	 * @param nowrap if true then support GZIP compatible compression
	 */
	public Inflater(boolean nowrap) {
		zsRef = new ZStreamRef(init(nowrap));
	}

	/**
	 * Creates a new decompressor.
	 */
	public Inflater() {
		this(false);
	}

	/**
	 * Sets input data for decompression. Should be called whenever
	 * needsInput() returns true indicating that more input data is
	 * required.
	 * @param b the input data bytes
	 * @param off the start offset of the input data
	 * @param len the length of the input data
	 * @see Inflater#needsInput
	 */
	public void setInput(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			this.buf = b;
			this.off = off;
			this.len = len;
		}
	}

	/**
	 * Sets input data for decompression. Should be called whenever
	 * needsInput() returns true indicating that more input data is
	 * required.
	 * @param b the input data bytes
	 * @see Inflater#needsInput
	 */
	public void setInput(byte[] b) {
		setInput(b, 0, b.length);
	}

	/**
	 * Sets the preset dictionary to the given array of bytes. Should be
	 * called when inflate() returns 0 and needsDictionary() returns true
	 * indicating that a preset dictionary is required. The method getAdler()
	 * can be used to get the Adler-32 value of the dictionary needed.
	 * @param b the dictionary data bytes
	 * @param off the start offset of the data
	 * @param len the length of the data
	 * @see Inflater#needsDictionary
	 * @see Inflater#getAdler
	 */
	public void setDictionary(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			ensureOpen();
			setDictionary(zsRef.address(), b, off, len);
			needDict = false;
		}
	}

	/**
	 * Sets the preset dictionary to the given array of bytes. Should be
	 * called when inflate() returns 0 and needsDictionary() returns true
	 * indicating that a preset dictionary is required. The method getAdler()
	 * can be used to get the Adler-32 value of the dictionary needed.
	 * @param b the dictionary data bytes
	 * @see Inflater#needsDictionary
	 * @see Inflater#getAdler
	 */
	public void setDictionary(byte[] b) {
		setDictionary(b, 0, b.length);
	}

	/**
	 * Returns the total number of bytes remaining in the input buffer.
	 * This can be used to find out what bytes still remain in the input
	 * buffer after decompression has finished.
	 * @return the total number of bytes remaining in the input buffer
	 */
	public int getRemaining() {
		synchronized (zsRef) {
			return len;
		}
	}

	/**
	 * Returns true if no data remains in the input buffer. This can
	 * be used to determine if #setInput should be called in order
	 * to provide more input.
	 * @return true if no data remains in the input buffer
	 */
	public boolean needsInput() {
		synchronized (zsRef) {
			return len <= 0;
		}
	}

	/**
	 * Returns true if a preset dictionary is needed for decompression.
	 * @return true if a preset dictionary is needed for decompression
	 * @see Inflater#setDictionary
	 */
	public boolean needsDictionary() {
		synchronized (zsRef) {
			return needDict;
		}
	}

	/**
	 * Returns true if the end of the compressed data stream has been
	 * reached.
	 * @return true if the end of the compressed data stream has been
	 * reached
	 */
	public boolean finished() {
		synchronized (zsRef) {
			return finished;
		}
	}

	/**
	 * Uncompresses bytes into specified buffer. Returns actual number
	 * of bytes uncompressed. A return value of 0 indicates that
	 * needsInput() or needsDictionary() should be called in order to
	 * determine if more input data or a preset dictionary is required.
	 * In the latter case, getAdler() can be used to get the Adler-32
	 * value of the dictionary required.
	 * @param b the buffer for the uncompressed data
	 * @param off the start offset of the data
	 * @param len the maximum number of uncompressed bytes
	 * @return the actual number of uncompressed bytes
	 * @exception DataFormatException if the compressed data format is invalid
	 * @see Inflater#needsInput
	 * @see Inflater#needsDictionary
	 */
	public int inflate(byte[] b, int off, int len)
			throws DataFormatException
	{
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			ensureOpen();
			int thisLen = this.len;
			int n = inflateBytes(zsRef.address(), b, off, len);
			bytesWritten += n;
			bytesRead += (thisLen - this.len);
			return n;
		}
	}

	/**
	 * Uncompresses bytes into specified buffer. Returns actual number
	 * of bytes uncompressed. A return value of 0 indicates that
	 * needsInput() or needsDictionary() should be called in order to
	 * determine if more input data or a preset dictionary is required.
	 * In the latter case, getAdler() can be used to get the Adler-32
	 * value of the dictionary required.
	 * @param b the buffer for the uncompressed data
	 * @return the actual number of uncompressed bytes
	 * @exception DataFormatException if the compressed data format is invalid
	 * @see Inflater#needsInput
	 * @see Inflater#needsDictionary
	 */
	public int inflate(byte[] b) throws DataFormatException {
		return inflate(b, 0, b.length);
	}

	/**
	 * Returns the ADLER-32 value of the uncompressed data.
	 * @return the ADLER-32 value of the uncompressed data
	 */
	public int getAdler() {
		synchronized (zsRef) {
			ensureOpen();
			return getAdler(zsRef.address());
		}
	}

	/**
	 * Returns the total number of compressed bytes input so far.
	 *
	 * <p>Since the number of bytes may be greater than
	 * Integer.MAX_VALUE, the {@link #getBytesRead()} method is now
	 * the preferred means of obtaining this information.</p>
	 *
	 * @return the total number of compressed bytes input so far
	 */
	public int getTotalIn() {
		return (int) getBytesRead();
	}

	/**
	 * Returns the total number of compressed bytes input so far.
	 *
	 * @return the total (non-negative) number of compressed bytes input so far
	 * @since 1.5
	 */
	public long getBytesRead() {
		synchronized (zsRef) {
			ensureOpen();
			return bytesRead;
		}
	}

	/**
	 * Returns the total number of uncompressed bytes output so far.
	 *
	 * <p>Since the number of bytes may be greater than
	 * Integer.MAX_VALUE, the {@link #getBytesWritten()} method is now
	 * the preferred means of obtaining this information.</p>
	 *
	 * @return the total number of uncompressed bytes output so far
	 */
	public int getTotalOut() {
		return (int) getBytesWritten();
	}

	/**
	 * Returns the total number of uncompressed bytes output so far.
	 *
	 * @return the total (non-negative) number of uncompressed bytes output so far
	 * @since 1.5
	 */
	public long getBytesWritten() {
		synchronized (zsRef) {
			ensureOpen();
			return bytesWritten;
		}
	}

	/**
	 * Resets inflater so that a new set of input data can be processed.
	 */
	public void reset() {
		synchronized (zsRef) {
			ensureOpen();
			reset(zsRef.address());
			buf = defaultBuf;
			finished = false;
			needDict = false;
			off = len = 0;
			bytesRead = bytesWritten = 0;
		}
	}

	/**
	 * Closes the decompressor and discards any unprocessed input.
	 * This method should be called when the decompressor is no longer
	 * being used, but will also be called automatically by the finalize()
	 * method. Once this method is called, the behavior of the Inflater
	 * object is undefined.
	 */
	public void end() {
		synchronized (zsRef) {
			ZStream addr = zsRef.address();
			zsRef.clear();
			if (addr != null) {
				end(addr);
				buf = null;
			}
		}
	}

	/**
	 * Closes the decompressor when garbage is collected.
	 */
	@Override
	protected void finalize() {
		end();
	}

	private void ensureOpen () {
		if (zsRef.address() == null)
			throw new NullPointerException("Inflater has been closed");
	}

	boolean ended() {
		synchronized (zsRef) {
			return zsRef.address() == null;
		}
	}

	private static ZStream init(boolean nowrap) {
		try {
			return new com.jcraft.jzlib.Inflater(nowrap);
		} catch (GZIPException e) {
			return null;
		}
	}
	private static void setDictionary(ZStream addr, byte[] b, int off,
			int len) {
		addr.inflateSetDictionary(Arrays.copyOfRange(b, off, len), len);
	}
	private int inflateBytes(ZStream addr, byte[] b, int off, int len)
			throws DataFormatException {
		int this_off = this.off;
		int this_len = this.len;
		byte[] this_buf = this.buf;
		addr.next_in = this_buf;
		addr.next_in_index = this_off;
		addr.avail_in = this_len;
		addr.next_out = b;
		addr.next_out_index = off;
		addr.avail_out = len;
		int ret = addr.inflate(1);
		switch (ret) {
		case 1:
			finished = true;
		case 0:
			this_off += this_len - addr.avail_in;
			this.off = this_off;
			this.len = addr.avail_in;
			return len - addr.avail_out;

		case 2:
			needDict = true;
			this_off += this_len - addr.avail_in;
			this.off = this_off;
			this.len = addr.avail_in;
			return 0;

		case -5:
			return 0;

		case -3:
			throw new DataFormatException(addr.msg);

		case -4:
			throw new RuntimeException("Out of Memory");

		default:
			throw new RuntimeException(addr.msg);
		}
	}
	private static int getAdler(ZStream addr) {
		return (int) addr.getAdler();
	}
	private static void reset(ZStream addr) {
		DomGlobal.console.error("Reset not supported");
	}
	private static void end(ZStream addr) {
		addr.inflateEnd();
	}
}
