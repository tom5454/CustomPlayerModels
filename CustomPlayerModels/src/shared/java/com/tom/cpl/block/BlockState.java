package com.tom.cpl.block;

import java.util.List;

import com.tom.cpl.item.Stack;
import com.tom.cpm.shared.MinecraftCommonAccess;

public class BlockState {
	@SuppressWarnings("unchecked")
	public static final BlockStateHandler<Object> handler = (BlockStateHandler<Object>) MinecraftCommonAccess.get().getBlockStateHandler();
	public static final BlockState AIR = handler.emptyObject();
	private Object state;

	protected BlockState(Object state) {
		this.state = state;
	}

	protected Object getState() {
		return state;
	}

	public String getBlockId() {
		return handler.getBlockId(state);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BlockState other = (BlockState) obj;
		return handler.equals(state, other.state);
	}

	public List<String> getBlockStates() {
		return handler.getBlockStates(state);
	}

	public String getPropertyValue(String property) {
		return handler.getPropertyValue(state, property);
	}

	public int getPropertyValueInt(String property) {
		return handler.getPropertyValueInt(state, property);
	}

	public List<String> getAllValuesFor(String property) {
		return handler.getAllValuesFor(state, property);
	}

	public boolean equalsFull(BlockState other) {
		return handler.equalsFull(state, other.state);
	}

	public Stack getStackFromState() {
		return handler.getStackFromState(state);
	}

	public boolean isInTag(String tag) {
		return handler.isInTag(tag, state);
	}

	public List<String> listTags() {
		return handler.listTags(state);
	}
}
