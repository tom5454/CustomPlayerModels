package com.atlassian.sourcemap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generates Source Map version 3.
 *
 * Code based on Google Closure Compiler https://code.google.com/p/closure-compiler
 */
class Generator {

	// A pre-order traversal ordered list of mappings stored in this map.
	private List<Mapping> mappings = new ArrayList<>();

	private LinkedHashMap<String, Integer> sourceFileNames = new LinkedHashMap<>();
	private int nextSourceFileNameIndex = 0;

	private LinkedHashMap<String, Integer> sourceSymbolNames = new LinkedHashMap<>();
	private int nextSourceSymbolNameIndex = 0;

	private String sourceRootPath;

	/**
	 * Adds a mapping for the given node.  Mappings must be added in order.
	 */
	public void addMapping(Mapping mapping) {
		if (mapping.getSourceFileName() != null) {//PATCH
			if (!sourceFileNames.containsKey(mapping.getSourceFileName())) {
				sourceFileNames.put(mapping.getSourceFileName(), nextSourceFileNameIndex);
				nextSourceFileNameIndex += 1;
			}

			if ((mapping.getSourceSymbolName() != null) && (!sourceSymbolNames.containsKey(mapping.getSourceSymbolName()))) {
				sourceSymbolNames.put(mapping.getSourceSymbolName(), nextSourceSymbolNameIndex);
				nextSourceSymbolNameIndex += 1;
			}
		}

		mappings.add(mapping);
	}

	public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
		addMapping(new MappingImpl(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, sourceSymbolName));
	}

	public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName) {
		addMapping(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, null);
	}

	/**
	 * Writes out the source map in the following format (line numbers are for
	 * reference only and are not part of the format):
	 *
	 * 1.  {
	 * 2.    version: 3,
	 * 3.    file: "out.js",
	 * 4.    lineCount: 2,
	 * 5.    sourceRoot: "",
	 * 6.    sources: ["foo.js", "bar.js"],
	 * 7.    names: ["src", "maps", "are", "fun"],
	 * 8.    mappings: "a;;abcde,abcd,a;"
	 * 9.    x_org_extension: value
	 * 10. }
	 *
	 * Line 1: The entire file is a single JSON object
	 * Line 2: File revision (always the first entry in the object)
	 * Line 3: The name of the file that this source map is associated with.
	 * Line 4: The number of lines represented in the source map.
	 * Line 5: An optional source root, useful for relocating source files on a
	 *     server or removing repeated prefix values in the "sources" entry.
	 * Line 6: A list of sources used by the "mappings" entry relative to the
	 *     sourceRoot.
	 * Line 7: A list of symbol names used by the "mapping" entry.  This list
	 *     may be incomplete.
	 * Line 8: The mappings field.
	 * Line 9: Any custom field (extension).
	 */
	public void generate(Appendable out) {
		try {
			// Add the header fields.
			out.append("{\n");
			appendFirstField(out, "version", "3");
			// appendField(out, "file", escapeString(generatedFileName));
			// appendField(out, "lineCount", String.valueOf(maxLine));

			//optional source root
			if (this.sourceRootPath != null && !this.sourceRootPath.isEmpty())
				appendField(out, "sourceRoot", escapeString(this.sourceRootPath));

			// Files names
			appendFieldStart(out, "sources");
			out.append("[");
			addNameMap(out, sourceFileNames);
			out.append("]");
			appendFieldEnd(out);

			// Files names
			appendFieldStart(out, "names");
			out.append("[");
			addNameMap(out, sourceSymbolNames);
			out.append("]");
			appendFieldEnd(out);

			// Add the mappings themselves.
			appendFieldStart(out, "mappings");
			(new LineMapper(out)).appendLineMappings();

			appendFieldEnd(out);

			out.append("\n}");
		} catch (IOException e) { throw new RuntimeException(e); }
	}

	public String generate() {
		StringBuilder out = new StringBuilder();
		generate(out);
		return out.toString();
	}

	/**
	 * A prefix to be added to the beginning of each sourceName passed to
	 * {@link #addMapping}. Debuggers expect (prefix + sourceName) to be a URL
	 * for loading the source code.
	 *
	 * @param path The URL prefix to save in the sourcemap file. (Not validated.)
	 */
	public void setSourceRoot(String path) {
		this.sourceRootPath = path;
	}


	private void addNameMap(Appendable out, Map<String, Integer> map) throws IOException {
		int i = 0;
		for (Entry<String, Integer> entry : map.entrySet()) {
			String key = entry.getKey();
			if (i != 0) out.append(",");
			out.append(escapeString(key));
			i++;
		}
	}

	/**
	 * Escapes the given string for JSON.
	 */
	private static String escapeString(String value) {
		return InternalUtil.escapeString(value);
	}

	private static void appendFirstField(Appendable out, String name, CharSequence value) throws IOException {
		out.append("  \"");
		out.append(name);
		out.append("\"");
		out.append(":");
		out.append(value);
	}

	private static void appendField(Appendable out, String name, CharSequence value) throws IOException {
		out.append(",\n");
		out.append("  \"");
		out.append(name);
		out.append("\"");
		out.append(":");
		out.append(value);
	}

	private static void appendFieldStart(Appendable out, String name) throws IOException {
		appendField(out, name, "");
	}

	private static void appendFieldEnd(Appendable out) throws IOException {}

	private int getSourceFileNameIndex(String sourceName) {
		Integer index = sourceFileNames.get(sourceName);
		if (index == null) throw new RuntimeException("source file name " + sourceName + " is unknown!");
		return index;
	}

	private int getSourceSymbolNameIndex(String symbolName) {
		Integer index = sourceSymbolNames.get(symbolName);//PATCH
		if (index == null) throw new RuntimeException("source symbol name " + symbolName + " is unknown!");
		return index;
	}

	private class LineMapper {
		// The destination.
		private final Appendable out;

		private int previousLine = -1;
		private int previousColumn = 0;

		// Previous values used for storing relative ids.
		private int previousSourceFileNameId;
		private int previousSourceLine;
		private int previousSourceColumn;
		private int previousSourceSymbolNameId;

		LineMapper(Appendable out) {
			this.out = out;
		}

		// Append the line mapping entries.
		void appendLineMappings() throws IOException {
			openLine(true);
			for (Mapping mapping : mappings)  {
				int generatedLine = mapping.getGeneratedLine();
				int generatedColumn = mapping.getGeneratedColumn();

				if (generatedLine > 0 && previousLine != generatedLine) {
					int start = previousLine == -1 ? 0 : previousLine;
					for (int i = start; i < generatedLine; i++) {
						closeLine(false);
						openLine(false);
					}
				}

				if (previousLine != generatedLine) previousColumn = 0;
				else out.append(',');

				writeEntry(mapping, generatedColumn);
				previousLine = generatedLine;
				previousColumn = generatedColumn;
			}
			closeLine(true);
		}

		/**
		 * Begin the entry for a new line.
		 */
		private void openLine(boolean firstEntry) throws IOException {
			if (firstEntry) out.append('\"');
		}

		/**
		 * End the entry for a line.
		 */
		private void closeLine(boolean finalEntry) throws IOException {
			out.append(';');
			if (finalEntry) out.append('\"');
		}

		/**
		 * Writes an entry for the given column (of the generated text) and
		 * associated mapping.
		 * The values are stored as relative to the last seen values for each
		 * field and encoded as Base64VLQs.
		 */
		void writeEntry(Mapping m, int column) throws IOException {
			// The relative generated column number
			Base64VLQ.encode(out, column - previousColumn);
			previousColumn = column;
			if (m.getSourceFileName() != null) {//PATCH
				// The relative source file id
				int sourceId = getSourceFileNameIndex(m.getSourceFileName());
				Base64VLQ.encode(out, sourceId - previousSourceFileNameId);
				previousSourceFileNameId = sourceId;

				// The relative source file line and column
				int srcline = m.getSourceLine();
				int srcColumn = m.getSourceColumn();
				Base64VLQ.encode(out, srcline - previousSourceLine);
				previousSourceLine = srcline;

				Base64VLQ.encode(out, srcColumn - previousSourceColumn);
				previousSourceColumn = srcColumn;

				if (m.getSourceSymbolName() != null) {
					// The relative id for the associated symbol name
					int nameId = getSourceSymbolNameIndex(m.getSourceSymbolName());
					Base64VLQ.encode(out, (nameId - previousSourceSymbolNameId));
					previousSourceSymbolNameId = nameId;
				}
			}
		}
	}
}