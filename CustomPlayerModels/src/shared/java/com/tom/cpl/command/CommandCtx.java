package com.tom.cpl.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tom.cpl.text.IText;

public class CommandCtx<S> {
	private S sender;
	public final CommandHandler<S> handler;
	private Map<String, Object> arguments = new HashMap<>();
	private Set<String> flagsSet = new HashSet<>();
	private int result;
	private IText fail;

	public CommandCtx(S sender, CommandHandler<S> handler) {
		this.sender = sender;
		this.handler = handler;
	}

	public void success() {
		this.result = 1;
	}

	public void success(int count) {
		this.result = count;
	}

	public void sendSuccess(IText message) {
		handler.sendSuccess(sender, message);
	}

	public void fail(IText message) {
		this.fail = message;
	}

	public int getResult() {
		return result;
	}

	public void arg(String id, Object value) {
		arguments.put(id, value);
	}

	public boolean hasFlag(String id) {
		return flagsSet.contains(id);
	}

	@SuppressWarnings("unchecked")
	public <T> T getArgument(String id) {
		return (T) arguments.get(id);
	}

	public IText getFail() {
		return fail;
	}
}
