package com.tom.cpm.client.vr;

import org.vivecraft.client.render.VRPlayerModel;
import org.vivecraft.client.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.render.ModelRenderManager.Field;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

public class RedirectHolderVRPlayer extends RDH<VRPlayerModel<AbstractClientPlayer>> {
	private RedirectRenderer<ModelPart> head;
	private RedirectRenderer<ModelPart> leftArm;
	private RedirectRenderer<ModelPart> rightArm;
	private boolean seated;

	public RedirectHolderVRPlayer(PlayerRenderManager mngr, VRPlayerModel<AbstractClientPlayer> model) {
		super(mngr, model);

		seated = !(model instanceof VRPlayerModel_WithArms);

		head = registerHead(new Field<>(    () -> model.head     , v -> model.head      = v, PlayerModelParts.HEAD));
		register(new Field<>(           () -> model.body     , v -> model.body      = v, PlayerModelParts.BODY));
		if(seated) {
			rightArm = register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
		} else {
			VRPlayerModel_WithArms<AbstractClientPlayer> w = (VRPlayerModel_WithArms<AbstractClientPlayer>) model;
			rightArm = register(new Field<>(() -> w.rightHand, v -> w.rightHand = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> w.leftHand , v -> w.leftHand  = v, PlayerModelParts.LEFT_ARM));
		}
		register(new Field<>(           () -> model.rightLeg , v -> model.rightLeg  = v, PlayerModelParts.RIGHT_LEG));
		register(new Field<>(           () -> model.leftLeg  , v -> model.leftLeg   = v, PlayerModelParts.LEFT_LEG));

		register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);
		register(new Field<>(() -> model.leftSleeve , v -> model.leftSleeve  = v, null));
		register(new Field<>(() -> model.rightSleeve, v -> model.rightSleeve = v, null));
		register(new Field<>(() -> model.leftPants  , v -> model.leftPants   = v, null));
		register(new Field<>(() -> model.rightPants , v -> model.rightPants  = v, null));
		register(new Field<>(() -> model.jacket     , v -> model.jacket      = v, null));

		register(new Field<>(() -> model.vrHMD, v -> model.vrHMD = v, null));//disable
		if(!seated) {
			VRPlayerModel_WithArms<AbstractClientPlayer> w = (VRPlayerModel_WithArms<AbstractClientPlayer>) model;
			register(new Field<>(() -> w.leftShoulder , v -> w.leftShoulder  = v, null));//disable
			register(new Field<>(() -> w.rightShoulder, v -> w.rightShoulder = v, null));//disable

			register(new Field<>(() -> w.leftShoulder_sleeve , v -> w.leftShoulder_sleeve  = v, null));//disable
			register(new Field<>(() -> w.rightShoulder_sleeve, v -> w.rightShoulder_sleeve = v, null));//disable
		}

		//register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
	}

	@Override
	protected void setupTransform(MatrixStack stack, RedirectRenderer<ModelPart> part, boolean pre) {
		if(!pre && (leftArm == part || rightArm == part) && !seated) {
			stack.translate(0, -8 / 16f, 0);
		}
	}

	@Override
	protected ModelPart getRoot() {
		return null;
	}

	@Override
	protected void setRoot(ModelPart part) {

	}
}
