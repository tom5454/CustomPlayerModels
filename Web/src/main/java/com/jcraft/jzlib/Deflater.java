package com.jcraft.jzlib;

final public class Deflater extends ZStream {

	static final private int MAX_WBITS = 15; // 32K LZ77 window
	static final private int DEF_WBITS = MAX_WBITS;

	static final private int Z_NO_FLUSH = 0;
	static final private int Z_PARTIAL_FLUSH = 1;
	static final private int Z_SYNC_FLUSH = 2;
	static final private int Z_FULL_FLUSH = 3;
	static final private int Z_FINISH = 4;

	static final private int MAX_MEM_LEVEL = 9;

	static final private int Z_OK = 0;
	static final private int Z_STREAM_END = 1;
	static final private int Z_NEED_DICT = 2;
	static final private int Z_ERRNO = -1;
	static final private int Z_STREAM_ERROR = -2;
	static final private int Z_DATA_ERROR = -3;
	static final private int Z_MEM_ERROR = -4;
	static final private int Z_BUF_ERROR = -5;
	static final private int Z_VERSION_ERROR = -6;

	private boolean finished = false;

	public Deflater() {
		super();
	}

	public Deflater(int level) throws GZIPException {
		this(level, MAX_WBITS);
	}

	public Deflater(int level, boolean nowrap) throws GZIPException {
		this(level, MAX_WBITS, nowrap);
	}

	public Deflater(int level, int bits) throws GZIPException {
		this(level, bits, false);
	}

	public Deflater(int level, int bits, boolean nowrap) throws GZIPException {
		super();
		int ret = init(level, bits, nowrap);
		if (ret != Z_OK) throw new GZIPException(ret + ": " + msg);
	}

	public Deflater(int level, int bits, int memlevel, JZlib.WrapperType wrapperType) throws GZIPException {
		super();
		int ret = init(level, bits, memlevel, wrapperType);
		if (ret != Z_OK) throw new GZIPException(ret + ": " + msg);
	}

	public Deflater(int level, int bits, int memlevel) throws GZIPException {
		super();
		int ret = init(level, bits, memlevel);
		if (ret != Z_OK) throw new GZIPException(ret + ": " + msg);
	}

	public int init(int level) {
		return init(level, MAX_WBITS);
	}

	public int init(int level, boolean nowrap) {
		return init(level, MAX_WBITS, nowrap);
	}

	public int init(int level, int bits) {
		return init(level, bits, false);
	}

	public int init(int level, int bits, int memlevel, JZlib.WrapperType wrapperType) {
		if (bits < 9 || bits > 15) {
			return Z_STREAM_ERROR;
		}
		if (wrapperType == JZlib.W_NONE) {
			bits *= -1;
		} else if (wrapperType == JZlib.W_GZIP) {
			bits += 16;
		} else if (wrapperType == JZlib.W_ANY) {
			return Z_STREAM_ERROR;
		} else if (wrapperType == JZlib.W_ZLIB) {
		}
		return init(level, bits, memlevel);
	}

	public int init(int level, int bits, int memlevel) {
		finished = false;
		dstate = new Deflate(this);
		return dstate.deflateInit(level, bits, memlevel);
	}

	public int init(int level, int bits, boolean nowrap) {
		finished = false;
		dstate = new Deflate(this);
		return dstate.deflateInit(level, nowrap ? -bits : bits);
	}

	public int deflate(int flush) {
		if (dstate == null) {
			return Z_STREAM_ERROR;
		}
		int ret = dstate.deflate(flush);
		if (ret == Z_STREAM_END) finished = true;
		return ret;
	}

	public int end() {
		finished = true;
		if (dstate == null) return Z_STREAM_ERROR;
		int ret = dstate.deflateEnd();
		dstate = null;
		free();
		return ret;
	}

	public int params(int level, int strategy) {
		if (dstate == null) return Z_STREAM_ERROR;
		return dstate.deflateParams(level, strategy);
	}

	public int setDictionary(byte[] dictionary, int dictLength) {
		if (dstate == null) return Z_STREAM_ERROR;
		return dstate.deflateSetDictionary(dictionary, dictLength);
	}

	public boolean finished() {
		return finished;
	}

	public int copy(Deflater src) {
		this.finished = src.finished;
		return Deflate.deflateCopy(this, src);
	}
}
