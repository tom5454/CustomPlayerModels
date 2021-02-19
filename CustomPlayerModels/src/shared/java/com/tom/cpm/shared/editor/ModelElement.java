package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.RenderedCube;

public class ModelElement extends Cube implements IElem, TreeElement {
	public Editor editor;
	public String name;
	public ModelElement parent;
	public List<ModelElement> children = new ArrayList<>();
	public ElementType type;
	public Object typeData;
	public RenderedCube rc;
	public boolean show = true;
	public boolean texture, mirror;
	public int textureSize;
	public boolean glow;
	public boolean recolor;
	public long storeID;
	public boolean hidden;

	public ModelElement(Editor editor) {
		this(editor, ElementType.NORMAL, null, null);
	}

	public ModelElement(Editor editor, ElementType type, Object typeData, IGui gui) {
		this.type = type;
		this.typeData = typeData;
		this.editor = editor;
		type.buildElement(gui, editor, this, typeData);
	}

	public void preRender() {
		type.preRenderUpdate(this);
		children.forEach(ModelElement::preRender);
	}

	@Override
	public Vec3f getPosition() {
		return pos;
	}

	@Override
	public Vec3f getRotation() {
		return rotation;
	}

	@Override
	public Vec3f getColor() {
		int r = (rgb & 0xff0000) >> 16;
		int g = (rgb & 0x00ff00) >> 8;
		int b =  rgb & 0x0000ff;
		return new Vec3f(r, g, b);
	}

	@Override
	public boolean isVisible() {
		return show;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		children.forEach(c);
	}

	@Override
	public void onClick() {
		editor.selectedElement = this;
	}

	@Override
	public int textColor() {
		return !show ? editor.colors().button_text_disabled : 0;
	}

	@Override
	public int bgColor() {
		return editor.selectedElement == this ? editor.colors().select_background : 0;
	}

	@Override
	public void accept(TreeElement elem) {
		editor.moveElement((ModelElement) elem, this);
	}

	@Override
	public boolean canAccept(TreeElement elem) {
		return elem instanceof ModelElement;
	}

	@Override
	public boolean canMove() {
		return type == ElementType.NORMAL;
	}
}
