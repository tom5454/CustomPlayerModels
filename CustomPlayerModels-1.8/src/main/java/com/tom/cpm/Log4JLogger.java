package com.tom.cpm;

import org.apache.logging.log4j.Logger;

import com.tom.cpl.util.ILogger;

public class Log4JLogger implements ILogger {
	private Logger log;

	public Log4JLogger(Logger log) {
		this.log = log;
	}

	@Override
	public void warn(String text, Throwable thr) {
		log.warn(text, thr);
	}

	@Override
	public void warn(String text) {
		log.warn(text);
	}

	@Override
	public void info(String text) {
		log.info(text);
	}

	@Override
	public void error(String text, Throwable thr) {
		log.error(text, thr);
	}

	@Override
	public void error(String text) {
		log.error(text);
	}
}
