package com.tom.cpm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.packet.PluginMessageS2C;

public class CommonApi extends SharedApi implements ICommonAPI {
	private Map<String, BiConsumer<Object, NBTTagCompound>> pluginMessageHandlers = new HashMap<>();

	@Override
	protected void callInit0(ICPMPlugin plugin) {
		plugin.initCommon(this);
	}

	protected CommonApi() {
	}

	public static class ApiBuilder {
		private final CPMApiManager api;

		protected ApiBuilder(CPMApiManager api) {
			this.api = api;
			api.common = new CommonApi();
		}

		public <P> ApiBuilder player(Class<P> player) {
			api.common.classes.put(Clazz.PLAYER, player);
			return this;
		}

		public void init() {
			api.initCommon();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void setPlayerModel(Class<P> playerClass, P player, String b64, boolean forced, boolean persistent) {
		if(checkClass(playerClass, Clazz.PLAYER))return;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		h.setSkin(player, b64, forced, persistent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void setPlayerModel(Class<P> playerClass, P player, ModelFile model, boolean forced) {
		if(checkClass(playerClass, Clazz.PLAYER))return;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		h.setSkin(player, model.getDataBlock(), forced);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void resetPlayerModel(Class<P> playerClass, P player) {
		if(checkClass(playerClass, Clazz.PLAYER))return;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		h.setSkin(player, null, false, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void playerJumped(Class<P> playerClass, P player) {
		if(checkClass(playerClass, Clazz.PLAYER))return;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		h.onJump(player);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> void playAnimation(Class<P> playerClass, P player, String name, int value) {
		if(checkClass(playerClass, Clazz.PLAYER))return;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		h.playAnimation(player, name, MathHelper.clamp(value, -1, 255));
	}

	@Override
	public <P> void playAnimation(Class<P> playerClass, P player, String name) {
		playAnimation(playerClass, player, name, -1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> MessageSender<P> registerPluginMessage(Class<P> clazz, String messageId,
			BiConsumer<P, NBTTagCompound> handler) {
		if(checkClass(clazz, Clazz.PLAYER))return null;
		if(initingPlugin == null)return null;
		String fullID = initingPlugin.getOwnerModId() + ":" + messageId;
		pluginMessageHandlers.put(fullID, (a, b) -> handler.accept((P) a, b));
		return new SenderImpl<>(fullID);
	}

	private class SenderImpl<P> implements MessageSender<P> {
		private final String id;

		public SenderImpl(String id) {
			this.id = id;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean sendMessageTo(P player, NBTTagCompound tag) {
			NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
			ServerNetH net = h.getSNetH(player);
			if(net.cpm$hasMod()) {
				h.sendPacketTo(net, new PluginMessageS2C(id, h.getPlayerId(player), tag));
				return true;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void sendMessageToTracking(P player, NBTTagCompound tag, boolean sendToSelf) {
			NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
			PluginMessageS2C m = new PluginMessageS2C(id, h.getPlayerId(player), tag);
			h.sendPacketToTracking(player, m);
			if(sendToSelf)h.sendPacketTo(h.getSNetH(player), m);
		}
	}

	public void handlePacket(String id, NBTTagCompound tag, Object player) {
		BiConsumer<Object, NBTTagCompound> h = pluginMessageHandlers.get(id);
		h.accept(player, tag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> int getAnimationPlaying(Class<P> playerClass, P player, String name) {
		if(checkClass(playerClass, Clazz.PLAYER))return -1;
		NetHandler<?, P, ?> h = (NetHandler<?, P, ?>) MinecraftServerAccess.get().getNetHandler();
		return h.getAnimationPlaying(player, name);
	}
}
