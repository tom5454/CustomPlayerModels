package com.tom.cpm.web.client.java;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;

public class SimpleDateFormat {
	private final DateTimeFormat dtf;

	public SimpleDateFormat(String format) {
		dtf = DateTimeFormat.getFormat(format);
	}

	public String format(Date date) {
		return dtf.format(date);
	}
}
