package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.item.Stack;

public class BlockStateHandlerImpl extends BlockStateHandler<BlockState> {
	public static final BlockStateHandlerImpl impl = new BlockStateHandlerImpl();

	@Override
	public String getBlockId(BlockState stack) {
		return Registry.BLOCK.getKey(stack.getBlock()).toString();
	}

	@Override
	public List<String> getBlockStates(BlockState stack) {
		return stack.getProperties().stream().map(p -> p.getName()).toList();
	}

	private Stream<Map.Entry<Property<?>, Comparable<?>>> getProp(BlockState state, String property) {
		return state.getValues().entrySet().stream().filter(e -> e.getKey().getName().equals(property));
	}

	@Override
	public String getPropertyValue(BlockState state, String property) {
		return getProp(state, property).map(this::propValue).findFirst().orElse(null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String propValue(Map.Entry<Property<?>, Comparable<?>> e) {
		Property p = e.getKey();
		return p.getName(e.getValue());
	}

	@Override
	public int getPropertyValueInt(BlockState state, String property) {
		return getProp(state, property).mapToInt(e -> state.getValue(e.getKey()) instanceof Number n ? n.intValue() : -1).findFirst().orElse(-1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllValuesFor(BlockState state, String property) {
		return getProp(state, property).flatMap(e -> e.getKey().getPossibleValues().stream().map(v -> {
			Property p = e.getKey();
			return p.getName(v);
		})).toList();
	}

	@Override
	public List<String> listTags(BlockState stack) {
		return BlockTags.getAllTags().getMatchingTags(stack.getBlock()).stream().map(e -> "#" + e).collect(Collectors.toList());
	}

	@Override
	public List<com.tom.cpl.block.BlockState> listNativeEntries(String tag) {
		List<com.tom.cpl.block.BlockState> stacks = new ArrayList<>();
		if (tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				Tag<Block> i = BlockTags.getAllTags().getTag(rl);
				i.getValues().stream().map(b -> wrap(b.defaultBlockState())).forEach(stacks::add);
			}
		} else {
			ResourceLocation rl = ResourceLocation.tryParse(tag);
			Block item = Registry.BLOCK.get(rl);
			if (item != null) {
				stacks.add(wrap(item.defaultBlockState()));
			}
		}
		return stacks;
	}

	@Override
	public List<String> listNativeTags() {
		return BlockTags.getAllTags().getAvailableTags().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}

	@Override
	public List<com.tom.cpl.block.BlockState> getAllElements() {
		return Registry.BLOCK.stream().map(b -> wrap(b.defaultBlockState())).toList();
	}

	@Override
	public boolean isInTag(String tag, BlockState stack) {
		if (tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				Tag<Block> i = BlockTags.getAllTags().getTag(rl);
				return stack.is(i);
			}
		} else {
			return getBlockId(stack).equals(tag);
		}
		return false;
	}

	@Override
	public boolean equals(BlockState a, BlockState b) {
		return a.getBlock() == b.getBlock();
	}

	@Override
	public Stack getStackFromState(BlockState state) {
		return ItemStackHandlerImpl.impl.wrap(new ItemStack(state.getBlock()));
	}

	@Override
	public boolean equalsFull(BlockState a, BlockState b) {
		return a.equals(b);
	}

	@Override
	public com.tom.cpl.block.BlockState emptyObject() {
		return wrap(Blocks.AIR.defaultBlockState());
	}
}
