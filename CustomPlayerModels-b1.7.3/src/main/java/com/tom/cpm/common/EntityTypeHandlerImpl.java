package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.class_206;

import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.block.entity.EntityTypeHandler;

public class EntityTypeHandlerImpl extends EntityTypeHandler<Class<?>> {
	public static final EntityTypeHandlerImpl impl = new EntityTypeHandlerImpl();

	@Override
	public List<EntityType> listNativeEntries(String tag) {
		if(tag.charAt(0) == '#') {
			return Collections.emptyList();
		} else {
			Class<?> item = (Class<?>) class_206.field_757.get(tag);
			if (item != null) {
				return Collections.singletonList(wrap(item));
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public EntityType emptyObject() {
		return null;
	}

	@Override
	public boolean isInTag(String tag, Class<?> state) {
		return getEntityId(state).equals(tag);
	}

	@Override
	public List<String> listTags(Class<?> state) {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EntityType> getAllElements() {
		return ((Set<Class<?>>) class_206.field_758.keySet()).stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Class<?> a, Class<?> b) {
		return a == b;
	}

	@Override
	public String getEntityId(Class<?> state) {
		return (String) class_206.field_758.get(state);
	}

	@Override
	public List<String> listAllActiveEffectTypes() {
		return Collections.emptyList();
	}
}
