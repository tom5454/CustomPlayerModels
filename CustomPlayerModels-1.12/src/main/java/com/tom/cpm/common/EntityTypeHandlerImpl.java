package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.block.entity.EntityTypeHandler;

public class EntityTypeHandlerImpl extends EntityTypeHandler<EntityEntry> {
	public static final EntityTypeHandlerImpl impl = new EntityTypeHandlerImpl();

	@Override
	public List<EntityType> listNativeEntries(String tag) {
		if(tag.charAt(0) == '#') {
			return Collections.emptyList();
		} else {
			ResourceLocation rl = ItemStackHandlerImpl.tryParse(tag);
			EntityEntry item = ForgeRegistries.ENTITIES.getValue(rl);
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
	public boolean isInTag(String tag, EntityEntry state) {
		return getEntityId(state).equals(tag);
	}

	@Override
	public List<String> listTags(EntityEntry state) {
		return Collections.emptyList();
	}

	@Override
	public List<EntityType> getAllElements() {
		return ForgeRegistries.ENTITIES.getValuesCollection().stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(EntityEntry a, EntityEntry b) {
		return a == b;
	}

	@Override
	public String getEntityId(EntityEntry state) {
		return ForgeRegistries.ENTITIES.getKey(state).toString();
	}

	@Override
	public List<String> listAllActiveEffectTypes() {
		return ForgeRegistries.POTIONS.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}
}
