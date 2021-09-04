package com.tom.cpm.shared.gui.panel;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.render.RenderMode;

public abstract class Panel3d extends GuiElement {
	private Panel3dNative nat;
	protected int backgroundColor;

	public Panel3d(IGui gui) {
		super(gui);

		nat = gui.getNative().getNative(Panel3d.class, this);
	}

	public static abstract class Panel3dNative {
		protected Panel3d panel;
		protected Vec2i renderPos = new Vec2i();

		public Panel3dNative(Panel3d panel) {
			this.panel = panel;
		}

		public abstract void render(float partialTicks);
		public abstract RenderTypes<RenderMode> getRenderTypes();
		public abstract RenderTypes<RenderMode> getRenderTypes(String tex);
		public abstract int getColorUnderMouse();
		public abstract Image takeScreenshot(Vec2i size);
		public abstract void renderItem(MatrixStack stack, ItemSlot hand, DisplayItem item);

		public Box getBounds() {
			return panel.bounds;
		}
	}

	public abstract void render(MatrixStack stack, VBuffers buf, float partialTicks);
	public abstract ViewportCamera getCamera();

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();
		if(backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		if(enabled) {
			nat.renderPos = gui.getOffset();
			nat.render(partialTicks);
		}
		gui.popMatrix();
		gui.setupCut();
	}

	protected RenderTypes<RenderMode> getRenderTypes() {
		return nat.getRenderTypes();
	}

	protected RenderTypes<RenderMode> getRenderTypes(String tex) {
		return nat.getRenderTypes(tex);
	}

	public IGui getGui() {
		return gui;
	}

	protected int getColorUnderMouse() {
		return nat.getColorUnderMouse();
	}

	public Image takeScreenshot(Vec2i size) {
		return nat.takeScreenshot(size);
	}

	public void renderItem(MatrixStack stack, ItemSlot hand, DisplayItem item) {
		nat.renderItem(stack, hand, item);
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
