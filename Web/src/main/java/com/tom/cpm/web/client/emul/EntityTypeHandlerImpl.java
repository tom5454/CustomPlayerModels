package com.tom.cpm.web.client.emul;

import java.util.Collections;
import java.util.List;

import com.tom.cpl.block.entity.EntityTypeHandler;

public class EntityTypeHandlerImpl extends EntityTypeHandler<EntityType> {
	public static final EntityTypeHandlerImpl impl = new EntityTypeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.entity.EntityType> listNativeEntries(String tag) {
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.entity.EntityType> getAllElements() {
		return Collections.emptyList();
	}

	@Override
	public com.tom.cpl.block.entity.EntityType emptyObject() {
		return null;
	}

	@Override
	public boolean isInTag(String tag, EntityType state) {
		return false;
	}

	@Override
	public List<String> listTags(EntityType state) {
		return Collections.emptyList();
	}

	@Override
	public boolean equals(EntityType a, EntityType b) {
		return a == b;
	}

	@Override
	public String getEntityId(EntityType state) {
		return "";
	}

	@Override
	public List<String> listAllActiveEffectTypes() {
		return Collections.emptyList();
	}
}
