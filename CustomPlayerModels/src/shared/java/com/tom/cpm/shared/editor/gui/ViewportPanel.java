package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;

public class ViewportPanel extends GuiElement {
	protected Editor editor;
	protected int mx, my;
	protected boolean enableDrag;
	protected boolean dragMode;
	protected int paintColor;
	protected ViewportPanelNative nat;
	protected Vec2i mouseCursorPos = new Vec2i();

	public ViewportPanel(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
		nat = gui.getNative().getNative(ViewportPanel.class, this);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		mouseCursorPos.x = mouseX;
		mouseCursorPos.y = mouseY;

		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();
		gui.drawBox(0, 0, bounds.w, bounds.h, 0xff333333);

		nat.render(partialTicks, mouseX, mouseY);

		gui.popMatrix();
		gui.setupCut();
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y)) {
			this.mx = x;
			this.my = y;
			this.enableDrag = true;
			this.dragMode = gui.isShiftDown();
			return true;
		} else if(bounds.isInBounds(x, y)){
			editor.position.x = 0.5f;
			editor.position.y = 1;
			editor.position.z = 0.5f;
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
			if(dragMode) {
				float yaw = editor.look.getYaw();
				double px = 0, pz = 0;
				int dx = x - mx;
				int dy = y - my;
				float move = -1 / editor.camDist;
				if ( dx != 0) {
					px += Math.sin(yaw - Math.PI / 2) * -1.0f * dx * move;
					pz += Math.cos(yaw - Math.PI / 2) * dx * move;
				}

				if(dy != 0) {
					px += Math.sin(yaw) * -1.0f * dy * move;
					pz += Math.cos(yaw) * dy * move;
				}

				float f = 1 - editor.look.y;
				Vec3f by = new Vec3f((float) (px * editor.look.y), 0, (float) (pz * editor.look.y));
				Vec3f by1 = by.mul(dy * 0.1f * f);
				editor.position.x += px + by1.x;
				editor.position.y += -f * move * dy;
				editor.position.z += pz + by1.z;
			} else {
				float pitch = (float) Math.asin(editor.look.y);
				float yaw = editor.look.getYaw();
				if(Float.isNaN(pitch))pitch = 0;
				if(Float.isNaN(yaw))yaw = 0;
				yaw += Math.toRadians(x - mx);
				pitch -= Math.toRadians(y - my);
				yaw = (float) MathHelper.clamp(yaw, -Math.PI, Math.PI);
				pitch = (float) MathHelper.clamp(pitch, -Math.PI/2, Math.PI/2);
				editor.look.y = (float) Math.sin(pitch);

				double sin = Math.sin(yaw);
				double cos = Math.cos(yaw);
				editor.look.x = (float) cos;
				editor.look.z = (float) sin;
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
		editor.camDist += (dir * (editor.camDist / 16f));
		if(editor.camDist < 32)editor.camDist = 32;
		if(editor.camDist > 512)editor.camDist = 512;
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
		protected ViewportPanel panel;
		protected Editor editor;
		public int colorUnderMouse;

		public ViewportPanelNative(ViewportPanel panel) {
			this.panel = panel;
			this.editor = panel.editor;
		}

		public abstract void render(float partialTicks, int mouseX, int mouseY);

		public Box getBounds() {
			return panel.bounds;
		}
	}
}
