package com.tom.cpm.client.vr;

import org.vivecraft.render.VRPlayerModel;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.model.ModelRenderer;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager.Field;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

public class RedirectHolderVRPlayer extends RDH {
	private RedirectRenderer<ModelRenderer> head;
	private RedirectRenderer<ModelRenderer> leftArm;
	private RedirectRenderer<ModelRenderer> rightArm;

	public RedirectHolderVRPlayer(PlayerRenderManager mngr, VRPlayerModel<AbstractClientPlayerEntity> model) {
		super(mngr, model);

		head = registerHead(new Field<>(    () -> model.head     , v -> model.head      = v, PlayerModelParts.HEAD));
		register(new Field<>(           () -> model.body     , v -> model.body      = v, PlayerModelParts.BODY));
		if(model.seated) {
			rightArm = register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
		} else {
			rightArm = register(new Field<>(() -> model.rightHand, v -> model.rightHand = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> model.leftHand , v -> model.leftHand  = v, PlayerModelParts.LEFT_ARM));
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
		if(!model.seated) {
			register(new Field<>(() -> model.leftShoulder , v -> model.leftShoulder  = v, null));//disable
			register(new Field<>(() -> model.rightShoulder, v -> model.rightShoulder = v, null));//disable
		}

		register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
	}

	@Override
	protected void setupTransform(MatrixStack stack, RedirectRenderer<ModelRenderer> part, boolean pre) {
		if(!pre && (leftArm == part || rightArm == part) && !((VRPlayerModel<AbstractClientPlayerEntity>)model).seated) {
			stack.translate(0, -8 / 16f, 0);
		}
	}
}
