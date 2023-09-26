package com.tom.cpm.web.client.emul;

import java.util.Collections;
import java.util.List;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.item.Stack;

public class BlockStateHandlerImpl extends BlockStateHandler<Block> {
	public static final BlockStateHandlerImpl impl = new BlockStateHandlerImpl();

	@Override
	public List<BlockState> listNativeEntries(String tag) {
		return Collections.emptyList();
	}

	@Override
	public String getBlockId(Block stack) {
		return stack.id;
	}

	@Override
	public List<String> getBlockStates(Block stack) {
		return Collections.emptyList();
	}

	@Override
	public String getPropertyValue(Block state, String property) {
		return null;
	}

	@Override
	public int getPropertyValueInt(Block state, String property) {
		return -1;
	}

	@Override
	public List<String> getAllValuesFor(Block state, String property) {
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public List<BlockState> getAllElements() {
		return Collections.emptyList();
	}

	@Override
	public boolean equals(Block a, Block b) {
		return a.id.equals(b.id);
	}

	@Override
	public boolean equalsFull(Block a, Block b) {
		return a.equals(b);
	}

	@Override
	public Stack getStackFromState(Block state) {
		return ItemStackHandlerImpl.impl.wrap(new ItemStack(state.item, 1));
	}

	@Override
	public boolean isInTag(String tag, Block state) {
		return false;
	}

	@Override
	public List<String> listTags(Block unwrap) {
		return Collections.emptyList();
	}

	@Override
	public BlockState emptyObject() {
		return null;
	}
}
