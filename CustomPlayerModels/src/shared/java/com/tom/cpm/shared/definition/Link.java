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

	public Link(String fullPath) {
		String[] sp = fullPath.split(":", 2);
		this.loader = sp[0];
		this.path = sp[1];
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loader == null) ? 0 : loader.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Link other = (Link) obj;
		if (loader == null) {
			if (other.loader != null) return false;
		} else if (!loader.equals(other.loader)) return false;
		if (path == null) {
			if (other.path != null) return false;
		} else if (!path.equals(other.path)) return false;
		return true;
	}
}
