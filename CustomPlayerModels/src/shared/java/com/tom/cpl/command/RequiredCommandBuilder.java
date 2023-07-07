package com.tom.cpl.command;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequiredCommandBuilder extends AbstractCommandBuilder<RequiredCommandBuilder> {
	private String id;
	private ArgType type;
	private Object settings;
	private Function<CommandCtx<?>, List<String>> possibleValues;

	public RequiredCommandBuilder(String id, ArgType type) {
		this.id = id;
		this.type = type;
	}

	public RequiredCommandBuilder(String id, ArgType type, Object settings) {
		this.id = id;
		this.type = type;
		this.settings = settings;
	}

	public String getId() {
		return id;
	}

	public ArgType getType() {
		return type;
	}

	public Object getSettings() {
		return settings;
	}

	public Function<CommandCtx<?>, List<String>> getPossibleValues() {
		return possibleValues;
	}

	public RequiredCommandBuilder setPossibleValues(Supplier<List<String>> possibleValues) {
		this.possibleValues = __ -> possibleValues.get();
		return this;
	}

	public RequiredCommandBuilder setPossibleValues(Function<CommandCtx<?>, List<String>> possibleValues) {
		this.possibleValues = possibleValues;
		return this;
	}
}
