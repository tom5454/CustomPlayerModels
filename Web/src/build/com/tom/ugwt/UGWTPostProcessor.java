package com.tom.ugwt;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UGWTPostProcessor {
	private static final String REGEX = "\\$\\$__ugwt_([\\w]+)__\\$\\$\\(\\'([\\w.]+)\\'(?:[\\w.,\\s$]+)?\\);";
	//public static final Pattern REGEX2 = Pattern.compile("var \\$uwgt_funcTemp = \\{\\};\\s*((?:\\$uwgt_funcTemp.fn_[0-9a-fA-F_]+ = [\\w\\$]+;\\s*)*)\\s*\\$\\$__ugwt2_native__\\$\\$\\(\\'([\\w.=]+)\\', \\$uwgt_funcTemp\\);");
	//public static final Pattern REGEX3 = Pattern.compile("(\\$uwgt_funcTemp.fn_[0-9a-fA-F_]+) = ([\\w\\$]+);");
	//public static final Pattern METHOD_FIND = Pattern.compile("\\$\\$__ugwt2_native__\\$\\$\\(\\'([\\S]+)\\',\\s*([\\w_$]+)\\);");
	//public static final String JVM_REGEX = "\\$\\$_jvm_([\\w.]+)\\$([\\w]+)\\(";
	//private static final String JVM_REPLACE = " \\$ugwt_pub.$1(";
	private static final String ARRAY_REGEX = "\\.\\$\\$array_([\\d]+)_\\$\\$";
	private static final String DIRECT_REGEX = "\\$wnd\\.(?:G\\.)?\\$\\$ugwt_m_([\\w]+)_\\$\\$";


	/*private void appendLink(StringBuilder ssb, String r) {
		ssb.append("$wnd." + r + " = " + r + ";");
	}

	private static Pair<String, Map<String, List<Pair<String, String>>>> findPreprocessorRegex(String in, String regex) {
		Map<String, List<Pair<String, String>>> l = new HashMap<>();
		Matcher m = Pattern.compile(regex).matcher(in);
		while (m.find()) {
			int fn = in.lastIndexOf("function", m.start()) + 9;
			int nameEnd = in.indexOf('(', fn);
			String fnn = in.substring(fn, nameEnd);
			l.computeIfAbsent(m.group(1), k -> new ArrayList<>()).add(Pair.of(m.group(2), fnn));
		}
		return Pair.of(in.replaceAll(regex, ""), l);
	}*/

	public static String postProcess(String in) {
		in = in.replaceAll(ARRAY_REGEX, "[$1]").replaceAll(DIRECT_REGEX, "$1").replaceAll("\\$wnd\\.goog\\s?=\\s?\\$wnd\\.goog\\s?\\|\\|\\s?\\{\\};", "").
				replaceAll("\\$wnd\\.goog\\.global\\s?=\\s?\\$wnd\\.goog\\.global\\s?\\|\\|\\s?\\$wnd;", "").replace("$wnd.goog.global.", "$wnd.");

		if(System.getProperty("ugwt.useContext", "false").equals("true")) {
			in = "var __ugwt_ctx__ = window.parent;\n" + in;

			String[] contextSensitiveClasses = new String[] {"WebGL\\w*", "Element", "HTML\\w*Element", "File", "Event", "Response", "Canvas\\w*", "Blob", "\\w*Buffer", "Window", "DataTransferItem"};
			in = in.replaceAll("\\$wnd\\.(" + Arrays.stream(contextSensitiveClasses).collect(Collectors.joining("|")) + ")", "__ugwt_ctx__.$1");
		} else {
			in = "var __ugwt_ctx__ = window;\n" + in;
		}
		System.out.println("UGWT Post-Processor finished");

		return in;
	}

	//Pair<String, Map<String, List<Pair<String, String>>>> prp = findPreprocessorRegex(result.getJavaScript(), REGEX);

	/*prp.getValue().getOrDefault("include", Collections.emptyList()).forEach(r -> {
				//TODO include
			});*/

	/*prp.getValue().getOrDefault("require", Collections.emptyList()).forEach(p -> {
				appendLink(ssb, p.getKey());
			});
			ssb.append('\n');

			String bsf = prp.getValue().getOrDefault("bootstrap", Collections.singletonList(Pair.of("com/tom/ugwt/UGWTSingleLinker.js", null))).get(0).getKey();
	 */

	/*Matcher mf = METHOD_FIND.matcher(buf.toString());
	while(mf.find()) {
		String src = new String(Base64.getDecoder().decode(mf.group(1)), StandardCharsets.UTF_8);
		String funcField = mf.group(2);
	}
	buf = new StringBuffer(UGWTTransformer.replace(buf.toString(), REGEX2, m -> {
		Matcher s = REGEX3.matcher(m.group(1));
		Map<String, String> map = new HashMap<>();
		while(s.find()) {
			map.put(s.group(1), s.group(2));
		}
		String[] src = new String[] {new String(Base64.getDecoder().decode(m.group(2)), StandardCharsets.UTF_8)};
		map.forEach((k, v) -> src[0] = src[0].replace(k, v));
		return src[0];
	}));*/
	//buf = new StringBuffer(buf.toString().replaceAll(JVM_REGEX, JVM_REPLACE));
}
