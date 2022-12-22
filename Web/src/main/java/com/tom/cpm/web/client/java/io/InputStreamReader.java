package com.tom.cpm.web.client.java.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.web.client.java.FakeReader;

public class InputStreamReader extends Reader implements FakeReader {
	private String text;
	private StringReader rd;

	public InputStreamReader(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOHelper.copy(in, baos);
		text = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
		rd = new StringReader(text);
	}

	public InputStreamReader(InputStream in, Charset cs) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOHelper.copy(in, baos);
		text = new String(baos.toByteArray(), cs);
		rd = new StringReader(text);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return rd.read(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public String getContent() {
		return text;
	}
}
