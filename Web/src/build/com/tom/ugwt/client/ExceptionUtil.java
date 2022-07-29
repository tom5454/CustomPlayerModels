package com.tom.ugwt.client;

public class ExceptionUtil {
	public static native String getStackTrace(Throwable thr)/*-{
		var jserr = thr.@java.lang.Throwable::backingJsObject;
		return jserr ? (jserr.stack ? jserr.stack : "?") : "?";
	}-*/;
}
