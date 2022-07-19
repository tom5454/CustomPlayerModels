package com.tom.cpm.client.vr;

import org.vivecraft.render.VRPlayerModel;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

public class VRPlayerRenderer {

	public static boolean isVRPlayer(Object model) {
		return model instanceof VRPlayerModel;
	}

	public static RedirectHolder<?, MultiBufferSource, ModelTexture, ModelPart> createVRPlayer(PlayerRenderManager mngr, Object model) {
		return new RedirectHolderVRPlayer(mngr, (VRPlayerModel<AbstractClientPlayer>) model);
	}
}
