package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplType;
import com.tom.cpm.shared.editor.gui.TextureDisplay;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.util.ValueOp;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class ModelElement extends Cube implements IElem, TreeElement {
	private static boolean movePopupShown = false;
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
	public boolean templateElement, generated;
	public boolean duplicated;

	public ModelElement(ModelElement element, ModelElement parent) {
		this(element.editor);
		this.parent = parent;
		element.children.forEach(c -> children.add(new ModelElement(c, this)));
		name = editor.gui().i18nFormat("label.cpm.dup", element.name);
		show = element.show;
		texture = element.texture;
		textureSize = element.textureSize;
		offset = new Vec3f(element.offset);
		pos = new Vec3f(element.pos);
		rotation = new Vec3f(element.rotation);
		size = new Vec3f(element.size);
		scale = new Vec3f(element.scale);
		u = element.u;
		v = element.v;
		rgb = element.rgb;
		mirror = element.mirror;
		mcScale = element.mcScale;
		glow = element.glow;
		recolor = element.recolor;
		hidden = element.hidden;
	}

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
		String name = this.name;
		if(hidden)name = editor.gui().i18nFormat("label.cpm.tree.hidden", name);
		if(duplicated)name = editor.gui().i18nFormat("label.cpm.tree.duplicated", name);
		return name;
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		for (ModelElement e : children) {
			if(!e.templateElement)
				c.accept(e);
		}
	}

	@Override
	public int textColor() {
		return !show ? editor.colors().button_text_disabled : 0;
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

	@Override
	public void setVec(Vec3f v, VecType object) {
		if(type == ElementType.ROOT_PART && generated && object == VecType.POSITION && !movePopupShown) {
			movePopupShown = true;
			editor.frame.openPopup(new MessagePopup(editor.gui(), editor.gui().i18nFormat("label.cpm.info"), editor.gui().i18nFormat("label.cpm.warnMoveGenPart")));
		}
		editor.setVec(this, v, object);
	}

	@Override
	public void setElemName(String name) {
		this.name = name;
	}

	@Override
	public String getElemName() {
		return name;
	}

	@Override
	public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
		if(show || editor.selectedElement == this)
			TextureDisplay.drawBoxTextureOverlay(gui, this, x, y, xs, ys, editor.selectedElement == this ? 0xcc : 0x55);
	}

	@Override
	public EditorTexture getTexture() {
		return editor.skinProvider;
	}

	@Override
	public void modeSwitch() {
		editor.addUndo(new ValueOp<>(this, this.texture, (a, b) -> a.texture = b));
		this.texture = !this.texture;
		editor.currentOp = new ValueOp<>(this, this.texture, (a, b) -> a.texture = b);
		editor.setModeBtn.accept(this.texture ? editor.gui().i18nFormat("button.cpm.mode.tex") : editor.gui().i18nFormat("button.cpm.mode.color"));
		editor.setModePanel.accept(texture ? ModeDisplType.TEX : ModeDisplType.COLOR);
		editor.setTexturePanel.accept(this.texture ? new Vec3i(this.u, this.v, this.textureSize) : new Vec3i(this.rgb, 0, 0));
		if(!this.texture || this.recolor)
			editor.setPartColor.accept(this.rgb);
		else
			editor.setPartColor.accept(null);
		editor.markDirty();
	}

	@Override
	public void updateGui() {
		editor.setVis.accept(this.show);
		editor.setAddEn.accept(!templateElement);
		switch(this.type) {
		case NORMAL:
			editor.setOffset.accept(this.offset);
			editor.setRot.accept(this.rotation);
			editor.setPosition.accept(this.pos);
			editor.setSize.accept(this.size);
			editor.setScale.accept(this.scale);
			editor.setMCScale.accept(this.mcScale);
			editor.setMirror.accept(this.mirror);
			editor.setModeBtn.accept(this.texture ? editor.gui().i18nFormat("button.cpm.mode.tex") : editor.gui().i18nFormat("button.cpm.mode.color"));
			editor.setModePanel.accept(texture ? ModeDisplType.TEX : ModeDisplType.COLOR);
			editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize));
			if(!this.texture || this.recolor)
				editor.setPartColor.accept(this.rgb);
			editor.setDelEn.accept(!templateElement);
			editor.setGlow.accept(this.glow);
			editor.setReColor.accept(this.recolor);
			editor.setHiddenEffect.accept(this.hidden);
			editor.updateName.accept(this.name);
			break;

		case ROOT_PART:
			editor.setPosition.accept(this.pos);
			editor.setRot.accept(this.rotation);
			editor.setHiddenEffect.accept(!this.show);
			editor.setDelEn.accept(this.duplicated);
			break;

		default:
			break;
		}
	}

	@Override
	public void addNew() {
		if(templateElement)return;
		ModelElement elem = new ModelElement(editor);
		editor.addUndo(() -> {
			children.remove(elem);
			editor.selectedElement = null;
		});
		editor.runOp(() -> children.add(elem));
		elem.parent = this;
		editor.selectedElement = elem;
		editor.markDirty();
		editor.updateGui();
	}

	@Override
	public void delete() {
		if(templateElement)return;
		if(type == ElementType.NORMAL) {
			editor.addUndo(() -> {
				if(parent != null) {
					parent.children.add(this);
				}
			});
			editor.runOp(() -> {
				if(parent != null) {
					parent.children.remove(this);
				}
				editor.selectedElement = null;
			});
			editor.markDirty();
			editor.updateGui();
		} else if(duplicated) {
			editor.addUndo(() -> editor.elements.add(this));
			editor.runOp(() -> {
				editor.elements.remove(this);
				editor.selectedElement = null;
			});
			editor.markDirty();
			editor.updateGui();
		}
	}

	@Override
	public void setElemColor(int color) {
		editor.updateValueOp(this, rgb, color, (a, b) -> a.rgb = b, editor.setPartColor);
	}

	@Override
	public void setMCScale(float value) {
		editor.addUndo(new ValueOp<>(this, this.mcScale, (a, b) -> a.mcScale = b));
		this.mcScale = value;
		if(this.mcScale > 7) {
			this.mcScale = 7;
			editor.setMCScale.accept(this.mcScale);
		}
		if(this.mcScale < -7) {
			this.mcScale = -7;
			editor.setMCScale.accept(this.mcScale);
		}
		editor.currentOp = new ValueOp<>(this, this.mcScale, (a, b) -> a.mcScale = b);
		editor.markDirty();
	}

	@Override
	public void switchVis() {
		editor.updateValueOp(this, this.show, !this.show, (a, b) -> a.show = b, editor.setVis);
		if(type == ElementType.ROOT_PART) {
			editor.setHiddenEffect.accept(!show);
		}
	}

	@Override
	public void switchEffect(Effect effect) {
		switch (effect) {
		case GLOW:
			editor.updateValueOp(this, this.glow, !this.glow, (a, b) -> a.glow = b, editor.setGlow);
			break;

		case HIDE:
			if(type == ElementType.ROOT_PART) {
				switchVis();
				editor.setHiddenEffect.accept(!show);
			} else {
				editor.updateValueOp(this, this.hidden, !this.hidden, (a, b) -> a.hidden = b, editor.setHiddenEffect);
				editor.updateGui.accept(null);
			}
			break;

		case MIRROR:
			editor.updateValueOp(this, this.mirror, !this.mirror, (a, b) -> a.mirror = b, editor.setMirror);
			break;

		case RECOLOR:
			editor.addUndo(new ValueOp<>(this, this.recolor, (a, b) -> a.recolor = b));
			this.recolor = !this.recolor;
			editor.setReColor.accept(this.recolor);
			if(!this.texture || this.recolor)
				editor.setPartColor.accept(this.rgb);
			else
				editor.setPartColor.accept(null);
			editor.currentOp = new ValueOp<>(this, this.recolor, (a, b) -> a.recolor = b);
			editor.markDirty();
			break;

		default:
			break;

		}
	}

	@Override
	public Box getTextureBox() {
		if(type == ElementType.ROOT_PART) {
			PartValues pv = ((VanillaModelPart) typeData).getDefaultSize(editor.skinType);
			Vec3f size = pv.getSize();
			Vec2i uv = pv.getUV();
			int dx = MathHelper.ceil(size.x);
			int dy = MathHelper.ceil(size.y);
			int dz = MathHelper.ceil(size.z);
			return new Box(
					(int) (uv.x / 64f * editor.skinProvider.size.x),
					(int) (uv.y / 64f * editor.skinProvider.size.y),
					(int) (2 * (dx + dz) / 64f * editor.skinProvider.size.x),
					(int) ((dz + dy) / 64f * editor.skinProvider.size.y));
		}
		int dx = MathHelper.ceil(size.x);
		int dy = MathHelper.ceil(size.y);
		int dz = MathHelper.ceil(size.z);
		return new Box(u * textureSize, v * textureSize, 2 * (dx + dz) * textureSize, (dz + dy) * textureSize);
	}

	@Override
	public void populatePopup(PopupMenu popup) {
		if(type == ElementType.NORMAL) {
			popup.addButton(editor.gui().i18nFormat("button.cpm.duplicate"), () -> {
				ModelElement elem = new ModelElement(this, parent);
				editor.addUndo(() -> {
					parent.children.remove(elem);
					editor.selectedElement = null;
				});
				editor.runOp(() -> parent.children.add(elem));
				editor.selectedElement = elem;
				editor.markDirty();
				editor.updateGui();
			});
		} else if(type == ElementType.ROOT_PART) {
			popup.addButton(editor.gui().i18nFormat("button.cpm.duplicate"), () -> {
				ModelElement elem = new ModelElement(editor, ElementType.ROOT_PART, typeData, editor.gui());
				elem.duplicated = true;
				editor.addUndo(() -> {
					editor.elements.remove(elem);
					editor.selectedElement = null;
				});
				editor.runOp(() -> editor.elements.add(elem));
				editor.selectedElement = elem;
				editor.markDirty();
				editor.updateGui();
			});
		}
	}

	@Override
	public int bgColor() {
		return editor.selectedElement != this && editor.selectedAnim != null && editor.applyAnim && editor.selectedAnim.getComponentsFiltered().contains(this) ? editor.colors().anim_part_background : 0;
	}
}
