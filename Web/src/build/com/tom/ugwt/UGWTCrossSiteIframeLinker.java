package com.tom.ugwt;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.linker.CrossSiteIframeLinker;

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
		return UGWTPostProcessor.postProcess(script);
	}
}
