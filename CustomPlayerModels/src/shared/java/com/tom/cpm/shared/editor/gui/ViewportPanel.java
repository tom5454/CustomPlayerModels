package com.tom.cpm.shared.editor.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.model.render.VanillaPlayerModel;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class ViewportPanel extends ViewportPanelBase3d {
	protected Editor editor;

	public ViewportPanel(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void render(MatrixStack stack, VBuffers buf, float partialTicks) {
		if(editor.renderBase)renderBase(stack, buf);
		renderModel(stack, buf, partialTicks);
	}

	@Override
	public ViewportCamera getCamera() {
		return editor.camera;
	}

	@Override
	public void preRender() {
		editor.preRender();
	}

	@Override
	public ModelDefinition getDefinition() {
		return editor.definition;
	}

	@Override
	public boolean applyLighting() {
		return true;
	}

	@Override
	public DisplayItem getHeldItem(ItemSlot hand) {
		return editor.handDisplay.getOrDefault(hand, DisplayItem.NONE);
	}

	@Override
	public float getScale() {
		return editor.applyScaling ? editor.scaling : 1;
	}

	@Override
	public AnimationMode getAnimMode() {
		return AnimationMode.PLAYER;
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
		setHeldItem(this, ItemSlot.RIGHT_HAND, ap -> p.rightArmPose = ap);
		setHeldItem(this, ItemSlot.LEFT_HAND, ap -> p.leftArmPose = ap);
		PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);

		if(!editor.applyAnim && editor.playerTpose) {
			p.rightArm.zRot = (float) Math.toRadians(90);
			p.leftArm.zRot = (float) Math.toRadians(-90);
		}

		float lsa = 0.75f;
		float ls = editor.playVanillaAnims || editor.selectedAnim == null ? MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks) : 1;

		editor.applyRenderPoseForAnim(pose -> {
			switch (pose) {
			case SLEEPING:
				matrixstack.translate(0.0D, 1.501F, 0.0D);
				matrixstack.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90));
				matrixstack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(270.0F));
				break;

			case SNEAKING:
				p.crouching = true;
				PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
				break;

			case RIDING:
				p.riding = true;
				PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
				break;
			case CUSTOM:
			case DYING:
			case FALLING:
			case STANDING:
				break;

			case FLYING:
				matrixstack.translate(0.0D, 1.0D, -0.5d);
				matrixstack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				p.head.xRot = -(float)Math.PI / 4F;
				break;

			case RUNNING:
				PlayerModelSetup.setRotationAngles(p, ls, 1, Hand.RIGHT, false);
				break;

			case SWIMMING:
				PlayerModelSetup.setRotationAngles(p, ls, lsa, Hand.RIGHT, true);
				matrixstack.translate(0.0D, 1.0D, -0.5d);
				matrixstack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				break;

			case WALKING:
				PlayerModelSetup.setRotationAngles(p, ls, lsa, Hand.RIGHT, false);
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
	}

	private static void setHeldItem(ViewportPanelBase3d panel, ItemSlot hand, Consumer<ArmPose> pose) {
		switch (panel.getHeldItem(hand)) {
		case BLOCK:
		case SWORD:
		case SKULL:
			pose.accept(ArmPose.ITEM);
			break;
		case NONE:
		default:
			pose.accept(ArmPose.EMPTY);
			break;
		}
	}
}
