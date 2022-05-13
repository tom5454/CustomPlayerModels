package com.tom.cpm.shared.gui.panel;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.gui.ViewportCamera;

public class ModelDisplayPanel extends ViewportPanelBase3d {
	private IModelDisplayPanel skins;
	private String loadingText;

	public ModelDisplayPanel(Frame frm, IModelDisplayPanel skins) {
		super(frm);
		this.skins = skins;
	}

	@Override
	public void render(MatrixStack stack, VBuffers buf, float partialTicks) {
		renderModel(stack, buf, partialTicks);
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		Box bounds = getBounds();
		if(backgroundColor != 0) {
			gui.drawBox(bounds.x - 1, bounds.y - 1, bounds.w + 2, bounds.h + 2, 0xff000000);
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, backgroundColor);
		}
		if(!skins.doRender())return;
		if(skins.getSelectedDefinition() == null) {
			gui.drawText(bounds.x + bounds.w / 2 - gui.textWidth(loadingText) / 2, bounds.y + bounds.h / 2 - 4, loadingText, gui.getColors().label_text_color);
		} else {
			super.draw(event, partialTicks);
		}
	}

	@Override
	public ViewportCamera getCamera() {
		return skins.getCamera();
	}

	@Override
	public void preRender(MatrixStack stack, VBuffers buf) {
		skins.preRender();
	}

	@Override
	public ModelDefinition getDefinition() {
		return skins.getSelectedDefinition();
	}

	public void setLoadingText(String loadingText) {
		this.loadingText = loadingText;
	}

	public interface IModelDisplayPanel {
		ModelDefinition getSelectedDefinition();
		ViewportCamera getCamera();
		void preRender();
		boolean doRender();
	}
}
