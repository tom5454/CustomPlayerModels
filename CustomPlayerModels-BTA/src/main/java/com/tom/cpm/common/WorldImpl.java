package com.tom.cpm.common;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.enums.LightLayer;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpl.util.WeakStorage;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public WeakStorage<net.minecraft.core.world.World> level = new WeakStorage<>();
	public int x, y, z;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.core.world.World l = ent.world;
		i.level.set(l);
		i.x = (int) ent.x;
		i.y = (int) ent.y;
		i.z = (int) ent.z;

		a.dayTime = l.getWorldTime();
		a.skyLight = l.getSavedLightValue(LightLayer.Sky, i.x, i.y, i.z);
		a.blockLight = l.getSavedLightValue(LightLayer.Block, i.x, i.y, i.z);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		int block = level.call(l -> l.getBlockId(this.x + x, this.y + y, this.z + z), 0);
		int meta = level.call(l -> l.getBlockMetadata(this.x + x, this.y + y, this.z + z), 0);
		return BlockStateHandlerImpl.impl.wrap(new BlockMeta(block, meta));
	}

	@Override
	public boolean isCovered() {
		return level.call(l -> !l.canBlockSeeTheSky(x, y, z), false);
	}

	@Override
	public int getYHeight() {
		return y;
	}

	@Override
	public int getMaxHeight() {
		return level.call(l -> l.getHeightBlocks(), 0);
	}

	@Override
	public int getMinHeight() {
		return 0;
	}

	@Override
	public WeatherType getWeather() {
		//return level.call(l -> l.getCurrentWeather() ? WeatherType.THUNDER : l.method_270() ? WeatherType.RAIN : WeatherType.CLEAR, WeatherType.CLEAR);
		return WeatherType.CLEAR;
	}

	@Override
	public Biome getBiome() {
		return level.call(l -> BiomeHandlerImpl.impl.wrap(l.getBlockBiome(x, y, z)), null);
	}

	@Override
	public String getDimension() {
		return level.call(l -> "DIM-" + l.dimension.id, null);
	}
}
