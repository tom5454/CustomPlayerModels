package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;

public class SkinTextureDisplay extends GuiElement {
	private Editor editor;
	public SkinTextureDisplay(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		EditorTexture provider = editor.getTextureProvider();
		if(provider != null) {
			gui.pushMatrix();
			gui.setPosOffset(getBounds());
			gui.setupCut();
			gui.drawBox(0, 0, bounds.w, bounds.h, gui.getColors().button_fill);
			provider.bind();
			gui.drawTexture(0, 0, bounds.w, bounds.h, 0, 0, 1, 1);
			float x = bounds.w / (float) provider.size.x;
			float y = bounds.h / (float) provider.size.y;
			drawBoxTextureOverlay(gui, editor, 0, 0, x, y);
			gui.popMatrix();
			gui.setupCut();
		}
	}

	public static void drawBoxTextureOverlay(IGui gui, Editor editor, int x, int y, float xs, float ys) {
		if(editor.drawAllUVs) {
			EditorTexture provider = editor.getTextureProvider();
			Editor.walkElements(editor.elements, e -> {
				if(e.getTexture() == provider) {
					e.drawTexture(gui, x, y, xs, ys);
				}
			});
		} else if(editor.selectedElement != null) {
			editor.selectedElement.drawTexture(gui, x, y, xs, ys);
		}
	}

	public static void drawBoxTextureOverlay(IGui gui, ModelElement element, int x, int y, float xs, float ys, int alpha) {
		if(element.type == ElementType.NORMAL) {
			int ts = Math.abs(element.texSize);
			int bx = (int) (xs * element.u * ts);
			int by = (int) (ys * element.v * ts);
			int dx = MathHelper.ceil(element.size.x * ts);
			int dy = MathHelper.ceil(element.size.y * ts);
			int dz = MathHelper.ceil(element.size.z * ts);
			int a = alpha << 24;
			gui.drawBox(x + bx + dx * xs + dz * xs, y + by + dz * ys, dz * xs, dy * ys, 0xff0000 | a);
			gui.drawBox(x + bx, y + by + dz * ys, dz * xs, dy * ys, 0xdd0000 | a);
			gui.drawBox(x + bx + dz * xs, y + by, dx * xs, dz * ys, 0x00ff00 | a);
			gui.drawBox(x + bx + dz * xs + dx * xs, y + by, dx * xs, dz * ys, 0x00dd00 | a);
			gui.drawBox(x + bx + dz * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000ff | a);
			gui.drawBox(x + bx + dz * xs * 2 + dx * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000dd | a);
		} else if(element.type == ElementType.ROOT_PART && element.typeData instanceof PlayerModelParts) {
			PlayerPartValues val = PlayerPartValues.getFor((PlayerModelParts) element.typeData, element.editor.skinType);
			EditorTexture provider = element.getTexture();
			xs = xs * provider.size.x / 64f;
			ys = ys * provider.size.y / 64f;

			int bx = (int) (xs * val.u);
			int by = (int) (ys * val.v);
			int dx = MathHelper.ceil(val.sx);
			int dy = MathHelper.ceil(val.sy);
			int dz = MathHelper.ceil(val.sz);
			int a = alpha << 24;
			gui.drawBox(x + bx + dx * xs + dz * xs, y + by + dz * ys, dz * xs, dy * ys, 0xff0000 | a);
			gui.drawBox(x + bx, y + by + dz * ys, dz * xs, dy * ys, 0xdd0000 | a);
			gui.drawBox(x + bx + dz * xs, y + by, dx * xs, dz * ys, 0x00ff00 | a);
			gui.drawBox(x + bx + dz * xs + dx * xs, y + by, dx * xs, dz * ys, 0x00dd00 | a);
			gui.drawBox(x + bx + dz * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000ff | a);
			gui.drawBox(x + bx + dz * xs * 2 + dx * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000dd | a);
		}
	}

}
