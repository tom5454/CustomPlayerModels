package com.tom.cpmcore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.RetroGL;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.NetH;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, GameProfile profile) {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
		if(profile != null) {
			ClientProxy.INSTANCE.renderSkull(skullModel, profile);
		}
	}

	public static void renderSkullPost(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.INSTANCE.manager.unbind(skullModel);
		}
	}

	public static void loadSkinHook(MinecraftProfileTexture tex, final Type type, final SkinManager.SkinAvailableCallback cb) {
		if(cb instanceof PlayerProfile.SkinCB) {
			((PlayerProfile.SkinCB)cb).skinAvailable(type, new ResourceLocation("skins/" + tex.getHash()), tex);
		}
	}

	public static void renderPass(ModelBase model, Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_, RendererLivingEntity r, int callLoc) {
		RetroGL.renderCallLoc = callLoc;
		if(r instanceof RenderPlayer && model instanceof ModelBiped) {
			RenderPlayer rp = (RenderPlayer) r;
			if(model == rp.modelArmor || model == rp.modelArmorChestplate) {
				PlayerRenderManager m = ClientProxy.mc.getPlayerRenderManager();
				ModelBiped player = rp.modelBipedMain;
				ModelBiped armor = (ModelBiped) model;
				m.copyModelForArmor(player.bipedBody, armor.bipedBody);
				m.copyModelForArmor(player.bipedHead, armor.bipedHead);
				m.copyModelForArmor(player.bipedLeftArm, armor.bipedLeftArm);
				m.copyModelForArmor(player.bipedLeftLeg, armor.bipedLeftLeg);
				m.copyModelForArmor(player.bipedRightArm, armor.bipedRightArm);
				m.copyModelForArmor(player.bipedRightLeg, armor.bipedRightLeg);
				CPMClientAccess.setNoSetup(armor, true);
			}
		}
		model.render(p_78088_1_, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_);
	}

	public static void onLogout() {
		ClientProxy.INSTANCE.onLogout();
	}

	public static boolean onClientPacket(S3FPacketCustomPayload pckt, NetHandlerPlayClient handler) {
		if(pckt.func_149169_c().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			ClientProxy.INSTANCE.netHandler.receiveClient(new ResourceLocation(pckt.func_149169_c()), new FastByteArrayInputStream(pckt.func_149168_d()), (NetH) handler);
			return true;
		}
		return false;
	}

	public static void glColor4f(float r, float g, float b, float a) {
		RetroGL.color4f(r, g, b, a);
	}

	public static void glColor3f(float r, float g, float b) {
		RetroGL.color4f(r, g, b, 1);
	}

	public static void prePlayerRender() {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
	}

	public static void onHandPre(RenderPlayer this0, EntityPlayer player) {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
		ClientProxy.INSTANCE.manager.bindHand(player, null, this0.modelBipedMain);
		ClientProxy.INSTANCE.manager.bindSkin(this0.modelBipedMain, TextureSheetType.SKIN);
	}

	public static void onHandPost(RenderPlayer this0, EntityPlayer player) {
		ClientProxy.INSTANCE.manager.unbindClear(this0.modelBipedMain);
	}

	public static boolean renderCape(boolean evtRC, RenderPlayer this0, AbstractClientPlayer player, float partialTicks) {
		if(evtRC) {
			Player<?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
			if(pl != null) {
				ModelDefinition def = pl.getModelDefinition();
				if(def != null && def.hasRoot(RootModelType.CAPE)) {
					if(!player.isInvisible() && !player.getHideCape()) {
						ModelBiped model = this0.modelBipedMain;
						ClientProxy.mc.getPlayerRenderManager().rebindModel(model);
						ClientProxy.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
						ClientProxy.renderCape(player, partialTicks, model, def);
					}
					return false;
				}
			}
		}
		return evtRC;
	}

	public static boolean onRenderPlayerModel(RenderPlayer this0, EntityLivingBase player0, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		AbstractClientPlayer player = (AbstractClientPlayer) player0;
		IPlayerRenderer this1 = (IPlayerRenderer) this0;
		boolean pBodyVisible = !player.isInvisible();
		boolean pTranslucent = !pBodyVisible && !player.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
		if(!pBodyVisible && ClientProxy.mc.getPlayerRenderManager().isBound(this0.modelBipedMain)) {
			boolean r = ClientProxy.mc.getPlayerRenderManager().getHolderSafe(this0.modelBipedMain, null, h -> h.setInvisState(), false, false);
			if(pTranslucent)return false;
			if(!r)return false;
			this1.cpm$bindEntityTexture(player);

			ClientProxy.mc.getPlayerRenderManager().getHolderSafe(this0.modelBipedMain, null, h -> h.setInvis(false), false);
			this0.modelBipedMain.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			return true;
		}
		return false;
	}
}
