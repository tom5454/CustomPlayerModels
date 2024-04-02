package com.tom.cpm.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.texturepacks.TexturePackList;

import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMTagLoader {
	private static final List<String> DOMAINS = Arrays.asList("cpm");
	private final TagManager<?> tags;
	private final String prefix;

	public CPMTagLoader(TexturePackList mngr, TagManager<?> tags, String prefix) {
		this.tags = tags;
		this.prefix = prefix;
		tags.applyBuiltin(load(), prefix);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> load() {
		Map<String, List<Map<String, Object>>> el = new HashMap<>();
		Set<String> allEntries = new HashSet<>();
		DOMAINS.forEach(d -> {
			String rl = "/assets/" + d + "/cpm_tags/dictionary.json";
			try {
				InputStream is = CPMTagLoader.class.getResourceAsStream(rl);
				if (is == null)return;
				try (InputStream inputstream = is;
						Reader rd = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
						) {
					Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
					List<String> entries = (List<String>) tag.get(prefix);
					if (entries != null) {
						for (String e : entries) {
							allEntries.add(d + ":cpm_tags/" + prefix + "/" + e + ".json");
						}
					}
				} catch (Exception e) {
					ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm tag dictionary: " + rl, e);
				}
			} catch (Exception e) {
			}
		});
		allEntries.forEach(rl -> {
			List<Map<String, Object>> res = new ArrayList<>();
			String[] sp = rl.split(":");
			el.put(sp[0] + ":" + sp[1].substring(9, sp[1].length() - 5), res);
			try {
				InputStream is = CPMTagLoader.class.getResourceAsStream("/assets/" + sp[0] + "/" + sp[1]);
				if (is == null)return;
				try (
						InputStream inputstream = is;
						Reader rd = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
						) {
					Map<String, Object> tag = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
					res.add(tag);
				}
			} catch (Exception e) {
				ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm builtin tag: " + rl, e);
			}
		});
		return el;
	}
}
