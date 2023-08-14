package com.tom.cpm.common;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.EnumSkyBlock;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public net.minecraft.world.World level;
	public int x, y, z;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.world.World l = ent.worldObj;
		i.level = l;
		i.x = (int) ent.posX;
		i.y = (int) ent.posY;
		i.z = (int) ent.posZ;
		a.dayTime = l.getWorldTime();
		a.skyLight = l.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i.x, i.y, i.z);
		a.blockLight = l.getSkyBlockTypeBrightness(EnumSkyBlock.Block, i.x, i.y, i.z);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		Block block = level.getBlock(this.x + x, this.y + y, this.z + z);
		int meta = level.getBlockMetadata(this.x + x, this.y + y, this.z + z);
		return BlockStateHandlerImpl.impl.wrap(new BlockMeta(block, meta));
	}
}
