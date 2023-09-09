package com.tom.cpm.common;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpl.util.WeakStorage;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public WeakStorage<net.minecraft.world.World> level = new WeakStorage<>();
	public BlockPos base;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.world.World l = ent.world;
		i.level.set(l);
		i.base = ent.getPosition();
		a.dayTime = l.getWorldTime();
		a.skyLight = l.getLightFor(EnumSkyBlock.SKY, i.base);
		a.blockLight = l.getLightFor(EnumSkyBlock.BLOCK, i.base);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		return level.call(l -> BlockStateHandlerImpl.impl.wrap(l.getBlockState(base.add(x, y, z))), BlockState.AIR);
	}

	@Override
	public boolean isCovered() {
		return level.call(l -> l.canSeeSky(base), false);
	}

	@Override
	public int getYHeight() {
		return base.getY();
	}

	@Override
	public int getMaxHeight() {
		return level.call(l -> l.getHeight(), 0);
	}

	@Override
	public int getMinHeight() {
		return 0;
	}

	@Override
	public WeatherType getWeather() {
		return level.call(l -> l.isThundering() ? WeatherType.THUNDER : l.isRaining() ? WeatherType.RAIN : WeatherType.CLEAR, WeatherType.CLEAR);
	}

	@Override
	public Biome getBiome() {
		return level.call(l -> BiomeHandlerImpl.impl.wrap(l.getBiome(base)), null);
	}

	@Override
	public String getDimension() {
		return level.call(l -> l.provider.getDimensionType().getName(), null);
	}
}
