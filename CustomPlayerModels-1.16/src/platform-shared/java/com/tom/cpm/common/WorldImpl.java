package com.tom.cpm.common;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public net.minecraft.world.World level;
	public BlockPos base;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.world.World l = ent.level;
		i.level = l;
		i.base = ent.blockPosition();
		a.dayTime = l.dayTime();
		a.skyLight = l.getBrightness(LightType.SKY, i.base);
		a.blockLight = l.getBrightness(LightType.BLOCK, i.base);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		return BlockStateHandlerImpl.impl.wrap(level.getBlockState(base.offset(x, y, z)));
	}
}
