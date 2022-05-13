package com.tom.cpm.api;

import java.util.EnumMap;
import java.util.Map;

import com.tom.cpm.shared.util.Log;

public class CommonApi implements ICommonAPI {
	private Map<Clazz, Class<?>> classes = new EnumMap<>(Clazz.class);
	private ICPMPlugin initingPlugin;

	public void callInit(ICPMPlugin plugin) {
		initingPlugin = plugin;
		try {
			plugin.initCommon(this);
		} catch (Throwable e) {
			Log.error("Plugin init error, modid: " + plugin.getOwnerModId(), e);
		}
		initingPlugin = null;
	}

	protected CommonApi() {
	}

	private boolean checkClass(Class<?> clazz, Clazz clz) {
		if(classes.containsKey(clz)) {
			if(!classes.get(clz).isAssignableFrom(clazz)) {
				throw new IllegalArgumentException("Class " + clazz + " is not valid for " + clz);
			} else
				return false;
		} else return true;
	}

	public static class ApiBuilder {
		private final CPMApiManager api;

		protected ApiBuilder(CPMApiManager api) {
			this.api = api;
			api.common = new CommonApi();
		}

		public void init() {
			api.initCommon();
		}
	}

	public static enum Clazz {
	}
}
