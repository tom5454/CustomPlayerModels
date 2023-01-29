package com.tom.ugwt;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.impl.BinaryOnlyArtifactWrapper;
import com.google.gwt.core.linker.CrossSiteIframeLinker;
import com.google.gwt.core.linker.SymbolMapsLinker;
import com.google.gwt.dev.util.Util;

@LinkerOrder(Order.PRIMARY)
@Shardable
public class UGWTCrossSiteIframeLinker extends CrossSiteIframeLinker {

	@Override
	public String getDescription() {
		return "UGWT-Cross-Site-Iframe";
	}

	@Override
	protected String wrapPrimaryFragment(TreeLogger logger, LinkerContext context, String script, ArtifactSet artifacts,
			CompilationResult result) throws UnableToCompleteException {
		return UGWTPostProcessor.postProcess(script, result != null ? result.getStrongName() : "null");
	}

	@Override
	protected void maybeAddHostedModeFile(TreeLogger logger, LinkerContext context, ArtifactSet artifacts,
			CompilationResult result_) throws UnableToCompleteException {
		if(result_ == null) {
			BinaryOnlyArtifactWrapper result = artifacts.find(BinaryOnlyArtifactWrapper.class).stream().filter(a -> a.getVisibility() == Visibility.Public && a.getPartialPath().endsWith(".cache.js")).findFirst().orElse(null);
			if(result != null) {
				String nm = result.getPartialPath().substring(0, result.getPartialPath().length() - 9);
				SyntheticArtifact r = emitString(logger, UGWTPostProcessor.writeOut(), nm + ".mapoff");
				r.setVisibility(Visibility.Deploy);
				artifacts.add(r);

				UGWTPostProcessor.fixSourceMaps(logger, artifacts, nm, this::emitSourceMapString);
			}
		}
		super.maybeAddHostedModeFile(logger, context, artifacts, result_);
	}

	protected SyntheticArtifact emitSourceMapString(TreeLogger logger, String contents,
			String partialPath) throws UnableToCompleteException {
		SyntheticArtifact emArt = new SyntheticArtifact(SymbolMapsLinker.class, partialPath, Util.getBytes(contents));
		emArt.setVisibility(Visibility.LegacyDeploy);
		return emArt;
	}
}
