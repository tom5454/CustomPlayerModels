package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplType;
import com.tom.cpm.shared.editor.gui.TextureDisplay;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV;
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
	public boolean singleTex;
	public long storeID;
	public boolean hidden;
	public boolean templateElement, generated;
	public boolean duplicated;
	public PerFaceUV faceUV;
	public Tooltip tooltip;

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
		singleTex = element.singleTex;
		if(element.faceUV != null)faceUV = new PerFaceUV(element.faceUV);
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
			editor.frame.openPopup(new MessagePopup(editor.frame, editor.gui().i18nFormat("label.cpm.info"), editor.gui().i18nFormat("label.cpm.warnMoveGenPart")));
		}
		switch (object) {
		case SIZE:
			v.round(10);
			editor.action("set", "label.cpm.size").
			updateValueOp(this, this.size, v, 0, 25, false, (a, b) -> a.size = b, editor.setSize).
			execute();
			break;

		case OFFSET:
			editor.action("set", "label.cpm.offset").
			updateValueOp(this, this.offset, v, -Vec3f.MAX_POS, Vec3f.MAX_POS, false, (a, b) -> a.offset = b, editor.setOffset).
			execute();
			break;

		case ROTATION:
			editor.action("set", "label.cpm.rotation").
			updateValueOp(this, this.rotation, v, 0, 360, true, (a, b) -> a.rotation = b, editor.setRot).
			execute();
			break;

		case POSITION:
			editor.action("set", "label.cpm.position").
			updateValueOp(this, this.pos, v, -Vec3f.MAX_POS, Vec3f.MAX_POS, false, (a, b) -> a.pos = b, editor.setPosition).
			execute();
			break;

		case SCALE:
			editor.action("set", "label.cpm.scale").
			updateValueOp(this, this.pos, v, 0, 25, false, (a, b) -> a.scale = b, editor.setScale).
			execute();
			break;

		case TEXTURE:
		{
			editor.action("set", "action.cpm.texUV").
			updateValueOp(this, this.u, (int) v.x, 0, Integer.MAX_VALUE, (a, b) -> a.u = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			updateValueOp(this, this.v, (int) v.y, 0, Integer.MAX_VALUE, (a, b) -> a.v = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			updateValueOp(this, this.textureSize, (int) v.z, 0, 64, (a, b) -> a.textureSize = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			execute();
		}
		break;

		default:
			break;
		}
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
	public ETextures getTexture() {
		if(type == ElementType.ROOT_PART && typeData instanceof RootModelType) {
			RootGroups gr = RootGroups.getGroup((RootModelType) typeData);
			if(gr != null) {
				ETextures tex = editor.textures.get(gr.getTexSheet((RootModelType) typeData));
				if(tex != null)return tex;
			}
		}
		if(parent != null)return parent.getTexture();
		return editor.textures.get(TextureSheetType.SKIN);
	}

	@Override
	public void modeSwitch() {
		editor.action("switch", "action.cpm.cubeMode").updateValueOp(this, this.texture, !this.texture, (a, b) -> a.texture = b).execute();
		editor.setModeBtn.accept(this.texture ? editor.gui().i18nFormat("button.cpm.mode.tex") : editor.gui().i18nFormat("button.cpm.mode.color"));
		editor.setModePanel.accept(texture ? ModeDisplType.TEX : ModeDisplType.COLOR);
		editor.setTexturePanel.accept(this.texture ? new Vec3i(this.u, this.v, this.textureSize) : new Vec3i(this.rgb, 0, 0));
		if(!this.texture || this.recolor)
			editor.setPartColor.accept(this.rgb);
		else
			editor.setPartColor.accept(null);
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
			editor.setModePanel.accept(this.faceUV != null ? ModeDisplType.TEX_FACE : texture ? ModeDisplType.TEX : ModeDisplType.COLOR);
			editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize));
			if(!this.texture || this.recolor)
				editor.setPartColor.accept(this.rgb);
			editor.setDelEn.accept(!templateElement);
			editor.setGlow.accept(this.glow);
			editor.setReColor.accept(this.recolor);
			editor.setHiddenEffect.accept(this.hidden);
			if(faceUV == null)editor.setSingleTex.accept(this.singleTex);
			else {
				editor.setFaceRot.accept(faceUV.getRot(editor.perfaceFaceDir));
				editor.setFaceUVs.accept(faceUV.getVec(editor.perfaceFaceDir));
				editor.setAutoUV.accept(faceUV.isAutoUV(editor.perfaceFaceDir));
			}
			if(!singleTex)editor.setPerFaceUV.accept(this.faceUV != null);
			editor.updateName.accept(this.name);
			break;

		case ROOT_PART:
			editor.setPosition.accept(this.pos);
			editor.setRot.accept(this.rotation);
			editor.setHiddenEffect.accept(!this.show);
			editor.setDelEn.accept(this.duplicated || this.typeData instanceof RootModelType);
			break;

		default:
			break;
		}
	}

	@Override
	public void addNew() {
		if(templateElement)return;
		ModelElement elem = new ModelElement(editor);
		elem.parent = this;
		editor.selectedElement = elem;
		editor.action("add", "action.cpm.cube").addToList(children, elem).execute();
		editor.updateGui();
	}

	@Override
	public void delete() {
		if(templateElement)return;
		if(type == ElementType.NORMAL) {
			editor.action("remove", "action.cpm.cube").removeFromList(parent.children, this).onRun(() -> editor.selectedElement = null).execute();
			editor.updateGui();
		} else if(duplicated || typeData instanceof RootModelType) {
			editor.action("remove", "action.cpm.root").removeFromList(editor.elements, this).onRun(() -> editor.selectedElement = null).execute();
			editor.updateGui();
		}
	}

	@Override
	public void setElemColor(int color) {
		editor.action("set", "action.cpm.color").updateValueOp(this, rgb, color, (a, b) -> a.rgb = b, editor.setPartColor).execute();
	}

	@Override
	public void setMCScale(float value) {
		editor.action("set", "label.cpm.mcScale").updateValueOp(this, mcScale, value, -7f, 7f, (a, b) -> a.mcScale = b, editor.setMCScale).execute();
	}

	@Override
	public void switchVis() {
		editor.action("toggleVis").updateValueOp(this, this.show, !this.show, (a, b) -> a.show = b, editor.setVis).execute();
		if(type == ElementType.ROOT_PART) {
			editor.setHiddenEffect.accept(!show);
		}
	}

	@Override
	public void switchEffect(Effect effect) {
		switch (effect) {
		case GLOW:
			editor.action("switch", "label.cpm.glow").updateValueOp(this, this.glow, !this.glow, (a, b) -> a.glow = b, editor.setGlow).execute();
			break;

		case HIDE:
			if(type == ElementType.ROOT_PART) {
				switchVis();
				editor.setHiddenEffect.accept(!show);
			} else {
				editor.action("switch", "label.cpm.hidden_effect").updateValueOp(this, this.hidden, !this.hidden, (a, b) -> a.hidden = b, editor.setHiddenEffect).execute();
				editor.updateGui.accept(null);
			}
			break;

		case MIRROR:
			editor.action("switch", "label.cpm.mirror").updateValueOp(this, this.mirror, !this.mirror, (a, b) -> a.mirror = b, editor.setMirror).execute();
			break;

		case RECOLOR:
			editor.action("switch", "label.cpm.recolor").
			updateValueOp(this, this.recolor, !this.recolor, (a, b) -> a.recolor = b, editor.setReColor).execute();
			if(!this.texture || this.recolor)
				editor.setPartColor.accept(this.rgb);
			else
				editor.setPartColor.accept(null);
			break;

		case SINGLE_TEX:
			editor.action("switch", "label.cpm.singleTex").updateValueOp(this, this.singleTex, !this.singleTex, (a, b) -> a.singleTex = b, editor.setSingleTex).execute();;
			break;

		case PER_FACE_UV:
			editor.action("switch", "label.cpm.perfaceUV").updateValueOp(this, this.texture, true, (a, b) -> a.texture = b).
			update(editor.setModePanel, ModeDisplType.TEX_FACE).
			updateValueOp(this, this.faceUV, faceUV == null ? new PerFaceUV(this) : null, (a, b) -> a.faceUV = b, v -> editor.setPerFaceUV.accept(v != null)).
			execute();
			editor.updateGui();
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
			EditorTexture skin = getTexture().provider;
			return new Box(
					(int) (uv.x / 64f * skin.size.x),
					(int) (uv.y / 64f * skin.size.y),
					(int) (2 * (dx + dz) / 64f * skin.size.x),
					(int) ((dz + dy) / 64f * skin.size.y));
		}
		int dx = MathHelper.ceil(size.x);
		int dy = MathHelper.ceil(size.y);
		int dz = MathHelper.ceil(size.z);
		if(singleTex) {
			if(mcScale == 0 && (size.x == 0 || size.y == 0 || size.z == 0)) {
				if(size.x == 0) {
					return new Box(u * textureSize, v * textureSize, dz * textureSize, dy * textureSize);
				} else if(size.y == 0) {
					return new Box(u * textureSize, v * textureSize, dx * textureSize, dz * textureSize);
				} else if(size.z == 0) {
					return new Box(u * textureSize, v * textureSize, dx * textureSize, dy * textureSize);
				}
			}
			int txS = Math.max(dx, Math.max(dy, dz));
			return new Box(u * textureSize, v * textureSize, txS * textureSize, txS * textureSize);
		}
		return new Box(u * textureSize, v * textureSize, 2 * (dx + dz) * textureSize, (dz + dy) * textureSize);
	}

	@Override
	public void populatePopup(PopupMenu popup) {
		popup.addButton(editor.gui().i18nFormat("button.cpm.duplicate"), this::duplicate);
	}

	private void duplicate() {
		if(type == ElementType.NORMAL) {
			ModelElement elem = new ModelElement(this, parent);
			editor.action("duplicate").addToList(children, elem).onUndo(() -> editor.selectedElement = null).execute();
			editor.selectedElement = elem;
			editor.updateGui();
		} else if(type == ElementType.ROOT_PART) {
			ModelElement elem = new ModelElement(editor, ElementType.ROOT_PART, typeData, editor.gui());
			elem.duplicated = true;
			editor.action("duplicate").addToList(editor.elements, elem).onUndo(() -> editor.selectedElement = null).execute();
			editor.selectedElement = elem;
			editor.updateGui();
		}
	}

	@Override
	public int bgColor() {
		return editor.selectedElement != this && editor.selectedAnim != null && editor.applyAnim && editor.selectedAnim.getComponentsFiltered().contains(this) ? editor.colors().anim_part_background : 0;
	}

	public ModelElement getRoot() {
		return type == ElementType.ROOT_PART ? this : parent != null ? parent.getRoot() : null;
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}
}
