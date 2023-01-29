package com.tom.ugwt;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.atlassian.sourcemap.Mapping;
import com.atlassian.sourcemap.SourceMapImpl;

import com.google.gson.Gson;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.impl.BinaryOnlyArtifactWrapper;
import com.google.gwt.dev.util.Util;

import com.tom.cpm.web.gwt.ZipArchive;

public class UGWTPostProcessor {
	private static final String ARRAY_REGEX = "\\.\\$\\$array_([\\d]+)_\\$\\$";
	private static final String DIRECT_REGEX = "\\$wnd\\.(?:G\\.)?\\$\\$ugwt_m_([\\w\\.]+)_\\$\\$";
	private static List<ReplaceInfo> infos = new ArrayList<>();
	private static int linesOff;

	public static String postProcess(String in, String strongName) {
		infos.clear();
		linesOff = 0;
		{
			Matcher m = Pattern.compile(DIRECT_REGEX).matcher(in);
			while (m.find()) {
				String gr = m.group(1);
				if(gr.length() <= 3) {
					System.out.println("[Warn] Direct access is too short, may be overwritten by minifier");
				}
			}
		}

		String srcMap = System.getProperty("ugwt.sourcemap", "http://localhost:9876/sourcemaps/cpmweb/" + strongName + "_sourcemap.json");

		List<String> ln = new BufferedReader(new StringReader(in)).lines().collect(Collectors.toList());

		replaceAll(ln,
				//"\\$wnd\\.\\$\\$ugwt_sourceMap_\\$\\$", "\"" + srcMap + "\"",
				ARRAY_REGEX, "[$1]",
				DIRECT_REGEX, "$1",
				"\\$wnd\\.goog\\s?=\\s?\\$wnd\\.goog\\s?\\|\\|\\s?\\{\\};", "",
				"\\$wnd\\.goog\\.global\\s?=\\s?\\$wnd\\.goog\\.global\\s?\\|\\|\\s?\\$wnd;", "",
				"\\$wnd\\.goog\\.global\\.", "\\$wnd.");

		ln.add(0, "var __ugwt_sourceMap__ = \"" + srcMap + "\";");
		addPre(1);

		if(System.getProperty("ugwt.useContext", "false").equals("true")) {
			ln.add(0, "var __ugwt_ctx__ = window.parent;");
			addPre(1);

			String[] contextSensitiveClasses = new String[] {"WebGL\\w*", "Element", "HTML\\w*Element", "File", "Event", "Response", "Canvas\\w*", "Blob", "\\w*Buffer", "Window", "DataTransferItem"};
			replaceAll(ln, "\\$wnd\\.(" + Arrays.stream(contextSensitiveClasses).collect(Collectors.joining("|")) + ")", "__ugwt_ctx__.$1");
		} else {
			ln.add(0, "var __ugwt_ctx__ = window;");
			addPre(1);
		}
		System.out.println("UGWT Post-Processor finished");

		return ln.stream().collect(Collectors.joining("\n"));
	}

	public static void addPre(int len) {
		linesOff += len;
	}

	private static void replaceAll(List<String> ln, String... replace) {
		for (int j = 0; j < ln.size(); j++) {
			String in = ln.get(j);
			for (int i = 0; i < replace.length; i += 2) {
				Matcher m = Pattern.compile(replace[i]).matcher(in);
				boolean result = m.find();
				if (result) {
					StringBuffer sb = new StringBuffer();
					int sizeOff = 0;
					do {
						int st = sizeOff + m.start();
						int end = sizeOff + m.end();
						m.appendReplacement(sb, replace[i + 1]);
						int rEnd = sb.length();
						sizeOff += (rEnd - end);
						add(st, end, rEnd, j);
						result = m.find();
					} while (result);
					m.appendTail(sb);
					in = sb.toString();
				}
			}
			ln.set(j, in);
		}
	}

	private static void add(int start, int end, int replaceEnd, int line) {
		infos.forEach(r -> r.update(start, end, replaceEnd, line));
		infos.add(new ReplaceInfo(start, end, replaceEnd, line));
	}

	private static class ReplaceInfo {
		private final int start, end, line;
		private int replaceStart, replaceEnd;
		private int d;

		public ReplaceInfo(int start, int end, int replaceEnd, int line) {
			this.replaceStart = start;
			this.start = start;
			this.end = end;
			this.replaceEnd = replaceEnd;
			this.line = line;
		}

		public ReplaceInfo(Map<String, Object> m) {
			start = ((Number) m.get("s")).intValue();
			end = ((Number) m.get("e")).intValue();
			replaceStart = ((Number) m.get("rs")).intValue();
			replaceEnd = ((Number) m.get("re")).intValue();
			d = (replaceEnd - replaceStart) - (end - start);
			this.line = ((Number) m.get("ln")).intValue();
		}

		public void update(int start, int end, int replaceEnd, int line) {
			if(this.replaceStart >= start && this.line == line) {
				int d = replaceEnd - end;
				if(this.replaceStart != start)this.replaceStart += d;
				this.replaceEnd += d;
			}
		}

		public Map<String, Object> toMap() {
			Map<String, Object> m = new HashMap<>();
			m.put("s", start);
			m.put("e", end);
			m.put("rs", replaceStart);
			m.put("re", replaceEnd);
			m.put("ln", line);
			return m;
		}

		public int getD(int c) {
			if(start <= c) {
				return d;
			}
			return 0;
		}
	}

	public static String writeOut() {
		System.out.println("Writing source map offsets");
		Map<String, Object> m = new HashMap<>();
		m.put("corrections", infos.stream().map(ReplaceInfo::toMap).collect(Collectors.toList()));
		m.put("linesOff", linesOff);
		return new Gson().toJson(m);
	}

	public static FixedSourceMap fixSourceMap(String sy) {
		System.out.println("Fixing source maps");
		FixedSourceMap mapOut = new FixedSourceMap();
		mapOut.unFixed = sy;
		mapOut.map = writeOut();
		Map<Integer, List<ReplaceInfo>> infos;
		{
			//Map<String, Object> m = (Map<String, Object>) new Gson().fromJson(fixIn, Object.class);
			infos = UGWTPostProcessor.infos.stream().collect(Collectors.groupingBy(r -> r.line));
			//infos = ((List<Map<String, Object>>) m.get("corrections")).stream().map(ReplaceInfo::new).collect(Collectors.groupingBy(r -> r.line));
			//linesOff = ((Number)m.get("linesOff")).intValue();
		}
		SourceMapImpl map = new SourceMapImpl(sy);
		mapOut.unFixedH = map.generateForHumans();
		List<MappingImpl> out = new ArrayList<>();
		System.out.println("Loaded mappings");
		map.eachMapping(m -> {
			int ncl = m.getGeneratedColumn() + infos.getOrDefault(m.getGeneratedLine(), Collections.emptyList()).stream().mapToInt(ri -> ri.getD(m.getGeneratedColumn())).sum();
			if(m.getSourceFileName() != null) {
				out.add(new MappingImpl(m.getGeneratedLine() + linesOff,
						ncl,
						m.getSourceLine(),
						m.getSourceColumn(),
						m.getSourceFileName(),
						m.getSourceSymbolName()));
			} else {
				out.add(new MappingImpl(m.getGeneratedLine() + linesOff, ncl));
			}
		});
		System.out.println("Remapped lines numbers");
		Collections.sort(out);
		SourceMapImpl srcOut = new SourceMapImpl();
		out.forEach(srcOut::addMapping);
		System.out.println("Writing result");
		mapOut.fixed = srcOut.generate();
		mapOut.fixedH = srcOut.generateForHumans();
		return mapOut;
	}

	public static class FixedSourceMap {
		public String unFixed, fixed;
		public String unFixedH, fixedH;
		public String map;
	}

	private static String setBaseURL(String file) {
		URL url = UGWTPostProcessor.class.getResource("/" + file);
		if(url != null && url.getProtocol().equals("file")) {
			String path = url.getPath();
			if(path.contains("Web/src/main"))return "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/Web/src/main/java/" + file;
			if(path.contains("Web/src/build"))return "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/Web/src/main/build/" + file;
			if(path.contains("Web/src/blockbench"))return "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/Web/src/blockbench/java/" + file;
			if(path.contains("CustomPlayerModels/src/shared"))return "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/CustomPlayerModels/src/shared/java/" + file;
		}
		return "http://0.0.0.0/" + file;
	}

	private static class MappingImpl implements Mapping, Comparable<Mapping> {
		private final int generatedLine;
		private final int generatedColumn;
		private final int sourceLine;
		private final int sourceColumn;
		private final String sourceFileName;
		private final String sourceSymbolName;

		public MappingImpl(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
			this.generatedLine = generatedLine;
			this.generatedColumn = generatedColumn;
			this.sourceLine = sourceLine;
			this.sourceColumn = sourceColumn;
			this.sourceFileName = sourceFileName;
			this.sourceSymbolName = sourceSymbolName;
		}

		public MappingImpl(int generatedLine, int generatedColumn) {
			this.generatedLine = generatedLine;
			this.generatedColumn = generatedColumn;
			this.sourceLine = -1;
			this.sourceColumn = -1;
			this.sourceFileName = null;
			this.sourceSymbolName = null;
		}

		@Override
		public String toString() {
			return "Mapping " + generatedLine + ":" + generatedColumn + " -> " + sourceFileName + ":" + sourceLine + ":" + sourceColumn;
		}

		@Override
		public int getGeneratedLine() {
			return generatedLine;
		}

		@Override
		public int getGeneratedColumn() {
			return generatedColumn;
		}

		@Override
		public int getSourceLine() {
			return sourceLine;
		}

		@Override
		public int getSourceColumn() {
			return sourceColumn;
		}

		@Override
		public String getSourceFileName() {
			return sourceFileName;
		}

		@Override
		public String getSourceSymbolName() {
			return sourceSymbolName;
		}

		@Override
		public int compareTo(Mapping o) {
			int l = Integer.compare(generatedLine, o.getGeneratedLine());
			if(l != 0)return l;
			return Integer.compare(generatedColumn, o.getGeneratedColumn());
		}
	}

	public static interface SourceEmitter {
		SyntheticArtifact emit(TreeLogger logger, String contents, String partialPath) throws UnableToCompleteException;
	}

	@SuppressWarnings("unchecked")
	public static void fixSourceMaps(TreeLogger logger, ArtifactSet artifacts, String strongName, SourceEmitter emitter) throws UnableToCompleteException {
		SyntheticArtifact srcMapFix = artifacts.find(SyntheticArtifact.class).stream().filter(a -> a.getVisibility() == Visibility.Deploy && a.getPartialPath().endsWith(".mapoff")).findFirst().orElse(null);
		BinaryOnlyArtifactWrapper srcMap = artifacts.find(BinaryOnlyArtifactWrapper.class).stream().filter(a -> a.getVisibility() == Visibility.LegacyDeploy && a.getPartialPath().endsWith("_sourceMap0.json")).findFirst().orElse(null);
		if(srcMapFix != null && srcMap != null) {
			String fix = Util.readStreamAsString(srcMapFix.getContents(logger));
			String src = Util.readStreamAsString(srcMap.getContents(logger));

			{
				Map<String, Object> m = (Map<String, Object>) new Gson().fromJson(fix, Object.class);
				infos = ((List<Map<String, Object>>) m.get("corrections")).stream().map(ReplaceInfo::new).collect(Collectors.toList());
				linesOff = ((Number)m.get("linesOff")).intValue();
			}

			FixedSourceMap fsm = UGWTPostProcessor.fixSourceMap(src);
			artifacts.add(emitter.emit(logger, fsm.fixed, strongName + "_sourceMap0.json"));
			artifacts.add(emitter.emit(logger, fsm.unFixed, strongName + "_raw.json"));
			artifacts.add(emitter.emit(logger, fsm.fixedH, strongName + "_sourceMap0.hmap"));
			artifacts.add(emitter.emit(logger, fsm.unFixedH, strongName + "_raw.hmap"));

			artifacts.remove(srcMap);
		}
	}

	public static void emitSourceMaps(FixedSourceMap fsm, TreeLogger logger, ArtifactSet artifacts, String strongName, SourceEmitter emitter) throws UnableToCompleteException {
		BinaryOnlyArtifactWrapper srcMap = artifacts.find(BinaryOnlyArtifactWrapper.class).stream().filter(a -> a.getVisibility() == Visibility.LegacyDeploy && a.getPartialPath().endsWith("_sourceMap0.json")).findFirst().orElse(null);
		artifacts.add(emitter.emit(logger, fsm.fixed, strongName + "_sourceMap0.json"));
		artifacts.add(emitter.emit(logger, fsm.unFixed, strongName + "_raw.json"));
		artifacts.add(emitter.emit(logger, fsm.fixedH, strongName + "_sourceMap0.hmap"));
		artifacts.add(emitter.emit(logger, fsm.unFixedH, strongName + "_raw.hmap"));
		artifacts.add(emitter.emit(logger, fsm.map, strongName + ".mapoff"));

		artifacts.remove(srcMap);
	}

	public static String createInlineSourceMap(FixedSourceMap fsm) {
		ZipArchive za = new ZipArchive();

		za.setEntry("map", fsm.fixed.getBytes(StandardCharsets.UTF_8));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			za.save(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Z:" + Base64.getEncoder().encodeToString(baos.toByteArray());
	}

}