package com.tom.cpm.shared.editor.gui;

import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.skin.TextureProvider;

public class TextureEditorPanel extends GuiElement {
	private int lastMx, lastMy;
	private boolean dragging;
	private float zoom;
	private int offX, offY, dragX, dragY;
	private Editor editor;
	public Supplier<Vec2i> cursorPos;
	private Vec2i mouseCursorPos = new Vec2i();

	public TextureEditorPanel(IGui gui, Editor editor, int zoom) {
		super(gui);
		this.editor = editor;
		this.zoom = 0;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		mouseCursorPos.x = mouseX;
		mouseCursorPos.y = mouseY;

		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();

		TextureProvider provider = editor.getTextureProvider();
		if(provider != null && provider.texture != null) {
			gui.drawBox(0, 0, bounds.w, bounds.h, gui.getColors().button_fill);
			provider.bind();
			if(zoom == 0) {
				zoom = bounds.h / (float) provider.getImage().getWidth();
			}
			int rw = (int) (zoom * provider.getImage().getWidth());
			int rh = (int) (zoom * provider.getImage().getHeight());
			gui.drawBox(offX, offY, rw, rh, 0xffffffff);
			gui.drawTexture(offX, offY, rw, rh, 0, 0, 1, 1);
			int imgX = (int) ((mouseX - offX - bounds.x) / zoom);
			int imgY = (int) ((mouseY - offY - bounds.y) / zoom);
			TextureDisplay.drawBoxTextureOverlay(gui, editor, offX, offY, zoom, zoom);
			Vec2i p1 = imgX >= 0 && imgY >= 0 && imgX < provider.getImage().getWidth() && imgY < provider.getImage().getHeight() ? new Vec2i(imgX, imgY) : null;
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
				if(p != null && p.x >= 0 && p.y >= 0 && p.x < provider.getImage().getWidth() && p.y < provider.getImage().getHeight()) {
					int imgC = provider.getImage().getRGB(p.x, p.y);
					int outlineColor = ((0xffffff - (imgC & 0xffffff)) & 0xffffff);
					int a = (imgC >> 24) & 0xff;
					if(a < 64)outlineColor = 0x000000;
					if(a == 0)imgC = 0xffffffff;
					gui.drawBox(p.x * zoom + offX, p.y * zoom + offY, zoom, zoom, 0xff000000 | outlineColor);
					gui.drawBox(p.x * zoom + offX + 1, p.y * zoom + offY + 1, zoom - 2, zoom - 2, 0xff000000);
					gui.drawBox(p.x * zoom + offX + 1, p.y * zoom + offY + 1, zoom - 2, zoom - 2, imgC);
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
		ModelElement[] elem = new ModelElement[1];
		Editor.walkElements(editor.elements, e -> {
			if(elem[0] != null)return;
			if(e.type != ElementType.NORMAL)return;
			Box b = e.getTextureBox();
			if(b != null && b.isInBounds(x, y)) {
				elem[0] = e;
			}
		});
		return elem[0];
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
							int u = me.u;
							int v = me.v;
							editor.addUndo(() -> {
								me.u = u;
								me.v = v;
							});
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
								me.u = MathHelper.clamp(me.u + xoff / me.textureSize, 0, 256);
								me.v = MathHelper.clamp(me.v + yoff / me.textureSize, 0, 256);
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
					int u = me.u;
					int v = me.v;
					editor.setCurrentOp(() -> {
						me.u = u;
						me.v = v;
					});
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
