package com.tom.cpm.web.client.java.gson;

public class GsonBuilder {
	private boolean pretty;

	public Gson create() {
		return new Gson(pretty);
	}

	public GsonBuilder setPrettyPrinting() {
		pretty = true;
		return this;
	}

	public GsonBuilder enableComplexMapKeySerialization() {
		return this;
	}

	public GsonBuilder serializeSpecialFloatingPointValues() {
		return this;
	}
}
