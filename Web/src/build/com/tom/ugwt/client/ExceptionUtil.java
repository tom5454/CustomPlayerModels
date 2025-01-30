package com.tom.ugwt.client;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.sourcemap.Mapping;
import com.atlassian.sourcemap.SourceMapImpl;

import com.tom.cpm.web.client.util.JSZip;

import elemental2.core.JsError;
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
	private static final JsRegExp formatRegex = new JsRegExp("(at (?:new )?)([\\w$\\<\\>]+(?:\\.[\\w$\\<\\>]+)? (?:\\[[\\w $]+\\] )?)\\(((?:[\\w:\\/\\.]+\\/)|(?:PLUGINS\\/\\(Plugin\\):)[\\w-]+\\.js)(\\?_=\\d+)?:(\\d+):(\\d+)\\)", "g");
	private static final JsRegExp formatRegexFF = new JsRegExp("()([\\w$]+(?:\\.[\\w$]+)?)\\@((?:[\\w:\\/\\.]+\\/)?[\\w-]+\\.js)(\\?_=\\d+)?:(\\d+):(\\d+)", "g");
	private static final String SOURCE_NAME = new JsError().fileName;

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

	private static native JsError getJsErr(Throwable thr)/*-{
		return thr.@java.lang.Throwable::backingJsObject;
	}-*/;

	private static String mapError(String st) {
		if(sourceMap != null) {
			StringEx str = Js.uncheckedCast(st);
			str = str.replace(formatRegex, ExceptionUtil::formatExc).replace(formatRegexFF, ExceptionUtil::formatExc);
			return Js.uncheckedCast(str);
		}
		return st;
	}

	public static String getStackTrace(Throwable thr) {
		return getStackTrace0(thr, new HashSet<>());
	}

	private static String getStackTrace0(Throwable thr, Set<Throwable> dejavu) {
		String s = getStackTrace1(thr);
		if(dejavu.add(thr)) {
			if(thr.getCause() != null) {
				s += "\nCaused by: " + getStackTrace0(thr.getCause(), dejavu);
			}
			if(thr.getSuppressed() != null && thr.getSuppressed().length > 0) {
				for (Throwable se : thr.getSuppressed()) {
					s += "\nSupressed: " + getStackTrace0(se, dejavu);
				}

			}
		} else {
			return "[CIRCULAR REFERENCE: " + thr + "]";
		}
		return s;
	}

	private static String getStackTrace1(Throwable thr) {
		JsError error = getJsErr(thr);
		if(error == null) {
			return thr.getMessage() + "\n\tStacktrace unknown";
		}
		String st = getStackTrace(error);
		return mapError(st);
	}

	public static String getStackTrace(JsError thr) {
		String st = thr.stack != null ? thr.stack : "?";
		return mapError(st);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "String")
	private static class StringEx {
		public native StringEx replace(JsRegExp replaceregex, ReplaceCallback object);
	}

	@JsFunction
	private interface ReplaceCallback {
		String getReplacement(String fullText, String at, String func, String file, String q, String line, String col);
	}

	private static String formatExc(String fullText, String at, String func, String file, String q, String line, String col) {
		if(Js.isFalsy(q))q = "";
		if(Js.isTruthy(file) && (
				(SOURCE_NAME != null && SOURCE_NAME.equals(file + q)) ||
				file.equals("cpmweb-0.js") ||
				file.endsWith("cpm_plugin.js")
				)) {
			Mapping m = sourceMap.getMapping(Integer.parseInt(line) - 1, Integer.parseInt(col) - 1);
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
