package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.TriangleBoundingBox;
import com.tom.cpl.math.TriangleBoundingBox.BoxBuilder;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.OptionalBuffer;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.BoxRender;
import com.tom.cpm.shared.model.render.GuiModelRenderManager.RedirectPartRenderer;
import com.tom.cpm.shared.model.render.Mesh;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class EditorDefinition extends ModelDefinition {
	private Editor editor;
	public List<EditorRenderer.Bounds> bounds = new ArrayList<>();
	public ViewportPanel renderingPanel;

	public EditorDefinition(Editor editor) {
		this.editor = editor;
		this.setScale(new ScaleData(new HashMap<>()) {

			@Override
			public Vec3f getRPos() {
				return editor.scalingElem.pos;
			}

			@Override
			public Vec3f getRRotation() {
				return editor.scalingElem.rotation;
			}

			@Override
			public Vec3f getRScale() {
				return editor.scalingElem.scale;
			}
		});
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
		if(root.isEmpty())return null;
		return root;
	}

	@Override
	public boolean isEditor() {
		return true;
	}

	@Override
	public TextureProvider getTexture(TextureSheetType key, boolean inGui) {
		ETextures tex = editor.textures.get(key);
		return tex != null ? tex.getRenderTexture() : null;
	}

	@Override
	public void cleanup() {
		super.cleanup();
	}

	@Override
	public SkinType getSkinType() {
		return editor.skinType;
	}

	@Override
	public boolean hasRoot(VanillaModelPart type) {
		return editor.elements.stream().map(e -> ((RootModelElement) e.rc).getPart()).anyMatch(t -> t == type);
	}

	public void render(RedirectPartRenderer renderer, MatrixStack stack, VBuffers buf, RenderTypes<RenderMode> renderTypes, RenderedCube cube) {
		Mesh mesh = cube.renderObject;
		EditorRenderer.Bounds b = new EditorRenderer.Bounds();
		b.elem = (ModelElement) cube.getCube();
		BoxBuilder builder = TriangleBoundingBox.builder();
		if(mesh != null) {
			mesh.draw(stack, builder, 0, 0, 0, 0);
		} else {
			if(b.elem.type == ElementType.ROOT_PART && cube.doDisplay()) {
				renderer.renderParent(builder);
			}
		}
		b.bb = builder.build();
		addBounds(b);
		drawSelect(cube, stack, buf, renderTypes);
		Cube c = cube.getCube();
		if(c.size == null || c.size.x != 0 || c.size.y != 0 || c.size.z != 0 || c.mcScale != 0) {
			OptionalBuffer vb = new OptionalBuffer(buf.getBuffer(renderTypes, RenderMode.OUTLINE));
			b.drawHover = vb;
			BoxRender.drawBoundingBox(
					stack, vb,
					cube.getBounds(),
					1, 0, 0, 1
					);
		}
	}

	public void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, VBuffers bufferIn, RenderTypes<RenderMode> renderTypes) {
		ElementSelectMode sel = cube.getSelected();
		if(sel.isRenderOutline()) {
			boolean s = sel == ElementSelectMode.SELECTED;
			if(s)BoxRender.drawOrigin(matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE), 0, 0, 0, 1);
			Cube c = cube.getCube();
			if(c.size == null || c.size.x != 0 || c.size.y != 0 || c.size.z != 0 || c.mcScale != 0)
				BoxRender.drawBoundingBox(
						matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE),
						cube.getBounds(),
						s ? 1 : 0.5f, s ? 1 : 0.5f, s ? 1 : 0, 1
						);
			if(renderingPanel != null) {
				if(s && c.offset != null && c.size != null && renderingPanel.draggingVec != null) {
					EditorRenderer.drawDrag(matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE), renderingPanel.draggingVec,
							(bt, bb) -> {
								EditorRenderer.Bounds b = new EditorRenderer.Bounds();
								b.elem = (ModelElement) cube.getCube();
								b.bb = bb.build();
								b.type = bt;
								addBounds(b);
								OptionalBuffer vb1 = new OptionalBuffer(bufferIn.getBuffer(renderTypes, RenderMode.COLOR));
								OptionalBuffer vb2 = new OptionalBuffer(bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE));
								b.drawHover = () -> {vb1.run();vb2.run();};
								return new VertexBuffer[] {vb1, vb2};
							}, cube, renderingPanel.oldValue == null ? null : renderingPanel.oldValue[0]);
				}
				if(renderingPanel.draggingElement == cube.getCube() && renderingPanel.draggingVec != null) {
					EditorRenderer.Bounds b = new EditorRenderer.Bounds();
					b.elem = (ModelElement) cube.getCube();
					b.bb = EditorRenderer.drawDragPane(matrixStackIn, bufferIn.getBuffer(renderTypes, RenderMode.OUTLINE), renderingPanel.draggingType, renderingPanel.draggingVec, cube, 48, renderingPanel.oldValue[0]).build(true);
					b.type = EditorRenderer.BoundType.DRAG_PANE;
					addBounds(b);
				}
			}
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

	public void addBounds(EditorRenderer.Bounds b) {
		if(renderingPanel != null)renderingPanel.finishTransform(b);
		bounds.add(b);
	}

	public EditorRenderer.Bounds select() {
		return bounds.stream().filter(b -> Float.isFinite(b.bb.isHovered())).
				max(Comparator.comparingDouble(b -> (b.type == EditorRenderer.BoundType.CLICK ? 0 : (b.type == EditorRenderer.BoundType.DRAG_PANE ? 2 : 1)) * 100 - b.bb.isHovered())).orElse(null);
	}

	public void preRender() {
		itemTransforms.clear();
		bounds.clear();
	}
}
