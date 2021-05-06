package com.tom.cpm.bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tom.cpl.util.ILogger;

public class BukkitLogger implements ILogger {
	private Logger log;

	public BukkitLogger(Logger log) {
		this.log = log;
	}

	@Override
	public void warn(String text, Throwable thr) {
		log.log(Level.WARNING, text, thr);
	}

	@Override
	public void warn(String text) {
		log.log(Level.WARNING, text);
	}

	@Override
	public void info(String text) {
		log.log(Level.INFO, text);
	}

	@Override
	public void error(String text, Throwable thr) {
		log.log(Level.SEVERE, text, thr);
	}

	@Override
	public void error(String text) {
		log.log(Level.SEVERE, text);
	}
}
