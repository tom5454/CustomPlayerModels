package com.tom.cpm.blockbench.util;

import java.util.function.Consumer;

import com.tom.cpm.blockbench.PluginStart;
import com.tom.cpm.blockbench.proxy.Action.Condition;
import com.tom.cpm.blockbench.proxy.Action.ConditionMethod;

import jsinterop.base.Js;

public class ConditionUtil {

	public static void and(Condition cond, Consumer<Condition> set, ConditionMethod condition) {
		if(cond == null) {
			Condition c = new Condition();
			c.method = condition;
			set.accept(c);
			PluginStart.cleanup.add(() -> set.accept(Js.uncheckedCast(Js.undefined())));
		} else
			PluginStart.cleanup.add(new FieldReplace<>(cond, a -> a.method, (a, b) -> a.method = b, condition, (a, b) -> c -> (a.check(c) && b.check(c))));
	}
}
