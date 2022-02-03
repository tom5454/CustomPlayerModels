package com.tom.cpl.command;

public class LiteralCommandBuilder extends AbstractCommandBuilder<LiteralCommandBuilder> {
	private final String name;

	public LiteralCommandBuilder(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
