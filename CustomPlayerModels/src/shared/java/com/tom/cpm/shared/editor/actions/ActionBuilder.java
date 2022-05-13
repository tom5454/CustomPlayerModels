package com.tom.cpm.shared.editor.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ListAction;
import com.tom.cpm.shared.definition.MapAction;
import com.tom.cpm.shared.editor.Editor;

public class ActionBuilder extends Action {
	private final Editor e;
	private List<Action> actions = new ArrayList<>();
	public ActionBuilder(Editor e) {
		this.e = e;
	}

	public ActionBuilder(Editor e, String name) {
		super(name);
		this.e = e;
	}

	@Override
	@Deprecated
	public void undo() {
		actions.forEach(Action::undo);
	}

	@Override
	@Deprecated
	public void run() {
		actions.forEach(Action::run);
	}

	public <E, T> ActionBuilder updateValueOp(E elem, T currVal, T newVal, BiConsumer<E, T> setter) {
		actions.add(new ValueAction<>(elem, setter, currVal, newVal));
		return this;
	}

	public <E, T> ActionBuilder updateValueOp(E elem, T currVal, T newVal, BiConsumer<E, T> setter, Consumer<T> updater) {
		actions.add(new ValueAction<>(elem, (e, t) -> {
			setter.accept(e, t);
			updater.accept(t);
		}, currVal, newVal));
		return this;
	}

	public <E, T extends Number> ActionBuilder updateValueOp(E elem, T currVal, T newVal, T min, T max, BiConsumer<E, T> setter, Consumer<T> updater) {
		if(newVal.floatValue() > max.floatValue()) {
			newVal = max;
			updater.accept(newVal);
		}
		if(newVal.floatValue() < min.floatValue()) {
			newVal = min;
			updater.accept(newVal);
		}
		updateValueOp(elem, currVal, newVal, setter);
		return this;
	}

	public <E> ActionBuilder updateValueOp(E elem, Vec3f currVal, Vec3f newVal, int min, int max, boolean wrap, BiConsumer<E, Vec3f> setter, Consumer<Vec3f> updater) {
		if(limitVec(newVal, min, max, wrap))updater.accept(newVal);
		updateValueOp(elem, currVal, newVal, setter);
		return this;
	}

	public static boolean limitVec(Vec3f newVal, int min, int max, boolean wrap) {
		boolean changed = false;
		if(newVal.x < min || newVal.x > max) {
			newVal.x = calcVal(newVal.x, min, max, wrap);
			changed = true;
		}
		if(newVal.y < min || newVal.y > max) {
			newVal.y = calcVal(newVal.y, min, max, wrap);
			changed = true;
		}
		if(newVal.z < min || newVal.z > max) {
			newVal.z = calcVal(newVal.z, min, max, wrap);
			changed = true;
		}
		return changed;
	}

	private static float calcVal(float val, int min, int max, boolean wrap) {
		if(val < min) {
			if(wrap)while(val < min)val += max;
			else val = min;
		}
		if(val > max) {
			if(wrap)while(val >= max)val -= max;
			else val = max;
		}
		return val;
	}

	public <T> ActionBuilder addToList(List<T> list, T value) {
		actions.add(ListAction.add(value, list));
		return this;
	}

	public <T> ActionBuilder removeFromList(List<T> list, T value) {
		actions.add(ListAction.remove(value, list));
		return this;
	}

	public <K, V> ActionBuilder addToMap(Map<K, V> map, K key, V value) {
		actions.add(MapAction.add(map, key, value));
		return this;
	}

	public <K, V> ActionBuilder removeFromMap(Map<K, V> map, K key, V value) {
		actions.add(MapAction.remove(map, key, value));
		return this;
	}

	public <T> ActionBuilder update(Consumer<T> updater, T value) {
		actions.add(new RunnableAction(() -> updater.accept(value), null));
		return this;
	}

	public ActionBuilder action(Action action) {
		actions.add(action);
		return this;
	}

	public ActionBuilder runnable(Runnable run, Runnable undo) {
		actions.add(new RunnableAction(run, undo));
		return this;
	}

	public ActionBuilder onUndo(Runnable undo) {
		actions.add(new RunnableAction(null, undo));
		return this;
	}

	public ActionBuilder onRun(Runnable run) {
		actions.add(new RunnableAction(run, null));
		return this;
	}

	public ActionBuilder onAction(Runnable run) {
		actions.add(new RunnableAction(run, run));
		return this;
	}

	public <T> ActionBuilder onAction(T v, Consumer<T> run) {
		actions.add(new RunnableAction(() -> run.accept(v), () -> run.accept(v)));
		return this;
	}

	public void execute() {
		e.executeAction(this);
		e.markDirty();
	}
}