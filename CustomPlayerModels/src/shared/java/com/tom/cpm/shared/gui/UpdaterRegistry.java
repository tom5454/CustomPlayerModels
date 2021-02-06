package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpdaterRegistry {
	private List<Updater<?>> updaters = new ArrayList<>();

	public <T> Updater<T> create() {
		Updater<T> u = new Updater<>();
		updaters.add(u);
		return u;
	}

	public void reset() {
		updaters.forEach(u -> u.setters.clear());
	}

	public static class Updater<T> implements Consumer<T> {
		private List<Consumer<T>> setters = new ArrayList<>();

		private Updater() {
		}

		@Override
		public void accept(T t) {
			setters.forEach(s -> s.accept(t));
		}

		public void add(Consumer<T> c) {
			setters.add(c);
		}

		public void add(Runnable c) {
			setters.add(v -> c.run());
		}
	}
}
