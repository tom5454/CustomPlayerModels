package com.tom.cpl.tag;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.item.Stack;
import com.tom.cpl.util.TriConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;

public class AllTagManagers {
	private final TagManager<BlockState> blockTags = new TagManager<>(BlockState.handler);
	private final TagManager<Stack> itemTags = new TagManager<>(Stack.handler);
	private final TagManager<EntityType> entityTags = new TagManager<>(EntityType.handler);
	private final TagManager<Biome> biomeTags;

	public <E> AllTagManagers(E e, TriConsumer<E, TagManager<?>, String> loader) {
		biomeTags = new TagManager<>(MinecraftClientAccess.get().getBiomeHandler());
		loader.accept(e, blockTags, "block");
		loader.accept(e, itemTags, "item");
		loader.accept(e, entityTags, "entity");
		loader.accept(e, biomeTags, "biome");
	}

	public AllTagManagers(AllTagManagers parent) {
		biomeTags = new TagManager<>(parent.biomeTags);
		blockTags.setParent(parent.blockTags);
		itemTags.setParent(parent.itemTags);
		entityTags.setParent(parent.entityTags);
	}

	public AllTagManagers() {
		biomeTags = new TagManager<>(MinecraftClientAccess.get().getBiomeHandler());
	}

	public TagManager<BlockState> getBlockTags() {
		return blockTags;
	}

	public TagManager<Stack> getItemTags() {
		return itemTags;
	}

	public TagManager<EntityType> getEntityTags() {
		return entityTags;
	}

	public TagManager<Biome> getBiomeTags() {
		return biomeTags;
	}
}
