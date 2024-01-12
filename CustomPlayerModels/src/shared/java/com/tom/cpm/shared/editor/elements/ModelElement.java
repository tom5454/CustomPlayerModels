package com.tom.cpm.shared.editor.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.Effect;
import com.tom.cpm.shared.editor.FormatLimits;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimFrame.FrameData;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.gui.TextureDisplay;
import com.tom.cpm.shared.editor.gui.popup.CopyTransformSettingsPopup;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.VecType;
import com.tom.cpm.shared.editor.util.QuickTask;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class ModelElement extends Cube implements IElem, TreeElement {
	private static final Pattern DUP_PATTERN = Pattern.compile("(.*) \\((\\d+)\\)$");
	private static boolean movePopupShown = false;
	public Editor editor;
	public String name;
	public ModelElement parent;
	public List<ModelElement> children = new ArrayList<>();
	public ElementType type;
	public Object typeData;
	public RenderedCube rc;
	public boolean showInEditor = true;
	public boolean texture, mirror;
	public int textureSize;
	public boolean glow;
	public boolean recolor;
	public boolean singleTex;
	public boolean extrude;
	public long storeID;
	public boolean templateElement, generated;
	public boolean duplicated;
	public boolean disableVanillaAnim;
	public boolean locked;
	public PerFaceUV faceUV;
	public ItemRenderer itemRenderer;
	public CopyTransformEffect copyTransform;
	public Mat4f matrixPosition;

	public ModelElement(ModelElement element, ModelElement parent) {
		this(element.editor);
		this.parent = parent;
		element.children.forEach(c -> children.add(new ModelElement(c, this)));
		if(element.itemRenderer == null) {
			Matcher m = DUP_PATTERN.matcher(element.name);
			String nm;
			int count;
			if (m.find()) {
				nm = m.group(1);
				try {
					count = Integer.parseInt(m.group(2)) + 1;
				} catch (NumberFormatException e) {
					count = 2;
				}
			} else {
				nm = element.name;
				count = 2;
			}
			name = nm + " (" + count + ")";
		} else
			name = element.name;
		showInEditor = element.showInEditor;
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
		extrude = element.extrude;
		if(element.faceUV != null)faceUV = new PerFaceUV(element.faceUV);
		if(element.itemRenderer != null)itemRenderer = new ItemRenderer(element.itemRenderer);
	}

	public ModelElement(Editor editor) {
		this(editor, ElementType.NORMAL, null);
	}

	@Deprecated
	public ModelElement(Editor editor, ElementType type, Object typeData, IGui gui) {
		this(editor, type, typeData);
	}

	public ModelElement(Editor editor, ElementType type, Object typeData) {
		this.type = type;
		this.typeData = typeData;
		this.editor = editor;
		type.buildElement(editor, this, typeData);
	}

	public void preRender() {
		type.preRenderUpdate(this);
		children.forEach(ModelElement::preRender);
	}

	public void postRender() {
		if(copyTransform != null)copyTransform.apply();
		children.forEach(ModelElement::postRender);
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
	public Vec3f getScale() {
		return new Vec3f(1, 1, 1);
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
		return !hidden;
	}

	@Override
	public String getName() {
		String name = this.name;
		if(hidden)name = editor.ui.i18nFormat("label.cpm.tree.hidden", name);
		if(copyTransform != null)name = editor.ui.i18nFormat("label.cpm.copyTransformFlag", name);
		if(duplicated)name = editor.ui.i18nFormat("label.cpm.tree.duplicated", name);
		if(disableVanillaAnim)name = editor.ui.i18nFormat("label.cpm.tree.disableVanillaAnim", name);
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
	public int textColor(IGui gui) {
		if(itemRenderer != null && editor.definition.rendererObjectMap.get(itemRenderer) == itemRenderer) {
			return gui.getColors().link_normal;
		}
		return !showInEditor || locked ? gui.getColors().button_text_disabled : 0;
	}

	@Override
	public void accept(TreeElement elem) {
		editor.moveElement((ModelElement) elem, this);
	}

	@Override
	public boolean canAccept(TreeElement elem) {
		return !locked && elem instanceof ModelElement;
	}

	@Override
	public boolean canMove() {
		return !locked && type == ElementType.NORMAL;
	}

	public void markDirty() {
		rc.updateObject = true;
	}

	@Override
	public void setVec(Vec3f v, VecType object) {
		if (locked)return;
		if(type == ElementType.ROOT_PART && generated && object == VecType.POSITION && !movePopupShown) {
			movePopupShown = true;
			editor.ui.displayMessagePopup(editor.ui.i18nFormat("label.cpm.info"), editor.ui.i18nFormat("label.cpm.warnMoveGenPart"));
		}
		switch (object) {
		case SIZE:
			v.round(10);
			editor.action("set", "label.cpm.size").
			updateValueOp(this, this.size, v, 0, FormatLimits.getSizeLimit(), false, (a, b) -> a.size = b, editor.setSize).
			onAction(this::markDirty).
			execute();
			break;

		case OFFSET:
			editor.action("set", "label.cpm.offset").
			updateValueOp(this, this.offset, v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false, (a, b) -> a.offset = b, editor.setOffset).
			onAction(this::markDirty).
			execute();
			break;

		case ROTATION:
			editor.action("set", "label.cpm.rotation").
			updateValueOp(this, this.rotation, v, 0, 360, true, (a, b) -> a.rotation = b, editor.setRot).
			execute();
			break;

		case POSITION:
			editor.action("set", "label.cpm.position").
			updateValueOp(this, this.pos, v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false, (a, b) -> a.pos = b, editor.setPosition).
			execute();
			break;

		case SCALE:
			editor.action("set", "label.cpm.scale").
			updateValueOp(this, this.scale, v, 0, FormatLimits.getSizeLimit(), false, (a, b) -> a.scale = b, editor.setScale).
			onAction(this::markDirty).
			execute();
			break;

		case TEXTURE:
		{
			editor.action("set", "action.cpm.texUV").
			updateValueOp(this, this.u, (int) v.x, 0, Integer.MAX_VALUE, (a, b) -> a.u = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			updateValueOp(this, this.v, (int) v.y, 0, Integer.MAX_VALUE, (a, b) -> a.v = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			updateValueOp(this, this.textureSize, (int) v.z, 0, 64, (a, b) -> a.textureSize = b, __ -> editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize))).
			onAction(this::markDirty).
			execute();
		}
		break;

		default:
			break;
		}
	}

	@Override
	public Vec3f getVec(VecType v) {
		switch (v) {
		case OFFSET:
			return new Vec3f(offset);
		case POSITION:
			return new Vec3f(pos);
		case ROTATION:
			return new Vec3f(rotation);
		case SCALE:
			return new Vec3f(scale);
		case SIZE:
			return new Vec3f(size);
		case TEXTURE:
			return new Vec3f(this.u, this.v, textureSize);
		default:
			return null;
		}
	}

	@Override
	public void setVecTemp(VecType vt, Vec3f v) {
		switch (vt) {
		case OFFSET:
			ActionBuilder.limitVec(v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false);
			offset = v;
			editor.setOffset.accept(offset);
			markDirty();
			break;

		case POSITION:
			ActionBuilder.limitVec(v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false);
			pos = v;
			editor.setPosition.accept(pos);
			break;

		case ROTATION:
			ActionBuilder.limitVec(v, 0, 360, true);
			rotation = v;
			editor.setRot.accept(rotation);
			break;

		case SCALE:
			ActionBuilder.limitVec(v, 0, FormatLimits.getSizeLimit(), false);
			scale = v;
			editor.setScale.accept(scale);
			markDirty();
			break;

		case SIZE:
			ActionBuilder.limitVec(v, 0, FormatLimits.getSizeLimit(), false);
			v.round(10);
			size = v;
			editor.setSize.accept(size);
			markDirty();
			break;

		case TEXTURE:
			this.u = Math.max((int) v.x, 0);
			this.v = Math.max((int) v.y, 0);
			textureSize = Math.max((int) v.z, 1);
			editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize));
			markDirty();
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
		if (locked)return;
		if(texture && (showInEditor || editor.selectedElement == this))
			TextureDisplay.drawBoxTextureOverlay(gui, this, x, y, xs, ys, TextureDisplay.getAlphaForBox(editor.selectedElement == this));
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
		editor.action("switch", "action.cpm.cubeMode").updateValueOp(this, this.texture, !this.texture, (a, b) -> a.texture = b).
		onAction(this::markDirty).execute();
		editor.updateGui();
	}

	@Override
	public void updateGui() {
		editor.setVis.accept(this.showInEditor);
		editor.setAddEn.accept(!templateElement && itemRenderer == null);
		switch(this.type) {
		case NORMAL:
			editor.setOffset.accept(this.offset);
			editor.setRot.accept(this.rotation);
			editor.setPosition.accept(this.pos);
			if(itemRenderer == null)
				editor.setSize.accept(this.size);
			editor.setScale.accept(this.scale);
			if(itemRenderer == null) {
				editor.setMCScale.accept(this.mcScale);
				editor.setMirror.accept(this.mirror);
				editor.setModeBtn.accept(this.texture ? editor.ui.i18nFormat("button.cpm.mode.tex") : editor.ui.i18nFormat("button.cpm.mode.color"));
				editor.setModePanel.accept(this.faceUV != null ? ModeDisplayType.TEX_FACE : texture ? ModeDisplayType.TEX : ModeDisplayType.COLOR);
				editor.setTexturePanel.accept(new Vec3i(this.u, this.v, this.textureSize));
				if(!this.texture || this.recolor)
					editor.setPartColor.accept(this.rgb);
			}
			editor.setCopyTransformEffect.accept(copyTransform != null);
			editor.setDelEn.accept(!templateElement);
			editor.setHiddenEffect.accept(this.hidden);
			if(itemRenderer == null) {
				editor.setGlow.accept(this.glow);
				if (this.texture) {
					editor.setReColor.accept(this.recolor);
					if(faceUV == null) {
						editor.setSingleTex.accept(this.singleTex);
						editor.setExtrudeEffect.accept(this.extrude);
					} else {
						editor.setFaceRot.accept(faceUV.getRot(editor.perfaceFaceDir.get()));
						editor.setFaceUVs.accept(faceUV.getVec(editor.perfaceFaceDir.get()));
						editor.setAutoUV.accept(faceUV.isAutoUV(editor.perfaceFaceDir.get()));
						editor.setSingleTex.accept(null);
					}
					if(!singleTex)editor.setPerFaceUV.accept(this.faceUV != null);
					else editor.setPerFaceUV.accept(null);
				}
				editor.updateName.accept(this.name);
			}
			break;

		case ROOT_PART:
			editor.setPosition.accept(this.pos);
			editor.setRot.accept(this.rotation);
			editor.setHiddenEffect.accept(this.hidden);
			editor.setDelEn.accept(this.duplicated || this.typeData instanceof RootModelType);
			editor.setDisableVanillaEffect.accept(disableVanillaAnim);
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
		if(type == ElementType.NORMAL || duplicated || typeData instanceof RootModelType) {
			List<ModelElement> lst = type == ElementType.NORMAL ? parent.children : editor.elements;
			ActionBuilder ab = editor.action("remove", "action.cpm.cube").removeFromList(lst, this).
					onRun(() -> editor.selectedElement = null);
			editor.animations.stream().flatMap(a -> a.getFrames().stream()).forEach(f -> f.clearSelectedData(ab, this));
			ab.onAction(() -> editor.animations.forEach(EditorAnim::clearCache));
			ab.execute();
			editor.updateGui();
		}
	}

	@Override
	public void setElemColor(int color) {
		editor.action("set", "action.cpm.color").updateValueOp(this, rgb, color, (a, b) -> a.rgb = b, editor.setPartColor).onAction(this::markDirty).execute();
	}

	@Override
	public void setMCScale(float value) {
		editor.action("set", "label.cpm.mcScale").updateValueOp(this, mcScale, value, -7f, 7f, (a, b) -> a.mcScale = b, editor.setMCScale).onAction(this::markDirty).execute();
	}

	@Override
	public void switchVis() {
		showInEditor = !showInEditor;
		editor.setVis.accept(showInEditor);
	}

	@Override
	public void switchEffect(Effect effect) {
		switch (effect) {
		case GLOW:
			editor.action("switch", "label.cpm.glow").updateValueOp(this, this.glow, !this.glow, (a, b) -> a.glow = b, editor.setGlow).execute();
			break;

		case HIDE:
			editor.action("switch", "label.cpm.hidden_effect").updateValueOp(this, this.hidden, !this.hidden, (a, b) -> a.hidden = b, editor.setHiddenEffect).execute();
			editor.treeHandler.update();
			editor.updateGui.accept(null);
			break;

		case MIRROR:
			editor.action("switch", "label.cpm.mirror").updateValueOp(this, this.mirror, !this.mirror, (a, b) -> a.mirror = b, editor.setMirror).onAction(this::markDirty).execute();
			break;

		case RECOLOR:
			editor.action("switch", "label.cpm.recolor").
			updateValueOp(this, this.recolor, !this.recolor, (a, b) -> a.recolor = b, editor.setReColor).onAction(this::markDirty).execute();
			if(!this.texture || this.recolor)
				editor.setPartColor.accept(this.rgb);
			else
				editor.setPartColor.accept(null);
			break;

		case SINGLE_TEX:
			editor.action("switch", "label.cpm.singleTex").updateValueOp(this, this.singleTex, !this.singleTex, (a, b) -> a.singleTex = b, editor.setSingleTex).onAction(this::markDirty).execute();
			break;

		case EXTRUDE:
			editor.action("switch", "label.cpm.extrude_effect").updateValueOp(this, this.extrude, !this.extrude, (a, b) -> a.extrude = b, editor.setExtrudeEffect).onAction(this::markDirty).execute();
			break;

		case PER_FACE_UV:
			editor.action("switch", "label.cpm.perfaceUV").updateValueOp(this, this.texture, true, (a, b) -> a.texture = b).
			update(editor.setModePanel, ModeDisplayType.TEX_FACE).
			updateValueOp(this, this.faceUV, faceUV == null ? new PerFaceUV(this) : null, (a, b) -> a.faceUV = b, v -> editor.setPerFaceUV.accept(v != null)).
			onAction(this::markDirty).execute();
			editor.updateGui();
			break;

		case COPY_TRANSFORM:
			editor.action("switch", "label.cpm.copyTransform").
			updateValueOp(this, this.copyTransform, copyTransform == null ? new CopyTransformEffect(this) : null, (a, b) -> a.copyTransform = b, v -> editor.setCopyTransformEffect.accept(v != null)).
			execute();
			editor.updateGui();
			if(copyTransform != null) {
				editor.ui.displayPopup(f -> new CopyTransformSettingsPopup(f, editor, copyTransform));
			}
			break;

		case DISABLE_VANILLA_ANIM:
			if(type == ElementType.ROOT_PART) {
				editor.action("switch", "label.cpm.disableVanillaAnim").
				updateValueOp(this, this.disableVanillaAnim, !this.disableVanillaAnim, (a, b) -> a.disableVanillaAnim = b).execute();
				editor.updateGui();
			}
			break;

		default:
			break;

		}
	}

	@Override
	public Box getTextureBox() {
		if (locked)return null;
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
		if(!texture)return new Box(0, 0, 0, 0);
		int dx = MathHelper.ceil(size.x);
		int dy = MathHelper.ceil(size.y);
		int dz = MathHelper.ceil(size.z);
		if(extrude) {
			return new Box(u * textureSize, v * textureSize, dx * textureSize, dy * textureSize);
		}
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
		if(faceUV != null)return null;
		return new Box(u * textureSize, v * textureSize, 2 * (dx + dz) * textureSize, (dz + dy) * textureSize);
	}

	@Override
	public void populatePopup(PopupMenu popup) {
		if (locked) {
			popup.addButton(editor.ui.i18nFormat("button.cpm.unlock"), () -> {
				locked = false;
				editor.updateGui();
			});
			return;
		}
		popup.addButton(editor.ui.i18nFormat("button.cpm.duplicate"), this::duplicate);
		if(type == ElementType.NORMAL && copyTransform != null) {
			popup.addButton(editor.ui.i18nFormat("button.cpm.editCopyTransform"), () -> {
				popup.getGui().getFrame().openPopup(new CopyTransformSettingsPopup(popup.getGui().getFrame(), editor, copyTransform));
			});
		}
		Checkbox boxHidden = popup.addCheckbox(editor.ui.i18nFormat("label.cpm.hidden_effect"), () -> switchEffect(Effect.HIDE));
		boxHidden.setSelected(hidden);
		boxHidden.setTooltip(new Tooltip(popup.getGui().getFrame(), editor.ui.i18nFormat("tooltip.cpm.hidden_effect")));

		popup.addButton(editor.ui.i18nFormat("button.cpm.lock"), () -> {
			locked = true;
			editor.updateGui();
		});
	}

	private void duplicate() {
		if(type == ElementType.NORMAL) {
			ModelElement elem = new ModelElement(this, parent);
			editor.action("duplicate").addToList(parent.children, elem).onUndo(() -> editor.selectedElement = null).execute();
			editor.selectedElement = elem;
			editor.updateGui();
		} else if(type == ElementType.ROOT_PART) {
			ModelElement elem = new ModelElement(editor, ElementType.ROOT_PART, typeData);
			elem.duplicated = true;
			elem.storeID = Math.abs(new Random().nextLong());
			editor.action("duplicate").addToList(editor.elements, elem).onUndo(() -> editor.selectedElement = null).execute();
			editor.selectedElement = elem;
			editor.updateGui();
		}
		ModelElement el = editor.getSelectedElement();
		if(!editor.animations.isEmpty()) {
			editor.setQuickAction.accept(new QuickTask(editor.ui.i18nFormat("button.cpm.dupAnimations"), editor.ui.i18nFormat("tooltip.cpm.dupAnimations"), () -> {
				ActionBuilder ab = editor.action("duplicate");
				editor.animations.forEach(a -> a.getFrames().forEach(f -> dupAnim(ab, this, el, f)));
				ab.onAction(() -> editor.animations.forEach(EditorAnim::clearCache));
				ab.execute();
			}).add(editor.ui.i18nFormat("button.cpm.copyFromOriginal"), editor.ui.i18nFormat("tooltip.cpm.copyFromOriginal"), () -> addCts(el)));
		} else {
			editor.setQuickAction.accept(new QuickTask(editor.ui.i18nFormat("button.cpm.copyFromOriginal"), editor.ui.i18nFormat("tooltip.cpm.copyFromOriginal"), () -> addCts(el), true));
		}
	}

	private void addCts(ModelElement el) {
		ActionBuilder ab = editor.action("switch", "label.cpm.copyTransform");
		addCts(ab, this, el);
		ab.onAction(editor::updateGui);
		ab.execute();
	}

	private void addCts(ActionBuilder ab, ModelElement from, ModelElement to) {
		CopyTransformEffect c = new CopyTransformEffect(to);
		c.from = from;
		c.setAll(true);
		ab.updateValueOp(to, null, c, (a, b) -> a.copyTransform = b);
		for (int i = 0; i < from.children.size(); i++) {
			ModelElement f = from.children.get(i);
			ModelElement t = to.children.get(i);
			addCts(ab, f, t);
		}
	}

	private void dupAnim(ActionBuilder ab, ModelElement from, ModelElement to, AnimFrame frm) {
		FrameData fd = frm.getComponents().get(from);
		if(fd != null)frm.importFrameData(ab, to, fd);
		for (int i = 0; i < from.children.size(); i++) {
			ModelElement f = from.children.get(i);
			ModelElement t = to.children.get(i);
			dupAnim(ab, f, t, frm);
		}
	}

	@Override
	public int bgColor(IGui gui) {
		return editor.selectedElement != this && editor.selectedAnim != null && editor.applyAnim && editor.selectedAnim.getComponentsFiltered().contains(this) ? gui.getColors().anim_part_background : 0;
	}

	public ModelElement getRoot() {
		return type == ElementType.ROOT_PART ? this : parent != null ? parent.getRoot() : null;
	}

	@Override
	public Tooltip getTooltip(IGui gui) {
		StringBuilder sb = new StringBuilder();
		if(copyTransform != null)sb.append(copyTransform.getTooltip(editor.ui));
		return sb.length() == 0 ? null : new Tooltip(gui.getFrame(), sb.toString());
	}

	@Override
	public void onClick(IGui gui, Editor e, MouseEvent evt) {
		if (locked)return;
		if(gui.isCtrlDown()) {
			if(e.selectedElement instanceof MultiSelector) {
				if(((MultiSelector)e.selectedElement).add(this))e.selectedElement = null;
			} else if(e.getSelectedElement() == null)e.selectedElement = this;
			else {
				MultiSelector ms = new MultiSelector.ElementImpl(e);
				ms.add(e.getSelectedElement());
				ms.add(this);
				e.selectedElement = ms;
			}
		} else {
			e.selectedElement = this;
		}
	}

	@Override
	public boolean canEditVec(VecType type) {
		if (locked)return false;
		if(this.type == ElementType.ROOT_PART)return type == VecType.POSITION || type == VecType.ROTATION;
		if(itemRenderer != null)return type == VecType.POSITION || type == VecType.ROTATION || type == VecType.SCALE || type == VecType.OFFSET;
		else return true;
	}

	@Override
	public List<TreeSettingElement> getSettingsElements() {
		if (locked)return Collections.emptyList();
		return faceUV != null ? faceUV.getDragBoxes(this) : Collections.emptyList();
	}

	@Override
	public boolean canSelect() {
		return !locked;
	}

	@Override
	public void drawName(IGui gui, int x, int y, int color) {
		if(locked) {
			gui.drawTexture(x, y, 8, 8, 16, 8, "editor", color);
		}
	}

	@Override
	public int getExtraWidth(IGui gui) {
		return locked ? 10 : 0;
	}
}
