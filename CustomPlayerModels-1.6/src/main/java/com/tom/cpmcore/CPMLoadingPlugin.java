package com.tom.cpmcore;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class CPMLoadingPlugin implements IFMLLoadingPlugin {
	public static boolean deobf;

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"com.tom.cpmcore.CPMTransformerService"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		Object deobf = data.get("runtimeDeobfuscationEnabled");
		if(deobf == Boolean.FALSE) {
			CPMLoadingPlugin.deobf = true;
		}
		CPMTransformerService.init();
	}

	@Override
	public String[] getLibraryRequestClass() {
		return new String[0];
	}

}
