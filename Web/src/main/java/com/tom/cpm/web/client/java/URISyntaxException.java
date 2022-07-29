package com.tom.cpm.web.client.java;

public class URISyntaxException extends Exception {

	public URISyntaxException(String input, String reason) {
		super(reason + ": " + input);
	}
}
