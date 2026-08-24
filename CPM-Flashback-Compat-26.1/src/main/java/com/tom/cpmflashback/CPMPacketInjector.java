package com.tom.cpmflashback;

import java.util.Collection;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.moulberry.flashback.Flashback;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.network.packet.ReceiveEventS2C;
import com.tom.cpm.shared.network.packet.SetSkinS2C;

@SuppressWarnings("unchecked")
public class CPMPacketInjector {
	public static NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, net.minecraft.world.entity.player.Player, FlashbackNet> netHandler;

	static {
		netHandler = new NetHandler<>((k, v) -> new CustomPacketPayload.Type<>(Identifier.tryBuild(k, v)));
		netHandler.setExecutor(() -> Minecraft.getInstance());
		netHandler.setSendPacketServer(Function.identity(), (c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(new ByteArrayPayload(rl, pb))), ent -> {
			return (Collection<net.minecraft.world.entity.player.Player>) ent.level().players();
		}, Function.identity());
		netHandler.setPlayerToLoader(net.minecraft.world.entity.player.Player::getGameProfile);
	}

	public static void injectStartPackets() {
		FlashbackNet recorder = new FlashbackNet();
		var net = MinecraftClientAccess.get().getNetHandler();
		if (net.hasModClient()) {
			Minecraft.getInstance().level.players().forEach(p -> {
				byte[] data = MinecraftClientAccess.get().getDefinitionLoader().getModel(p.getGameProfile());
				if (data != null) {
					NBTTagCompound d = new NBTTagCompound();
					d.setByteArray(NetworkUtil.DATA_TAG, data);
					netHandler.sendPacketTo(recorder, new SetSkinS2C(p.getId(), d));
				}
				Player<?> loaded = MinecraftClientAccess.get().getDefinitionLoader().getLoadedPlayer(p.getGameProfile());
				if (loaded != null) {
					if (loaded.persistentState.gestureData != null) {
						NBTTagCompound evt = new NBTTagCompound();
						evt.setByteArray(NetworkUtil.GESTURE, loaded.persistentState.gestureData);
						netHandler.sendPacketTo(recorder, new ReceiveEventS2C(p.getId(), evt));
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
