package com.tom.cpm.shared.editor.gui;

import java.util.function.Supplier;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.math.MathHelper;
import com.tom.cpm.shared.math.Vec2i;

public class TextureDisplayPanel extends GuiElement {
	private int lastMx, lastMy;
	private boolean dragging;
	private float zoom;
	private int offX, offY;
	private Editor editor;
	public Supplier<Vec2i> cursorPos;
	public TextureDisplayPanel(IGui gui, Editor editor, int zoom) {
		super(gui);
		this.editor = editor;
		this.zoom = 0;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if(editor.skinProvider.texture != null) {
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_fill);
			editor.skinProvider.bind();
			if(zoom == 0) {
				zoom = bounds.h / (float) editor.skinProvider.getImage().getWidth();
			}
			int rw = (int) (zoom * editor.skinProvider.getImage().getWidth());
			int rh = (int) (zoom * editor.skinProvider.getImage().getHeight());
			gui.drawBox(bounds.x + offX, bounds.y + offY, rw, rh, 0xffffffff);
			gui.drawTexture(bounds.x + offX, bounds.y + offY, rw, rh, 0, 0, 1, 1);
			int imgX = (int) ((mouseX - offX - bounds.x) / zoom);
			int imgY = (int) ((mouseY - offY - bounds.y) / zoom);
			SkinTextureDisplay.drawBoxTextureOverlay(gui, editor, bounds.x + offX, bounds.y + offY, zoom, zoom);
			Vec2i p1 = imgX >= 0 && imgY >= 0 && imgX < editor.skinProvider.getImage().getWidth() && imgY < editor.skinProvider.getImage().getHeight() ? new Vec2i(imgX, imgY) : null;
			Vec2i p2 = editor.cursorPos.get();
			Vec2i p = p2 != null ? p2 : p1;
			if(p != null && p.x >= 0 && p.y >= 0 && p.x < editor.skinProvider.getImage().getWidth() && p.y < editor.skinProvider.getImage().getHeight()) {
				int imgC = editor.skinProvider.getImage().getRGB(p.x, p.y);
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
			float zv = (dir * (zoom / 8f));
			float iw = editor.skinProvider.getImage().getWidth() * zoom;
			float ih = editor.skinProvider.getImage().getHeight() * zoom;
			zoom += zv;
			float niw = editor.skinProvider.getImage().getWidth() * zoom;
			float nih = editor.skinProvider.getImage().getHeight() * zoom;
			//offX -= (bounds.w / 2 - x - offX - bounds.x) * zv / 2;
			//offY -= (bounds.h / 2 - y - offY - bounds.y) * zv / 2;
			offX -= ((niw - iw) / 2) * MathHelper.clamp(Math.abs(1-(offX / (bounds.w / 2))), 0, 1);
			offY -= ((nih - ih) / 2) * MathHelper.clamp(Math.abs(1-(offY / (bounds.h / 2))), 0, 1);
			offX = MathHelper.clamp(offX, (int) (-editor.skinProvider.getImage().getWidth() * zoom + 10), bounds.w - 10);
			offY = MathHelper.clamp(offY, (int) (-editor.skinProvider.getImage().getWidth() * zoom + 10), bounds.h - 10);
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
			if(btn == 0) {
				int px = (int) ((x - offX - bounds.x) / zoom);
				int py = (int) ((y - offY - bounds.y) / zoom);
				if(px >= 0 && py >= 0 && px < editor.skinProvider.getImage().getWidth() && py < editor.skinProvider.getImage().getHeight()) {
					if(gui.isCtrlDown()) {
						editor.penColor = editor.skinProvider.getImage().getRGB(px, py);
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

				if(btn == 2) {
					offX += dx;
					offY += dy;
					offX = MathHelper.clamp(offX, (int) (-editor.skinProvider.getImage().getWidth() * zoom + 10), bounds.w - 10);
					offY = MathHelper.clamp(offY, (int) (-editor.skinProvider.getImage().getWidth() * zoom + 10), bounds.h - 10);
				} else if(btn == 0) {
					int px = (int) ((x - offX - bounds.x) / zoom);
					int py = (int) ((y - offY - bounds.y) / zoom);
					if(px >= 0 && py >= 0 && px < editor.skinProvider.getImage().getWidth() && py < editor.skinProvider.getImage().getHeight()) {
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
}
