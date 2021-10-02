package com.tom.cpmcore;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.NetH;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.INSTANCE.renderSkull(skullModel, profile);
		}
	}

	public static void renderSkullPost(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.INSTANCE.unbind(skullModel);
		}
	}

	public static void unbindHand(AbstractClientPlayer player) {
		ClientProxy.INSTANCE.unbindHand(player);
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
			CPMClientAccess.setNoSetup(armor, true);
		}
		in.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
	}

	public static void postRenderSkull(ModelRenderer r, float scale, RenderPlayer rpe) {
		if(rpe != null) {
			rpe.getMainModel().bipedHead.postRender(scale);
		} else {
			r.postRender(scale);
		}
	}

	public static void onLogout() {
		ClientProxy.INSTANCE.onLogout();
	}

	public static boolean onClientPacket(S3FPacketCustomPayload pckt, NetHandlerPlayClient handler) {
		if(pckt.getChannelName().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			ClientProxy.INSTANCE.netHandler.receiveClient(new ResourceLocation(pckt.getChannelName()), pckt.getBufferData(), (NetH) handler);
			return true;
		}
		return false;
	}

	public static void onArmorPre(LayerArmorBase this0, EntityLivingBase entitylivingbaseIn) {
		if(entitylivingbaseIn instanceof AbstractClientPlayer) {
			ClientProxy.INSTANCE.renderArmor(this0.field_177186_d, this0.field_177189_c, (EntityPlayer) entitylivingbaseIn);
		}
	}

	public static void onArmorPost(LayerArmorBase this0, EntityLivingBase entitylivingbaseIn) {
		if(entitylivingbaseIn instanceof AbstractClientPlayer) {
			ClientProxy.INSTANCE.unbind(this0.field_177186_d);
			ClientProxy.INSTANCE.unbind(this0.field_177189_c);
		}
	}

	public static boolean renderCape(LayerCape this0, AbstractClientPlayer player, float partialTicks) {
		Player<?, ?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				if(!player.isInvisible() && player.isWearing(EnumPlayerModelParts.CAPE)) {
					ModelPlayer model = this0.playerRenderer.getMainModel();
					ClientProxy.mc.getPlayerRenderManager().rebindModel(model);
					ClientProxy.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
					ClientProxy.renderCape(player, partialTicks, model, def);
				}
				return true;
			}
		}
		return false;
	}

	public static boolean renderCape(LayerCape this0, EntityLivingBase entitylivingbaseIn, float partialTicks) {
		return renderCape(this0, (AbstractClientPlayer) entitylivingbaseIn, partialTicks);
	}
}
