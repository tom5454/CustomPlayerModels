package com.tom.cpm.common;

import net.minecraft.block.Block;

public class BlockMeta {
	private final Block block;
	private final int meta;

	public BlockMeta(Block block, int meta) {
		this.block = block;
		this.meta = meta;
	}

	public Block getBlock() {
		return block;
	}

	public int getMeta() {
		return meta;
	}
}
