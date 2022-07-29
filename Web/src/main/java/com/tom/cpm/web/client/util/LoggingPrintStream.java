package com.tom.cpm.web.client.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class LoggingPrintStream extends PrintStream {
	private Consumer<String> logger;
	private String name;

	public LoggingPrintStream(String name, PrintStream outStream) {
		super(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				outStream.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				logString(outStream::println, new String(b, off, len), 16);
			}
		});
		this.name = "[" + name + "]: ";
		this.logger = outStream::println;
		builder.append(this.name);
	}

	public LoggingPrintStream(String name, Consumer<String> logger) {
		super(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				logString(logger, new String(b, off, len), 16);
			}
		});
		this.name = "[" + name + "]: ";
		this.logger = logger;
		builder.append(this.name);
	}

	private static void logString(Consumer<String> logger, String string, int d) {
		/*StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
		for (; d < astacktraceelement.length; d++) {
			StackTraceElement s = astacktraceelement[d];
			if(s.getFileName().contains("Logger"))continue;
			break;
		}
		StackTraceElement stacktraceelement = astacktraceelement[Math.min(d, astacktraceelement.length)];*/
		//"[" + stacktraceelement.getFileName() + ":" + stacktraceelement.getLineNumber() + "]: " +
		if(string.endsWith("\n") || string.endsWith("\r"))string = string.substring(0, string.length() - 2);
		logger.accept(string);
	}

	private StringBuilder builder = new StringBuilder();

	@Override
	public void print(boolean b) {
		this.builder.append(b);
	}

	@Override
	public void print(char c) {
		this.builder.append(c);
	}

	@Override
	public void print(int i) {
		this.builder.append(i);
	}

	@Override
	public void print(long l) {
		this.builder.append(l);
	}

	@Override
	public void print(float f) {
		this.builder.append(f);
	}

	@Override
	public void print(double d) {
		this.builder.append(d);
	}

	@Override
	public void print(char[] s) {
		this.builder.append(s);
	}

	@Override
	public void print(String s) {
		this.builder.append(s);
	}

	@Override
	public void print(Object obj) {
		this.builder.append(obj);
	}

	@Override
	public void println() {
		String msg = builder.toString();
		builder.setLength(name.length());
		logString(logger, msg, 4);
	}

	@Override
	public void println(boolean x) {
		print(x);
		println();
	}
	@Override
	public void println(double x) {
		print(x);
		println();
	}
	@Override
	public void println(int x) {
		print(x);
		println();
	}
	@Override
	public void println(char[] x) {
		print(x);
		println();
	}
	@Override
	public void println(char x) {
		print(x);
		println();
	}
	@Override
	public void println(float x) {
		print(x);
		println();
	}
	@Override
	public void println(long x) {
		print(x);
		println();
	}
	@Override
	public void println(Object x) {
		print(x);
		println();
	}
	@Override
	public void println(String x) {
		print(x);
		println();
	}
}
