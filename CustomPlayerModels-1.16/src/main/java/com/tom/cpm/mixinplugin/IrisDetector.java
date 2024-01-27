package com.tom.cpm.mixinplugin;

import net.minecraftforge.fml.loading.LoadingModList;

public class IrisDetector {

	public static boolean doApply() {
		return LoadingModList.get().getModFileById("oculus") != null;
	}
}
