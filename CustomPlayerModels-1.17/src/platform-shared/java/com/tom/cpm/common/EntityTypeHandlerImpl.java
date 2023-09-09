package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

import com.tom.cpl.block.entity.EntityTypeHandler;

public class EntityTypeHandlerImpl extends EntityTypeHandler<EntityType<?>> {
	public static final EntityTypeHandlerImpl impl = new EntityTypeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.entity.EntityType> listNativeEntries(String tag) {
		List<com.tom.cpl.block.entity.EntityType> stacks = new ArrayList<>();
		if(tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				Tag<EntityType<?>> t = EntityTypeTags.getAllTags().getTag(rl);
				if (t != null) {
					t.getValues().stream().map(this::wrap).forEach(stacks::add);
				}
			}
		} else {
			ResourceLocation rl = ResourceLocation.tryParse(tag);
			EntityType<?> item = Registry.ENTITY_TYPE.get(rl);
			if (item != null) {
				stacks.add(wrap(item));
			}
		}
		return stacks;
	}

	@Override
	public List<String> listNativeTags() {
		return EntityTypeTags.getAllTags().getAvailableTags().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}

	@Override
	public com.tom.cpl.block.entity.EntityType emptyObject() {
		return null;
	}

	@Override
	public boolean isInTag(String tag, EntityType<?> state) {
		if(tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				Tag<EntityType<?>> t = EntityTypeTags.getAllTags().getTag(rl);
				if (t != null) {
					return t.contains(state);
				}
			}
		} else {
			return getEntityId(state).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(EntityType<?> state) {
		return EntityTypeTags.getAllTags().getMatchingTags(state).stream().map(e -> "#" + e).collect(Collectors.toList());
	}

	@Override
	public List<com.tom.cpl.block.entity.EntityType> getAllTypes() {
		return Registry.ENTITY_TYPE.stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(EntityType<?> a, EntityType<?> b) {
		return a == b;
	}

	@Override
	public String getEntityId(EntityType<?> state) {
		return Registry.ENTITY_TYPE.getKey(state).toString();
	}
}
