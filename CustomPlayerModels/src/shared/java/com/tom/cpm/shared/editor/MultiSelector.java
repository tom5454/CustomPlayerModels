package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.gui.TextureDisplay;
import com.tom.cpm.shared.editor.tree.TreeElement;

public class MultiSelector implements TreeElement {
	private List<ModelElement> elements = new ArrayList<>();
	private final Editor editor;

	public MultiSelector(Editor editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return "";
	}

	public boolean add(ModelElement modelElement) {
		if(elements.contains(modelElement))
			elements.remove(modelElement);
		else
			elements.add(modelElement);
		return elements.isEmpty();
	}

	@Override
	public boolean isSelected(Editor e, TreeElement other) {
		return elements.contains(other);
	}

	@Override
	public void updateGui() {
		if(elements.stream().noneMatch(e -> e.type == ElementType.ROOT_PART)) {
			editor.setOffset.accept(getVec(VecType.OFFSET));
			if(elements.stream().allMatch(e -> e.itemRenderer == null && e.texture && e.faceUV == null)) {
				editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.mode.tex"));
				editor.setModePanel.accept(ModeDisplayType.TEX);
				editor.setTexturePanel.accept(getVecUV());
			} else if(elements.stream().allMatch(e -> e.itemRenderer == null && !e.texture)) {
				editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.mode.color"));
				editor.setModePanel.accept(ModeDisplayType.COLOR);
				editor.setPartColor.accept(0);
			}
			editor.setDelEn.accept(true);
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
	}

	@Override
	public void setMCScale(float scale) {
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
		Vec3i uv = elements.stream().map(e -> new Vec3i(e.u, e.v, e.textureSize)).
				reduce(new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, 256), (a, b) -> new Vec3i(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z)));
		return new Vec3i(uv.x, uv.y, uv.z);
	}

	public Vec3f getVec(ModelElement e, VecType type) {
		switch (type) {
		case OFFSET:
			return e.offset;
		case POSITION:
			return e.getPosition();
		case ROTATION:
			return e.getRotation();
		case SCALE:
			return e.scale;
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
}
