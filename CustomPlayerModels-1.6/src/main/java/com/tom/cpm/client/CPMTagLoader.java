package com.tom.cpm.client;

import java.io.BufferedReader;
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

import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMTagLoader implements ResourceManagerReloadListener {
	private final TagManager<?> tags;
	private final String prefix;

	public CPMTagLoader(ResourceManager mngr, TagManager<?> tags, String prefix) {
		this.tags = tags;
		this.prefix = prefix;
		//tags.applyBuiltin(load(mngr), prefix);
		if(mngr instanceof ReloadableResourceManager)
			((ReloadableResourceManager)mngr).registerReloadListener(this);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> load(ResourceManager mngr) {
		Map<String, List<Map<String, Object>>> el = new HashMap<>();
		Set<ResourceLocation> allEntries = new HashSet<>();
		mngr.getResourceDomains().forEach(d -> {
			ResourceLocation rl = new ResourceLocation((String) d, "cpm_tags/dictionary.json");
			try {
				List<Resource> res = mngr.getAllResources(rl);
				for (Resource r : res) {
					try (
							InputStream inputstream = r.getInputStream();
							Reader rd = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
							) {
						Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
						List<String> entries = (List<String>) tag.get(prefix);
						if (entries != null) {
							for (String e : entries) {
								allEntries.add(new ResourceLocation((String) d, "cpm_tags/" + prefix + "/" + e + ".json"));
							}
						}
					} catch (Exception e) {
						ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm tag dictionary: " + rl, e);
					}
				}
			} catch (Exception e) {
			}
		});
		allEntries.forEach(rl -> {
			List<Map<String, Object>> res = new ArrayList<>();
			el.put(rl.getResourceDomain() + ":" + rl.getResourcePath().substring(9, rl.getResourcePath().length() - 5), res);
			try {
				for (Resource r : (List<Resource>) mngr.getAllResources(rl)) {
					try (
							InputStream inputstream = r.getInputStream();
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
	public void onResourceManagerReload(ResourceManager resourceManager) {
		tags.applyBuiltin(load(resourceManager), prefix);
	}
}
