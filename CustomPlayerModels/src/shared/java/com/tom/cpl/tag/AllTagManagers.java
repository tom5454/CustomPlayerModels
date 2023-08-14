package com.tom.cpl.tag;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.item.Stack;
import com.tom.cpl.util.TriConsumer;

public class AllTagManagers {
	private final TagManager<BlockState> blockTags = new TagManager<>(BlockState.handler);
	private final TagManager<Stack> itemTags = new TagManager<>(Stack.handler);

	public <E> AllTagManagers(E e, TriConsumer<E, TagManager<?>, String> loader) {
		loader.accept(e, blockTags, "block");
		loader.accept(e, itemTags, "item");
	}

	public AllTagManagers(AllTagManagers parent) {
		blockTags.setParent(parent.blockTags);
		itemTags.setParent(parent.itemTags);
	}

	public AllTagManagers() {
	}

	public TagManager<BlockState> getBlockTags() {
		return blockTags;
	}

	public TagManager<Stack> getItemTags() {
		return itemTags;
	}
}
