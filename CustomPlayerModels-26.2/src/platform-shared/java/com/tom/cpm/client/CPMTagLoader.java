package com.tom.cpm.client;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMTagLoader extends SimplePreparableReloadListener<Map<String, List<Map<String, Object>>>> {
	private final TagManager<?> tags;
	protected final String prefix;
	protected final Identifier id;

	public CPMTagLoader(Consumer<CPMTagLoader> mc, TagManager<?> tags, String prefix) {
		this.tags = tags;
		this.prefix = prefix;
		this.id = Identifier.tryBuild("cpm", "tags_" + prefix);
		mc.accept(this);
	}

	@Override
	protected Map<String, List<Map<String, Object>>> prepare(ResourceManager mngr, ProfilerFiller profiler) {
		return load(mngr);
	}

	@Override
	protected void apply(Map<String, List<Map<String, Object>>> tagMap, ResourceManager mngr, ProfilerFiller profiler) {
		tags.applyBuiltin(tagMap, prefix);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> load(ResourceManager mngr) {
		Map<String, List<Map<String, Object>>> el = new HashMap<>();
		mngr.listResourceStacks("cpm_tags/" + prefix, l -> l.getPath().endsWith(".json")).forEach((rl, rs) -> {
			List<Map<String, Object>> res = new ArrayList<>();
			el.put(rl.getNamespace() + ":" + rl.getPath().substring(9, rl.getPath().length() - 5), res);
			rs.forEach(r -> {
				try (BufferedReader rd = r.openAsReader()) {
					Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
					res.add(tag);
				} catch (Exception e) {
					ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm builtin tag: " + rl, e);
				}
			});
		});
		return el;
	}
}
