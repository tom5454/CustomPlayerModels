package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.InputStream;

public class FileInputStream extends InputStream {

	public FileInputStream(File file) throws FileNotFoundException {
		throw new FileNotFoundException();
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

}
