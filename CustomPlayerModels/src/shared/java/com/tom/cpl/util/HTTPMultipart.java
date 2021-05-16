package com.tom.cpl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class HTTPMultipart {
	private static final char[] MULTIPART_CHARS =
			"-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
	public static final String CONTENT_TYPE = "Content-Type";
	private static final String STRING_TYPE = "text/plain; charset=utf-8";
	private static final String TWO_DASHES = "--";
	private static final String CR_LF = "\r\n";

	private final String boundary;
	private List<MultipartElement> elements = new ArrayList<>();
	private byte[] encoded;

	public HTTPMultipart() {
		StringBuilder buffer = new StringBuilder();
		Random rand = new Random();
		int count = rand.nextInt(11) + 30; // a random size from 30 to 40
		for (int i = 0; i < count; i++) {
			buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		boundary = buffer.toString();
	}

	public String getContentType() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("multipart/form-data; boundary=");
		buffer.append(boundary);
		return buffer.toString();
	}

	public void encode() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter wr = new PrintWriter(baos, true);
		for (MultipartElement httpElement : elements) {
			wr.print(TWO_DASHES);
			wr.print(boundary);
			wr.print(CR_LF);
			for(Entry<String, String> e : httpElement.fields.entrySet()) {
				wr.print(e.getKey());
				wr.print(": ");
				wr.print(e.getValue());
				wr.print(CR_LF);
			}
			wr.print(CR_LF);
			wr.flush();
			baos.write(httpElement.content);
			wr.print(CR_LF);
		}
		wr.print(TWO_DASHES);
		wr.print(boundary);
		wr.print(TWO_DASHES);
		wr.print(CR_LF);
		wr.close();
		encoded = baos.toByteArray();
	}

	public static class MultipartElement {
		private Map<String, String> fields = new HashMap<>();
		private byte[] content;
	}

	public void addString(String name, String value) {
		MultipartElement me = new MultipartElement();
		me.fields.put(CONTENT_DISPOSITION, "form-data; name=\"" + name + "\"");
		me.fields.put(CONTENT_TYPE, STRING_TYPE);
		me.fields.put(CONTENT_TRANSFER_ENCODING, "8bit");
		me.content = value.getBytes(StandardCharsets.UTF_8);
		elements.add(me);
	}

	public void addBinary(String name, String file, String contentType, byte[] content) {
		MultipartElement me = new MultipartElement();
		me.fields.put(CONTENT_DISPOSITION, "form-data; name=\"" + name + "\"; filename=\"" + file + "\"");
		me.fields.put(CONTENT_TYPE, contentType);
		me.fields.put(CONTENT_TRANSFER_ENCODING, "binary");
		me.content = content;
		elements.add(me);
	}

	public void writeTo(OutputStream os) throws IOException {
		os.write(encoded);
	}

	public int getLen() {
		return encoded.length;
	}
}
