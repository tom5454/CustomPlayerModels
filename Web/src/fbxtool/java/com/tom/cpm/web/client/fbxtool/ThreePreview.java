package com.tom.cpm.web.client.fbxtool;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpm.web.client.fbxtool.three.ThreeModule;
import com.tom.cpm.web.client.render.RenderSystem;

public class ThreePreview extends GuiElement {
	private Runnable update;
	private boolean dirty;

	public ThreePreview(IGui gui, int x, int y, int width, int height, Runnable update) {
		super(gui);
		this.update = update;
		setBounds(new Box(x, y, width, height));
		ThreeModule.prepare().then(__ -> {
			ThreeModule.renderer.domElement.style.left = (RenderSystem.displayRatio * x) + "px";
			ThreeModule.renderer.domElement.style.top = (RenderSystem.displayRatio * y) + "px";
			ThreeModule.renderer.setSize(width * RenderSystem.displayRatio, height * RenderSystem.displayRatio);
			ThreeModule.camera.aspect = width / (float) height;
			dirty = true;
			return null;
		});
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if (ThreeModule.isLoaded) {
			if(dirty) {
				update.run();
				dirty = false;
			}
			ThreeModule.render();
			if(event.isConsumed()) {
				ThreeModule.renderer.domElement.style.visibility = "hidden";
				RenderSystem.renderCanvas(ThreeModule.renderer.domElement, bounds.x, bounds.y, bounds.w, bounds.h);
			} else {
				ThreeModule.renderer.domElement.style.visibility = "";
			}
		} else
			gui.drawText(bounds.w / 4, bounds.h * 3 / 4, "Three.js Loading...", gui.getColors().label_text_color);
	}

	public void markDirty() {
		dirty = true;
	}
}
