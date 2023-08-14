package com.tom.cpl.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;

public class TagManager<T> {
	private final NativeTagManager<T> nativeManager;
	private TagManager<T> parent;
	private Map<String, CPMTag<T>> tags = new HashMap<>();

	public TagManager(NativeTagManager<T> nativeManager) {
		this.nativeManager = nativeManager;
	}

	public TagManager(NativeTagManager<T> nativeManager, TagManager<T> parent) {
		this.nativeManager = nativeManager;
		this.parent = parent;
	}

	public Tag<T> create(String id, String... strings) {
		CPMTag<T> tag = new CPMTag<>(this, id, Arrays.asList(strings));
		tags.put(id, tag);
		return tag;
	}

	public List<T> listStacks(List<String> entries) {
		return listStacks(entries, true);
	}

	private List<T> listStacks(List<String> entries, boolean scanNative) {
		List<T> list = new ArrayList<>();
		if(parent != null)
			list.addAll(parent.listStacks(entries, false));
		for (String e : entries) {
			if (!e.isEmpty()) {
				if (e.charAt(0) == '$') {
					CPMTag<T> tag = tags.get(e.substring(1));
					if (tag != null) {
						list.addAll(tag.getAllStacks());
					}
				} else if(scanNative)
					list.addAll(nativeManager.listNativeEntries(e));
			}
		}
		return list;
	}

	public boolean isInTag(List<String> entries, T stack) {
		return isInTag(entries, stack, true);
	}

	private boolean isInTag(List<String> entries, T stack, boolean scanNative) {
		if(parent != null && parent.isInTag(entries, stack, false))return true;
		for (String e : entries) {
			if (!e.isEmpty()) {
				if (e.charAt(0) == '$') {
					CPMTag<T> tag = tags.get(e.substring(1));
					if (tag != null && tag.is(stack))
						return true;
				} else if(scanNative) {
					if(nativeManager.isInNativeTag(e, stack))
						return true;
				}
			}
		}
		return false;
	}

	public List<Tag<T>> listStackTags(T stack) {
		return listStackTags(stack, true);
	}

	private List<Tag<T>> listStackTags(T stack, boolean scanNative) {
		List<Tag<T>> list = new ArrayList<>();
		if(parent != null)
			list.addAll(parent.listStackTags(stack, false));
		for (CPMTag<T> e : tags.values()) {
			if (e.is(stack))list.add(e);
		}
		if(scanNative)
			list.addAll(nativeManager.listNativeTags(stack).stream().map(t -> new NativeTag<>(nativeManager, t)).collect(Collectors.toList()));
		return list;
	}

	@SuppressWarnings("unchecked")
	public void applyBuiltin(Map<String, List<Map<String, Object>>> tagMap, String prefix) {
		tags.clear();
		tagMap.forEach((t, j) -> {
			try {
				List<String> entries = new ArrayList<>();
				for (Map<String, Object> map : j) {
					if ((boolean) map.getOrDefault("replace", false))entries.clear();
					((List<Object>) map.get("entries")).forEach(e -> entries.add((String) e));
				}
				tags.put(t, new CPMTag<>(this, t, entries));
			} catch (Exception e) {
				ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm builtin tag: " + t, e);
			}
		});
		Log.info("Loaded " + tags.size() + " builtin " + prefix + " tags");
	}

	protected void setParent(TagManager<T> parent) {
		this.parent = parent;
	}
}
