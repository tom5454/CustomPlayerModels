package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.item.Stack;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class BlockStateHandlerImpl extends BlockStateHandler<BlockMeta> {
	public static final String META = "metadata";
	public static final List<String> STATES = Collections.singletonList(META), META_VALUES = new ArrayList<>();
	public static final BlockStateHandlerImpl impl = new BlockStateHandlerImpl();

	static {
		for (int i = 0;i<16;i++) {
			META_VALUES.add(String.valueOf(i));
		}
	}

	@Override
	public String getBlockId(BlockMeta stack) {
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getBlock());
		if (id != null)return id.modId + ":" + id.name;
		return "unloc:" + stack.getBlock().getUnlocalizedName();
	}

	@Override
	public List<String> getBlockStates(BlockMeta stack) {
		return STATES;
	}

	@Override
	public String getPropertyValue(BlockMeta state, String property) {
		if (property.equals(META)) {
			return String.valueOf(state.getMeta());
		}
		return null;
	}

	@Override
	public int getPropertyValueInt(BlockMeta state, String property) {
		if (property.equals(META)) {
			return state.getMeta();
		}
		return -1;
	}

	@Override
	public List<String> getAllValuesFor(BlockMeta state, String property) {
		if (property.equals(META))return META_VALUES;
		return Collections.emptyList();
	}

	@Override
	public List<String> listTags(BlockMeta stack) {
		List<String> tags = new ArrayList<>();
		tags.add("#idmeta:" + stack.getBlock() + "/" + stack.getMeta());
		tags.add("#id:" + stack.getBlock());
		return tags;
	}

	@Override
	public List<BlockState> listNativeEntries(String tag) {
		/*if (tag.charAt(0) == '#') {//TODO idmeta
			return Collections.emptyList();
		} else {
			List<BlockState> stacks = new ArrayList<>();
			ResourceLocation rl = ItemStackHandlerImpl.tryParse(tag);
			Block item = (Block) GameData.getBlockRegistry().getObject(rl);
			if (item != null) {
				stacks.add(wrap(new BlockMeta(item, 0)));
			}
			return stacks;
		}*/

		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public List<BlockState> getAllElements() {
		List<BlockState> list = new ArrayList<>();
		for (int i = 0; i < Block.blocksList.length; i++) {
			Block b = Block.blocksList[i];
			if (b == null)continue;
			list.add(wrap(new BlockMeta(i, 0)));
		}
		return list;
	}

	@Override
	public boolean isInTag(String tag, BlockMeta stack) {
		if (tag.charAt(0) == '#') {
			return ItemStackHandlerImpl.checkIdMetaTags(tag, stack.getBlockId(), stack.getMeta());
		} else {
			return getBlockId(stack).equals(tag);
		}
	}

	@Override
	public boolean equals(BlockMeta a, BlockMeta b) {
		return a.getBlock() == b.getBlock();
	}

	@Override
	public Stack getStackFromState(BlockMeta state) {
		return ItemStackHandlerImpl.impl.wrap(new ItemStack(state.getBlock(), 1, 0));
	}

	@Override
	public boolean equalsFull(BlockMeta a, BlockMeta b) {
		return a.equals(b);
	}

	@Override
	public BlockState emptyObject() {
		return wrap(new BlockMeta(0, 0));
	}
}
