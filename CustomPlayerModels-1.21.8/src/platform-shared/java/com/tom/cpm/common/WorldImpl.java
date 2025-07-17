package com.tom.cpm.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpl.util.WeakStorage;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public WeakStorage<Level> level = new WeakStorage<>();
	public BlockPos base;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		Level l = ent.level();
		i.level.set(l);
		i.base = ent.blockPosition();
		a.dayTime = l.dayTime();
		a.skyLight = l.getBrightness(LightLayer.SKY, i.base);
		a.blockLight = l.getBrightness(LightLayer.BLOCK, i.base);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		return level.call(l -> BlockStateHandlerImpl.impl.wrap(l.getBlockState(base.offset(x, y, z))), BlockState.AIR);
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
		return level.call(l -> l.getMaxY(), 0);
	}

	@Override
	public int getMinHeight() {
		return level.call(l -> l.getMinY(), 0);
	}

	@Override
	public WeatherType getWeather() {
		return level.call(l -> l.isThundering() ? WeatherType.THUNDER : l.isRaining() ? WeatherType.RAIN : WeatherType.CLEAR, WeatherType.CLEAR);
	}

	@Override
	public Biome getBiome() {
		return level.call(l -> BiomeHandlerImpl.getImpl(l).wrap(new BiomeInfo(l.getBiome(base), base)), null);
	}

	@Override
	public String getDimension() {
		return level.call(l -> l.dimension().location().toString(), null);
	}

	public static record BiomeInfo(Holder<net.minecraft.world.level.biome.Biome> biome, BlockPos at) {

		public net.minecraft.world.level.biome.Biome value() {
			return biome.value();
		}

	}
}
