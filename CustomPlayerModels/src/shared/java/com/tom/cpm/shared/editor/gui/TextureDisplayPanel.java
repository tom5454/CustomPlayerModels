package com.tom.cpm.shared.editor.gui;

import java.util.function.Supplier;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.math.MathHelper;
import com.tom.cpm.shared.math.Vec2i;
import com.tom.cpm.shared.skin.TextureProvider;

public class TextureDisplayPanel extends GuiElement {
	private int lastMx, lastMy;
	private boolean dragging;
	private float zoom;
	private int offX, offY;
	private Editor editor;
	public Supplier<Vec2i> cursorPos;
	private Vec2i mouseCursorPos = new Vec2i();

	public TextureDisplayPanel(IGui gui, Editor editor, int zoom) {
		super(gui);
		this.editor = editor;
		this.zoom = 0;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		mouseCursorPos.x = mouseX;
		mouseCursorPos.y = mouseY;

		TextureProvider provider = editor.getTextureProvider();
		if(provider != null && provider.texture != null) {
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_fill);
			provider.bind();
			if(zoom == 0) {
				zoom = bounds.h / (float) provider.getImage().getWidth();
			}
			int rw = (int) (zoom * provider.getImage().getWidth());
			int rh = (int) (zoom * provider.getImage().getHeight());
			gui.drawBox(bounds.x + offX, bounds.y + offY, rw, rh, 0xffffffff);
			gui.drawTexture(bounds.x + offX, bounds.y + offY, rw, rh, 0, 0, 1, 1);
			int imgX = (int) ((mouseX - offX - bounds.x) / zoom);
			int imgY = (int) ((mouseY - offY - bounds.y) / zoom);
			SkinTextureDisplay.drawBoxTextureOverlay(gui, editor, bounds.x + offX, bounds.y + offY, zoom, zoom);
			Vec2i p1 = imgX >= 0 && imgY >= 0 && imgX < provider.getImage().getWidth() && imgY < provider.getImage().getHeight() ? new Vec2i(imgX, imgY) : null;
			Vec2i p2 = editor.cursorPos.get();
			Vec2i p = p2 != null ? p2 : p1;
			if(p != null && p.x >= 0 && p.y >= 0 && p.x < provider.getImage().getWidth() && p.y < provider.getImage().getHeight()) {
				int imgC = provider.getImage().getRGB(p.x, p.y);
				int outlineColor = ((0xffffff - (imgC & 0xffffff)) & 0xffffff);
				int a = (imgC >> 24) & 0xff;
				if(a < 64)outlineColor = 0x000000;
				if(a == 0)imgC = 0xffffffff;
				gui.drawBox(bounds.x + p.x * zoom + offX, bounds.y + p.y * zoom + offY, zoom, zoom, 0xff000000 | outlineColor);
				gui.drawBox(bounds.x + p.x * zoom + offX + 1, bounds.y + p.y * zoom + offY + 1, zoom - 2, zoom - 2, 0xff000000);
				gui.drawBox(bounds.x + p.x * zoom + offX + 1, bounds.y + p.y * zoom + offY + 1, zoom - 2, zoom - 2, imgC);
			}
		}
	}

	@Override
	public boolean mouseWheel(int x, int y, int dir) {
		if(bounds.isInBounds(x, y)) {
			zoom(x, y, dir);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(bounds.isInBounds(x, y)) {
			lastMx = x;
			lastMy = y;
			dragging = true;
			TextureProvider provider = editor.getTextureProvider();
			if(btn == 0) {
				int px = (int) ((x - offX - bounds.x) / zoom);
				int py = (int) ((y - offY - bounds.y) / zoom);
				if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight()) {
					if(gui.isCtrlDown()) {
						editor.penColor = provider.getImage().getRGB(px, py);
						editor.setPenColor.accept(editor.penColor);
						dragging = false;
					} else
						editor.drawPixel(px, py);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(bounds.isInBounds(x, y)) {
			if(dragging) {
				int dx = x - lastMx;
				int dy = y - lastMy;
				TextureProvider provider = editor.getTextureProvider();

				if(btn == EditorGui.getRotateMouseButton()) {
					offX += dx;
					offY += dy;
					offX = MathHelper.clamp(offX, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.w - 10);
					offY = MathHelper.clamp(offY, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.h - 10);
				} else if(btn == 0) {
					int px = (int) ((x - offX - bounds.x) / zoom);
					int py = (int) ((y - offY - bounds.y) / zoom);
					if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight()) {
						editor.drawPixel(px, py);
					}
				}

				lastMx = x;
				lastMy = y;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseRelease(int x, int y, int btn) {
		if(bounds.isInBounds(x, y)) {
			dragging = false;
			return true;
		}
		return false;
	}

	private void zoom(int x, int y, int dir) {
		TextureProvider provider = editor.getTextureProvider();
		float zv = (dir * (zoom / 8f));
		float iw = provider.getImage().getWidth() * zoom;
		float ih = provider.getImage().getHeight() * zoom;
		zoom += zv;
		float niw = provider.getImage().getWidth() * zoom;
		float nih = provider.getImage().getHeight() * zoom;
		//offX -= (bounds.w / 2 - x - offX - bounds.x) * zv / 2;
		//offY -= (bounds.h / 2 - y - offY - bounds.y) * zv / 2;
		offX -= ((niw - iw) / 2) * MathHelper.clamp(Math.abs(1-(offX / (bounds.w / 2))), 0, 1);
		offY -= ((nih - ih) / 2) * MathHelper.clamp(Math.abs(1-(offY / (bounds.h / 2))), 0, 1);
		offX = MathHelper.clamp(offX, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.w - 10);
		offY = MathHelper.clamp(offY, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.h - 10);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(!event.isConsumed() && bounds.isInBounds(mouseCursorPos)) {
			if(event.matches("+")) {
				zoom(mouseCursorPos.x, mouseCursorPos.y, 1);
			} else if(event.matches("-")) {
				zoom(mouseCursorPos.x, mouseCursorPos.y, -1);
			}
		}
	}
}
