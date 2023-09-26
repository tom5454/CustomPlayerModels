package com.tom.cpl.block.entity;

import java.util.List;

import com.tom.cpl.tag.NativeTagManager;

public abstract class EntityTypeHandler<E> implements NativeTagManager<EntityType> {

	public EntityType wrap(E stack) {
		return new EntityType(stack);
	}

	@SuppressWarnings("unchecked")
	public E unwrap(EntityType state) {
		return (E) state.getType();
	}

	@Override
	public List<String> listNativeTags(EntityType state) {
		return listTags(unwrap(state));
	}

	@Override
	public boolean isInNativeTag(String tag, EntityType state) {
		return isInTag(tag, unwrap(state));
	}

	@Override
	public String getId(EntityType type) {
		return type.getId();
	}

	public abstract boolean isInTag(String tag, E state);
	public abstract List<String> listTags(E state);
	public abstract boolean equals(E a, E b);
	public abstract String getEntityId(E state);
	public abstract List<String> listAllActiveEffectTypes();
}
