package com.tom.cpm.client.vr;

import org.vivecraft.client.render.VRPlayerModel;
import org.vivecraft.client.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.geom.ModelPart;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.render.ModelRenderManager.Field;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

public class RedirectHolderVRPlayer extends RDH<VRPlayerModel> {
	private RedirectRenderer<ModelPart> head;
	private boolean seated;

	public RedirectHolderVRPlayer(PlayerRenderManager mngr, VRPlayerModel model) {
		super(mngr, model);

		seated = !(model instanceof VRPlayerModel_WithArms);

		head = registerHead(createRendered(    () -> model.head     , v -> model.head      = v, PlayerModelParts.HEAD));
		register(createRendered(           () -> model.body     , v -> model.body      = v, PlayerModelParts.BODY));
		if(seated) {
			register(createRendered(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(createRendered( () -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
		} else {
			VRPlayerModel_WithArms w = (VRPlayerModel_WithArms) model;
			register(createRendered(() -> w.rightHand, v -> w.rightHand = v, PlayerModelParts.RIGHT_ARM));
			register(createRendered( () -> w.leftHand , v -> w.leftHand  = v, PlayerModelParts.LEFT_ARM));
		}
		register(createRendered(           () -> model.rightLeg , v -> model.rightLeg  = v, PlayerModelParts.RIGHT_LEG));
		register(createRendered(           () -> model.leftLeg  , v -> model.leftLeg   = v, PlayerModelParts.LEFT_LEG));

		register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);
		register(new Field<>(() -> model.leftSleeve , v -> model.leftSleeve  = v, null));
		register(new Field<>(() -> model.rightSleeve, v -> model.rightSleeve = v, null));
		register(new Field<>(() -> model.leftPants  , v -> model.leftPants   = v, null));
		register(new Field<>(() -> model.rightPants , v -> model.rightPants  = v, null));
		register(new Field<>(() -> model.jacket     , v -> model.jacket      = v, null));

		//register(new Field<>(() -> model.vrHMD, v -> model.vrHMD = v, null));//disable
		if(!seated) {
			VRPlayerModel_WithArms w = (VRPlayerModel_WithArms) model;
			register(new Field<>(() -> w.leftArm , v -> w.leftArm  = v, null));//disable
			register(new Field<>(() -> w.rightArm, v -> w.rightArm = v, null));//disable

			register(new Field<>(() -> w.leftHandSleeve , v -> w.leftHandSleeve  = v, null));//disable
			register(new Field<>(() -> w.rightHandSleeve, v -> w.rightHandSleeve = v, null));//disable
		}
		//register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
	}

	@Override
	protected void setupTransform(MatrixStack stack, RedirectRenderer<ModelPart> part, boolean pre) {
	}
}
