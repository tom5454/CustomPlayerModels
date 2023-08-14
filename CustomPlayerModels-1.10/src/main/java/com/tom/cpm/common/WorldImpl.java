package com.tom.cpm.common;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public net.minecraft.world.World level;
	public BlockPos base;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.world.World l = ent.worldObj;
		i.level = l;
		i.base = ent.getPosition();
		a.dayTime = l.getWorldTime();
		a.skyLight = l.getLightFor(EnumSkyBlock.SKY, i.base);
		a.blockLight = l.getLightFor(EnumSkyBlock.BLOCK, i.base);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		return BlockStateHandlerImpl.impl.wrap(level.getBlockState(base.add(x, y, z)));
	}
}
