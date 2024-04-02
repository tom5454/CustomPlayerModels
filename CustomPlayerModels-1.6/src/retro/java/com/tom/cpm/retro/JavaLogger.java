package com.tom.cpm.retro;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tom.cpl.util.ILogger;

public class JavaLogger implements ILogger {
	private Logger l;

	public JavaLogger(Logger parent, String prefix) {
		l = Logger.getLogger(prefix);
		l.setParent(parent);
	}

	@Override
	public void warn(String text, Throwable thr) {
		l.log(Level.WARNING, text, thr);
	}

	@Override
	public void warn(String text) {
		l.warning(text);
	}

	@Override
	public void info(String text) {
		l.info(text);
	}

	@Override
	public void error(String text, Throwable thr) {
		l.log(Level.SEVERE, text, thr);
	}

	@Override
	public void error(String text) {
		l.severe(text);
	}

	@Override
	public void info(String text, Throwable thr) {
		l.log(Level.INFO, text, thr);
	}
}
