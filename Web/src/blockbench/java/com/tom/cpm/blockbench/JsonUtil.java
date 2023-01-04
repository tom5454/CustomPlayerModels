package com.tom.cpm.blockbench;

import java.util.Map;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.JsonMapImpl;

public class JsonUtil {

	public static String toJson(Map<String, Object> from) {
		return MinecraftObjectHolder.gson.toJson(from);
	}

	public static JsonMap fromJson(String text) {
		return new JsonMapImpl((Map<String, Object>) MinecraftObjectHolder.gson.fromJson(text, Object.class));
	}
}
