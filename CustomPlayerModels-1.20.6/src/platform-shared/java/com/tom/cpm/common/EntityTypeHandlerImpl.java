package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.tom.cpl.block.entity.EntityTypeHandler;

public class EntityTypeHandlerImpl extends EntityTypeHandler<EntityType<?>> {
	public static final EntityTypeHandlerImpl impl = new EntityTypeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.entity.EntityType> listNativeEntries(String tag) {
		List<com.tom.cpl.block.entity.EntityType> stacks = new ArrayList<>();
		if (tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<EntityType<?>> i = TagKey.create(Registries.ENTITY_TYPE, rl);
				BuiltInRegistries.ENTITY_TYPE.getTag(i).map(t -> {
					return t.stream().map(h -> h.unwrap().right()).filter(Optional::isPresent)
							.map(o -> wrap(o.get())).toList();
				}).ifPresent(stacks::addAll);
			}
		} else {
			ResourceLocation rl = ResourceLocation.tryParse(tag);
			EntityType<?> item = BuiltInRegistries.ENTITY_TYPE.get(rl);
			if (item != null) {
				stacks.add(wrap(item));
			}
		}
		return stacks;
	}

	@Override
	public List<String> listNativeTags() {
		return BuiltInRegistries.ENTITY_TYPE.getTagNames().map(k -> k.location().toString()).toList();
	}

	@Override
	public com.tom.cpl.block.entity.EntityType emptyObject() {
		return null;
	}

	@Override
	public boolean isInTag(String tag, EntityType<?> state) {
		if (tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<EntityType<?>> i = TagKey.create(Registries.ENTITY_TYPE, rl);
				return state.is(i);
			}
		} else {
			return getEntityId(state).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(EntityType<?> state) {
		return state.builtInRegistryHolder().tags().map(k -> "#" + k.location()).toList();
	}

	@Override
	public List<com.tom.cpl.block.entity.EntityType> getAllElements() {
		return BuiltInRegistries.ENTITY_TYPE.stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(EntityType<?> a, EntityType<?> b) {
		return a == b;
	}

	@Override
	public String getEntityId(EntityType<?> state) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(state).toString();
	}

	@Override
	public List<String> listAllActiveEffectTypes() {
		return BuiltInRegistries.POTION.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}
}
