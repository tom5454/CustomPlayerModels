package com.atlassian.sourcemap;

import static com.atlassian.sourcemap.InternalUtil.join;

import java.util.ArrayList;
import java.util.List;

public class SourceMapImpl implements SourceMap {
	private State state;

	/**
	 * Parse source map.
	 * @param sourceMap source map content.
	 */
	public SourceMapImpl(String sourceMap) {
		this.state = new Read(this, new Consumer(sourceMap));
	}

	/**
	 * Create empty source map.
	 */
	public SourceMapImpl() {
		this.state = new None(this);
	}

	protected SourceMapImpl(SourceMap sourceMap, int offset) {
		this.state = new DeferredOffset(this, sourceMap, offset);
	}

	@Override
	public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName) {
		addMapping(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, null);
	}

	@Override
	public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
		addMapping(new MappingImpl(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, sourceSymbolName));
	}

	@Override
	public void addMapping(Mapping mapping) {
		state.addMapping(mapping);
	}

	@Override
	public Mapping getMapping(int lineNumber, int column) {
		return state.getMapping(lineNumber, column);
	}

	@Override
	public String generate() {
		return state.generate();
	}

	@Override
	public String generateForHumans() {
		Consumer consumer = new Consumer(generate());
		final StringBuilder buff = new StringBuilder();
		buff.append("{\n");
		buff.append("  sources  : [\n    " + join(consumer.getSourceFileNames(), "\n    ") + "\n  ]\n");
		buff.append("  mappings : [\n    ");
		final int[] previousLine = new int[]{-1};
		consumer.eachMapping(new EachMappingCallback() {

			@Override
			public void apply(Mapping mapping) {
				if ((mapping.getGeneratedLine() != previousLine[0]) && (previousLine[0] != -1)) buff.append("\n    ");
				else if (previousLine[0] != -1) buff.append(", ");
				previousLine[0] = mapping.getGeneratedLine();

				if(mapping.getSourceFileName() == null) {//PATCH
					buff.append("(" + mapping.getGeneratedLine() + ":" + mapping.getGeneratedColumn() + ")");
				} else {
					String shortName = mapping.getSourceFileName().replaceAll(".*/", "");
					buff.append("(" + mapping.getGeneratedLine() + ":" + mapping.getGeneratedColumn() + " -> "
							+ shortName + ":" + mapping.getSourceLine() + ":" + mapping.getSourceColumn() + ")");
				}
			}
		});
		buff.append("\n  ]\n}");
		return buff.toString();
	}

	@Override
	public List<String> getSourceFileNames() {
		return state.getSourceFileNames();
	}

	@Override
	public void eachMapping(EachMappingCallback callback) {
		state.eachMapping(callback);
	}

	/**
	 * Due to the poor current implementation it's possible to either write or read the source map but not
	 * read and write it simultaneously.
	 *
	 * In order to simplify working with source map its API looks as if it allows simultaneous read and write
	 * but it does it by serializing / deserializing the source map to the string.
	 *
	 * It's implemented as a state machine, switching into Read, Write or DeferredOffset state.
	 */
	private static interface State {
		void addMapping(Mapping mapping);

		public String generate();

		public void eachMapping(EachMappingCallback callback);

		public Mapping getMapping(int lineNumber, int column);

		public List<String> getSourceFileNames();
	}

	/**
	 * Initial state, does nothing except of switching into another states.
	 */
	private static class None implements State {
		private final SourceMapImpl thisSourceMap;

		public None(SourceMapImpl thisSourceMap) {
			this.thisSourceMap = thisSourceMap;
		}

		@Override
		public void addMapping(Mapping mapping) {
			switchIntoWriteState().addMapping(mapping);
		}

		@Override
		public String generate() {
			return switchIntoReadState().generate();
		}

		@Override
		public void eachMapping(EachMappingCallback callback) {
			switchIntoReadState().eachMapping(callback);
		}

		@Override
		public Mapping getMapping(int lineNumber, int column) {
			return switchIntoReadState().getMapping(lineNumber, column);
		}

		@Override
		public List<String> getSourceFileNames() {
			return switchIntoReadState().getSourceFileNames();
		}

		private State switchIntoReadState() {
			this.thisSourceMap.state = new Read(thisSourceMap, new Consumer(
					"{\n" +
							"  \"version\":3,\n" +
							"  \"sources\":[],\n" +
							"  \"names\":[],\n" +
							"  \"mappings\":\"\"\n" +
							"}"
					));
			return this.thisSourceMap.state;
		}

		private State switchIntoWriteState() {
			this.thisSourceMap.state = new Write(thisSourceMap);
			return this.thisSourceMap.state;
		}
	}

	/**
	 * State of reading source map.
	 */
	private static class Read implements State {
		private Consumer consumer;
		private SourceMapImpl thisSourceMap;

		public Read(SourceMapImpl thisSourceMap, Consumer consumer) {
			this.thisSourceMap = thisSourceMap;
			this.consumer = consumer;
		}

		@Override
		public void eachMapping(final EachMappingCallback callback) {
			consumer.eachMapping(callback);
		}

		@Override
		public Mapping getMapping(int lineNumber, int column) {
			return consumer.getMapping(lineNumber, column);
		}

		@Override
		public List<String> getSourceFileNames() {
			return new ArrayList<>(consumer.getSourceFileNames());
		}

		@Override
		public void addMapping(Mapping mapping) {
			throw new RuntimeException("operation getSourceFileNames not supported in " + this.getClass().getSimpleName() + " state!");
		}

		@Override
		public String generate() {
			final Generator generator = new Generator();
			eachMapping(new EachMappingCallback()
			{
				@Override
				public void apply(Mapping mapping)
				{
					generator.addMapping(mapping);
				}
			});
			return generator.generate();
		}
	}

	/**
	 * State of writing source map.
	 */
	private static class Write implements State {
		private SourceMapImpl thisSourceMap;
		private Generator generator;
		private int lastGeneratedLine = 0;
		private int lastGeneratedColumn = 0;

		public Write(SourceMapImpl thisSourceMap) {
			this.thisSourceMap = thisSourceMap;
			this.generator = new Generator();
		}

		@Override
		public void addMapping(Mapping mapping) {
			// In current implementation of generator it's required that lines where added in proper order,
			// checking for it.
			if (lastGeneratedLine > mapping.getGeneratedLine())
				throw new RuntimeException("mappings should be added in a proper order!");
			else if ((lastGeneratedLine == mapping.getGeneratedLine()) && (lastGeneratedColumn > mapping.getGeneratedColumn()))
				throw new RuntimeException("mappings should be added in a proper order!");
			lastGeneratedLine = mapping.getGeneratedLine();
			lastGeneratedColumn = mapping.getGeneratedColumn();

			generator.addMapping(mapping);
		}

		@Override
		public String generate() {
			return generator.generate();
		}

		@Override
		public void eachMapping(final EachMappingCallback callback) {
			performanceInefficientSwitchIntoReadState().eachMapping(callback);
		}

		@Override
		public Mapping getMapping(int lineNumber, int column) {
			return performanceInefficientSwitchIntoReadState().getMapping(lineNumber, column);
		}

		@Override
		public List<String> getSourceFileNames() {
			return performanceInefficientSwitchIntoReadState().getSourceFileNames();
		}

		// Switching into read mode, no writes would be allowed after this call, it's also
		// not efficient because it requires serialization of source map data to and from string.
		private Read performanceInefficientSwitchIntoReadState() {
			Read read = new Read(thisSourceMap, new Consumer(generate()));
			thisSourceMap.state = read;
			return read;
		}
	}

	// The actual offset calculation is deferred in order to improve the performance in case of multiple
	// offset transformations.
	private static class DeferredOffset implements State {
		private final SourceMap sourceMapWithoutOffset;
		private int offset = 0;
		private SourceMapImpl thisSourceMap;

		public DeferredOffset(SourceMapImpl thisSourceMap, SourceMap sourceMapWithoutOffset, int offset) {
			this.thisSourceMap = thisSourceMap;
			this.sourceMapWithoutOffset = sourceMapWithoutOffset;
			this.offset = offset;
		}

		@Override
		public void addMapping(Mapping mapping) {
			throw new RuntimeException("operation addMapping not supported in " + this.getClass().getSimpleName() + " state!");
		}

		@Override
		public String generate() {
			return calculateOffsetAndSwitchIntoWriteState().generate();
		}

		@Override
		public void eachMapping(EachMappingCallback callback) {
			calculateOffsetAndSwitchIntoWriteState().eachMapping(callback);
		}

		@Override
		public Mapping getMapping(int lineNumber, int column) {
			return calculateOffsetAndSwitchIntoWriteState().getMapping(lineNumber, column);
		}

		@Override
		public List<String> getSourceFileNames() {
			return calculateOffsetAndSwitchIntoWriteState().getSourceFileNames();
		}

		private State calculateOffsetAndSwitchIntoWriteState() {
			final Write write = new Write(thisSourceMap);
			sourceMapWithoutOffset.eachMapping(new EachMappingCallback() {
				@Override
				public void apply(Mapping mapping) {
					write.addMapping(new MappingImpl(
							offset + mapping.getGeneratedLine(),
							mapping.getGeneratedColumn(),
							mapping.getSourceLine(),
							mapping.getSourceColumn(),
							mapping.getSourceFileName(),
							mapping.getSourceSymbolName()
							));
				}
			});
			thisSourceMap.state = write;
			return write;
		}
	}
}