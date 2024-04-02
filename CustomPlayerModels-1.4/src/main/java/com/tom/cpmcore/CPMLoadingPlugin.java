package com.tom.cpmcore;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class CPMLoadingPlugin implements IFMLLoadingPlugin {
	public static boolean deobf;
	public static boolean isLoaded;

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
		CPMLoadingPlugin.deobf = System.getProperty("cpmcore.deobf", "false").equalsIgnoreCase("true");
		isLoaded = true;
		CPMTransformerService.init();
	}

	@Override
	public String[] getLibraryRequestClass() {
		return new String[0];
	}

}
