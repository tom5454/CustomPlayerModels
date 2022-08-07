package com.tom.cpl.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.PopupMenu;

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

	public BooleanUpdater createBool(boolean def) {
		BooleanUpdater u = new BooleanUpdater(def);
		updaters.add(u);
		return u;
	}

	public <T> UpdaterWithValue<T> createValue(T def) {
		UpdaterWithValue<T> u = new UpdaterWithValue<>(def);
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
		protected List<Consumer<T>> setters = new ArrayList<>();
		protected T defValue;
		protected boolean hasDefault;

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

	public static class UpdaterWithValue<T> extends Updater<T> implements Supplier<T> {
		private T value;

		public UpdaterWithValue(T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public void accept(T t) {
			super.accept(t);
			this.value = t;
		}

		@Override
		public void add(Consumer<T> c) {
			super.add(c);
			c.accept(value);
		}
	}

	public static class BooleanUpdater extends Updater<Boolean> {
		private boolean value;

		public BooleanUpdater(boolean def) {
			this.value = def;
		}

		public void toggle() {
			value = !value;
			accept(value);
		}

		public boolean get() {
			return value;
		}

		@Override
		public void add(Consumer<Boolean> c) {
			super.add(c);
			c.accept(value);
		}

		public Checkbox makeCheckbox(PopupMenu pp, String name) {
			Checkbox c = pp.addCheckbox(name, this::toggle);
			add(c::setSelected);
			return c;
		}
	}
}
