package com.tom.cpl.util;

public interface ILogger {
	void info(String text);
	void error(String text);
	void error(String text, Throwable thr);
	void warn(String text);
	void warn(String text, Throwable thr);
}
