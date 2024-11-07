package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.world.level.block.SkullBlock;

public class RefHolder {
	public static Function<SkullBlock.Type, SkullModelBase> CPM_MODELS;
}
