package com.tom.cpm.shared.gui;

import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.HeldItem;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.TextureProvider;

public abstract class ViewportPanelBase extends GuiElement {
	protected int mx, my;
	protected boolean enableDrag;
	protected boolean dragMode;
	protected int paintColor;
	protected ViewportPanelNative nat;
	protected Vec2i mouseCursorPos = new Vec2i();

	public ViewportPanelBase(IGui gui) {
		super(gui);
		nat = gui.getNative().getNative(ViewportPanelBase.class, this);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		mouseCursorPos.x = mouseX;
		mouseCursorPos.y = mouseY;

		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();
		nat.renderPos = gui.getOffset();

		draw0(partialTicks);

		gui.popMatrix();
		gui.setupCut();
	}

	public abstract void draw0(float partialTicks);

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y)) {
			this.mx = x;
			this.my = y;
			this.enableDrag = true;
			this.dragMode = gui.isShiftDown();
			return true;
		} else if(bounds.isInBounds(x, y)){
			ViewportCamera cam = getCamera();
			cam.position.x = 0.5f;
			cam.position.y = 1;
			cam.position.z = 0.5f;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseRelease(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y)) {
			enableDrag = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y) && enableDrag) {
			ViewportCamera cam = getCamera();
			if(dragMode) {
				float yaw = cam.look.getYaw();
				double px = 0, pz = 0;
				int dx = x - mx;
				int dy = y - my;
				float move = -1 / cam.camDist;
				if ( dx != 0) {
					px += Math.sin(yaw - Math.PI / 2) * -1.0f * dx * move;
					pz += Math.cos(yaw - Math.PI / 2) * dx * move;
				}

				if(dy != 0) {
					px += Math.sin(yaw) * -1.0f * dy * move;
					pz += Math.cos(yaw) * dy * move;
				}

				float f = 1 - cam.look.y;
				Vec3f by = new Vec3f((float) (px * cam.look.y), 0, (float) (pz * cam.look.y));
				Vec3f by1 = by.mul(dy * 0.1f * f);
				cam.position.x += px + by1.x;
				cam.position.y += -f * move * dy;
				cam.position.z += pz + by1.z;
			} else {
				float pitch = (float) Math.asin(cam.look.y);
				float yaw = cam.look.getYaw();
				if(Float.isNaN(pitch))pitch = 0;
				if(Float.isNaN(yaw))yaw = 0;
				yaw += Math.toRadians(x - mx);
				pitch -= Math.toRadians(y - my);
				yaw = (float) MathHelper.clamp(yaw, -Math.PI, Math.PI);
				pitch = (float) MathHelper.clamp(pitch, -Math.PI/2, Math.PI/2);
				cam.look.y = (float) Math.sin(pitch);

				double sin = Math.sin(yaw);
				double cos = Math.cos(yaw);
				cam.look.x = (float) cos;
				cam.look.z = (float) sin;
			}
			this.mx = x;
			this.my = y;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseWheel(int x, int y, int dir) {
		if(bounds.isInBounds(x, y)) {
			zoom(dir);
			return true;
		}
		return false;
	}

	private void zoom(int dir) {
		ViewportCamera cam = getCamera();
		cam.camDist += (dir * (cam.camDist / 16f));
		if(cam.camDist < 32)cam.camDist = 32;
		if(cam.camDist > 4096)cam.camDist = 4096;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(!event.isConsumed() && bounds.isInBounds(mouseCursorPos)) {
			if(event.matches("+")) {
				zoom(1);
			} else if(event.matches("-")) {
				zoom(-1);
			}
		}
	}

	public static abstract class ViewportPanelNative {
		protected ViewportPanelBase panel;
		protected Vec2i renderPos = new Vec2i();

		public ViewportPanelNative(ViewportPanelBase panel) {
			this.panel = panel;
		}

		public abstract void renderSetup();
		public abstract void renderFinish();
		public abstract void renderBase();
		public abstract void render(float partialTicks);
		public abstract int getColorUnderMouse();
		public abstract Image takeScreenshot(Vec2i size);
		public abstract boolean canRenderHeldItem();

		public Box getBounds() {
			return panel.bounds;
		}
	}

	public abstract ViewportCamera getCamera();
	public abstract void preRender();
	public abstract SkinType getSkinType();
	public abstract ModelDefinition getDefinition();
	public TextureProvider getTextureSheet() {
		return getDefinition().getSkinOverride();
	}
	public abstract boolean isTpose();
	public abstract boolean applyLighting();
	public HeldItem getHeldItem(Hand hand) {
		return HeldItem.NONE;
	}
	public AnimationMode getAnimMode() {
		return AnimationMode.PLAYER;
	}

	public static class ViewportCamera {
		public Vec3f position = new Vec3f(0.5f, 1, 0.5f);
		public Vec3f look = new Vec3f(0.25f, 0.5f, 0.25f);
		public float camDist = 64;
	}

	public void applyRenderPoseForAnim(Consumer<VanillaPose> func) {}

	public Image takeScreenshot(Vec2i size) {
		return nat.takeScreenshot(size);
	}

	public IGui getGui() {
		return gui;
	}

	public boolean canRenderHeldItem() {
		return nat.canRenderHeldItem();
	}

	public float getScale() {
		return 1;
	}
}
