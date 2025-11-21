package com.tom.cpm.mixinplugin;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class CPMMixinPlugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(mixinClassName.contains("_")) {
			String[] sp = mixinClassName.split("_");
			for (int i = 1; i < sp.length; i++) {
				String string = sp[i];
				if (string.startsWith("$")) {
					if(!MixinModLoaded.isLoaded(string.substring(1).replace('$', '_').toLowerCase(Locale.ROOT)))
						return false;
				} else {
					try {
						if(!(boolean) Class.forName("com.tom.cpm.mixinplugin." + string + "Detector").getDeclaredMethod("doApply").invoke(null))
							return false;
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
				}
			}
			return true;
		} else return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
