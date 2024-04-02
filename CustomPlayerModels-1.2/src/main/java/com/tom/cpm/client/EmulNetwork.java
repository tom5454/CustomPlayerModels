package com.tom.cpm.client;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityPlayer;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.FastByteArrayInputStream;

import cpw.mods.fml.client.FMLClientHandler;

public class EmulNetwork {
	public static final ClientNetworkImpl emulClient = new EmulClient();
	public static final ServerNetworkImpl emulServer = new EmulServer();

	public static ClientNetworkImpl getClient(EntityPlayer pl) {
		if (pl instanceof EntityClientPlayerMP) {
			return (ClientNetworkImpl) ((EntityClientPlayerMP) pl).sendQueue;
		}
		return EmulNetwork.emulClient;
	}

	public static void reset() {
		emulServer.cpm$setEncodedModelData(null);
		emulServer.cpm$setHasMod(false);
		emulClient.cpm$setHasMod(false);
	}

	private static class EmulClient extends EmulNetwork implements ClientNetworkImpl {
		private boolean hasMod;

		@Override
		public boolean cpm$hasMod() {
			return hasMod;
		}

		@Override
		public void cpm$setHasMod(boolean v) {
			hasMod = v;
		}

		@Override
		public void cpm$sendPacket(String id, byte[] data) {
			ServerHandler.netHandler.receiveServer(id, new FastByteArrayInputStream(data), emulServer);
		}

		@Override
		public Entity cpm$getEntityByID(int id) {
			return FMLClientHandler.instance().getClient().thePlayer;
		}
	}

	private static class EmulServer extends EmulNetwork implements ServerNetworkImpl {
		private boolean hasMod;
		private PlayerData pd;

		@Override
		public boolean cpm$hasMod() {
			return hasMod;
		}

		@Override
		public void cpm$setHasMod(boolean v) {
			hasMod = v;
		}

		@Override
		public void cpm$sendPacket(String id, byte[] data) {
			ClientProxy.INSTANCE.netHandler.receiveClient(id, new FastByteArrayInputStream(data), emulClient);
		}

		@Override
		public PlayerData cpm$getEncodedModelData() {
			return pd;
		}

		@Override
		public void cpm$setEncodedModelData(PlayerData data) {
			this.pd = data;
		}

		@Override
		public void cpm$sendChat(String msg) {
			FMLClientHandler.instance().getClient().ingameGUI.addChatMessage(msg);
		}

		@Override
		public EntityPlayer cpm$getPlayer() {
			return FMLClientHandler.instance().getClient().thePlayer;
		}

		@Override
		public void cpm$kickPlayer(String msg) {
		}
	}
}
