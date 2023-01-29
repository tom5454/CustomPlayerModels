package com.tom.ugwt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.core.ext.linker.impl.BinaryOnlyArtifactWrapper;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.core.linker.SingleScriptLinker;
import com.google.gwt.core.linker.SymbolMapsLinker;
import com.google.gwt.dev.util.Util;

import com.tom.ugwt.UGWTPostProcessor.FixedSourceMap;

/**
 * A Linker for producing a single JavaScript file from a GWT module. The use of
 * this Linker requires that the module has exactly one distinct compilation
 * result.
 *
 * Fork of {@link SingleScriptLinker}
 */
@LinkerOrder(Order.PRIMARY)
@Shardable
public class UGWTSingleLinker extends SelectionScriptLinker {

	@Override
	public String getDescription() {
		return "UGWTSingle Script";
	}

	@Transferable
	private static class Script extends Artifact<Script> {
		private final String javaScript;
		private final String strongName;
		private FixedSourceMap fixedMap;

		public Script(String strongName, String javaScript) {
			super(UGWTSingleLinker.class);
			this.strongName = strongName;
			this.javaScript = javaScript;
		}

		@Override
		public int compareToComparableArtifact(Script that) {
			int res = strongName.compareTo(that.strongName);
			if (res == 0) {
				res = javaScript.compareTo(that.javaScript);
			}
			return res;
		}

		@Override
		public Class<Script> getComparableArtifactType() {
			return Script.class;
		}

		public String getJavaScript() {
			return javaScript;
		}

		public String getStrongName() {
			return strongName;
		}

		@Override
		public int hashCode() {
			return strongName.hashCode() ^ javaScript.hashCode();
		}

		@Override
		public String toString() {
			return "Script " + strongName;
		}
	}

	@Override
	protected Collection<Artifact<?>> doEmitCompilation(TreeLogger logger,
			LinkerContext context, CompilationResult result, ArtifactSet artifacts)
					throws UnableToCompleteException {

		String[] js = result.getJavaScript();
		if (js.length != 1) {
			logger.branch(TreeLogger.ERROR,
					"The module must not have multiple fragments when using the "
							+ getDescription() + " Linker.", null);
			throw new UnableToCompleteException();
		}

		Collection<Artifact<?>> toReturn = new ArrayList<>();
		toReturn.add(new Script(result.getStrongName(), js[0]));
		toReturn.addAll(emitSelectionInformation(result.getStrongName(), result));
		return toReturn;
	}

	@Override
	protected EmittedArtifact emitSelectionScript(TreeLogger logger,
			LinkerContext context, ArtifactSet artifacts)
					throws UnableToCompleteException {

		// Find the single Script result
		Set<Script> results = artifacts.find(Script.class);
		if (results.size() != 1) {
			logger.log(TreeLogger.ERROR, "The module must have exactly one distinct"
					+ " permutation when using the " + getDescription() + " Linker; found " + results.size(),
					null);
			throw new UnableToCompleteException();
		}
		Script result = results.iterator().next();

		{
			File out = new File("dump", "ugwt_result.js");
			out.getParentFile().mkdirs();
			try (PrintWriter wr = new PrintWriter(out)){
				wr.println(result.getJavaScript());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String js = UGWTPostProcessor.postProcess(result.getJavaScript(), result.getStrongName());

		StringBuilder ssb = new StringBuilder();
		StringBuilder gsb = new StringBuilder();

		boolean useStrong = System.getProperty("ugwt.useStrongName", "false").equals("true");

		gsb.append("(function() {\n");
		gsb.append("var $wnd = window, $moduleName, $moduleBase;\n");

		UGWTPostProcessor.addPre(2);
		BinaryOnlyArtifactWrapper srcMap = artifacts.find(BinaryOnlyArtifactWrapper.class).stream().filter(a -> a.getVisibility() == Visibility.LegacyDeploy && a.getPartialPath().endsWith("_sourceMap0.json")).findFirst().orElse(null);
		String src = Util.readStreamAsString(srcMap.getContents(logger));
		result.fixedMap = UGWTPostProcessor.fixSourceMap(src);

		ssb.append(js);
		ssb.append('\n');

		ssb.append("__ugwt_sourceMap__ = \"" + UGWTPostProcessor.createInlineSourceMap(result.fixedMap) + "\";\n");
		ssb.append("gwtOnLoad(undefined, '" + context.getModuleName() + "', '', 0);\n})();");
		//ssb.append("\n//# sourceMappingURL=src/" + (useStrong ? result.getStrongName() : context.getModuleName()) + ".map");

		gsb.append(ssb);

		return emitString(logger, gsb.toString(), useStrong ? result.getStrongName() + ".cache.js" : context.getModuleName() + ".nocache.js");
	}

	@Override
	protected void maybeAddHostedModeFile(TreeLogger logger, LinkerContext context, ArtifactSet artifacts,
			CompilationResult result_) throws UnableToCompleteException {
		if(result_ == null) {
			Set<Script> results = artifacts.find(Script.class);
			if (results.size() != 1) {
				logger.log(TreeLogger.ERROR, "The module must have exactly one distinct"
						+ " permutation when using the " + getDescription() + " Linker; found " + results.size(),
						null);
				throw new UnableToCompleteException();
			}
			Script result = results.iterator().next();

			UGWTPostProcessor.emitSourceMaps(result.fixedMap, logger, artifacts, result.getStrongName(), this::emitSourceMapString);
		}
		super.maybeAddHostedModeFile(logger, context, artifacts, result_);
	}

	protected SyntheticArtifact emitSourceMapString(TreeLogger logger, String contents,
			String partialPath) throws UnableToCompleteException {
		SyntheticArtifact emArt = new SyntheticArtifact(SymbolMapsLinker.class, partialPath, Util.getBytes(contents));
		emArt.setVisibility(Visibility.LegacyDeploy);
		return emArt;
	}

	/**
	 * Unimplemented. Normally required by
	 * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult, ArtifactSet)}.
	 */
	@Override
	protected String getCompilationExtension(TreeLogger logger,
			LinkerContext context) throws UnableToCompleteException {
		throw new UnableToCompleteException();
	}

	/**
	 * Unimplemented. Normally required by
	 * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult, ArtifactSet)}.
	 */
	@Override
	protected String getModulePrefix(TreeLogger logger, LinkerContext context,
			String strongName) throws UnableToCompleteException {
		throw new UnableToCompleteException();
	}

	@Override
	protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		return "com/tom/ugwt/UGWTSingleLinker.js";
	}
}
