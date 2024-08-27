package com.tom.cpm.shared.parts.anim.menu;

import java.util.List;

import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;

public abstract class AbstractDropdownButtonData extends AbstractCommandTriggerableData {
	public abstract List<String> getActiveOptions();
	public abstract void set(String elem);
	public abstract String get();
}
