package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.DimensionType;
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
	public int getTicksElytraFlying() {
		return 0;
	}

	@Override
	public EnumHandSide getPrimaryHand() {
		return Minecraft.getMinecraft().gameSettings.mainHand;
	}

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
				public DimensionType getDimensionType() {
					return DimensionType.OVERWORLD;
				}
			}, null, true);
			provider.setWorld(this);
		}

		@Override
		protected IChunkProvider createChunkProvider() {
			return null;
		}

		@Override
		protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
			return false;
		}

	}
}
