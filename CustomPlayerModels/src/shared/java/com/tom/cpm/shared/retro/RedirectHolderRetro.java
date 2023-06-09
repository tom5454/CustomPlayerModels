package com.tom.cpm.shared.retro;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class RedirectHolderRetro<M, P> extends RedirectHolder<M, Void, Void, P> {
	private TextureProvider skin;
	private boolean invis, glow, invisOn;

	public RedirectHolderRetro(ModelRenderManager<Void, Void, P, M> mngr, M model) {
		super(mngr, model);
	}

	@Override
	protected void bindSkin() {
		MinecraftClientAccess.get().getRenderBuilder().build(renderTypes, skin);
		if(invis) {
			super.setInvis(glow);
			invis = false;
			invisOn = true;
		}
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
		invisOn = false;
	}

	@Override public void swapIn0() {}

	@Override
	public void setInvis(boolean glow) {
		invis = true;
		this.glow = glow;
	}

	@Override
	protected boolean enableParentRendering(VanillaModelPart part) {
		if(part instanceof PlayerModelParts && invisOn)return false;
		return true;
	}
}
