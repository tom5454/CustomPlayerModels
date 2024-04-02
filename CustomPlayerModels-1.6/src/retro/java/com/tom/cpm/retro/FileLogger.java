package com.tom.cpm.retro;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.tom.cpl.util.ILogger;

public class FileLogger implements ILogger {
	private String prefix;
	private PrintStream out;

	@SuppressWarnings("resource")
	public FileLogger(String prefix) {
		this.prefix = prefix;
		try {
			FileOutputStream fo = new FileOutputStream(prefix.replace(' ', '-') + ".log");
			out = new PrintStream(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					System.out.write(b);
					fo.write(b);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					System.out.write(b, off, len);
					fo.write(b, off, len);
				}

			}, true, "UTF-8");
		} catch (IOException e) {
			out = System.out;
		}
	}

	private String format(String level, String text) {
		return "[" + (new SimpleDateFormat("HH:mm:ss").format(new Date())) + "] [" + prefix + "/" + level + "]: " + text;
	}

	@Override
	public void warn(String text, Throwable thr) {
		out.println(format("WARN", text));
		thr.printStackTrace(out);
	}

	@Override
	public void warn(String text) {
		out.println(format("WARN", text));
	}

	@Override
	public void info(String text) {
		out.println(format("INFO", text));
	}

	@Override
	public void error(String text, Throwable thr) {
		out.println(format("ERROR", text));
		thr.printStackTrace(out);
	}

	@Override
	public void error(String text) {
		out.println(format("ERROR", text));
	}

	@Override
	public void info(String text, Throwable thr) {
		out.println(format("INFO", text));
		thr.printStackTrace(out);
	}
}
