package com.tom.cpm.shared.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.SkinType;

public class ModelDisplayPanel extends ViewportPanelBase {
	private IModelDisplayPanel skins;

	public ModelDisplayPanel(IGui gui, IModelDisplayPanel skins) {
		super(gui);
		this.skins = skins;
	}

	@Override
	public void draw0(float partialTicks) {
		Box bounds = getBounds();
		gui.drawBox(0, 0, bounds.w, bounds.h, 0xff000000);
		gui.drawBox(1, 1, bounds.w-2, bounds.h-2, 0xff555555);
		if(!skins.doRender())return;
		if(skins.getSelectedDefinition() == null) {
			gui.drawText(5, 5, gui.i18nFormat("label.cpm.loading"), gui.getColors().label_text_color);
		} else {
			nat.renderSetup();
			nat.render(partialTicks);
			nat.renderFinish();
		}
	}

	@Override
	public ViewportCamera getCamera() {
		return skins.getCamera();
	}

	@Override
	public void preRender() {
		skins.preRender();
	}

	@Override
	public SkinType getSkinType() {
		return skins.getSelectedDefinition().getSkinType();
	}

	@Override
	public ModelDefinition getDefinition() {
		return skins.getSelectedDefinition();
	}

	@Override
	public boolean isTpose() {
		return false;
	}

	@Override
	public boolean applyLighting() {
		return true;
	}

	public interface IModelDisplayPanel {
		ModelDefinition getSelectedDefinition();
		ViewportCamera getCamera();
		void preRender();
		boolean doRender();
	}
}
