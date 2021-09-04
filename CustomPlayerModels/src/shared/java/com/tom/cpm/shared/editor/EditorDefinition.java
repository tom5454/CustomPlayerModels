package com.tom.cpm.shared.editor;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.BoxRender;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.PaintImageCreator;

public class EditorDefinition extends ModelDefinition {
	private Editor editor;
	private TextureProvider paint = new TextureProvider(PaintImageCreator.createImage(), null) {

		@Override
		public Vec2i getSize() {
			ETextures tex = editor.getTextureProvider();
			if(tex != null)return tex.provider.size;
			return new Vec2i(64, 64);
		}
	};

	public EditorDefinition(Editor editor) {
		this.editor = editor;
	}

	@Override
	public PartRoot getModelElementFor(VanillaModelPart part) {
		PartRoot root = new PartRoot();
		editor.elements.forEach(e -> {
			RootModelElement el = (RootModelElement) e.rc;
			if(el.getPart() == part) {
				root.add(el);
				if(!e.duplicated)root.setMainRoot(el);
			}
		});
		return root;
	}

	@Override
	public boolean isEditor() {
		return true;
	}

	@Override
	public TextureProvider getTexture(TextureSheetType key, boolean inGui) {
		if(editor.renderPaint)return paint;
		else {
			ETextures tex = editor.textures.get(key);
			return tex != null ? tex.getRenderTexture() : null;
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();
		paint.free();
	}

	@Override
	public SkinType getSkinType() {
		return editor.skinType;
	}

	@Override
	public boolean hasRoot(VanillaModelPart type) {
		return editor.elements.stream().map(e -> ((RootModelElement) e.rc).getPart()).anyMatch(t -> t == type);
	}

	public void render(MatrixStack stack, VBuffers buf, RenderTypes<RenderMode> renderTypes, RenderedCube cube) {
		if(editor.renderPaint)return;
		editor.render(stack, buf);
		drawSelect(cube, stack, buf, renderTypes);
	}

	public void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, VBuffers bufferIn, RenderTypes<RenderMode> renderTypes) {
		ElementSelectMode sel = cube.getSelected();
		if(sel.isRenderOutline()) {
			boolean s = sel == ElementSelectMode.SELECTED;
			if(s)BoxRender.drawOrigin(matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE), 1);
			Cube c = cube.getCube();
			if(c.size == null || c.size.x != 0 || c.size.y != 0 || c.size.z != 0 || c.mcScale != 0)
				BoxRender.drawBoundingBox(
						matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE),
						cube.getBounds(),
						s ? 1 : 0.5f, s ? 1 : 0.5f, s ? 1 : 0, 1
						);
		}
	}

	@Override
	public boolean isHideHeadIfSkull() {
		return editor.hideHeadIfSkull;
	}

	@Override
	public boolean isRemoveArmorOffset() {
		return editor.removeArmorOffset;
	}
}
