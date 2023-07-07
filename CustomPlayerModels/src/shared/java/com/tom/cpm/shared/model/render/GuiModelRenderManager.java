package com.tom.cpm.shared.model.render;

import java.util.Locale;

import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.VanillaPartRenderer;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.skin.TextureProvider;

public class GuiModelRenderManager extends DirectModelRenderManager<ViewportPanelBase3d> {
	public static final String PLAYER = "player";

	public static class GuiHolderPlayer extends DirectHolderPlayer<ViewportPanelBase3d> {

		public GuiHolderPlayer(
				ModelRenderManager<VBuffers, ViewportPanelBase3d, VanillaPartRenderer, VanillaPlayerModel> mngr,
				VanillaPlayerModel model, String arg) {
			super(mngr, model, arg);
		}

		@Override
		protected void setupRenderSystem(ViewportPanelBase3d cbi, TextureSheetType tex) {
			cbi.putRenderTypes(renderTypes);
		}

		@Override
		protected void bindTexture(ViewportPanelBase3d cbi, TextureProvider skin, TextureSheetType tex) {
			skin.bind();
			cbi.load();
		}

		@Override
		protected void bindDefaultTexture(ViewportPanelBase3d cbi, TextureSheetType tex) {
			cbi.load(tex.name().toLowerCase(Locale.ROOT));
		}
	}

	@Override
	protected DirectHolderPlayer<ViewportPanelBase3d> createHolder(VanillaPlayerModel model, String arg) {
		return new GuiHolderPlayer(this, model, arg);
	}
}
