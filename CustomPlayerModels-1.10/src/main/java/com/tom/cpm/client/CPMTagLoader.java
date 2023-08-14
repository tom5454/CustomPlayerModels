package com.tom.cpm.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMTagLoader implements IResourceManagerReloadListener {
	private final TagManager<?> tags;
	private final String prefix;

	public CPMTagLoader(IResourceManager mngr, TagManager<?> tags, String prefix) {
		this.tags = tags;
		this.prefix = prefix;
		//tags.applyBuiltin(load(mngr), prefix);
		if(mngr instanceof IReloadableResourceManager)
			((IReloadableResourceManager)mngr).registerReloadListener(this);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> load(IResourceManager mngr) {
		Map<String, List<Map<String, Object>>> el = new HashMap<>();
		Set<ResourceLocation> allEntries = new HashSet<>();
		mngr.getResourceDomains().forEach(d -> {
			ResourceLocation rl = new ResourceLocation(d, "cpm_tags/dictionary.json");
			try {
				List<IResource> res = mngr.getAllResources(rl);
				for (IResource r : res) {
					try (
							IResource iresource = r;
							InputStream inputstream = iresource.getInputStream();
							Reader rd = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
							) {
						Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
						List<String> entries = (List<String>) tag.get(prefix);
						if (entries != null) {
							for (String e : entries) {
								allEntries.add(new ResourceLocation(d, "cpm_tags/" + prefix + "/" + e + ".json"));
							}
						}
					} catch (Exception e) {
						ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm tag dictionary: " + rl, e);
					}
				}
			} catch (IOException e) {
			}
		});
		allEntries.forEach(rl -> {
			List<Map<String, Object>> res = new ArrayList<>();
			el.put(rl.getResourceDomain() + ":" + rl.getResourcePath().substring(9, rl.getResourcePath().length() - 5), res);
			try {
				for (IResource r : mngr.getAllResources(rl)) {
					try (
							IResource iresource = r;
							InputStream inputstream = iresource.getInputStream();
							Reader rd = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
							) {
						Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
						res.add(tag);
					}
				}
			} catch (Exception e) {
				ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm builtin tag: " + rl, e);
			}
		});
		return el;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		tags.applyBuiltin(load(resourceManager), prefix);
	}
}
