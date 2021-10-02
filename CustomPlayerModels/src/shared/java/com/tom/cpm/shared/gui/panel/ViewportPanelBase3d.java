package com.tom.cpm.shared.gui.panel;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.editor.RootGroups;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.GuiModelRenderManager;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaPlayerModel;
import com.tom.cpm.shared.util.PlayerModelLayer;

public abstract class ViewportPanelBase3d extends Panel3d {
	private static final GuiModelRenderManager manager = new GuiModelRenderManager();
	private static final EnumMap<SkinType, VanillaPlayerModel> models = new EnumMap<>(SkinType.class);
	static {
		for (SkinType t : SkinType.VANILLA_TYPES) {
			models.put(t, new VanillaPlayerModel(t));
		}
	}

	protected int mx, my;
	protected boolean enableDrag;
	protected boolean dragMode;
	protected int paintColor;
	protected Vec2i mouseCursorPos = new Vec2i();

	public ViewportPanelBase3d(IGui gui) {
		super(gui);
		setBackgroundColor(gui.getColors().popup_background);
	}

	public void renderModel(MatrixStack stack, VBuffers buf, float partialTicks) {
		float scale = getScale();
		stack.push();
		stack.translate(0.5f, 0, 0.5f);
		stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
		stack.scale((-scale), -scale, scale);
		stack.translate(0, -1.5f, 0);

		preRender();

		ModelDefinition def = getDefinition();
		VanillaPlayerModel p = models.get(def.getSkinType());
		VBuffers rp = buf.replay();

		manager.bindModel(p, GuiModelRenderManager.PLAYER, rp, def, null, getAnimMode());
		manager.bindSkin(p, this, TextureSheetType.SKIN);

		poseModel(p, stack, partialTicks);

		p.render(stack, rp.getBuffer(types, getMode()));

		Set<PlayerModelLayer> layers = getArmorLayers();
		for(PlayerModelLayer l : PlayerModelLayer.VALUES) {
			if(layers.contains(l)) {
				p.poseLayer(l, layers);
			}
		}

		stack.push();
		p.rightArm.translateRotatePart(stack);
		renderItem(stack, ItemSlot.RIGHT_HAND, getHeldItem(ItemSlot.RIGHT_HAND));
		stack.pop();

		stack.push();
		p.leftArm.translateRotatePart(stack);
		renderItem(stack, ItemSlot.LEFT_HAND, getHeldItem(ItemSlot.LEFT_HAND));
		stack.pop();

		stack.push();
		p.head.translateRotatePart(stack);
		renderItem(stack, ItemSlot.HEAD, getHeldItem(ItemSlot.HEAD));
		stack.pop();

		manager.unbindModel(p);

		for(PlayerModelLayer l : PlayerModelLayer.VALUES) {
			if(getArmorLayers().contains(l)) {
				manager.bindModel(p, l.name(), rp, def, null, getAnimMode());
				manager.bindSkin(p, this, RootGroups.getGroup(l.parts[0]).getTexSheet(l.parts[0]));
				p.renderLayer(stack, rp.getBuffer(types, getMode()), l);
				manager.unbindModel(p);
			}
		}

		rp.finishAll();
		stack.pop();
	}

	protected void poseModel(VanillaPlayerModel p, MatrixStack stack, float partialTicks) {
		p.reset();
		p.setAllVisible(true);
		PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
	}

	protected RenderMode getMode() {
		return applyLighting() ? RenderMode.DEFAULT : RenderMode.PAINT;
	}

	public void renderBase(MatrixStack stack, VBuffers buf) {
		RenderTypes<RenderMode> rt = getRenderTypes("area");
		VertexBuffer t = buf.getBuffer(rt, RenderMode.DEFAULT);
		Mat3f n = stack.getLast().getNormal();
		Mat4f m = stack.getLast().getMatrix();
		t.pos(m, 4,  0,  4).tex(1, 1).normal(n, 0, 1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 4,  0, -3).tex(0, 1).normal(n, 0, 1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, -3, 0, -3).tex(0, 0).normal(n, 0, 1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, -3, 0,  4).tex(1, 0).normal(n, 0, 1, 0).color(1, 1, 1, 1).endVertex();
		t.finish();

		rt = getRenderTypes("base");
		t = buf.getBuffer(rt, RenderMode.DEFAULT);
		float y = -1.001f;
		t.pos(m, 1, y,     0).tex(1, 1).normal(n, 0, 0, -1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y,     0).tex(0, 1).normal(n, 0, 0, -1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 0).tex(0, 0).normal(n, 0, 0, -1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y + 1, 0).tex(1, 0).normal(n, 0, 0, -1).color(1, 1, 1, 1).endVertex();

		t.pos(m, 0, y,     1).tex(1, 1).normal(n, 0, 0, 1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y,     1).tex(0, 1).normal(n, 0, 0, 1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y + 1, 1).tex(0, 0).normal(n, 0, 0, 1).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 1).tex(1, 0).normal(n, 0, 0, 1).color(1, 1, 1, 1).endVertex();

		t.pos(m, 1, y,     1).tex(1, 1).normal(n, 1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y,     0).tex(0, 1).normal(n, 1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y + 1, 0).tex(0, 0).normal(n, 1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y + 1, 1).tex(1, 0).normal(n, 1, 0, 0).color(1, 1, 1, 1).endVertex();

		t.pos(m, 0, y,     0).tex(1, 1).normal(n, -1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y,     1).tex(0, 1).normal(n, -1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 1).tex(0, 0).normal(n, -1, 0, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 0).tex(1, 0).normal(n, -1, 0, 0).color(1, 1, 1, 1).endVertex();

		t.pos(m, 1, y,     0).tex(1, 1).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y,     1).tex(0, 1).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y,     1).tex(0, 0).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y,     0).tex(1, 0).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();

		t.pos(m, 1, y + 1, 1).tex(1, 1).normal(n, 0,  1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 1, y + 1, 0).tex(0, 1).normal(n, 0,  1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 0).tex(0, 0).normal(n, 0,  1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 0, y + 1, 1).tex(1, 0).normal(n, 0,  1, 0).color(1, 1, 1, 1).endVertex();
		t.finish();
	}

	private RenderTypes<RenderMode> types;

	public void load() {
		types = getRenderTypes();
	}

	public void putRenderTypes(RenderTypes<RenderMode> types) {
		if(this.types == null)load();
		types.putAll(this.types);
	}

	protected float getScale() {return 1;}
	protected abstract void preRender();
	protected abstract ModelDefinition getDefinition();
	protected AnimationMode getAnimMode() {return AnimationMode.PLAYER;}
	protected Set<PlayerModelLayer> getArmorLayers() {return Collections.emptySet();}
	public DisplayItem getHeldItem(ItemSlot hand) {return DisplayItem.NONE;}
	protected boolean applyLighting() {return true;}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y)) {
			this.mx = x;
			this.my = y;
			this.enableDrag = true;
			this.dragMode = gui.isShiftDown();
			return true;
		} else if(bounds.isInBounds(x, y)){
			ViewportCamera cam = getCamera();
			cam.position.x = 0.5f;
			cam.position.y = 1;
			cam.position.z = 0.5f;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseRelease(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y)) {
			enableDrag = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(btn == EditorGui.getRotateMouseButton() && bounds.isInBounds(x, y) && enableDrag) {
			ViewportCamera cam = getCamera();
			if(dragMode) {
				float yaw = cam.look.getYaw();
				double px = 0, pz = 0;
				int dx = x - mx;
				int dy = y - my;
				float move = -1 / cam.camDist;
				if ( dx != 0) {
					px += Math.sin(yaw - Math.PI / 2) * -1.0f * dx * move;
					pz += Math.cos(yaw - Math.PI / 2) * dx * move;
				}

				if(dy != 0) {
					px += Math.sin(yaw) * -1.0f * dy * move;
					pz += Math.cos(yaw) * dy * move;
				}

				float f = 1 - cam.look.y;
				Vec3f by = new Vec3f((float) (px * cam.look.y), 0, (float) (pz * cam.look.y));
				Vec3f by1 = by.mul(dy * 0.1f * f);
				cam.position.x += px + by1.x;
				cam.position.y += -f * move * dy;
				cam.position.z += pz + by1.z;
			} else {
				float pitch = (float) Math.asin(cam.look.y);
				float yaw = cam.look.getYaw();
				if(Float.isNaN(pitch))pitch = 0;
				if(Float.isNaN(yaw))yaw = 0;
				yaw += Math.toRadians(x - mx);
				pitch -= Math.toRadians(y - my);
				yaw = (float) MathHelper.clamp(yaw, -Math.PI, Math.PI);
				pitch = (float) MathHelper.clamp(pitch, -Math.PI/2, Math.PI/2);
				cam.look.y = (float) Math.sin(pitch);

				double sin = Math.sin(yaw);
				double cos = Math.cos(yaw);
				cam.look.x = (float) cos;
				cam.look.z = (float) sin;
			}
			this.mx = x;
			this.my = y;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseWheel(int x, int y, int dir) {
		if(bounds.isInBounds(x, y)) {
			zoom(dir);
			return true;
		}
		return false;
	}

	private void zoom(int dir) {
		ViewportCamera cam = getCamera();
		cam.camDist += (dir * (cam.camDist / 16f));
		if(cam.camDist < 32)cam.camDist = 32;
		if(cam.camDist > 4096)cam.camDist = 4096;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(!event.isConsumed() && bounds.isInBounds(mouseCursorPos)) {
			if(event.matches("+")) {
				zoom(1);
			} else if(event.matches("-")) {
				zoom(-1);
			} else if(event.matches("r")) {
				ViewportCamera cam = getCamera();
				cam.camDist = 64;
				cam.position = new Vec3f(0.5f, 1, 0.5f);
				cam.look = new Vec3f(0.25f, 0.5f, 0.25f);
			}
		}
	}
}
