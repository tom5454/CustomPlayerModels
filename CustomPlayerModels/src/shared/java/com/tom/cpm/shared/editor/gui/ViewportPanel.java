package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeybindHandler;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.ButtonIconToggle;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ElementGroup;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Quaternion;
import com.tom.cpl.math.Quaternion.RotationOrder;
import com.tom.cpl.math.TriangleBoundingBox;
import com.tom.cpl.math.Vec2f;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VirtualTriangleRenderer;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorRenderer;
import com.tom.cpm.shared.editor.EditorRenderer.BoundType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.ModelTree;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;
import com.tom.cpm.shared.gui.Keybinds;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class ViewportPanel extends ViewportPanelBase3d {
	protected Editor editor;
	private TextureProvider debugTexture;
	public TreeElement draggingElement;
	public EditorRenderer.BoundType draggingType;
	public Vec3f[] oldValue;
	private ElementGroup<VecType, ButtonIconToggle> dragGroup;
	public VecType draggingVec;
	public Vec2f draggingClickOffset;
	public Mat4f modelView;
	public int dragged, dragged2;

	public ViewportPanel(Frame frm, Editor editor) {
		super(frm);
		this.editor = editor;

		dragGroup = new ElementGroup<>(ButtonIconToggle::setSelected);
		VecType[] vs = getVecTypes();
		for (int i = 0; i < vs.length; i++) {
			VecType v = vs[i];
			ButtonIconToggle icon = new ButtonIconToggle(gui, "editor", v.ordinal() * 16, 48, true, () -> setVec(v));
			icon.setBounds(new Box(i * 20, 0, 20, 20));
			icon.setTooltip(new Tooltip(frm, gui.i18nFormat("label.cpm." + v.name().toLowerCase(Locale.ROOT))));
			addElement(icon);
			dragGroup.addElement(v, icon);
		}
		if(vs.length != 0)
			setVec(vs[0]);
	}

	@Override
	public void render(MatrixStack stack, VBuffers buf, float partialTicks) {
		if(editor.renderBase.get())renderBase(stack, buf);
		editor.definition.renderingPanel = this;
		renderModel(stack, buf, partialTicks);
		VBuffers rp = buf.replay();
		editor.render(stack, rp, this);
		rp.finishAll();

		editor.definition.renderingPanel = null;
	}

	public void finishTransform(EditorRenderer.Bounds b) {
		TriangleBoundingBox t = b.bb;
		Mat4f view = getView();
		Mat4f proj = getProjection();
		t.transform(modelView);
		t.transform(view);
		t.transform(proj);
		Vec2i ws = get3dSize();
		t.finishTransform(ws.x, ws.y, get3dMousePos());
		if(MinecraftObjectHolder.DEBUGGING && gui.isCtrlDown()) {
			ETextures tex = editor.getTextureProvider();
			if(tex != null)
				VirtualTriangleRenderer.plot(t, debugTexture.getImage(), get3dMousePos(), b.type == EditorRenderer.BoundType.CLICK ? tex.getImage() : null);
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.drawText(0, -10, "a", 0xffffffff);//For some reason this fixes #221
		if(MinecraftObjectHolder.DEBUGGING && gui.isCtrlDown()) {
			Vec2i ws = get3dSize();
			if(debugTexture == null)debugTexture = new TextureProvider();
			if(debugTexture.getImage() == null || debugTexture.getImage().getWidth() != ws.x || debugTexture.getImage().getHeight() != ws.y)
				debugTexture.setImage(new Image(ws.x, ws.y));
			debugTexture.getImage().fill(0x00000000);
		}
		super.draw(event, partialTicks);
		if(MinecraftObjectHolder.DEBUGGING && gui.isCtrlDown()) {
			debugTexture.texture.markDirty();
			debugTexture.bind();
			draw3dOverlay();
		}

		if(MinecraftObjectHolder.DEBUGGING) {
			ViewportCamera cam = getCamera();
			gui.drawText(bounds.x, bounds.y + 50, "Cam Pos: " + cam.position.x + " " + cam.position.y + " " + cam.position.z, 0xff000000, 0xffffffff);
			gui.drawText(bounds.x, bounds.y + 60, "Cam Look: " + cam.look.x + " " + cam.look.y + " " + cam.look.z, 0xff000000, 0xffffffff);
			gui.drawText(bounds.x, bounds.y + 70, "Cam Dist: " + cam.camDist, 0xff000000, 0xffffffff);

			EditorRenderer.Bounds hovered = editor.definition.bounds.stream().filter(b -> b.isHovered).findFirst().orElse(null);
			if(hovered != null) {
				Vec2f h = hovered.bb.getHoverPointer();
				gui.drawText(bounds.x, bounds.y + 80, "Hover Pos: " + h.x + " " + h.y + " D: " + hovered.bb.isHovered(), 0xff000000, 0xffffffff);
			}
		}
	}

	@Override
	public ViewportCamera getCamera() {
		return editor.camera;
	}

	@Override
	public void preRender(MatrixStack stack, VBuffers buf) {
		editor.preRender();
	}

	@Override
	protected void postRender(MatrixStack stack, VBuffers buf) {
		EditorRenderer.Bounds hovered = editor.definition.select();
		if(hovered != null && Float.isFinite(hovered.bb.isHovered())) {
			if(hovered.drawHover != null)
				hovered.drawHover.run();
			hovered.isHovered = true;
		}
	}

	@Override
	public ModelDefinition getDefinition() {
		return editor.definition;
	}

	@Override
	public DisplayItem getHeldItem(ItemSlot hand) {
		return editor.handDisplay.getOrDefault(hand, DisplayItem.NONE);
	}

	@Override
	public float getScale() {
		return editor.applyScaling ? editor.scalingElem.getScale() : 1;
	}

	@Override
	public AnimationMode getAnimMode() {
		return editor.getRenderedPose() == VanillaPose.SKULL_RENDER ? AnimationMode.SKULL : AnimationMode.PLAYER;
	}

	@Override
	public Set<PlayerModelLayer> getArmorLayers() {
		ModelElement el = editor.getSelectedElement();
		if(el != null) {
			ModelElement root = el.getRoot();
			if(root != null && root.typeData instanceof RootModelType) {
				PlayerModelLayer l = PlayerModelLayer.getLayer((RootModelType) root.typeData);
				if(l != null) {
					Set<PlayerModelLayer> set = new HashSet<>(editor.modelDisplayLayers);
					set.add(l);
					return set;
				}
			}
		}
		return editor.modelDisplayLayers;
	}

	@Override
	protected void poseModel(VanillaPlayerModel p, MatrixStack matrixstack, float partialTicks) {
		p.reset();
		p.setAllVisible(true);
		p.rightArmPose = getHeldItem(ItemSlot.RIGHT_HAND).pose;
		p.leftArmPose = getHeldItem(ItemSlot.LEFT_HAND).pose;
		Hand hand = poseModel0(p, matrixstack, partialTicks);
		PlayerModelSetup.setRotationAngles(p, 0, 0, hand, false);

		if(!editor.applyAnim && editor.playerTpose.get()) {
			p.rightArm.zRot = (float) Math.toRadians(90);
			p.leftArm.zRot = (float) Math.toRadians(-90);
		}

		float lsa = 0.75f;
		float ls = editor.playVanillaAnims.get() || editor.selectedAnim == null ? MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks) : 1;

		editor.applyRenderPoseForAnim(pose -> {
			switch (pose) {
			case SLEEPING:
				matrixstack.translate(0.0D, 1.501F, 0.0D);
				matrixstack.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90));
				matrixstack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(270.0F));
				break;

			case SNEAKING:
				p.crouching = true;
				PlayerModelSetup.setRotationAngles(p, 0, 0, hand, false);
				break;

			case SNEAK_WALK:
				p.crouching = true;
				PlayerModelSetup.setRotationAngles(p, ls, lsa, hand, false);
				break;

			case RIDING:
				p.riding = true;
				PlayerModelSetup.setRotationAngles(p, 0, 0, hand, false);
				break;
			case CUSTOM:
			case DYING:
			case FALLING:
			case STANDING:
				break;

			case FLYING:
			case TRIDENT_SPIN:
				matrixstack.translate(0.0D, 1.0D, -0.5d);
				matrixstack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				p.head.xRot = -(float)Math.PI / 4F;
				break;

			case RUNNING:
				PlayerModelSetup.setRotationAngles(p, ls, 1, hand, false);
				break;

			case SWIMMING:
				PlayerModelSetup.setRotationAngles(p, ls, lsa, hand, true);
				matrixstack.translate(0.0D, 1.0D, -0.5d);
				matrixstack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				break;

			case WALKING:
				PlayerModelSetup.setRotationAngles(p, ls, lsa, hand, false);
				break;

			case SKULL_RENDER:
				p.setAllVisible(false);
				p.head.visible = true;
				matrixstack.translate(0.0D, 1.501F, 0.0D);
				break;

			default:
				break;
			}
		});

		modelView = new Mat4f(matrixstack.getLast().getMatrix());
	}

	protected Hand poseModel0(VanillaPlayerModel p, MatrixStack matrixstack, float partialTicks) {
		return Hand.RIGHT;
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return super.getRenderTypes();
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String tex) {
		return super.getRenderTypes(tex);
	}

	@Override
	protected int drawParrots() {
		return editor.drawParrots.get() ? 3 : 0;
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.mouseClick(event.offset(getBounds()));
				}
			}
		}
		if(isMenu(event.btn) && event.isHovered(bounds)) {
			dragged2 = 1;
		} else if(event.btn == EditorGui.getSelectMouseButton() && event.isHovered(bounds)) {
			EditorRenderer.Bounds hovered = editor.definition.bounds.stream().filter(b -> b.isHovered && b.type != EditorRenderer.BoundType.DRAG_PANE).sorted(Comparator.comparingInt(b -> -b.type.ordinal())).findFirst().orElse(null);
			if(hovered != null) {
				dragged = 1;
				if(hovered.type != EditorRenderer.BoundType.CLICK && draggingVec != null && canEdit()) {
					draggingElement = hovered.elem;
					draggingType = hovered.type;
					if(draggingType == EditorRenderer.BoundType.DRAG_NX || draggingType == EditorRenderer.BoundType.DRAG_NY || draggingType == EditorRenderer.BoundType.DRAG_NZ)
						oldValue = new Vec3f[] {getVec(draggingVec), getVec(VecType.OFFSET)};
					else
						oldValue = new Vec3f[] {getVec(draggingVec)};
				}
				hovered.elem.onClick(editor, event);
				event.consume();
			} else {
				dragged = -1;
			}
			editor.updateGui();
		}
		super.mouseClick(event);
		if(event.btn == EditorGui.getSelectMouseButton() && !enableDrag && dragged == -1 && event.isConsumed())dragged = 0;
		if(isMenu(event.btn) && event.isHovered(bounds))event.consume();
	}

	protected Vec3f getVec(VecType type) {
		return draggingElement.getVec(type);
	}

	protected void setVec(VecType type, Vec3f vec, boolean temp) {
		if(temp)draggingElement.setVecTemp(type, vec);
		else draggingElement.setVec(vec, type);
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		if(event.btn == EditorGui.getSelectMouseButton() && event.isHovered(bounds) && canEdit()) {
			if(dragged > 0) {
				dragged = 2;
				EditorRenderer.Bounds hovered = editor.definition.bounds.stream().filter(b -> b.isHovered && b.type == EditorRenderer.BoundType.DRAG_PANE).findFirst().orElse(null);
				event.consume();
				if(hovered != null && draggingElement != null && draggingVec != null) {
					Vec3f[] n = makeDragVec(hovered);
					setVec(draggingVec, n[0], true);
					if(n.length > 1)setVec(VecType.OFFSET, n[1], true);
				}
			} else if(dragged < 0) {
				dragged = -2;
			}
		}
		if(isMenu(event.btn) && event.isHovered(bounds) && dragged2 > 0) {
			dragged2 = 2;
		}
		super.mouseDrag(event);
		if(isMenu(event.btn) && event.isHovered(bounds))event.consume();
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		if(event.btn == EditorGui.getSelectMouseButton() && event.isHovered(bounds) && dragged < 0) {
			if(dragged == -1 && editor.getSelectedElement() != null) {
				editor.selectedElement = null;
				editor.updateGui();
				event.consume();
			}
			dragged = 0;
		}
		if(isMenu(event.btn) && event.isHovered(bounds) && dragged2 == 1) {
			dragged2 = 0;
			event.consume();
			ModelElement elem = editor.getSelectedElement();
			if(elem != null) {
				((ModelTree) editor.treeHandler.getModel()).displayPopup(event, elem);
			}
		}
		if(event.btn == EditorGui.getSelectMouseButton() && event.isHovered(bounds) && dragged > 0 && canEdit()) {
			event.consume();
			endGizmoDrag(true);
		}
		super.mouseRelease(event);
	}

	protected void endGizmoDrag(boolean apply) {
		EditorRenderer.Bounds hovered = editor.definition.bounds.stream().filter(b -> b.isHovered && b.type == EditorRenderer.BoundType.DRAG_PANE).findFirst().orElse(null);
		dragged = 0;
		if(draggingElement != null && draggingVec != null) {
			if(hovered != null) {
				Vec3f[] n = makeDragVec(hovered);
				setVec(draggingVec, oldValue[0], true);
				if(apply)setVec(draggingVec, n[0], false);
				if(n.length > 1) {
					setVec(VecType.OFFSET, oldValue[1], true);
					if(apply)setVec(VecType.OFFSET, n[1], false);
				}
			} else {
				setVec(draggingVec, oldValue[0], true);
				if(oldValue.length > 1)setVec(VecType.OFFSET, oldValue[1], true);
			}
			editor.updateGui();
		}
		draggingElement = null;
		draggingType = null;
		oldValue = null;
		draggingClickOffset = null;
	}

	protected Vec3f[] makeDragVec(EditorRenderer.Bounds hovered) {
		if(draggingClickOffset == null) {
			draggingClickOffset = hovered.bb.getHoverPointer();
			return oldValue;
		}
		Vec3f v = oldValue[0];
		float hx = hovered.bb.getHoverPointer().x - draggingClickOffset.x;
		float hy = hovered.bb.getHoverPointer().y - draggingClickOffset.y;
		Vec3f[] res;
		if(draggingVec == VecType.ROTATION) {
			Vec3f t = new Vec3f(hovered.bb.getHoverPointer(), 0);
			t.normalize();
			double a = Math.toDegrees(Math.acos(t.y));
			double d = t.x;
			t = new Vec3f(draggingClickOffset, 0);
			t.normalize();
			double o = Math.toDegrees(Math.acos(t.y));
			if(d < 0)a = 360 - a;
			if(t.x < 0)o = 360 - o;
			hx = (float) (a - o);
			RotationOrder r = draggingType == BoundType.DRAG_X ? RotationOrder.ZYX : (draggingType == BoundType.DRAG_Y ? RotationOrder.ZXY : RotationOrder.XYZ);
			v = Quaternion.reorder(v, RotationOrder.ZYX, r);
			float x = v.x;
			float y = v.y;
			float z = v.z;
			switch (draggingType) {
			case DRAG_X:
				v = new Vec3f(x + hx, y, z);
				break;
			case DRAG_Y:
				v = new Vec3f(x, y + hx, z);
				break;
			case DRAG_Z:
				v = new Vec3f(x, y, z + hx);
				break;
			default:
				break;
			}
			res = new Vec3f[] {Quaternion.reorder(v, r, RotationOrder.ZYX)};
		} else {
			float x = v.x;
			float y = v.y;
			float z = v.z;
			switch (draggingType) {
			case DRAG_NX:
				res = new Vec3f[] {new Vec3f(x - hx, y, z), new Vec3f(oldValue[1].x + hy, oldValue[1].y, oldValue[1].z)};
				break;
			case DRAG_NY:
				res = new Vec3f[] {new Vec3f(x, y - hx, z), new Vec3f(oldValue[1].x, oldValue[1].y + hy, oldValue[1].z)};
				break;
			case DRAG_NZ:
				res = new Vec3f[] {new Vec3f(x, y, z - hx), new Vec3f(oldValue[1].x, oldValue[1].y, oldValue[1].z + hy)};
				break;
			case DRAG_X:
				res = new Vec3f[] {new Vec3f(x + hx, y, z)};
				break;
			case DRAG_Y:
				res = new Vec3f[] {new Vec3f(x, y + hx, z)};
				break;
			case DRAG_Z:
				res = new Vec3f[] {new Vec3f(x, y, z + hx)};
				break;
			case CLICK:
			case DRAG_PANE:
			default:
				res = oldValue;
				break;
			}
		}
		for (int i = 0; i < res.length; i++) {
			res[i] = new Vec3f(res[i]);
			if(draggingVec == VecType.ROTATION) {
				if(gui.isShiftDown())res[i].round(1);
				else {
					res[i].x = Math.round(res[i].x / 5) * 5;
					res[i].y = Math.round(res[i].y / 5) * 5;
					res[i].z = Math.round(res[i].z / 5) * 5;
				}
			} else
				res[i].round(gui.isShiftDown() ? 10 : 1);
		}
		return res;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.matches(gui.getKeyCodes().KEY_ESCAPE) && draggingElement != null) {
			event.consume();
			endGizmoDrag(false);
		}
		KeybindHandler h = editor.frame.getKeybindHandler();
		h.registerKeybind(Keybinds.POSITION, () -> setVec(VecType.POSITION));
		h.registerKeybind(Keybinds.OFFSET, () -> setVec(VecType.OFFSET));
		h.registerKeybind(Keybinds.SIZE, () -> setVec(VecType.SIZE));
		h.registerKeybind(Keybinds.ROTATION, () -> setVec(VecType.ROTATION));
		h.registerKeybind(Keybinds.DELETE, editor::deleteSel);
		h.registerKeybind(Keybinds.NEW_PART, editor::addNew);
		h.registerKeybind(Keybinds.FOCUS_CAMERA, this::focusOnSelected);
		super.keyPressed(event);
	}

	private void focusOnSelected() {
		ModelElement me = editor.getSelectedElement();
		if(me != null && me.matrixPosition != null) {
			Vec4f v = new Vec4f(0, 0, 1, 1);
			v.transform(me.matrixPosition);
			ViewportCamera cam = getCamera();
			cam.position.x = v.z - 0.5f;
			cam.position.y = 1.5f - v.y;
			cam.position.z = v.x + 0.5f;
		}
	}

	@Override
	protected boolean isRotate(int btn) {
		return btn == EditorGui.getRotateMouseButton() || btn == EditorGui.getDragMouseButton();
	}

	@Override
	protected boolean isDrag(int btn) {
		int d = EditorGui.getDragMouseButton();
		return d == -1 ? gui.isShiftDown() : d == btn;
	}

	protected boolean isMenu(int btn) {
		int d = EditorGui.getMenuMouseButton();
		return d == -1 ? gui.isAltDown() && btn == EditorGui.getSelectMouseButton() : d == btn;
	}

	protected VecType[] getVecTypes() {
		return VecType.MOUSE_EDITOR_TYPES;
	}

	private void setVec(VecType vt) {
		if(dragGroup.containsKey(vt)) {
			draggingVec = vt;
			dragGroup.accept(vt);
		}
	}

	public boolean canEdit() {
		return editor.selectedElement != null && editor.selectedElement.canEditVec(draggingVec);
	}
}
