package com.tom.cpm.shared.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.SkinType;

public class SkinDisplayPanel extends ViewportPanelBase {
	private SkinsPanel skins;

	public SkinDisplayPanel(IGui gui, SkinsPanel skins) {
		super(gui);
		this.skins = skins;
	}

	@Override
	public void draw0(float partialTicks) {
		if(skins.getSelectedDefinition() == null) {
			//TODO loading text
		} else {
			nat.renderSetup();
			nat.render(partialTicks);
			nat.renderFinish();
		}
	}

	@Override
	public ViewportCamera getCamera() {
		return skins.camera;
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
}
