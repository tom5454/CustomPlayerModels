package com.tom.cpl.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class AbstractCommandBuilder<T extends AbstractCommandBuilder<T>> {
	private List<AbstractCommandBuilder<?>> next = new ArrayList<>();
	private Consumer<CommandCtx<?>> func;

	public <A extends AbstractCommandBuilder<A>> T then(AbstractCommandBuilder<A> arg) {
		next.add(arg);
		return (T) this;
	}

	public <A extends AbstractCommandBuilder<A>> T thenAll(Supplier<List<? extends AbstractCommandBuilder<A>>> argSup) {
		next.addAll(argSup.get());
		return (T) this;
	}

	public T run(Consumer<CommandCtx<?>> func) {
		this.func = func;
		return (T) this;
	}

	public List<AbstractCommandBuilder<?>> getNext() {
		return next;
	}

	public Consumer<CommandCtx<?>> getFunc() {
		return func;
	}
}
