package com.tom.cpl.block.entity;

import com.tom.cpm.shared.MinecraftCommonAccess;

public class EntityType {
	@SuppressWarnings("unchecked")
	public static final EntityTypeHandler<Object> handler = (EntityTypeHandler<Object>) MinecraftCommonAccess.get().getEntityTypeHandler();
	private Object type;

	public EntityType(Object type) {
		this.type = type;
	}

	public Object getType() {
		return type;
	}

	public String getId() {
		return handler.getEntityId(type);
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EntityType other = (EntityType) obj;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		return true;
	}
}
