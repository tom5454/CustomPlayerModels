package com.tom.cpm.client;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;

import com.tom.cpl.tag.TagManager;

public class CPMTagLoaderFabric extends CPMTagLoader implements IdentifiableResourceReloadListener {

	public CPMTagLoaderFabric(ResourceManagerHelper mc, TagManager<?> tags, String prefix) {
		super(l -> mc.registerReloadListener((CPMTagLoaderFabric) l), tags, prefix);
	}

	@Override
	public Identifier getFabricId() {
		return id;
	}
}