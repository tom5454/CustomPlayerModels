package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTool;
import com.tom.cpm.shared.editor.anim.AnimatedTex;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.TreeSettingElement;
import com.tom.cpm.shared.editor.tree.VecType;
import com.tom.cpm.shared.editor.util.UVResizableArea;

public class TextureEditorPanel extends GuiElement {
	private int lastMx, lastMy;
	private boolean dragging;
	private float zoom;
	private int offX, offY, dragX, dragY;
	private Editor editor;
	public Supplier<Vec2i> cursorPos;
	private Vec2i mouseCursorPos = new Vec2i();
	private Vec2i moveStart;

	public TextureEditorPanel(IGui gui, Editor editor, int zoom) {
		super(gui);
		this.editor = editor;
		this.zoom = 0;
		int dark = gui.getColors().button_border;
		int light = gui.getColors().button_disabled;
		editor.textureEditorBg.getImage().setRGB(0, 0, dark);
		editor.textureEditorBg.getImage().setRGB(1, 0, light);
		editor.textureEditorBg.getImage().setRGB(0, 1, light);
		editor.textureEditorBg.getImage().setRGB(1, 1, dark);
		editor.textureEditorBg.texture.markDirty();
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
			if(zoom == 0) {
				zoom = bounds.h / (float) provider.getImage().getWidth();
			}
			int rw = (int) (zoom * provider.getImage().getWidth());
			int rh = (int) (zoom * provider.getImage().getHeight());

			editor.textureEditorBg.bind();
			gui.drawTexture(offX, offY, rw, rh, 0, 0, rw / 8f, rh / 8f);

			provider.provider.bind();
			gui.drawTexture(offX, offY, rw, rh, 0, 0, 1, 1);
			int imgX = (int) ((event.x - offX - bounds.x) / zoom);
			int imgY = (int) ((event.y - offY - bounds.y) / zoom);
			TextureDisplay.drawBoxTextureOverlay(gui, editor, offX, offY, zoom, zoom, true);
			Vec2i p1 = event.isHovered(bounds) && imgX >= 0 && imgY >= 0 && imgX < provider.getImage().getWidth() && imgY < provider.getImage().getHeight() ? new Vec2i(imgX, imgY) : null;
			switch (editor.drawMode.get()) {
			case MOVE_UV:
			{
				TreeElement e = getElementUnderMouse(event);
				if(dragging && moveStart != null)e = editor.getSelectedElement();
				if(e != null) {
					Box b = e.getTextureBox();
					if(b != null) {
						Vec4f s = UVResizableArea.expandBox(b, zoom);
						gui.drawRectangle(s.x * zoom + offX, s.y * zoom + offY, s.z * zoom, s.w * zoom, 0xff000000);
					}
				}
			}
			break;
			case PEN:
			case RUBBER:
			case COLOR_PICKER:
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

	private TreeElement getElementUnderMouse(MouseEvent event) {
		float x = (event.x - offX - bounds.x) / zoom;
		float y = (event.y - offY - bounds.y) / zoom;
		List<TreeElement> elems = new ArrayList<>();
		walkElements(e -> {
			if(UVResizableArea.isHovered(e.getTextureBox(), x, y, zoom))elems.add(e);
		});
		if(elems.contains(editor.selectedElement))return editor.selectedElement;
		return !elems.isEmpty() ? elems.get(0) : null;
	}

	private void walkElements(Consumer<TreeElement> c) {
		if(editor.selectedElement != null) {
			if(editor.getSelectedElement() == null || editor.getSelectedElement().type == ElementType.NORMAL) {
				c.accept(editor.selectedElement);
				editor.selectedElement.getSettingsElements().forEach(c);
				AnimatedTex.applyAnims(editor.selectedElement, c);
			}
		}
		Editor.walkElements(editor.elements, e -> {
			if(e.type != ElementType.NORMAL)return;
			c.accept(e);
			e.getSettingsElements().forEach(c);
		});
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
	public void mouseClick(MouseEvent event) {
		if(event.isHovered(bounds)) {
			lastMx = event.x;
			lastMy = event.y;
			dragging = true;
			ETextures provider = editor.getTextureProvider();
			int r = EditorGui.getRotateMouseButton();
			if(r == 0 ? gui.isShiftDown() : event.btn == r) {
			} else if(event.btn == 0) {
				int px = (int) ((event.x - offX - bounds.x) / zoom);
				int py = (int) ((event.y - offY - bounds.y) / zoom);

				switch (editor.drawMode.get()) {
				case PEN:
				case RUBBER:
				case FILL:
				case COLOR_PICKER:
				{
					if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight()) {
						if(gui.isCtrlDown() || editor.drawMode.get() == EditorTool.COLOR_PICKER) {
							editor.penColor = provider.getImage().getRGB(px, py);
							editor.setPenColor.accept(editor.penColor);
							dragging = false;
						} else
							editor.drawPixel(px, py, false);
					}
				}
				break;

				case MOVE_UV:
				{
					dragX = px;
					dragY = py;
					TreeElement me = getElementUnderMouse(event);
					if(me != null) {
						me.onClick(gui, editor, event);
						moveStart = new Vec2i();
						Vec3f uv = me.getVec(VecType.TEXTURE);
						moveStart.x = (int) uv.x;
						moveStart.y = (int) uv.y;
					} else editor.selectedElement = null;
					editor.updateGui();
				}
				break;

				default:
					break;
				}
			}
			event.consume();
		}
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(bounds.isInBounds(x, y)) {
			if(dragging) {
				int dx = x - lastMx;
				int dy = y - lastMy;
				ETextures provider = editor.getTextureProvider();

				int r = EditorGui.getRotateMouseButton();
				if(r == 0 ? gui.isShiftDown() : btn == r) {
					offX += dx;
					offY += dy;
					offX = MathHelper.clamp(offX, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.w - 10);
					offY = MathHelper.clamp(offY, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.h - 10);
				} else if(btn == 0) {
					int px = (int) ((x - offX - bounds.x) / zoom);
					int py = (int) ((y - offY - bounds.y) / zoom);

					switch (editor.drawMode.get()) {
					case PEN:
					case RUBBER:
					case FILL:
						if(px >= 0 && py >= 0 && px < provider.getImage().getWidth() && py < provider.getImage().getHeight())
							editor.drawPixel(px, py, false);
						break;

					case MOVE_UV:
					{
						TreeElement me = editor.selectedElement;
						if(me != null && moveStart != null) {
							int xoff = px - dragX;
							int yoff = py - dragY;
							Vec3f uv = me.getVec(VecType.TEXTURE);
							if(Math.abs(xoff) >= uv.z)dragX = px;
							if(Math.abs(yoff) >= uv.z)dragY = py;
							uv.x = MathHelper.clamp(uv.x + xoff / (int) uv.z, 0, provider.provider.size.x);
							uv.y = MathHelper.clamp(uv.y + yoff / (int) uv.z, 0, provider.provider.size.y);
							me.setVecTemp(VecType.TEXTURE, uv);
						}
					}
					break;

					default:
						break;
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
			switch (editor.drawMode.get()) {
			case MOVE_UV:
				endDrag(true);
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
		offX -= ((niw - iw) / 2) * MathHelper.clamp(Math.abs(1-(offX / (bounds.w / 2))), 0, 1);
		offY -= ((nih - ih) / 2) * MathHelper.clamp(Math.abs(1-(offY / (bounds.h / 2))), 0, 1);
		offX = MathHelper.clamp(offX, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.w - 10);
		offY = MathHelper.clamp(offY, (int) (-provider.getImage().getWidth() * zoom + 10), bounds.h - 10);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.matches(gui.getKeyCodes().KEY_ESCAPE) && moveStart != null) {
			event.consume();
			endDrag(false);
		}
		if(!event.isConsumed() && bounds.isInBounds(mouseCursorPos)) {
			if(event.matches("+")) {
				zoom(mouseCursorPos.x, mouseCursorPos.y, 1);
			} else if(event.matches("-")) {
				zoom(mouseCursorPos.x, mouseCursorPos.y, -1);
			}
		}
	}

	private void endDrag(boolean apply) {
		TreeElement me = editor.selectedElement;
		if(me != null && moveStart != null) {
			Vec3f uv = me.getVec(VecType.TEXTURE);
			me.setVecTemp(VecType.TEXTURE, new Vec3f(moveStart.x, moveStart.y, uv.z));
			if(apply)me.setVec(uv, VecType.TEXTURE);
			moveStart = null;
			if(me instanceof TreeSettingElement)
				editor.selectedElement = ((TreeSettingElement)me).getParent();
			editor.updateGui();
		}
	}
}
