package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

import com.mojang.authlib.GameProfile;

public class FakePlayer extends EntityOtherPlayerMP {

	public FakePlayer(GameProfile gameProfileIn) {
		super(new FakeWorld(), gameProfileIn);
	}

	public FakePlayer() {
		this(Minecraft.getMinecraft().getSession().getProfile());
	}

	private boolean sneaking;

	@Override
	public boolean isSneaking() {
		return sneaking;
	}

	@Override
	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}

	private static class FakeWorld extends World {

		public FakeWorld() {
			super(null, new WorldInfo(new NBTTagCompound()), new WorldProvider() {

				@Override
				public String getDimensionName() {
					return "";
				}

				@Override
				public String getInternalNameSuffix() {
					return "";
				}

			}, null, true);
			provider.registerWorld(this);
		}

		@Override
		protected IChunkProvider createChunkProvider() {
			return null;
		}

		@Override
		protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
			return false;
		}

		@Override
		protected int getRenderDistanceChunks() {
			return 0;
		}

	}
}
