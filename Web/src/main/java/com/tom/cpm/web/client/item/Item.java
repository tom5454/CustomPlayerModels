package com.tom.cpm.web.client.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Item {
	public static final Item AIR = new Item();
	public final String id;
	public final int spriteId;
	public final List<String> tooltip;

	@SuppressWarnings("unchecked")
	public Item(Map<String, Object> map) {
		this.id = (String) map.get("id");
		this.spriteId = ((Number) map.get("sprite")).intValue();
		this.tooltip = (List<String>) map.get("tooltip");
	}

	private Item() {
		this.id = "minecraft:air";
		this.spriteId = 0;
		this.tooltip = Arrays.asList("Air");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Item other = (Item) obj;
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;
		return true;
	}
}
