package com.tom.cpl.block;

import java.util.List;

import com.tom.cpl.item.Stack;
import com.tom.cpl.tag.NativeTagManager;

public abstract class BlockStateHandler<S> implements NativeTagManager<BlockState> {

	public BlockState wrap(S stack) {
		return new BlockState(stack);
	}

	@SuppressWarnings("unchecked")
	public S unwrap(BlockState state) {
		return (S) state.getState();
	}

	public abstract String getBlockId(S state);
	public abstract List<String> getBlockStates(S state);
	public abstract String getPropertyValue(S state, String property);
	public abstract int getPropertyValueInt(S state, String property);
	public abstract List<String> getAllValuesFor(S state, String property);

	@Override
	public abstract List<String> listNativeTags();
	public abstract boolean equals(S a, S b);
	public abstract boolean equalsFull(S a, S b);
	public abstract Stack getStackFromState(S state);
	public abstract boolean isInTag(String tag, S state);
	public abstract List<String> listTags(S state);

	@Override
	public String getId(BlockState type) {
		return type.getBlockId();
	}

	@Override
	public List<String> listNativeTags(BlockState state) {
		return listTags(unwrap(state));
	}

	@Override
	public boolean isInNativeTag(String tag, BlockState state) {
		return isInTag(tag, unwrap(state));
	}
}
