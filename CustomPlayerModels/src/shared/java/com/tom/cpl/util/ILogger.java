package com.tom.cpl.util;

public interface ILogger {
	void info(String text);
	void info(String text, Throwable thr);
	void error(String text);
	void error(String text, Throwable thr);
	void warn(String text);
	void warn(String text, Throwable thr);

	public static class SysLogger implements ILogger {

		@Override
		public void warn(String text, Throwable thr) {
			System.out.println("[WARN]: " + text);
			thr.printStackTrace(System.out);
		}

		@Override
		public void warn(String text) {
			System.out.println("[WARN]: " + text);
		}

		@Override
		public void info(String text) {
			System.out.println(text);
		}

		@Override
		public void error(String text, Throwable thr) {
			System.err.println(text);
			thr.printStackTrace();
		}

		@Override
		public void error(String text) {
			System.err.println(text);
		}

		@Override
		public void info(String text, Throwable thr) {
			System.out.println(text);
			thr.printStackTrace(System.out);
		}
	}
}
