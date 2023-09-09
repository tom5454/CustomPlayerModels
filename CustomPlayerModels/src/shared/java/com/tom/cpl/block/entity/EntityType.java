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
}
