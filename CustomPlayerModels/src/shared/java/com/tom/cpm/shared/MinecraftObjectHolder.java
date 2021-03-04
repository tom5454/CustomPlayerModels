package com.tom.cpm.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MinecraftObjectHolder {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
	protected static MinecraftClientAccess clientObject;
	protected static MinecraftCommonAccess commonObject;
	protected static MinecraftServerAccess serverAccess;

	public static final boolean DEBUGGING = System.getProperty("cpm.debug", "false").equals("true");

	public static void setClientObject(MinecraftClientAccess clientObject) {
		MinecraftObjectHolder.clientObject = clientObject;
	}

	public static void setCommonObject(MinecraftCommonAccess commonObject) {
		MinecraftObjectHolder.commonObject = commonObject;
	}

	public static void setServerObject(MinecraftServerAccess serverObject) {
		MinecraftObjectHolder.serverAccess = serverObject;
	}
}
