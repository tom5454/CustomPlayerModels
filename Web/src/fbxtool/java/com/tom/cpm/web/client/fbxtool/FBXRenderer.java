package com.tom.cpm.web.client.fbxtool;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorDefinition;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.VanillaPartRenderer;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.DirectModelRenderManager;
import com.tom.cpm.shared.model.render.DirectPartValues;
import com.tom.cpm.shared.model.render.DirectParts;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class FBXRenderer extends DirectModelRenderManager<FBXCreator> {

	@Override
	protected DirectHolderPlayer<FBXCreator> createHolder(VanillaPlayerModel model, String arg) {
		return new FBXHolder(this, model, arg);
	}

	@Override
	protected DirectPartRenderer<FBXCreator> createRenderer(DirectHolderPlayer<FBXCreator> access,
			Supplier<VanillaPartRenderer> modelPart, VanillaModelPart part) {
		return new FBXPartRenderer(access, modelPart, part);
	}

	public class FBXHolder extends DirectHolderPlayer<FBXCreator> {

		public FBXHolder(ModelRenderManager<VBuffers, FBXCreator, VanillaPartRenderer, VanillaPlayerModel> mngr,
				VanillaPlayerModel model, String arg) {
			super(mngr, model, arg);
		}

		@Override
		protected void setupRenderSystem(FBXCreator cbi, TextureSheetType tex) {
			cbi.putRenderTypes(renderTypes);
		}

		@Override
		protected void bindTexture(FBXCreator cbi, TextureProvider skin, TextureSheetType tex) {
			cbi.loadSkin(skin, tex);
		}

		@Override
		protected void bindDefaultTexture(FBXCreator cbi, TextureSheetType tex) {
			cbi.loadFallback(tex);
		}
	}

	public class FBXPartRenderer extends DirectPartRenderer<FBXCreator> {

		public FBXPartRenderer(DirectHolderPlayer<FBXCreator> holder, Supplier<VanillaPartRenderer> parent,
				VanillaModelPart part) {
			super(holder, parent, part);
		}

		@Override
		public void doRender0(RootModelElement elem, boolean doRender) {
			FBXStack stack = new FBXStack((FBXStack) this.stack);
			stack.push();
			holder.transform(stack, holder.def.getScale());
			translateRotate(stack);
			VBuffers buf = getVBuffers();
			Vec4f color = getColor();
			stack.pushBone = true;
			render(elem, stack, buf, color.x, color.y, color.z, color.w, true, true);
			stack.pushBone = false;
			stack.pop();
		}

		@Override
		public void translateRotate(RenderedCube rc, MatrixStack st) {
			super.translateRotate(rc, st);
			((FBXStack)st).initBone(rc);
		}
	}

	public static class FBXPartRoot extends PartRoot {
		private final PartRoot r;
		private FBXStack ms;

		public FBXPartRoot(PartRoot r, FBXStack ms) {
			this.r = r;
			this.ms = ms;
			setMainRoot(r.getMainRoot());
			r.forEach(this::add);
		}

		@Override
		public void forEach(Consumer<? super RootModelElement> action) {
			super.forEach(e -> {
				ms.b.push();
				ms.initBone(e);
				action.accept(e);
				ms.b.pop();
			});
		}

		@Override
		public void setRootPosAndRot(float px, float py, float pz, float rx, float ry, float rz) {
			r.setRootPosAndRot(px, py, pz, rx, ry, rz);
		}
	}

	public static class FBXDefinition extends EditorDefinition {
		private FBXStack stack;
		private Editor editor;

		public FBXDefinition(Editor editor) {
			super(editor);
			this.editor = editor;
		}

		@Override
		public PartRoot getModelElementFor(VanillaModelPart part) {
			PartRoot r = super.getModelElementFor(part);
			return stack != null ? new FBXPartRoot(r, stack) : r;
		}

		public void setStack(FBXStack stack) {
			this.stack = stack;
		}

		public void renderPre() {
			editor.preRender();
			editor.elements.forEach(e -> e.children.forEach(this::preRender));
		}

		private void preRender(ModelElement e) {
			e.rc.display = true;
			e.children.forEach(this::preRender);
		}
	}

	public static class FBXStack extends MatrixStack {
		private GeometryBuffer b;
		private boolean pushBone;

		public FBXStack(GeometryBuffer b) {
			this.b = b;
		}

		public FBXStack(FBXStack s) {
			this.b = s.b;
		}

		@Override
		public void push() {
			super.push();
			if(pushBone)b.push();
		}

		public void initBone(RenderedCube rc) {
			Cube c = rc.getCube();
			if(c instanceof ModelElement)
				b.getBone().name = ((ModelElement) c).getName();
			if(rc instanceof RootModelElement) {
				RootModelElement re = (RootModelElement) rc;
				VanillaModelPart part = re.getPart();
				SkinType s = c instanceof ModelElement ? ((ModelElement)c).editor.skinType : SkinType.DEFAULT;
				PartValues pv = DirectParts.getPartOverrides(part, s);
				Vec3f pos = pv.getPos().add(rc.pos);
				Vec3f rot = rc.rotation.asVec3f(false);
				if(pv instanceof DirectPartValues) {
					rot = rot.add(((DirectPartValues)pv).getRotation());
				}
				b.setBoneAbsolutePos(pos.x, pos.y, pos.z);
				b.getBone().rotation.set(rot);
			} else {
				/*Quaternion q = new Quaternion(getLast().getMatrix());
				Vec4f p = new Vec4f(rc.pos, 1);
				p.transform(new Mat4f(q));
				b.getBone().position.set(p);
				q.mul(rc.rotation.asQ());
				b.getBone().rotation.set(q);*/
				//b.getBone().position.set(rc.pos);
				//b.getBone().rotation.set(rc.rotation.asVec3f(false));
				Vec4f p = new Vec4f(0, 0, 0, 1);
				p.transform(getLast().getMatrix());
				p.mul(16);
				b.setBoneAbsolutePos(p.x, p.y, p.z);
				if(c instanceof ModelElement)
					b.getBone().hidden = ((ModelElement)c).hidden;
			}
		}

		@Override
		public void pop() {
			super.pop();
			if(pushBone)b.pop();
		}
	}
}
