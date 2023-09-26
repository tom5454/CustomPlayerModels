package com.tom.cpl.tag;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.item.Stack;

public interface IAllTags {
	TagManager<BlockState> getBlockTags();
	TagManager<Stack> getItemTags();
	TagManager<EntityType> getEntityTags();
	TagManager<Biome> getBiomeTags();

	default <T> TagManager<T> getByType(TagType type) {
		switch (type) {
		case BIOME: return (TagManager<T>) getBiomeTags();
		case BLOCK: return (TagManager<T>) getBlockTags();
		case ENTITY: return (TagManager<T>) getEntityTags();
		case ITEM: return (TagManager<T>) getItemTags();
		default: return null;
		}
	}
}
