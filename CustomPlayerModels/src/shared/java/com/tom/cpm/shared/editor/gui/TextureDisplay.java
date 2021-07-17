package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.RootGroups;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class TextureDisplay extends GuiElement {
	private Editor editor;
	public TextureDisplay(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		ETextures provider = editor.getTextureProvider();
		if(provider != null) {
			gui.pushMatrix();
			gui.setPosOffset(getBounds());
			gui.setupCut();
			gui.drawBox(0, 0, bounds.w, bounds.h, gui.getColors().button_fill);
			provider.provider.bind();
			gui.drawTexture(0, 0, bounds.w, bounds.h, 0, 0, 1, 1);
			float x = bounds.w / (float) provider.provider.size.x;
			float y = bounds.h / (float) provider.provider.size.y;
			drawBoxTextureOverlay(gui, editor, 0, 0, x, y);
			gui.popMatrix();
			gui.setupCut();
		}
	}

	public static void drawBoxTextureOverlay(IGui gui, Editor editor, int x, int y, float xs, float ys) {
		if(editor.drawAllUVs) {
			ETextures provider = editor.getTextureProvider();
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
			int a = alpha << 24;
			if(element.singleTex) {
				Box bx = element.getTextureBox();
				gui.drawBox(x + bx.x * xs, y + bx.y * ys, bx.w * xs, bx.h * ys, 0xffffff | a);
			} else if(element.faceUV != null) {
				if(element.faceUV.contains(element.editor.perfaceFaceDir)) {
					Vec4f vec = element.faceUV.getVec(element.editor.perfaceFaceDir);
					float su = Math.min(vec.x, vec.z);
					float sv = Math.min(vec.y, vec.w);
					float eu = Math.max(vec.x, vec.z);
					float ev = Math.max(vec.y, vec.w);
					gui.drawBox(x + su * xs, y + sv * ys, (eu - su) * xs, (ev - sv) * ys, 0xffffff | a);
				}
			} else {
				int ts = Math.abs(element.texSize);
				int bx = (int) (xs * element.u * ts);
				int by = (int) (ys * element.v * ts);
				int dx = MathHelper.ceil(element.size.x * ts);
				int dy = MathHelper.ceil(element.size.y * ts);
				int dz = MathHelper.ceil(element.size.z * ts);
				gui.drawBox(x + bx + dx * xs + dz * xs, y + by + dz * ys, dz * xs, dy * ys, 0xff0000 | a);
				gui.drawBox(x + bx, y + by + dz * ys, dz * xs, dy * ys, 0xdd0000 | a);
				gui.drawBox(x + bx + dz * xs, y + by, dx * xs, dz * ys, 0x00ff00 | a);
				gui.drawBox(x + bx + dz * xs + dx * xs, y + by, dx * xs, dz * ys, 0x00dd00 | a);
				gui.drawBox(x + bx + dz * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000ff | a);
				gui.drawBox(x + bx + dz * xs * 2 + dx * xs, y + by + dz * ys, dx * xs, dy * ys, 0x0000dd | a);
			}
		} else if(element.type == ElementType.ROOT_PART) {
			PartValues val = ((VanillaModelPart) element.typeData).getDefaultSize(element.editor.skinType);
			float texW = 64;
			float texH = 64;
			if(element.typeData instanceof RootModelType) {
				RootGroups gr = RootGroups.getGroup((RootModelType) element.typeData);
				if(gr != null) {
					TextureSheetType sheet = gr.getTexSheet((RootModelType) element.typeData);
					Vec2i size = sheet.getDefSize();
					texW = size.x;
					texH = size.y;
				}
			}
			ETextures provider = element.getTexture();
			xs = xs * provider.provider.size.x / texW;
			ys = ys * provider.provider.size.y / texH;
			Vec2i uv = val.getUV();
			Vec3f size = val.getSize();

			int bx = (int) (xs * uv.x);
			int by = (int) (ys * uv.y);
			int dx = MathHelper.ceil(size.x);
			int dy = MathHelper.ceil(size.y);
			int dz = MathHelper.ceil(size.z);
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
