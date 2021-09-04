package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.util.PaintImageCreator;

public class ViewportPaintPanel extends ViewportPanel {
	private int color;
	private int dragging;

	public ViewportPaintPanel(IGui gui, Editor editor) {
		super(gui, editor);
	}

	@Override
	public void render(MatrixStack stack, VBuffers buf, float partialTicks) {
		if(editor.renderBase && !editor.renderPaint)renderBase(stack, buf);
		renderModel(stack, buf, partialTicks);
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		editor.renderPaint = true;
		super.draw(event, partialTicks);
		editor.renderPaint = false;
		int colorUnderMouse = getColorUnderMouse();

		if(bounds.isInBounds(mouseCursorPos.x - bounds.x, mouseCursorPos.y - bounds.y)) {
			color = colorUnderMouse;
		} else
			color = 0;

		if(!MinecraftObjectHolder.DEBUGGING ||!gui.isAltDown()) {
			gui.drawBox(0, 0, bounds.w, bounds.h, 0xff333333);
			super.draw(event, partialTicks);
		}

		if(MinecraftObjectHolder.DEBUGGING) {
			Vec2i v = getHoveredTexPos();
			if(v != null)gui.drawText(bounds.x, bounds.y, v.x + " " + v.y, 0xffffffff);
			ETextures tex = editor.getTextureProvider();
			Vec2i size = tex != null ? tex.provider.size : new Vec2i(64, 64);
			gui.drawText(bounds.x, bounds.y + 10, "(" + size.x + " " + size.y + ")", 0xffffffff);
			if((color & 0xC00000) == 0) gui.drawText(bounds.x, bounds.y + 20, "Out of bounds", 0xffffffff);
			else {
				int x = (color >> 10) & 0x3ff;
				int y = color & 0x3ff;
				gui.drawText(bounds.x, bounds.y + 20, "(" + x + " " + y + ")", 0xffffffff);
			}
			gui.drawText(bounds.x, bounds.y + 30, Integer.toHexString(color), 0xffffffff);
			gui.drawBox(bounds.x, bounds.y + 40, 16, 16, 0xffffffff);
			gui.drawBox(bounds.x + 1, bounds.y + 41, 14, 14, color | 0xff000000);
		}
	}

	public Vec2i getHoveredTexPos() {
		ETextures tex = editor.getTextureProvider();
		Vec2i size = tex != null ? tex.provider.size : new Vec2i(64, 64);
		return PaintImageCreator.getImageCoords(color, size.x, size.y);
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(bounds.isInBounds(x, y) && btn == 0) {
			dragging = 1;
			Vec2i v = getHoveredTexPos();
			if(v != null) {
				if(gui.isCtrlDown()) {
					ETextures tex = editor.getTextureProvider();
					if(tex != null) {
						editor.penColor = tex.getImage().getRGB(v.x, v.y);
						editor.setPenColor.accept(editor.penColor);
						dragging = 2;
					}
				} else
					editor.drawPixel(v.x, v.y, true);
			}
			return true;
		}
		return super.mouseClick(x, y, btn);
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(bounds.isInBounds(x, y) && btn == 0) {
			Vec2i v = getHoveredTexPos();
			if(v != null) {
				if(dragging == 1) {
					editor.drawPixel(v.x, v.y, true);
				} else if(dragging == 2) {
					ETextures tex = editor.getTextureProvider();
					if(tex != null) {
						editor.penColor = tex.getImage().getRGB(v.x, v.y);
						editor.setPenColor.accept(editor.penColor);
					}
				}
			}
			return true;
		}
		return super.mouseDrag(x, y, btn);
	}

	@Override
	public boolean mouseRelease(int x, int y, int btn) {
		if(btn == 0 && dragging > 0) {
			dragging = 0;
			return true;
		}
		return super.mouseRelease(x, y, btn);
	}

	@Override
	public boolean applyLighting() {
		return !editor.renderPaint;
	}
}
