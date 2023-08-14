package com.tom.cpm.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;

import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMTagLoader extends ReloadListener<Map<String, List<Map<String, Object>>>> {
	private final TagManager<?> tags;
	private final String prefix;

	public CPMTagLoader(Minecraft mc, TagManager<?> tags, String prefix) {
		this.tags = tags;
		this.prefix = prefix;
		IResourceManager mngr = mc.getResourceManager();
		if (mngr != null) {
			init(mngr);
		} else {
			mc.tell(() -> init(mc.getResourceManager()));
		}
	}

	private void init(IResourceManager mngr) {
		tags.applyBuiltin(load(mngr), prefix);
		if(mngr instanceof IReloadableResourceManager)
			((IReloadableResourceManager)mngr).registerReloadListener(this);
	}

	@Override
	protected Map<String, List<Map<String, Object>>> prepare(IResourceManager mngr, IProfiler profiler) {
		return load(mngr);
	}

	@Override
	protected void apply(Map<String, List<Map<String, Object>>> tagMap, IResourceManager mngr, IProfiler profiler) {
		tags.applyBuiltin(tagMap, prefix);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> load(IResourceManager mngr) {
		Map<String, List<Map<String, Object>>> el = new HashMap<>();
		mngr.listResources("cpm_tags/" + prefix, l -> l.endsWith(".json")).forEach(rl -> {
			List<Map<String, Object>> res = new ArrayList<>();
			el.put(rl.getNamespace() + ":" + rl.getPath().substring(9, rl.getPath().length() - 5), res);
			try {
				for (IResource r : mngr.getResources(rl)) {
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
}
