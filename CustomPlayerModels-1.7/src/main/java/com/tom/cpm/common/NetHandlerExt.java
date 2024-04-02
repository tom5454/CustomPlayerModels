package com.tom.cpm.common;

import java.io.IOException;
import java.util.function.BiFunction;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;
import com.tom.cpm.shared.network.IS2CPacket;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

public class NetHandlerExt<RL, P, N> extends NetHandler<RL, P, N> {
	public static final String SKIN_LAYERS = "layer";

	public NetHandlerExt(BiFunction<String, String, RL> keyFactory) {
		super(keyFactory);

		register(packetC2S, SKIN_LAYERS, LayerC2S.class, LayerC2S::new);
		register(packetS2C, SKIN_LAYERS, LayerS2C.class, LayerS2C::new);
	}

	@Override
	public void sendPlayerData(P target, P to) {
		super.sendPlayerData(target, to);

		ServerNetH netTo = getSNetH(to);
		PlayerDataExt dt = (PlayerDataExt) getSNetH(target).cpm$getEncodedModelData();
		if(dt == null)return;
		sendPacketTo(netTo, new LayerS2C(getPlayerId(target), dt.getSkinLayer()));
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
			PlayerDataExt dt = (PlayerDataExt) handler.getSNetH(pl).cpm$getEncodedModelData();
			dt.setSkinLayer(layer);
			handler.sendPacketToTracking(pl, new LayerS2C(handler.getPlayerId(pl), layer));
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
			IPlayerProfile profile = (IPlayerProfile) MinecraftClientAccess.get().getDefinitionLoader().loadPlayer(handler.getLoaderId(ent), ModelDefinitionLoader.PLAYER_UNIQUE);
			profile.setEncGesture(layer);
		}
	}

	public void sendLayer(int value) {
		if(hasModClient()) {
			sendPacketToServer(new LayerC2S(value));
		}
	}

	public static class PlayerDataExt extends PlayerData {
		private int skinLayer;

		public PlayerDataExt() {
		}

		public void setSkinLayer(int skinLayer) {
			this.skinLayer = skinLayer;
		}

		public int getSkinLayer() {
			return skinLayer;
		}
	}

	public static interface IPlayerProfile {
		void setEncGesture(int g);
	}
}
