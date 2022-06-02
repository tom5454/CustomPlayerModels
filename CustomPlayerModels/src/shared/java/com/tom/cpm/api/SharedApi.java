package com.tom.cpm.api;

import java.util.EnumMap;
import java.util.Map;

import com.tom.cpm.shared.util.Log;

public abstract class SharedApi implements ISharedAPI {
	protected Map<Clazz, Class<?>> classes = new EnumMap<>(Clazz.class);
	protected ICPMPlugin initingPlugin;

	protected boolean checkClass(Class<?> clazz, Clazz clz) {
		if(classes.containsKey(clz)) {
			if(!classes.get(clz).isAssignableFrom(clazz)) {
				throw new IllegalArgumentException("Class " + clazz + " is not valid for " + clz);
			} else
				return false;
		} else return true;
	}

	public void callInit(ICPMPlugin plugin) {
		initingPlugin = plugin;
		try {
			callInit0(plugin);
		} catch (Throwable e) {
			Log.error("Plugin init error, modid: " + plugin.getOwnerModId(), e);
		}
		initingPlugin = null;
	}

	protected abstract void callInit0(ICPMPlugin plugin);

	public static enum Clazz {
		PLAYER,
		MODEL,
		RESOURCE_LOCATION,
		RENDER_TYPE,
		MULTI_BUFFER_SOURCE,
		GAME_PROFILE,
	}
}
