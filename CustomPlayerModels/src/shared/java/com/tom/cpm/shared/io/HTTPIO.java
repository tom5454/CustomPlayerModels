package com.tom.cpm.shared.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.util.Log;

public class HTTPIO {
	private static final String userAgent = "CPM " + MinecraftCommonAccess.get().getPlatformVersionString() + " Java/" + System.getProperty("java.version");

	public static HttpURLConnection createUrlConnection(final URL url, boolean noCache) throws IOException {
		Log.debug("Opening connection to " + url);
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection(MinecraftClientAccess.get().getProxy());
		connection.setConnectTimeout(15000);
		connection.setReadTimeout(15000);
		connection.setRequestProperty("User-Agent", userAgent.replace('$', '?'));
		if(noCache)connection.setUseCaches(false);
		return connection;
	}

	public static String getResponse(HttpURLConnection connection, URL url) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = connection.getInputStream();
			final String result = toString(inputStream, StandardCharsets.UTF_8);
			Log.debug("Successful read, server response was " + connection.getResponseCode());
			Log.debug("Response: " + result);
			return result;
		} catch (final IOException e) {
			Util.closeQuietly(inputStream);
			inputStream = connection.getErrorStream();

			if (inputStream != null) {
				Log.debug("Reading error page from " + url);
				final String result = toString(inputStream, StandardCharsets.UTF_8);
				Log.debug("Successful read, server response was " + connection.getResponseCode());
				Log.debug("Response: " + result);
				return result;
			} else {
				Log.debug("Request failed", e);
				throw e;
			}
		} finally {
			Util.closeQuietly(inputStream);
		}
	}

	//Utility methods from org.apache.commons.io.IOUtils
	public static class StringBuilderWriter extends Writer implements Serializable {

		private static final long serialVersionUID = -146927496096066153L;
		private final StringBuilder builder;

		public StringBuilderWriter() {
			this.builder = new StringBuilder();
		}

		public StringBuilderWriter(final int capacity) {
			this.builder = new StringBuilder(capacity);
		}

		public StringBuilderWriter(final StringBuilder builder) {
			this.builder = builder != null ? builder : new StringBuilder();
		}

		@Override
		public Writer append(final char value) {
			builder.append(value);
			return this;
		}

		@Override
		public Writer append(final CharSequence value) {
			builder.append(value);
			return this;
		}

		@Override
		public Writer append(final CharSequence value, final int start, final int end) {
			builder.append(value, start, end);
			return this;
		}

		@Override
		public void close() {
		}

		@Override
		public void flush() {
		}

		@Override
		public void write(final String value) {
			if (value != null) {
				builder.append(value);
			}
		}

		@Override
		public void write(final char[] value, final int offset, final int length) {
			if (value != null) {
				builder.append(value, offset, length);
			}
		}

		public StringBuilder getBuilder() {
			return builder;
		}

		@Override
		public String toString() {
			return builder.toString();
		}
	}

	public static String toString(final InputStream input, final Charset encoding) throws IOException {
		final StringBuilderWriter sw = new StringBuilderWriter();
		copyChars(new InputStreamReader(input, encoding), sw);
		return sw.toString();
	}

	public static long copyChars(final Reader input, final Writer output) throws IOException {
		char[] buffer = new char[4096];
		long count = 0;
		int n;
		while ((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
