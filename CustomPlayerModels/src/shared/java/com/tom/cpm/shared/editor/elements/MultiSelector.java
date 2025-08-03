package com.tom.cpm.shared.editor.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Effect;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.gui.TextureDisplay;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.VecType;

public interface MultiSelector extends TreeElement {
	boolean add(TreeElement modelElement);
	void forEachSelected(Consumer<TreeElement> c);

	@Override
	default String getName() {
		return "";
	}

	public static class ElementImpl implements MultiSelector {
		private List<ModelElement> elements = new ArrayList<>();
		private final Editor editor;

		public ElementImpl(Editor editor) {
			this.editor = editor;
		}

		private void addImpl(ModelElement modelElement) {
			if(elements.contains(modelElement))
				elements.remove(modelElement);
			else
				elements.add(modelElement);
		}

		@Override
		public boolean isSelected(Editor e, TreeElement other) {
			return elements.contains(other);
		}

		public boolean allMatch(Predicate<ModelElement> test) {
			for (int i = 0; i < elements.size(); i++) {
				ModelElement e = elements.get(i);
				if (!test.test(e))return false;
			}
			return true;
		}

		@Override
		public void updateGui() {
			if (elements.isEmpty())return;
			ModelElement first = elements.get(0);
			if (allMatch(e -> e.type == ElementType.NORMAL)) {
				editor.setOffset.accept(getVec(VecType.OFFSET));
				if (allMatch(e -> e.itemRenderer == null)) {
					if (allMatch(e -> e.texture)) {
						if (allMatch(e -> e.faceUV == null)) {
							editor.setModeBtn.accept(editor.ui.i18nFormat("button.cpm.mode.tex"));
							editor.setModePanel.accept(ModeDisplayType.TEX);
							editor.setTexturePanel.accept(getVecUV());
							editor.setReColor.accept(first.recolor);
							if (allMatch(e -> e.recolor)) {
								editor.setPartColor.accept(first.rgb);
							}
						}
					} else {
						editor.setModeBtn.accept(editor.ui.i18nFormat("button.cpm.mode.color"));
						editor.setModePanel.accept(ModeDisplayType.COLOR);
						editor.setPartColor.accept(first.rgb);
					}
					double mc = elements.stream().mapToDouble(e -> e.mcScale).average().orElse(0);
					editor.setMCScale.accept((float) mc);
				}
				editor.setDelEn.accept(true);
				editor.setHiddenEffect.accept(first.hidden);
				editor.setGlow.accept(first.glow);
				editor.setMirror.accept(first.mirror);
				editor.setSingleTex.accept(first.singleTex);
			}
			editor.setPosition.accept(getVec(VecType.POSITION));
			editor.setRot.accept(getVec(VecType.ROTATION));
		}

		@Override
		public void setElemColor(int color) {
			elements.forEach(e -> e.setElemColor(color));
		}

		@Override
		public void setVec(Vec3f v, VecType object) {
			if(object == VecType.TEXTURE) {
				Vec3i uv = getVecUV();
				float uOff = v.x - uv.x;
				float vOff = v.y - uv.y;
				elements.forEach(e -> e.setVec(new Vec3f(e.u + uOff, e.v + vOff, e.textureSize), object));
			} else {
				Vec3f s = getVec(object);
				Vec3f off = v.sub(s);
				elements.forEach(e -> e.setVec(getVec(e, object).add(off), object));
			}
		}

		public Vec3f getVecAnim(Function<ModelElement, Vec3f> getter) {
			return elements.stream().map(getter).reduce(new Vec3f(), Vec3f::add).mul(1f / elements.size());
		}

		public void setVecAnim(Vec3f v, Function<ModelElement, Vec3f> getter, BiConsumer<ModelElement, Vec3f> setter) {
			Vec3f s = elements.stream().map(getter).reduce(new Vec3f(), Vec3f::add).mul(1f / elements.size());
			Vec3f off = v.sub(s);
			elements.forEach(e -> setter.accept(e, getter.apply(e).add(off)));
		}

		@Override
		public void delete() {
			elements.forEach(ModelElement::delete);
		}

		@Override
		public void switchVis() {
			elements.forEach(ModelElement::switchVis);
		}

		@Override
		public void switchEffect(Effect effect) {
			elements.forEach(e -> e.switchEffect(effect));
		}

		@Override
		public void modeSwitch() {
			elements.forEach(ModelElement::modeSwitch);
		}

		@Override
		public void setMCScale(float scale) {
			float mc = (float) (scale - elements.stream().mapToDouble(e -> e.mcScale).average().orElse(0));
			elements.forEach(e -> e.setMCScale(e.mcScale + mc));
		}

		@Override
		public Vec3f getVec(VecType type) {
			if(type == VecType.TEXTURE) {
				Vec3i uv = getVecUV();
				return new Vec3f(uv.x, uv.y, uv.z);
			}
			return elements.stream().map(e -> getVec(e, type)).reduce(new Vec3f(), Vec3f::add).mul(1f / elements.size());
		}

		public Vec3i getVecUV() {
			return elements.stream().map(e -> new Vec3i(e.u, e.v, e.textureSize)).
					reduce(new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, 256), (a, b) -> new Vec3i(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z)));
		}

		public Vec3f getVec(ModelElement e, VecType type) {
			switch (type) {
			case OFFSET:
				return e.offset;
			case POSITION:
				return e.getPosition();
			case ROTATION:
				return e.getRotation();
			case MESH_SCALE:
				return e.meshScale;
			case SIZE:
				return e.size;
			case TEXTURE:
			default:
				break;
			}
			return null;
		}

		@Override
		public ETextures getTexture() {
			List<ETextures> texs = elements.stream().map(ModelElement::getTexture).distinct().collect(Collectors.toList());
			return texs.size() == 1 ? texs.get(0) : null;
		}

		@Override
		public Box getTextureBox() {
			return elements.stream().map(ModelElement::getTextureBox).filter(e -> e != null).reduce(null, (a, b) -> a == null ? b : (b == null ? a : a.union(b)));
		}

		@Override
		public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
			elements.forEach(e -> TextureDisplay.drawBoxTextureOverlay(gui, e, x, y, xs, ys, TextureDisplay.getAlphaForBox(true)));
		}

		@Override
		public void setVecTemp(VecType object, Vec3f v) {
			if(object == VecType.TEXTURE) {
				Vec3i uv = getVecUV();
				float uOff = v.x - uv.x;
				float vOff = v.y - uv.y;
				elements.forEach(e -> e.setVecTemp(object, new Vec3f(e.u + uOff, e.v + vOff, e.textureSize)));
			} else {
				Vec3f s = getVec(object);
				Vec3f off = v.sub(s);
				elements.forEach(e -> e.setVecTemp(object, getVec(e, object).add(off)));
			}
		}

		@Override
		public boolean add(TreeElement elem) {
			if(elem instanceof ModelElement)
				addImpl((ModelElement) elem);
			return elements.isEmpty();
		}

		@Override
		public void forEachSelected(Consumer<TreeElement> c) {
			elements.forEach(c);
		}

		public ModelElement getFirst() {
			return elements.get(0);
		}

		public boolean hasElements() {
			return !elements.isEmpty();
		}
	}
}
