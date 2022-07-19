package com.tom.cpm.shared.gui.panel;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeybindHandler;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.MatrixStack;
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
import com.tom.cpm.shared.gui.Keybinds;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.BlockModel;
import com.tom.cpm.shared.model.builtin.IItemModel;
import com.tom.cpm.shared.model.builtin.IItemModel.ItemRenderTransform;
import com.tom.cpm.shared.model.builtin.ItemModel;
import com.tom.cpm.shared.model.builtin.ParrotModel;
import com.tom.cpm.shared.model.builtin.ShieldModel;
import com.tom.cpm.shared.model.builtin.SkullModel;
import com.tom.cpm.shared.model.builtin.SpyglassModel;
import com.tom.cpm.shared.model.builtin.TridentModel;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.GuiModelRenderManager;
import com.tom.cpm.shared.model.render.ItemTransform;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.util.PlayerModelLayer;

public abstract class ViewportPanelBase3d extends Panel3d {
	public static final GuiModelRenderManager manager = new GuiModelRenderManager();
	protected static final EnumMap<SkinType, VanillaPlayerModel> models = new EnumMap<>(SkinType.class);
	protected static final VanillaPlayerModel defaultModel;
	protected static final ParrotModel parrot = new ParrotModel();
	protected static final ShieldModel shield = new ShieldModel();
	protected static final BlockModel block = new BlockModel();
	protected static final TridentModel trident = new TridentModel();
	protected static final SkullModel skull = new SkullModel();
	protected static final ItemRenderTransform[] swordTransform = new ItemRenderTransform[] {
			new ItemRenderTransform(new Vec3f(0, 4, 0.5F), new Vec3f(0, -90, 55), new Vec3f(0.85F, 0.85F, 0.85F)),
			new ItemRenderTransform(new Vec3f(0, 4, 0.5F), new Vec3f(0, 90, -55), new Vec3f(0.85F, 0.85F, 0.85F))
	};
	protected static final ItemRenderTransform[] bowTransform = new ItemRenderTransform[] {
			new ItemRenderTransform(new Vec3f(-1, -2, 2.5F), new Vec3f(-80, 260, -40), new Vec3f(0.9F, 0.9F, 0.9F)),
			new ItemRenderTransform(new Vec3f(-1, -2, 2.5F), new Vec3f(-80, -280, 40), new Vec3f(0.9F, 0.9F, 0.9F))
	};
	protected static final ItemRenderTransform[] crossbowTransform = new ItemRenderTransform[] {
			new ItemRenderTransform(new Vec3f(2, 0.1F, -3), new Vec3f(-90, 0, -60), new Vec3f(0.9F, 0.9F, 0.9F)),
			new ItemRenderTransform(new Vec3f(2, 0.1F, -3), new Vec3f(-90, 0,  30), new Vec3f(0.9F, 0.9F, 0.9F))
	};
	protected static final ItemRenderTransform[] itemTransform = new ItemRenderTransform[] {
			new ItemRenderTransform(new Vec3f(0, 3, 1), new Vec3f(0, 0, 0), new Vec3f(0.55F, 0.55F, 0.55F))
	};
	protected static final ItemRenderTransform[] goatHornTransform = new ItemRenderTransform[] {
			new ItemRenderTransform(new Vec3f(-1, 2, 2), new Vec3f(0, -125, 0), new Vec3f(0.5F, 0.5F, 0.5F)),
			new ItemRenderTransform(new Vec3f( 1, 2, 2), new Vec3f(0,  125, 0), new Vec3f(0.5F, 0.5F, 0.5F))
	};
	protected static final ItemModel sword = new ItemModel("sword", swordTransform);
	protected static final ItemModel food = new ItemModel("food", itemTransform);
	protected static final IItemModel[] bow = new IItemModel[] {
			new ItemModel("bow_pulling_0", bowTransform),
			new ItemModel("bow_pulling_1", bowTransform),
			new ItemModel("bow_pulling_2", bowTransform)
	};
	protected static final IItemModel[] crossbow = new IItemModel[] {
			new ItemModel("crossbow", crossbowTransform),
			new ItemModel("crossbow_pulling_0", crossbowTransform),
			new ItemModel("crossbow_pulling_1", crossbowTransform),
			new ItemModel("crossbow_pulling_2", crossbowTransform)
	};
	protected static final SpyglassModel spyglass = new SpyglassModel();
	protected static final ItemModel goatHorn = new ItemModel("goat_horn", goatHornTransform);
	protected static final EnumMap<DisplayItem, IItemModel[]> itemModels = new EnumMap<>(DisplayItem.class);

	static {
		for (SkinType t : SkinType.VANILLA_TYPES) {
			models.put(t, new VanillaPlayerModel(t));
		}
		defaultModel = models.get(SkinType.DEFAULT);
		models.put(SkinType.UNKNOWN, defaultModel);
		itemModels.put(DisplayItem.BLOCK, new IItemModel[] {block});
		itemModels.put(DisplayItem.SHIELD, new IItemModel[] {shield});
		itemModels.put(DisplayItem.TRIDENT, new IItemModel[] {trident});
		itemModels.put(DisplayItem.SKULL, new IItemModel[] {skull});
		itemModels.put(DisplayItem.SWORD, new IItemModel[] {sword});
		itemModels.put(DisplayItem.FOOD, new IItemModel[] {food});
		itemModels.put(DisplayItem.BOW, bow);
		itemModels.put(DisplayItem.CROSSBOW, crossbow);
		itemModels.put(DisplayItem.SPYGLASS, new IItemModel[] {spyglass});
		itemModels.put(DisplayItem.GOAT_HORN, new IItemModel[] {goatHorn});
	}

	protected int mx, my;
	protected boolean enableDrag;
	protected boolean dragMode;
	protected int paintColor;

	public ViewportPanelBase3d(Frame frm) {
		super(frm);
		setBackgroundColor(gui.getColors().popup_background);
	}

	public void renderModel(MatrixStack stack, VBuffers buf, float partialTicks) {
		float scale = getScale();
		stack.push();
		stack.translate(0.5f, 0, 0.5f);
		stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
		stack.scale((-scale), -scale, scale);
		stack.translate(0, -1.5f, 0);

		ModelDefinition def = getDefinition();
		VanillaPlayerModel p = models.get(def.getSkinType());
		if(p == null)p = defaultModel;
		VBuffers rp = buf.replay();

		preRender(stack, rp);

		manager.bindModel(p, GuiModelRenderManager.PLAYER, rp, def, null, getAnimMode());
		manager.bindSkin(p, this, TextureSheetType.SKIN);

		poseModel(p, stack, partialTicks);

		p.render(stack, rp.getBuffer(types, RenderMode.DEFAULT));

		Set<PlayerModelLayer> layers = getArmorLayers();
		for(PlayerModelLayer l : PlayerModelLayer.VALUES) {
			if(layers.contains(l)) {
				p.poseLayer(l, layers);
			}
		}

		stack.push();
		ItemTransform tr = def.getTransform(ItemSlot.RIGHT_HAND);
		if(tr != null)
			stack.mul(tr.getMatrix());
		else
			p.rightArm.translateRotatePart(stack);
		renderItem(stack, rp, ItemSlot.RIGHT_HAND, getHeldItem(ItemSlot.RIGHT_HAND));
		stack.pop();

		stack.push();
		tr = def.getTransform(ItemSlot.LEFT_HAND);
		if(tr != null)
			stack.mul(tr.getMatrix());
		else
			p.leftArm.translateRotatePart(stack);
		renderItem(stack, rp, ItemSlot.LEFT_HAND, getHeldItem(ItemSlot.LEFT_HAND));
		stack.pop();

		stack.push();
		tr = def.getTransform(ItemSlot.HEAD);
		if(tr != null)
			stack.mul(tr.getMatrix());
		else
			p.head.translateRotatePart(stack);
		renderItem(stack, rp, ItemSlot.HEAD, getHeldItem(ItemSlot.HEAD));
		stack.pop();

		if((drawParrots() & 1) != 0) {
			stack.push();
			tr = def.getTransform(ItemSlot.LEFT_SHOULDER);
			if(tr != null)
				stack.mul(tr.getMatrix());
			stack.translate(0.4F, p.crouching ? (double)-1.3F : -1.5D, 0.0D);
			parrot.render(stack, rp.getBuffer(getRenderTypes(parrot.getTexture()), RenderMode.DEFAULT));
			stack.pop();
		}

		if((drawParrots() & 2) != 0) {
			stack.push();
			tr = def.getTransform(ItemSlot.RIGHT_SHOULDER);
			if(tr != null)
				stack.mul(tr.getMatrix());
			stack.translate(-0.4F, p.crouching ? (double)-1.3F : -1.5D, 0.0D);
			parrot.render(stack, rp.getBuffer(getRenderTypes(parrot.getTexture()), RenderMode.DEFAULT));
			stack.pop();
		}

		manager.unbindModel(p);

		for(PlayerModelLayer l : PlayerModelLayer.VALUES) {
			if(layers.contains(l)) {
				manager.bindModel(p, l.name(), rp, def, null, getAnimMode());
				manager.bindSkin(p, this, RootGroups.getGroup(l.parts[0]).getTexSheet(l.parts[0]));
				p.renderLayer(stack, rp.getBuffer(types, RenderMode.DEFAULT), l);
				manager.unbindModel(p);
			}
		}

		postRender(stack, rp);

		rp.finishAll();
		stack.pop();
	}

	public void renderItem(MatrixStack stack, VBuffers rp, ItemSlot hand, DisplayItem item) {
		IItemModel[] models = itemModels.get(item);
		if(models != null) {
			IItemModel model = models[getItemState(hand, models.length)];
			model.render(stack, rp.getBuffer(getRenderTypes(model.getTexture()), RenderMode.DEFAULT), hand);
		}
	}

	protected void poseModel(VanillaPlayerModel p, MatrixStack stack, float partialTicks) {
		p.reset();
		p.setAllVisible(true);
		PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
	}

	public void renderBase(MatrixStack stack, VBuffers buf) {
		RenderTypes<RenderMode> rt = getRenderTypes("area");
		VertexBuffer t = buf.getBuffer(rt, RenderMode.DEFAULT);
		Mat3f n = stack.getLast().getNormal();
		Mat4f m = stack.getLast().getMatrix();
		t.pos(m, 4,  0,  4).tex(1, 1).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, 4,  0, -3).tex(0, 1).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, -3, 0, -3).tex(0, 0).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
		t.pos(m, -3, 0,  4).tex(1, 0).normal(n, 0, -1, 0).color(1, 1, 1, 1).endVertex();
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

	protected RenderTypes<RenderMode> types;

	public void load() {
		types = getRenderTypes();
	}

	public void load(String tex) {
		types = getRenderTypes(tex);
	}

	public void putRenderTypes(RenderTypes<RenderMode> types) {
		if(this.types == null)load();
		types.putAll(this.types);
	}

	protected float getScale() {return 1;}
	protected abstract void preRender(MatrixStack stack, VBuffers buf);
	protected void postRender(MatrixStack stack, VBuffers buf) {}
	protected abstract ModelDefinition getDefinition();
	protected AnimationMode getAnimMode() {return AnimationMode.PLAYER;}
	protected Set<PlayerModelLayer> getArmorLayers() {return Collections.emptySet();}
	public DisplayItem getHeldItem(ItemSlot hand) {return DisplayItem.NONE;}
	protected int drawParrots() {return 0;}
	protected int getItemState(ItemSlot slot, int maxStates) {return 0;}

	protected boolean isRotate(int btn) {
		return true;
	}
	protected boolean isDrag(int btn) {
		return btn == 1;
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		super.mouseClick(evt);
		if(isRotate(evt.btn) && evt.isHovered(bounds)) {
			this.mx = evt.x;
			this.my = evt.y;
			this.enableDrag = true;
			this.dragMode = isDrag(evt.btn);
			evt.consume();
		}
	}

	@Override
	public void mouseRelease(MouseEvent evt) {
		super.mouseRelease(evt);
		if(isRotate(evt.btn) && evt.isHovered(bounds)) {
			enableDrag = false;
			evt.consume();
		}
	}

	@Override
	public void mouseDrag(MouseEvent evt) {
		super.mouseDrag(evt);
		if(isRotate(evt.btn) && evt.isHovered(bounds) && enableDrag) {
			int x = evt.x;
			int y = evt.y;
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
				if(yaw < -Math.PI)yaw += 2 * Math.PI;
				if(yaw > Math.PI)yaw -= 2 * Math.PI;
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
			evt.consume();
		}
	}

	@Override
	public void mouseWheel(MouseEvent evt) {
		if(evt.isHovered(bounds)) {
			zoom(evt.btn);
			evt.consume();
		}
		super.mouseWheel(evt);
	}

	private void zoom(int dir) {
		ViewportCamera cam = getCamera();
		cam.camDist += (dir * (cam.camDist / 16f));
		if(cam.camDist < 32)cam.camDist = 32;
		if(cam.camDist > 4096)cam.camDist = 4096;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(keysCanControlCamera()) {
			KeybindHandler h = gui.getFrame().getKeybindHandler();
			h.registerKeybind(Keybinds.ZOOM_IN_CAMERA, () -> zoom(1));
			h.registerKeybind(Keybinds.ZOOM_OUT_CAMERA, () -> zoom(-1));
			h.registerKeybind(Keybinds.RESET_CAMERA, () -> getCamera().reset());
		}
		super.keyPressed(event);
	}

	protected boolean keysCanControlCamera() {
		return true;
	}
}
