package com.tom.cpmflashback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.moulberry.flashback.Flashback;

import com.google.common.cache.LoadingCache;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.network.packet.ReceiveEventS2C;
import com.tom.cpm.shared.network.packet.SetSkinS2C;

@SuppressWarnings("unchecked")
public class CPMPacketInjector {
	public static NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, net.minecraft.world.entity.player.Player, FlashbackNet> netHandler;
	private static FlashbackNet flashbackRecorder = new FlashbackNet();
	private static Function<GameProfile, Object> keyFactory;
	private static Supplier<Map<Object, byte[]>> serverModelsGetter;
	private static Supplier<LoadingCache<Object, Player<?>>> cacheGetter;

	static {
		netHandler = new NetHandler<>((k, v) -> new CustomPacketPayload.Type<>(ResourceLocation.tryBuild(k, v)));
		netHandler.setExecutor(() -> Minecraft.getInstance());
		netHandler.setSendPacketServer(Function.identity(), (c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(new ByteArrayPayload(rl, pb))), ent -> {
			return (Collection<net.minecraft.world.entity.player.Player>) ent.level().players();
		}, Function.identity());
		netHandler.setPlayerToLoader(net.minecraft.world.entity.player.Player::getGameProfile);

		try {
			Constructor<?> keyConstructor = Class.forName("com.tom.cpm.shared.definition.ModelDefinitionLoader$Key").getDeclaredConstructor(ModelDefinitionLoader.class, Object.class, String.class);
			keyConstructor.setAccessible(true);
			keyFactory = g -> {
				try {
					return keyConstructor.newInstance(MinecraftClientAccess.get().getDefinitionLoader(), g, ModelDefinitionLoader.PLAYER_UNIQUE);
				} catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			};

			Field cache = ModelDefinitionLoader.class.getDeclaredField("cache");
			Field serverModels = ModelDefinitionLoader.class.getDeclaredField("serverModels");
			cache.setAccessible(true);
			serverModels.setAccessible(true);
			serverModelsGetter = () -> {
				try {
					return (Map<Object, byte[]>) serverModels.get(MinecraftClientAccess.get().getDefinitionLoader());
				} catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			};
			cacheGetter = () -> {
				try {
					return (LoadingCache<Object, Player<?>>) cache.get(MinecraftClientAccess.get().getDefinitionLoader());
				} catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			};
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void injectStartPackets() {
		var net = MinecraftClientAccess.get().getNetHandler();
		if (net.hasModClient()) {
			Minecraft.getInstance().level.players().forEach(p -> {
				var key = keyFactory.apply(p.getGameProfile());
				byte[] data = serverModelsGetter.get().get(key);
				if (data != null) {
					NBTTagCompound d = new NBTTagCompound();
					d.setByteArray(NetworkUtil.DATA_TAG, data);
					netHandler.sendPacketTo(flashbackRecorder, new SetSkinS2C(p.getId(), d));
				}
				Player<?> loaded = cacheGetter.get().getIfPresent(key);
				if (loaded != null) {
					if (loaded.animState.gestureData != null) {
						NBTTagCompound evt = new NBTTagCompound();
						evt.setByteArray(NetworkUtil.GESTURE, loaded.animState.gestureData);
						netHandler.sendPacketTo(flashbackRecorder, new ReceiveEventS2C(p.getId(), evt));
					}
				}
			});
		}
	}

	private static class FlashbackNet implements ServerNetH {

		@Override
		public boolean cpm$hasMod() {
			return true;
		}

		public void send(ClientboundCustomPayloadPacket pck) {
			Flashback.RECORDER.writePacketAsync(pck, ConnectionProtocol.PLAY);
		}

		@Override
		public void cpm$setHasMod(boolean v) {
		}

		@Override
		public PlayerData cpm$getEncodedModelData() {
			return null;
		}

		@Override
		public void cpm$setEncodedModelData(PlayerData data) {
		}
	}
}
