package com.tom.cpl.tag;

import java.util.List;

public class CPMTag<T> implements Tag<T> {
	private final TagManager manager;
	private List<T> stacks;
	private final List<String> entries;
	private final String id;

	public CPMTag(TagManager manager, String id, List<String> entries) {
		this.manager = manager;
		this.id = id;
		this.entries = entries;
	}

	@Override
	public List<T> getAllStacks() {
		if(stacks == null)stacks = manager.listStacks(entries);
		return stacks;
	}

	@Override
	public boolean is(T stack) {
		return manager.isInTag(entries, stack);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CPMTag other = (CPMTag) obj;
		if (entries == null) {
			if (other.entries != null) return false;
		} else if (!entries.equals(other.entries)) return false;
		return true;
	}

	@Override
	public String getId() {
		return id;
	}
}
