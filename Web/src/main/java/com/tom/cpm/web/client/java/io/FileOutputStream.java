package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.OutputStream;

public class FileOutputStream extends OutputStream {

	public FileOutputStream(File file) throws IOException {
		throw new FileNotFoundException();
	}

	@Override
	public void write(int b) throws IOException {

	}

}
