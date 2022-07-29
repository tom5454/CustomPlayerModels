package com.tom.cpm.web.client.java;

import elemental2.dom.URL;

public class URI {
	private URL url;

	public URI(String in) throws URISyntaxException {
		try {
			url = new URL(in, "null://");
		} catch (Throwable e) {
			System.err.println("URI: " + in);
			throw new URISyntaxException(in, e.getMessage());
		}
	}

	@Override
	public String toString() {
		return url.toString();
	}

	public String getScheme() {
		return url.protocol != null && url.protocol.equals("null:") ? null : url.protocol.substring(0, url.protocol.length() - 1);
	}
}
