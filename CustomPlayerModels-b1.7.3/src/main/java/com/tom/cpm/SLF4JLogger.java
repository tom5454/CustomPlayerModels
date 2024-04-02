package com.tom.cpm;

import org.slf4j.Logger;

import com.tom.cpl.util.ILogger;

public class SLF4JLogger implements ILogger {
	private Logger logger;

	public SLF4JLogger(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void info(String text) {
		logger.info(text);
	}

	@Override
	public void info(String text, Throwable thr) {
		logger.info(text, thr);
	}

	@Override
	public void error(String text) {
		logger.error(text);
	}

	@Override
	public void error(String text, Throwable thr) {
		logger.error(text, thr);
	}

	@Override
	public void warn(String text) {
		logger.warn(text);
	}

	@Override
	public void warn(String text, Throwable thr) {
		logger.warn(text, thr);
	}
}
