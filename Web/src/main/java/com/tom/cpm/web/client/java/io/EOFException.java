package com.tom.cpm.web.client.java.io;

import java.io.IOException;

public class EOFException extends IOException {
	private static final long serialVersionUID = -7555538317525666750L;

	public EOFException() {
		super();
	}

	public EOFException(String message, Throwable cause) {
		super(message, cause);
	}

	public EOFException(String message) {
		super(message);
	}

	public EOFException(Throwable cause) {
		super(cause);
	}
}
