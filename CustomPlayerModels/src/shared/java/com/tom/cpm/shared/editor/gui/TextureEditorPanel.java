package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;

public class TextureEditorPanel extends GuiElement {
	private int lastMx, lastMy;
	private boolean dragging;
	private float zoom;
	private int offX, offY, dragX, dragY;
	private Editor editor;
	public Supplier<Vec2i> cursorPos;
	private Vec2i mouseCursorPos = new Vec2i();
	private Vec2i moveStart = new Vec2i();

	public TextureEditorPanel(IGui gui, Editor editor, int zoom) {
		super(gui);
		this.editor = editor;
		this.zoom = 0;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		mouseCursorPos.x = event.x;
		mouseCursorPos.y = event.y;

		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();

		gui.drawBox(0, 0, bounds.w, bounds.h, gui.getColors().button_fill);

		ETextures provider = editor.getTextureProvider();
		if(provider != null) {
			provider.provider.bind();
			if(zoom == 0) {
				zoom = bounds.h / (float) provider.getImage().getWidth();
			}
			int rw = (int) (zoom * provider.getImage().getWidth());
			int rh = (int) (zoom * provider.getImage().getHeight());

			{
				int chkW = Math.min(bounds.w, offX + rw);
				int chkH = Math.min(bounds.h, offY + rh);
				for(int x = offX;x<chkW;x+= 8) {
					for(int y = offY;y<chkH;y+= 8) {
						if(x + 8 < 0 || y + 8 < 0 || x - 8 > bounds.w || y - 8 > bounds.w)continue;
						int w = Math.min(8, chkW - x);
						int h = Math.min(8, chkH - y);
						if(((x - offX) / 8 + (y - offY) / 8) % 2 == 0)
							gui.drawBox(x, y, w, h, gui.getColors().button_border);
						else
							gui.drawBox(x, y, w, h, gui.getColors().button_disabled);
					}
				}
			}
			gui.drawTexture(offX, offY, rw, rh, 0, 0, 1, 1);
			int imgX = (int) ((event.x - offX - bounds.x) / zoom);
			int imgY = (int) ((event.y - offY - bounds.y) / zoom);
			TextureDisplay.drawBoxTextureOverlay(gui, editor, offX, offY, zoom, zoom);
			Vec2i p1 = event.isHovered(bounds) && imgX >= 0 && imgY >= 0 && imgX < provider.getImage().getWidth() && imgY < provider.getImage().getHeight() ? new Vec2i(imgX, imgY) : null;
			switch (editor.drawMode) {
			case MOVE_UV:
			{
				if(p1 != null) {
					ModelElement e = getElementUnderMouse(p1.x, p1.y);
					if(dragging)e = editor.getSelectedElement();
					if(e != null) {
						Box b = e.getTextureBox();
						gui.drawRectangle(b.x * zoom + offX, b.y * zoom + offY, b.w * zoom, b.h * zoom, 0xff000000);
					}
				}
			}
			break;
			case PEN:
			case RUBBER:
			{
				Vec2i p2 = editor.cursorPos.get();
				Vec2i p = p2 != null ? p2 : p1;
				if(provider.isEditable() && p != null && p.x >= 0 && p.y >= 0 && p.x < provider.getImage().getWidth() && p.y < provider.getImage().getHeight()) {
					int imgC = provider.getImage().getRGB(p.x, p.y);
					int outlineColor = ((0xffffff - (imgC & 0xffffff)) & 0xffffff);
					int a = (imgC >> 24) & 0xff;
					if(a < 64)outlineColor = 0x000000;
					if(a == 0)imgC = 0xffffffff;
					gui.drawRectangle(p.x * zoom + offX, p.y * zoom + offY, zoom, zoom, 0xff000000 | outlineColor);
				}
			}
			break;
			default:
				break;
			}
		}

		gui.popMatrix();
	}

	private ModelElement getElementUnderMouse(int x, int y) {
		List<ModelElement> elems = new ArrayList<>();
		Editor.walkElements(editor.elements, e -> {
			if(e.type != ElementType.NORMAL)return;
			Box b = e.getTextureBox();
			if(b != null && b.isInBounds(x, y)) {
				elems.add(e);
			}
		});
		if(elems.contains(editor.selectedElement))return editor.getSelectedElement();
		return !elems.isEmpty() ? elems.get(0) : null;
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
			ETextures provider = editor.getTextureProvider();
			if(btn == 0) {
				int px = (int) ((x - offX - bounds.x) / zoom);
				int py = (int) ((y - offY - bounds.y) / zoom);
				if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight()) {
					switch (editor.drawMode) {
					case PEN:
					case RUBBER:
					case FILL:
					{
						if(gui.isCtrlDown()) {
							editor.penColor = provider.getImage().getRGB(px, py);
							editor.setPenColor.accept(editor.penColor);
							dragging = false;
						} else
							editor.drawPixel(px, py, false);
					}
					break;

					case MOVE_UV:
					{
						dragX = px;
						dragY = py;
						ModelElement me = getElementUnderMouse(px, py);
						editor.selectedElement = me;
						if(me != null) {
							moveStart.x = me.u;
							moveStart.y = me.v;
						}
					}
					break;

					default:
						break;
					}
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
				ETextures provider = editor.getTextureProvider();

				if(btn == EditorGui.getRotateMouseButton()) {
					offX += dx;
					offY += dy;
					offX = MathHelper.clamp(offX, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.w - 10);
					offY = MathHelper.clamp(offY, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.h - 10);
				} else if(btn == 0) {
					int px = (int) ((x - offX - bounds.x) / zoom);
					int py = (int) ((y - offY - bounds.y) / zoom);
					if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight()) {
						switch (editor.drawMode) {
						case PEN:
						case RUBBER:
						case FILL:
							editor.drawPixel(px, py, false);
							break;

						case MOVE_UV:
						{
							ModelElement me = editor.getSelectedElement();
							if(me != null) {
								int xoff = px - dragX;
								int yoff = py - dragY;
								if(Math.abs(xoff) >= me.textureSize)dragX = px;
								if(Math.abs(yoff) >= me.textureSize)dragY = py;
								me.u = MathHelper.clamp(me.u + xoff / me.textureSize, 0, provider.provider.size.x);
								me.v = MathHelper.clamp(me.v + yoff / me.textureSize, 0, provider.provider.size.y);
								if(xoff > 0 || yoff > 0) {
									editor.updateGui();
									editor.markDirty();
								}
							}
						}
						break;

						default:
							break;
						}
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
		if(bounds.isInBounds(x, y) || dragging) {
			dragging = false;
			switch (editor.drawMode) {
			case MOVE_UV:
			{
				ModelElement me = editor.getSelectedElement();
				if(me != null) {
					editor.action("move", "action.cpm.texUV").updateValueOp(me, new Vec2i(moveStart), new Vec2i(me.u, me.v), (a, b) -> {
						a.u = b.x;
						a.v = b.y;
					}).execute();
				}
			}
			break;

			default:
				break;
			}
			return true;
		}
		return false;
	}

	private void zoom(int x, int y, int dir) {
		ETextures provider = editor.getTextureProvider();
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
