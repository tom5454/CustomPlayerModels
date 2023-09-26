package com.tom.cpm.shared.gui.panel;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypeBuilder;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.render.RenderMode;

public abstract class Panel3d extends Panel {
	private Panel3dNative nat;
	protected MouseEvent mouse;
	protected Frame frame;

	public Panel3d(Frame frm) {
		super(frm.getGui());
		this.frame = frm;

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
		public abstract Image takeScreenshot(Vec2i size);

		@SuppressWarnings("unchecked")
		protected <RL> RenderTypes<RenderMode> getRenderTypes0(RL rl) {
			RenderTypes<RenderMode> renderTypes = new RenderTypes<>(RenderMode.class);
			((RenderTypeBuilder<RL, ?>) MinecraftClientAccess.get().getRenderBuilder()).build(renderTypes, rl);
			return renderTypes;
		}

		public Box getBounds() {
			return panel.bounds;
		}

		public abstract Mat4f getView();
		public abstract Mat4f getProjection();

		public Vec2i get3dSize() {
			return new Vec2i(panel.frame.getBounds().w, panel.frame.getBounds().h);
		}

		public Vec2i getMouse() {
			return panel.mouse.getPos();
		}

		public void draw3dOverlay() {
			Box bounds = getBounds();
			Vec2i ws = get3dSize();
			Vec2i off = panel.gui.getOffset();
			float sx = (off.x + bounds.x) / (float) ws.x;
			float sy = (off.y + bounds.y) / (float) ws.y;
			panel.gui.drawTexture(bounds.x, bounds.y, bounds.w, bounds.h, sx, sy, sx + bounds.w / (float) ws.x, sy + bounds.h / (float) ws.y);
		}
	}

	public abstract void render(MatrixStack stack, VBuffers buf, float partialTicks);
	public abstract ViewportCamera getCamera();

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		this.mouse = event.offset(bounds);
		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();
		if(backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		if(enabled) {
			nat.renderPos = gui.getOffset();
			nat.render(partialTicks);
		}
		for (GuiElement guiElement : elements) {
			if(guiElement.isVisible())
				guiElement.draw(event.offset(bounds), partialTicks);
		}
		gui.popMatrix();
		gui.setupCut();
		this.mouse = null;
	}

	protected RenderTypes<RenderMode> getRenderTypes() {
		return nat.getRenderTypes();
	}

	protected RenderTypes<RenderMode> getRenderTypes(String tex) {
		return nat.getRenderTypes(tex);
	}

	@Override
	public IGui getGui() {
		return gui;
	}

	public Image takeScreenshot(Vec2i size) {
		return nat.takeScreenshot(size);
	}

	public Mat4f getView() {
		return nat.getView();
	}

	public Mat4f getProjection() {
		return nat.getProjection();
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public Vec2i get3dSize() {
		return nat.get3dSize();
	}

	public Vec2i get3dMousePos() {
		return nat.getMouse();
	}

	public void draw3dOverlay() {
		nat.draw3dOverlay();
	}

	public MouseEvent getMouse() {
		return mouse;
	}
}
