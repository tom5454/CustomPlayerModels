package com.tom.cpm.api;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.network.NetHandler;

public class CommonApi extends SharedApi implements ICommonAPI {

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

		public ApiBuilder player(Class<?> player) {
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
}
