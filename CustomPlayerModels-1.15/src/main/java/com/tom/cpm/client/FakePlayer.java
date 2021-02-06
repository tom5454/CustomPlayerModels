package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;

import com.mojang.authlib.GameProfile;

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
	public boolean isCrouching() {
		return sneaking;
	}

	@Override
	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}

	private static class FakeWorld extends ClientWorld {
		private static final FakeWorld WORLD = new FakeWorld();

		/*static {
			try {
				Field u = Unsafe.class.getDeclaredField("theUnsafe");
				u.setAccessible(true);
				Unsafe unsafe = (Unsafe) u.get(null);
				WORLD = (FakeWorld) unsafe.allocateInstance(FakeWorld.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}*/

		public FakeWorld() {
			super(null, new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.DEBUG_ALL_BLOCK_STATES), DimensionType.OVERWORLD, 0, null, null);
		}

		@Override
		public BlockPos getSpawnPoint() {
			return BlockPos.ZERO;
		}
	}
}
