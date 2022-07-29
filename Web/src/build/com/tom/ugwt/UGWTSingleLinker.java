package com.tom.ugwt;

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
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.core.linker.SingleScriptLinker;

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

		StringBuilder ssb = new StringBuilder();
		StringBuilder gsb = new StringBuilder();

		gsb.append("var $wnd = window;\n");
		gsb.append("var $moduleName, $moduleBase;\n");

		//gsb.append("var $gwt_version = \"" + About.getGwtVersionNum() + "\";\n");
		//gsb.append("var $strongName = '" + result.getStrongName() + "';\n");

		ssb.append(UGWTPostProcessor.postProcess(result.getJavaScript()));
		ssb.append('\n');

		ssb.append("gwtOnLoad(undefined, '" + context.getModuleName() + "', '', 0);\n");

		StringBuffer buf = readFileToStringBuffer("com/tom/ugwt/UGWTSingleLinker.js", logger);

		replaceAll(buf, "__INJECT_GLOBALS__()", gsb.toString());
		replaceAll(buf, "__INJECT_SCRIPT__()", ssb.toString());

		boolean useStrong = System.getProperty("ugwt.useStrongName", "false").equals("true");

		return emitString(logger, buf.toString(), useStrong ? result.getStrongName() + ".cache.js" : context.getModuleName() + ".nocache.js");
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
