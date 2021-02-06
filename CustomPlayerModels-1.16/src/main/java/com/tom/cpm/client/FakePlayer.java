package com.tom.cpm.client;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;

import com.mojang.authlib.GameProfile;

import sun.misc.Unsafe;

public class FakePlayer extends RemoteClientPlayerEntity {

	public FakePlayer(GameProfile gameProfileIn) {
		super(FakeWorld.WORLD, gameProfileIn);
	}

	public FakePlayer() {
		this(Minecraft.getInstance().getSession().getProfile());
	}

	private boolean sneaking;
	@Override
	public int getTicksElytraFlying() {
		return 0;
	}

	@Override
	public HandSide getPrimaryHand() {
		return Minecraft.getInstance().gameSettings.mainHand;
	}

	@Override
	public boolean isSneaking() {
		return sneaking;
	}

	@Override
	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}

	private static class FakeWorld extends ClientWorld {
		private static final FakeWorld WORLD;

		static {
			try {
				Field u = Unsafe.class.getDeclaredField("theUnsafe");
				u.setAccessible(true);
				Unsafe unsafe = (Unsafe) u.get(null);
				WORLD = (FakeWorld) unsafe.allocateInstance(FakeWorld.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public FakeWorld() {
			super(null, null, null, null, 0, null, null, false, 0);
		}

		@Override
		public BlockPos func_239140_u_() {
			return BlockPos.ZERO;
		}

		@Override
		public float func_243489_v() {
			return 0;
		}
	}
}
