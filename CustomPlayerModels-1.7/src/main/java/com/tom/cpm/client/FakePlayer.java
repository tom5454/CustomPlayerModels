package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.chunk.IChunkProvider;

import com.mojang.authlib.GameProfile;

public class FakePlayer extends EntityOtherPlayerMP {

	public FakePlayer(GameProfile gameProfileIn) {
		super(new FakeWorld(), gameProfileIn);
	}

	public FakePlayer() {
		this(Minecraft.getMinecraft().getSession().func_148256_e());
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
			super(null, "", new WorldProvider() {

				@Override
				public String getDimensionName() {
					return "";
				}

			}, new WorldSettings(0, GameType.CREATIVE, false, false, null), null);
			provider.worldObj = this;
		}

		@Override
		protected IChunkProvider createChunkProvider() {
			return null;
		}

		@Override
		protected int func_152379_p() {
			return 0;
		}

		@Override
		public Entity getEntityByID(int p_73045_1_) {
			return null;
		}

	}
}
