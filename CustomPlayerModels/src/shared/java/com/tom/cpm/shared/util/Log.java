package com.tom.cpm.shared.util;

import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

public class Log {
	public static void info(String text) {
		MinecraftCommonAccess.get().getLogger().info(text);
	}

	public static void info(Object text) {
		MinecraftCommonAccess.get().getLogger().info(String.valueOf(text));
	}

	public static void error(String text) {
		MinecraftCommonAccess.get().getLogger().error(text);
	}

	public static void error(String text, Throwable thr) {
		MinecraftCommonAccess.get().getLogger().error(text, thr);
	}

	public static void warn(String text) {
		MinecraftCommonAccess.get().getLogger().warn(text);
	}

	public static void warn(String text, Throwable thr) {
		MinecraftCommonAccess.get().getLogger().warn(text, thr);
	}

	public static void debug(Object text) {
		if(MinecraftObjectHolder.DEBUGGING)
			MinecraftCommonAccess.get().getLogger().info(String.valueOf(text));
	}

	public static void debug(Object text, Throwable thr) {
		if(MinecraftObjectHolder.DEBUGGING)
			MinecraftCommonAccess.get().getLogger().info(String.valueOf(text), thr);
	}
}
