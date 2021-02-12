package com.tom.cpm.shared.editor.gui;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.math.MathHelper;

public class SkinTextureDisplay extends GuiElement {
	private Editor editor;
	public SkinTextureDisplay(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if(editor.skinProvider.texture != null) {
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_fill);
			editor.skinProvider.bind();
			gui.drawTexture(bounds.x, bounds.y, bounds.w, bounds.h, 0, 0, 1, 1);
			float x = bounds.w / (float) editor.skinProvider.size.x;
			float y = bounds.h / (float) editor.skinProvider.size.y;
			drawBoxTextureOverlay(gui, editor, bounds.x, bounds.y, x, y);
		}
	}

	public static void drawBoxTextureOverlay(IGui gui, Editor editor, int x, int y, float xs, float ys) {
		if(editor.selectedElement != null && editor.selectedElement.type == ElementType.NORMAL) {
			int ts = Math.abs(editor.selectedElement.texSize);
			int bx = (int) (xs * editor.selectedElement.u * ts);
			int by = (int) (ys * editor.selectedElement.v * ts);
			int dx = MathHelper.ceil(editor.selectedElement.size.x * ts);
			int dy = MathHelper.ceil(editor.selectedElement.size.y * ts);
			int dz = MathHelper.ceil(editor.selectedElement.size.z * ts);
			gui.drawBox(x + bx + dx * xs + dz * xs, y + by + dz * ys, dz * xs, dy * ys, 0xccff0000);
			gui.drawBox(x + bx, y + by + dz * ys, dz * xs, dy * ys, 0xccdd0000);
			gui.drawBox(x + bx + dz * xs, y + by, dx * xs, dz * ys, 0xcc00ff00);
			gui.drawBox(x + bx + dz * xs + dx * xs, y + by, dx * xs, dz * ys, 0xcc00dd00);
			gui.drawBox(x + bx + dz * xs, y + by + dz * ys, dx * xs, dy * ys, 0xcc0000ff);
			gui.drawBox(x + bx + dz * xs * 2 + dx * xs, y + by + dz * ys, dx * xs, dy * ys, 0xcc0000dd);
		}
	}
}
