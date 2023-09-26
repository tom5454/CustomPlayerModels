package com.tom.cpl.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;

public class TagManager<T> {
	protected final NativeTagManager<T> nativeManager;
	protected TagManager<T> parent;
	protected Map<String, CPMTag<T>> tags = new HashMap<>();

	public TagManager(NativeTagManager<T> nativeManager) {
		this.nativeManager = nativeManager;
	}

	public TagManager(TagManager<T> parent) {
		this.nativeManager = parent.nativeManager;
		this.parent = parent;
	}

	public Tag<T> create(String id, String... strings) {
		CPMTag<T> tag = new CPMTag<>(this, id, Arrays.asList(strings));
		tags.put(id, tag);
		return tag;
	}

	public List<T> listStacks(List<String> entries) {
		return listStacksInt(entries, null);
	}

	protected List<T> listStacksInt(List<String> entries, List<CPMTag<T>> dejavu) {
		return listStacksInt(entries, true, dejavu);
	}

	private List<T> listStacksInt(List<String> entries, boolean scanNative, List<CPMTag<T>> dejavu) {
		if (dejavu == null)dejavu = new ArrayList<>();
		Set<T> list = new LinkedHashSet<>();
		if(parent != null)
			list.addAll(parent.listStacksInt(entries, false, dejavu));
		for (String e : entries) {
			if (!e.isEmpty()) {
				if (e.charAt(0) == '$') {
					CPMTag<T> tag = tags.get(e.substring(1));
					if (dejavu.add(tag)) {
						if (tag != null) {
							list.addAll(tag.getAllStacksInt(dejavu));
						}
					}
				} else if(scanNative)
					list.addAll(nativeManager.listNativeEntries(e));
			}
		}
		return new ArrayList<>(list);
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
				boolean builtin = true;
				for (Map<String, Object> map : j) {
					if ((boolean) map.getOrDefault("replace", false)) {
						entries.clear();
						builtin = true;
					}
					builtin &= (boolean) map.getOrDefault("builtin", false);
					((List<Object>) map.get("entries")).forEach(e -> entries.add((String) e));
				}
				tags.put(t, new CPMTag<>(this, t, entries, builtin));
			} catch (Exception e) {
				ErrorLog.addLog(LogLevel.WARNING, "Failed to load cpm builtin tag: " + t, e);
			}
		});
		Log.info("Loaded " + tags.size() + " builtin " + prefix + " tags");
	}

	protected void setParent(TagManager<T> parent) {
		this.parent = parent;
	}

	public List<Tag<T>> listAllTags() {
		return listAllTags(true);
	}

	private List<Tag<T>> listAllTags(boolean listNative) {
		List<Tag<T>> list = new ArrayList<>();
		if(parent != null)
			list.addAll(parent.listAllTags(false));
		list.addAll(tags.values());
		if (listNative)nativeManager.listNativeTags().stream().map(t -> new NativeTag<>(nativeManager, t)).forEach(list::add);
		return list;
	}

	public NativeTagManager<T> getNativeManager() {
		return nativeManager;
	}

	public void fromMap(Map<String, List<String>> tagsIn) {
		tagsIn.forEach((k, e) -> create(k, e.toArray(new String[0])));
	}
}
