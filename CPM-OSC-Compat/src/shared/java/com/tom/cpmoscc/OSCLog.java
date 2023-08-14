package com.tom.cpmoscc;

import com.tom.cpm.shared.util.Log;

public class OSCLog {
	private final String prefix;

	public OSCLog(String prefix) {
		this.prefix = prefix;
	}

	public void warn(String string, Object... objects) {
		Log.warn(format(string, objects));
	}

	private String format(String string, Object... objects) {
		return "[" + prefix + "]: " + String.format(string.replace("{}", "%s"), objects);
	}

	public void info(String string, Object... objects) {
		Log.info(format(string, objects));
	}

	public void error(String errorMsg) {
		Log.error(format(errorMsg));
	}

	public void error(String errorMsg, Throwable exception) {
		Log.error(format(errorMsg), exception);
	}

	public static OSCLog getLogger(Class<?> prefix) {
		return new OSCLog(prefix.getSimpleName());
	}

	public static OSCLog getLogger(String string) {
		return new OSCLog(string);
	}
}
