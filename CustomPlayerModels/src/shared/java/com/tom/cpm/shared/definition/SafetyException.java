package com.tom.cpm.shared.definition;

import java.io.IOException;

public class SafetyException extends IOException {
	private BlockReason blockReason;

	public SafetyException(BlockReason blockReason) {
		this.blockReason = blockReason;
	}

	private static final long serialVersionUID = 8223470990044738695L;

	public static enum BlockReason {
		BLOCK_LIST,
		LINK_OVERFLOW,
		CONFIG_DISABLED,
		TOO_MANY_CUBES,
		TEXTURE_OVERFLOW,
	}

	public BlockReason getBlockReason() {
		return blockReason;
	}
}
