package com.tom.cpm.shared.definition;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class Link {
	protected String loader;
	protected String path;

	public Link(IOHelper in) throws IOException {
		int pathLen = in.read();
		byte[] path = new byte[pathLen];
		in.readFully(path);
		String[] sp = new String(path).split(":");
		loader = sp[0];
		this.path = sp[1];
	}

	public Link(String loader, String path) {
		this.loader = loader;
		this.path = path;
	}

	public void write(IOHelper dout) throws IOException {
		byte[] path = (loader + ":" + this.path).getBytes();
		dout.write(path.length);
		dout.write(path);
	}

	@Override
	public String toString() {
		return loader + ":" + this.path;
	}
}
