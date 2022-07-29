package com.tom.cpm.web.client.java;

import com.google.common.io.BaseEncoding;

public class Base64 {
	private static final Base64 INST = new Base64();

	public static Base64 getDecoder() {
		return INST;
	}

	public static Base64 getEncoder() {
		return INST;
	}

	public String encodeToString(byte[] src) {
		return BaseEncoding.base64().encode(src);
	}

	public byte[] decode(String src) {
		return BaseEncoding.base64().decode(src);
	}
}
