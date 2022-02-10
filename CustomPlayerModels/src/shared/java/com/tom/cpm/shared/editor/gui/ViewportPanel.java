package com.tom.cpm.shared.editor.gui;

import java.util.HashSet;
import java.util.Set;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.RenderMode;
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
		editor.definition.renderingPanel = this;
		renderModel(stack, buf, partialTicks);
		editor.render(stack, buf, this);
		editor.definition.renderingPanel = null;
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
		return editor.drawParrots ? 3 : 0;
	}
}
