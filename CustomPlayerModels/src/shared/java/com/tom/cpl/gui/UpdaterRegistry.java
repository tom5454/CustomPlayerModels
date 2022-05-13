package com.tom.cpl.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpdaterRegistry {
	private List<Updater<?>> updaters = new ArrayList<>();

	public <T> Updater<T> create() {
		Updater<T> u = new Updater<>();
		updaters.add(u);
		return u;
	}

	public <T> Updater<T> create(T def) {
		Updater<T> u = new Updater<>();
		u.defValue = def;
		u.hasDefault = true;
		updaters.add(u);
		return u;
	}

	public void setDefault() {
		updaters.forEach(Updater::setDefault);
	}

	public void reset() {
		updaters.forEach(u -> u.setters.clear());
	}

	public static class Updater<T> implements Consumer<T> {
		private List<Consumer<T>> setters = new ArrayList<>();
		private T defValue;
		private boolean hasDefault;

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
			add(v -> c.run());
		}

		public void setDefault() {
			if(hasDefault) {
				accept(defValue);
			}
		}
	}

	public static <T> Updater<T> makeStatic(Supplier<T> value) {
		return new Updater<T>() {

			@Override
			public void add(Consumer<T> c) {
				c.accept(value.get());
			}
		};
	}
}
