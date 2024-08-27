package com.tom.cpm.shared.parts.anim.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;

public class LegacyDropdownButtonData extends AbstractDropdownButtonData {
	public Map<String, Integer> parameters;

	public LegacyDropdownButtonData() {
		parameters = new HashMap<>();
		parameters.put("", -1);
	}

	@Override
	public void onRegistered() {
		super.onRegistered();
		for (Entry<String, Integer> e : parameters.entrySet()) {
			String opt = e.getKey();
			commandActions.add(new SimpleParameterValueAction(opt, e.getValue(), 1, command));
		}
	}

	@Override
	public GestureButtonType getType() {
		return GestureButtonType.DROPDOWN;
	}

	@Override
	public List<String> getActiveOptions() {
		return new ArrayList<>(parameters.keySet());
	}

	@Override
	public void set(String selected) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		parameters.forEach((k, v) -> {
			if (v == -1)return;
			an.setGestureValue(v, k.equals(selected) ? 1 : 0);
		});
	}

	@Override
	public String get() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return parameters.entrySet().stream().filter(e -> an.getGestureValue(e.getValue()) > 0).map(Map.Entry::getKey).findFirst().orElse("");
	}

	@Override
	public String getKeybindId() {
		return "d" + name;
	}

	@Override
	public void storeTo(ConfigEntry ce) {
		String sel = get();
		parameters.forEach((k, v) -> {
			if (v == -1)return;
			ce.setBoolean(k, k.equals(sel));
		});
	}

	@Override
	public void loadFrom(ConfigEntry ce) {
		String val = parameters.entrySet().stream().filter(e -> ce.getBoolean(e.getKey(), false)).map(Map.Entry::getKey).findFirst().orElse("");
		set(val);
	}

	public void register(String id, int param) {
		parameters.put(id, param);
	}
}
