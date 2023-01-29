package com.tom.ugwt.client;

import com.atlassian.sourcemap.Mapping;
import com.atlassian.sourcemap.SourceMapImpl;

import com.tom.cpm.web.client.util.JSZip;

import elemental2.core.JsRegExp;
import elemental2.dom.DomGlobal;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class ExceptionUtil {
	private static SourceMapImpl sourceMap;
	private static final JsRegExp formatRegex = new JsRegExp("(at (?:new )?)([\\w$\\<\\>]+(?:\\.[\\w$\\<\\>]+)? (?:\\[[\\w $]+\\] )?)\\(([\\w:\\/\\.]+\\/\\w+\\.js)(\\?_=\\d+)?:(\\d+):(\\d+)\\)", "g");

	static {
		String pr = getSourceMap();
		if(pr != null) {
			boolean zip = false;
			if(pr.startsWith("Z:")) {
				zip = true;
				pr = "data:application/octet-binary;base64," + pr.substring(2);
			}
			Promise<Response> f = DomGlobal.fetch(pr);
			Promise<String> r;
			if(zip)r = f.then(Response::arrayBuffer).then(a -> new JSZip().loadAsync(a)).then(z -> z.file("map").async("string"));
			else r = f.then(Response::text);
			r.then(m -> {
				sourceMap = new SourceMapImpl(m);
				return null;
			});
		}
	}

	@JsProperty(namespace = JsPackage.GLOBAL, name = "$$ugwt_m___ugwt_sourceMap___$$")
	private static native String getSourceMap();

	private static native String getStackTrace0(Throwable thr)/*-{
		var jserr = thr.@java.lang.Throwable::backingJsObject;
		return jserr ? (jserr.stack ? jserr.stack : "?") : "?";
	}-*/;

	public static String getStackTrace(Throwable thr, boolean useSourceMap) {
		String st = getStackTrace0(thr);
		if(sourceMap != null) {// && useSourceMap
			StringEx str = Js.uncheckedCast(st);
			String r = str.replace(formatRegex, ExceptionUtil::formatExc);
			return r;
		}
		return st;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "String")
	private static class StringEx {
		public native String replace(JsRegExp replaceregex, ReplaceCallback object);
	}

	@JsFunction
	private interface ReplaceCallback {
		String getReplacement(String fullText, String at, String func, String file, String q, String line, String col);
	}

	private static String formatExc(String fullText, String at, String func, String file, String q, String line, String col) {
		if(file.endsWith("cpm_plugin.js")) {
			Mapping m = sourceMap.getMapping(Integer.parseInt(line), Integer.parseInt(col));
			if(m != null) {
				String src = m.getSourceFileName();
				int i = src.lastIndexOf('.');
				if(i != -1)src = src.substring(0, i);
				String s = m.getSourceSymbolName();
				if(s != null)src += "." + s;
				i = m.getSourceFileName().lastIndexOf('/');
				String name = m.getSourceFileName().substring(i + 1, m.getSourceFileName().length());
				return at + src.replace('/', '.') + "(" + name + ":" + m.getSourceLine() + ") (" + file + q + ":" + line + ":" + col + ")";
			}
		}
		return fullText;
	}

	public static void init() {}
}
