package com.tom.cpm.shared.retro;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;
import com.tom.cpm.shared.skin.TextureProvider;

public class RedirectHolderRetro<M, P> extends RedirectHolder<M, Void, Void, P> {
	private TextureProvider skin;

	public RedirectHolderRetro(ModelRenderManager<Void, Void, P, M> mngr, M model) {
		super(mngr, model);
	}

	@Override
	protected void bindSkin() {
		MinecraftClientAccess.get().getRenderBuilder().build(renderTypes, skin);
	}

	@Override
	protected void setupRenderSystem(Void cbi, TextureSheetType tex) {
	}

	@Override
	protected void bindTexture(Void cbi, TextureProvider skin) {
		this.skin = skin;
		skinBound = false;
	}

	@Override
	public void swapOut0() {
		skin = null;
	}

	@Override public void swapIn0() {}
}
