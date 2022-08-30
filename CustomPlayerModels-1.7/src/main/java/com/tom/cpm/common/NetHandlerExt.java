package com.tom.cpm.common;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.PlayerDataExt;
import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;
import com.tom.cpm.shared.network.IS2CPacket;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

public class NetHandlerExt<P, N> extends NetHandler<ResourceLocation, P, N> {
	public static final String SKIN_LAYERS = "layer";

	public NetHandlerExt() {
		super(ResourceLocation::new);

		register(packetC2S, SKIN_LAYERS, LayerC2S.class, LayerC2S::new);
		register(packetS2C, SKIN_LAYERS, LayerS2C.class, LayerS2C::new);
	}

	@Override
	protected PlayerData newData() {
		return new PlayerDataExt();
	}

	public static class LayerC2S implements IC2SPacket {
		private int layer;

		public LayerC2S(int layer) {
			this.layer = layer;
		}

		public LayerC2S() {
		}

		@Override
		public void read(IOHelper pb) throws IOException {
			layer = pb.readByte();
		}

		@Override
		public void write(IOHelper pb) throws IOException {
			pb.writeByte(layer);
		}

		@Override
		public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH from, P pl) {
			EntityPlayerMP player = (EntityPlayerMP) pl;
			PlayerDataExt.setSkinLayer(player, layer);
			handler.sendPacketToTracking(pl, new LayerS2C(player.getEntityId(), layer));
		}
	}

	public static class LayerS2C implements IS2CPacket {
		private int entId, layer;

		public LayerS2C() {
		}

		public LayerS2C(int entId, int layer) {
			this.entId = entId;
			this.layer = layer;
		}

		@Override
		public void read(IOHelper pb) throws IOException {
			entId = pb.readVarInt();
			layer = pb.readByte();
		}

		@Override
		public void write(IOHelper pb) throws IOException {
			pb.writeVarInt(entId);
			pb.writeByte(layer);
		}

		@Override
		public void handle(NetHandler<?, ?, ?> handler, NetH from) {
			handle0(handler, from);
		}

		private <P> void handle0(NetHandler<?, P, ?> handler, NetH from) {
			P ent = handler.getPlayerById(entId);
			PlayerProfile profile = (PlayerProfile) ClientProxy.mc.getDefinitionLoader().loadPlayer((GameProfile) handler.getLoaderId(ent), ModelDefinitionLoader.PLAYER_UNIQUE);
			profile.encGesture = layer;
		}
	}

	public void sendLayer(int value) {
		if(hasModClient()) {
			sendPacketToServer(new LayerC2S(value));
		}
	}
}
