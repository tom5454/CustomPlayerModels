package com.tom.cpmcore;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerRenderManager;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.instance.renderSkull(skullModel, profile);
		}
	}

	public static void renderArmor(ModelBase in, Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, RendererLivingEntity<?> renderer) {
		if(in instanceof ModelBiped && renderer instanceof RenderPlayer) {
			ModelBiped player = (ModelBiped) renderer.getMainModel();
			PlayerRenderManager m = ClientProxy.mc.getPlayerRenderManager();
			ModelBiped armor = (ModelBiped) in;
			m.copyModelForArmor(player.bipedBody, armor.bipedBody);
			m.copyModelForArmor(player.bipedHead, armor.bipedHead);
			m.copyModelForArmor(player.bipedLeftArm, armor.bipedLeftArm);
			m.copyModelForArmor(player.bipedLeftLeg, armor.bipedLeftLeg);
			m.copyModelForArmor(player.bipedRightArm, armor.bipedRightArm);
			m.copyModelForArmor(player.bipedRightLeg, armor.bipedRightLeg);
			setNoSetup(armor, true);
		}
		in.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
	}

	public static void setNoSetup(ModelBiped model, boolean value) {
		throw new AbstractMethodError();//model.cpm$noModelSetup = value;
	}

	public static void postRenderSkull(ModelRenderer r, float scale, RenderPlayer rpe) {
		if(rpe != null) {
			rpe.getMainModel().bipedHead.postRender(scale);
		} else {
			r.postRender(scale);
		}
	}
}
