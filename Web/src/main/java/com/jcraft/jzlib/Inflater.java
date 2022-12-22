package com.jcraft.jzlib;

final public class Inflater extends ZStream {

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

	public Inflater() {
		super();
		init();
	}

	public Inflater(JZlib.WrapperType wrapperType) throws GZIPException {
		this(DEF_WBITS, wrapperType);
	}

	public Inflater(int w, JZlib.WrapperType wrapperType) throws GZIPException {
		super();
		int ret = init(w, wrapperType);
		if (ret != Z_OK) throw new GZIPException(ret + ": " + msg);
	}

	public Inflater(int w) throws GZIPException {
		this(w, false);
	}

	public Inflater(boolean nowrap) throws GZIPException {
		this(DEF_WBITS, nowrap);
	}

	public Inflater(int w, boolean nowrap) throws GZIPException {
		super();
		int ret = init(w, nowrap);
		if (ret != Z_OK) throw new GZIPException(ret + ": " + msg);
	}

	private boolean finished = false;

	public int init() {
		return init(DEF_WBITS);
	}

	public int init(JZlib.WrapperType wrapperType) {
		return init(DEF_WBITS, wrapperType);
	}

	public int init(int w, JZlib.WrapperType wrapperType) {
		boolean nowrap = false;
		if (wrapperType == JZlib.W_NONE) {
			nowrap = true;
		} else if (wrapperType == JZlib.W_GZIP) {
			w += 16;
		} else if (wrapperType == JZlib.W_ANY) {
			w |= Inflate.INFLATE_ANY;
		} else if (wrapperType == JZlib.W_ZLIB) {
		}
		return init(w, nowrap);
	}

	public int init(boolean nowrap) {
		return init(DEF_WBITS, nowrap);
	}

	public int init(int w) {
		return init(w, false);
	}

	public int init(int w, boolean nowrap) {
		finished = false;
		istate = new Inflate(this);
		return istate.inflateInit(nowrap ? -w : w);
	}

	public int inflate(int f) {
		if (istate == null) return Z_STREAM_ERROR;
		int ret = istate.inflate(f);
		if (ret == Z_STREAM_END) finished = true;
		return ret;
	}

	public int end() {
		finished = true;
		if (istate == null) return Z_STREAM_ERROR;
		int ret = istate.inflateEnd();
		// istate = null;
		return ret;
	}

	public int sync() {
		if (istate == null) return Z_STREAM_ERROR;
		return istate.inflateSync();
	}

	public int syncPoint() {
		if (istate == null) return Z_STREAM_ERROR;
		return istate.inflateSyncPoint();
	}

	public int setDictionary(byte[] dictionary, int dictLength) {
		if (istate == null) return Z_STREAM_ERROR;
		return istate.inflateSetDictionary(dictionary, dictLength);
	}

	public boolean finished() {
		return istate.mode == 12 /* DONE */;
	}
}
