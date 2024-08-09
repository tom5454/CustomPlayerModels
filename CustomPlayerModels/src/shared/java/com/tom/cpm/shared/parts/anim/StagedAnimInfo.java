package com.tom.cpm.shared.parts.anim;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class StagedAnimInfo {

	public static void parse(IOHelper block, AnimLoaderState state) throws IOException {
		state.getStagedList().add(block.readVarInt());
	}
}
