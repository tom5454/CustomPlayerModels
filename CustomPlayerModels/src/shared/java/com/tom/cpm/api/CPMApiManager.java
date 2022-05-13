package com.tom.cpm.api;

import java.util.ArrayList;
import java.util.List;

public class CPMApiManager {
	private List<ICPMPlugin> plugins = new ArrayList<>();
	protected ClientApi client;
	protected CommonApi common;

	public void register(ICPMPlugin plugin) {
		plugins.add(plugin);
	}

	public String getPluginStatus() {
		StringBuilder bb = new StringBuilder();
		bb.append("Loaded plugins: (");
		bb.append(plugins.size());
		bb.append(")\n");
		plugins.forEach(p -> {
			bb.append('\t');
			bb.append(p.getOwnerModId());
			bb.append('\n');
		});
		return bb.toString();
	}

	public ClientApi clientApi() {
		return client;
	}

	protected void initClient() {
		plugins.forEach(client::callInit);
	}

	protected void initCommon() {
		plugins.forEach(common::callInit);
	}

	public ClientApi.ApiBuilder buildClient() {
		return new ClientApi.ApiBuilder(this);
	}

	public CommonApi.ApiBuilder buildCommon() {
		return new CommonApi.ApiBuilder(this);
	}
}
