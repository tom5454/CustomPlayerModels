package com.tom.cpm.shared.editor.gui;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.math.MathHelper;
import com.tom.cpm.shared.skin.TextureProvider;

public class SkinTextureDisplay extends GuiElement {
	private Editor editor;
	public SkinTextureDisplay(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		TextureProvider provider = editor.getTextureProvider();
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
		if(editor.selectedElement != null) {
			editor.selectedElement.drawTexture(gui, x, y, xs, ys);
		}
	}

	public static void drawBoxTextureOverlay(IGui gui, ModelElement element, int x, int y, float xs, float ys) {
		if(element.type == ElementType.NORMAL) {
			int ts = Math.abs(element.texSize);
			int bx = (int) (xs * element.u * ts);
			int by = (int) (ys * element.v * ts);
			int dx = MathHelper.ceil(element.size.x * ts);
			int dy = MathHelper.ceil(element.size.y * ts);
			int dz = MathHelper.ceil(element.size.z * ts);
			gui.drawBox(x + bx + dx * xs + dz * xs, y + by + dz * ys, dz * xs, dy * ys, 0xccff0000);
			gui.drawBox(x + bx, y + by + dz * ys, dz * xs, dy * ys, 0xccdd0000);
			gui.drawBox(x + bx + dz * xs, y + by, dx * xs, dz * ys, 0xcc00ff00);
			gui.drawBox(x + bx + dz * xs + dx * xs, y + by, dx * xs, dz * ys, 0xcc00dd00);
			gui.drawBox(x + bx + dz * xs, y + by + dz * ys, dx * xs, dy * ys, 0xcc0000ff);
			gui.drawBox(x + bx + dz * xs * 2 + dx * xs, y + by + dz * ys, dx * xs, dy * ys, 0xcc0000dd);
		}
	}
}
