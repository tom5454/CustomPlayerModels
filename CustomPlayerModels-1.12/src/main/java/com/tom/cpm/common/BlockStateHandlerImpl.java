package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.item.Stack;

public class BlockStateHandlerImpl extends BlockStateHandler<IBlockState> {
	public static final BlockStateHandlerImpl impl = new BlockStateHandlerImpl();

	@Override
	public String getBlockId(IBlockState stack) {
		return stack.getBlock().delegate.name().toString();
	}

	@Override
	public List<String> getBlockStates(IBlockState stack) {
		return stack.getPropertyKeys().stream().map(p -> p.getName()).collect(Collectors.toList());
	}

	private Stream<Map.Entry<IProperty<?>, Comparable<?>>> getProp(IBlockState state, String property) {
		return state.getProperties().entrySet().stream().filter(e -> e.getKey().getName().equals(property));
	}

	@Override
	public String getPropertyValue(IBlockState state, String property) {
		return getProp(state, property).map(this::propValue).findFirst().orElse(null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String propValue(Map.Entry<IProperty<?>, Comparable<?>> e) {
		IProperty p = e.getKey();
		return p.getName(e.getValue());
	}

	@Override
	public int getPropertyValueInt(IBlockState state, String property) {
		return getProp(state, property).mapToInt(e -> {
			Object o = state.getValue(e.getKey());
			return o instanceof Number ? ((Number) o).intValue() : -1;
		}).findFirst().orElse(-1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllValuesFor(IBlockState state, String property) {
		return getProp(state, property).flatMap(e -> e.getKey().getAllowedValues().stream().map(v -> {
			IProperty p = e.getKey();
			return p.getName(v);
		})).collect(Collectors.toList());
	}

	@Override
	public List<String> listTags(IBlockState stack) {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.BlockState> listNativeEntries(String tag) {
		if (tag.charAt(0) == '#') {
			return Collections.emptyList();
		} else {
			List<com.tom.cpl.block.BlockState> stacks = new ArrayList<>();
			ResourceLocation rl = ItemStackHandlerImpl.tryParse(tag);
			Block item = ForgeRegistries.BLOCKS.getValue(rl);
			if (item != null) {
				stacks.add(wrap(item.getDefaultState()));
			}
			return stacks;
		}
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.BlockState> getAllBlocks() {
		return ForgeRegistries.BLOCKS.getValuesCollection().stream().map(b -> wrap(b.getDefaultState())).collect(Collectors.toList());
	}

	@Override
	public boolean isInTag(String tag, IBlockState stack) {
		if (tag.charAt(0) == '#') {
			return false;
		} else {
			return getBlockId(stack).equals(tag);
		}
	}

	@Override
	public boolean equals(IBlockState a, IBlockState b) {
		return a.getBlock() == b.getBlock();
	}

	@Override
	public Stack getStackFromState(IBlockState state) {
		return ItemStackHandlerImpl.impl.wrap(new ItemStack(state.getBlock()));
	}

	@Override
	public boolean equalsFull(IBlockState a, IBlockState b) {
		return a.equals(b);
	}

	@Override
	public BlockState emptyObject() {
		return wrap(Blocks.AIR.getDefaultState());
	}
}
