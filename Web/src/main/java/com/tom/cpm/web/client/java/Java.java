package com.tom.cpm.web.client.java;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptException;

import com.tom.cpm.web.client.resources.Resources;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import elemental2.core.JsObject;
import elemental2.core.JsRegExp;
import elemental2.core.JsString;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class Java {
	private static SimpleDateFormat year = new SimpleDateFormat("yyyy");
	private static SimpleDateFormat month = new SimpleDateFormat("MM");
	private static SimpleDateFormat day = new SimpleDateFormat("dd");
	private static SimpleDateFormat hour = new SimpleDateFormat("HH");
	private static SimpleDateFormat minute = new SimpleDateFormat("mm");
	private static SimpleDateFormat second = new SimpleDateFormat("ss");
	private static final JsRegExp formatRegex = new JsRegExp("%(?:(\\d)+\\$)?([-#+ 0,(]+)?(\\d+)?(?:\\.(\\d)+)?([bBhHsScCoxXeEfgGaAn%]|[tT][HIklMSLNpzZsQBbhAaCYyjmdeRTrDTc])", "g");
	private static final JsRegExp replaceRegex = new JsRegExp("\\${(.*?)}", "g");

	public static String format(final String format, final Object... args) {
		int[] inc = new int[1];
		return doFormat(format, (argIn, flags, width, prec, mode) -> {
			if(mode.equals("%"))return "%";
			int arg = argIn.isEmpty() ? inc[0] : (Integer.parseInt(argIn) - 1);
			inc[0]++;
			switch (mode) {
			case "s":
				return String.valueOf(args[arg]);

			case "f":
				if(!prec.isEmpty())
					return fixed(((Number)args[arg]).floatValue(), Integer.parseInt(prec));
				return String.valueOf(args[arg]);

			case "X":
			{
				JsString s = Js.uncheckedCast(new JsNumber(((Number)args[arg]).intValue()).toString(16));
				return s.padStart(width.isEmpty() ? 0 : Integer.parseInt(width), "0");
			}

			case "tY":
				return year.format(new Date(((Number)args[arg]).longValue()));

			case "tm":
				return month.format(new Date(((Number)args[arg]).longValue()));

			case "td":
				return day.format(new Date(((Number)args[arg]).longValue()));

			case "tH":
				return hour.format(new Date(((Number)args[arg]).longValue()));

			case "tM":
				return minute.format(new Date(((Number)args[arg]).longValue()));

			case "tS":
				return second.format(new Date(((Number)args[arg]).longValue()));

			default:
				throw new RuntimeException("Format mode unknown: " + mode);
			}
		});
	}

	private static String doFormat(String format, FormatCallback cb) {
		JsString str = Js.uncheckedCast(format);
		StringEx stre = Js.uncheckedCast(str.replace(formatRegex, "$${$1:$2:$3:$4:$5}"));
		return stre.replace(replaceRegex, (x, fmt) -> {
			String[] sp = fmt.toString().split(":");
			return cb.exec(sp[0], sp[1], sp[2], sp[3], sp[4]);
		});
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "String")
	public static class StringEx {
		public native String replace(JsRegExp replaceregex, ReplaceCallback object);
	}

	@JsFunction
	public interface ReplaceCallback {
		String getReplacement(String fullText, String capture);
	}

	public interface FormatCallback {
		String exec(String arg, String flags, String width, String prec, String mode);
	}

	public static String fixed(float n, int w) {
		return new JsNumber(n).toFixed(w);
	}

	public static int parseUnsignedInt(String s, int radix)
			throws NumberFormatException {
		if (s == null)  {
			throw new NumberFormatException("null");
		}

		int len = s.length();
		if (len > 0) {
			char firstChar = s.charAt(0);
			if (firstChar == '-') {
				throw new
				NumberFormatException(String.format("Illegal leading minus sign " +
						"on unsigned string %s.", s));
			} else {
				if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
						(radix == 10 && len <= 9) ) { // Integer.MAX_VALUE in base 10 is 10 digits
					return Integer.parseInt(s, radix);
				} else {
					long ell = Long.parseLong(s, radix);
					if ((ell & 0xffff_ffff_0000_0000L) == 0) {
						return (int) ell;
					} else {
						throw new
						NumberFormatException(String.format("String value %s exceeds " +
								"range of unsigned int.", s));
					}
				}
			}
		} else {
			throw new NumberFormatException("For input string: \"" + s + "\"");
		}
	}

	public static InputStream getResourceAsStream(String path) {
		path = path.substring(1);
		String dt = Resources.getResource(path);
		if(dt != null) {
			return new ResourceInputStream(path, Base64.getDecoder().decode(dt));
		}
		System.err.println("Resource not found: " + path);
		return null;
	}

	public static class ResourceInputStream extends ByteArrayInputStream {
		public final String path;

		public ResourceInputStream(String path, byte[] buf) {
			super(buf);
			this.path = path;
		}

	}

	public static int toUnsignedInt(byte x) {
		return (x) & 0xff;
	}

	public static String getQueryVariable(String key) {
		String query = DomGlobal.window.location.search.substring(1);
		String[] vars = query.split("&");
		for (int i = 0; i < vars.length; i++) {
			String[] pair = vars[i].split("=");
			if (Global.decodeURIComponent(pair[0]).equals(key)) {
				return Global.decodeURIComponent(pair[1]);
			}
		}
		return null;
	}

	public static void removeQueryVariable(String key) {
		String newUrl = DomGlobal.window.location.protocol + "//" + DomGlobal.window.location.host + DomGlobal.window.location.pathname;
		List<String> q = new ArrayList<>();
		String query = DomGlobal.window.location.search.substring(1);
		if(query.isEmpty())return;
		String[] vars = query.split("&");
		for (int i = 0; i < vars.length; i++) {
			String[] pair = vars[i].split("=");
			if (!Global.decodeURIComponent(pair[0]).equals(key)) {
				q.add(vars[i]);
			}
		}
		if(!q.isEmpty()) {
			newUrl += q.stream().collect(Collectors.joining("&", "?", ""));
		}
		DomGlobal.window.history.pushState(new JsObject(), "", newUrl);
	}

	public static String getPlatform() {
		String ua = DomGlobal.navigator.userAgent;
		JsString jua = Js.uncheckedCast(ua);
		JsArray<String> M = jua.match(new JsRegExp("(opera|chrome|safari|firefox|msie|trident(?=\\/))\\/?\\s*(\\d+)", "i"));
		JsArray<String> tem;
		if(Js.isFalsy(M))M = new JsArray<>();
		if(new JsRegExp("trident", "i").test(M.getAt(1))) {
			tem = new JsRegExp("\\brv[ :]+(\\d+)", "g").exec(ua);
			if(Js.isFalsy(tem))tem = new JsArray<>();
			String v = tem.getAt(1);
			if(Js.isFalsy(v))v = "";
			return "IE " + v;
		}
		if("Chrome".equals(M.getAt(1))) {
			tem = jua.match(new JsRegExp("\\bOPR|Edge\\/(\\d+)"));
			if(tem != null) { return "Opera " + tem.getAt(1); }
		}
		M = Js.isTruthy(M.getAt(2)) ? JsArray.of(M.getAt(1), M.getAt(2)) : JsArray.of(DomGlobal.navigator.appName, DomGlobal.navigator.appVersion, "-?");
		if((tem = jua.match(new JsRegExp("version\\/(\\d+)", "i"))) != null) {M.splice(1, 1, tem.getAt(1));}
		return M.getAt(0) + " " + M.getAt(1);
	}

	public static <T> void promiseToCf(Promise<T> pr, CompletableFuture<T> cf) {
		pr.then(i -> {
			cf.complete(i);
			return null;
		}).catch_(e -> {
			cf.completeExceptionally(e instanceof Throwable ? (Throwable) e : new JavaScriptException(e));
			return null;
		});
	}
}
