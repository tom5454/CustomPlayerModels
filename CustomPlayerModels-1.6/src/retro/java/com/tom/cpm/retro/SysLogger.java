package com.tom.cpm.retro;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.tom.cpl.util.ILogger;

public class SysLogger implements ILogger {
	private String prefix;

	public SysLogger(String prefix) {
		this.prefix = prefix;
	}

	private String format(String level, String text) {
		return "[" + (new SimpleDateFormat("HH:mm:ss").format(new Date())) + "] [" + prefix + "/" + level + "]: " + text;
	}

	@Override
	public void warn(String text, Throwable thr) {
		System.out.println(format("WARN", text));
		thr.printStackTrace(System.out);
	}

	@Override
	public void warn(String text) {
		System.out.println(format("WARN", text));
	}

	@Override
	public void info(String text) {
		System.out.println(format("INFO", text));
	}

	@Override
	public void error(String text, Throwable thr) {
		System.err.println(format("ERROR", text));
		thr.printStackTrace();
	}

	@Override
	public void error(String text) {
		System.err.println(format("ERROR", text));
	}

	@Override
	public void info(String text, Throwable thr) {
		System.out.println(format("INFO", text));
		thr.printStackTrace(System.out);
	}
}
