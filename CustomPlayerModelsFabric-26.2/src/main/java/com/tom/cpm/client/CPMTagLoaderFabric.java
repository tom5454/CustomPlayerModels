package com.tom.cpm.client;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import com.tom.cpl.tag.TagManager;

public class CPMTagLoaderFabric extends CPMTagLoader implements PreparableReloadListener {

	public CPMTagLoaderFabric(ResourceLoader mc, TagManager<?> tags, String prefix) {
		super(l -> mc.registerReloadListener(l.id, l), tags, prefix);
	}

}