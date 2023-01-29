package com.tom.cpm.blockbench.format;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.util.JsonUtil;
import com.tom.cpm.shared.editor.project.JsonMap;

public class GroupData {
	private final Group group;
	private boolean changed;
	private String itemRenderer;

	public GroupData(Group group) {
		this.group = group;
		if(group.pluginData != null && !group.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(group.pluginData);
			if(s.containsKey("item")) {
				itemRenderer = s.getString("item");
			}
		}
	}

	public void flush() {
		if(changed) {
			Map<String, Object> pluginDt = new HashMap<>();
			if(itemRenderer != null)pluginDt.put("item", itemRenderer);
			if(!pluginDt.isEmpty())group.pluginData = JsonUtil.toJson(pluginDt);
			else group.pluginData = null;
			changed = false;
		}
	}

	public GroupData setItemRenderer(String itemRenderer) {
		this.itemRenderer = itemRenderer;
		markDirty();
		return this;
	}

	public String getItemRenderer() {
		return itemRenderer;
	}

	private void markDirty() {
		changed = true;
	}
}
