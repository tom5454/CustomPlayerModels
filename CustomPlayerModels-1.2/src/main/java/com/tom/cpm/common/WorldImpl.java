package com.tom.cpm.common;


import net.minecraft.src.Chunk;
import net.minecraft.src.Entity;
import net.minecraft.src.EnumSkyBlock;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.World;
import com.tom.cpl.util.WeakStorage;
import com.tom.cpm.shared.animation.AnimationState;

public class WorldImpl implements World {
	public WeakStorage<net.minecraft.src.World> level = new WeakStorage<>();
	public int x, y, z;

	public static void setWorld(AnimationState a, Entity ent) {
		if(!(a.world instanceof WorldImpl))a.world = new WorldImpl();
		WorldImpl i = (WorldImpl) a.world;
		net.minecraft.src.World l = ent.worldObj;
		i.level.set(l);
		i.x = (int) ent.posX;
		i.y = (int) ent.posY;
		i.z = (int) ent.posZ;
		a.dayTime = l.getWorldTime();
		a.skyLight = l.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i.x, i.y, i.z);
		a.blockLight = l.getSkyBlockTypeBrightness(EnumSkyBlock.Block, i.x, i.y, i.z);
	}

	@Override
	public BlockState getBlock(int x, int y, int z) {
		int block = level.call(l -> getBlockID(l, this.x + x, this.y + y, this.z + z), 0);
		int meta = level.call(l -> l.getBlockMetadata(this.x + x, this.y + y, this.z + z), 0);
		return BlockStateHandlerImpl.impl.wrap(new BlockMeta(block, meta));
	}

	private static int getBlockID(net.minecraft.src.World l, int par1, int par2, int par3) {
		if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 < 30000000) {
			if (par2 < 0) {
				return 0;
			} else if (par2 >= 256) {
				return 0;
			} else {
				Chunk var4 = l.getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
				par1 &= 15;
				par3 &= 15;
				return var4.getBlockID(par1, par2, par3);
			}
		} else {
			return 0;
		}
	}

	@Override
	public boolean isCovered() {
		return level.call(l -> l.canBlockSeeTheSky(x, y, z), false);
	}

	@Override
	public int getYHeight() {
		return y;
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
		return level.call(l -> BiomeHandlerImpl.impl.wrap(l.getBiomeGenForCoords(x, z)), null);
	}

	@Override
	public String getDimension() {
		return level.call(l -> {
			String s = l.worldProvider.getSaveFolder();
			if (s == null)return "DIM-0";
			return s;
		}, null);
	}
}
