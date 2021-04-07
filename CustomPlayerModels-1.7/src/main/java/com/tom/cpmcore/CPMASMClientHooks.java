package com.tom.cpmcore;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.INSTANCE.renderSkull(skullModel, profile);
		}
	}

	public static void loadSkinHook(MinecraftProfileTexture tex, final Type type, final SkinManager.SkinAvailableCallback cb) {
		if(cb instanceof PlayerProfile.SkinCB) {
			((PlayerProfile.SkinCB)cb).skinAvailable(type, new ResourceLocation("skins/" + tex.getHash()), tex);
		}
	}

	public static void renderPass(ModelBase model, Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_, RendererLivingEntity r) {
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
				setNoSetup(armor, true);
			}
		}
		model.render(p_78088_1_, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_);
	}

	public static void setNoSetup(ModelBiped model, boolean value) {
		throw new AbstractMethodError();//model.cpm$noModelSetup = value;
	}

	public static boolean hasMod(NetHandlerPlayClient handler) {
		throw new AbstractMethodError();//return handler.cpm$hasMod;
	}

	public static boolean hasMod(NetHandlerPlayServer handler) {
		throw new AbstractMethodError();//return handler.cpm$hasMod;
	}

	public static void setHasMod(NetHandlerPlayClient handler, boolean v) {
		throw new AbstractMethodError();//handler.cpm$hasMod = v;
	}

	public static void setHasMod(NetHandlerPlayServer handler, boolean v) {
		throw new AbstractMethodError();//handler.cpm$hasMod = v;
	}

	public static PlayerData getEncodedModelData(NetHandlerPlayServer handler) {
		throw new AbstractMethodError();//return handler.cpm$data;
	}

	public static void setEncodedModelData(NetHandlerPlayServer handler, PlayerData data) {
		throw new AbstractMethodError();//handler.cpm$data = data;
	}

	public static void onLogout() {
		ClientProxy.INSTANCE.onLogout();
	}

	public static boolean onClientPacket(S3FPacketCustomPayload pckt, NetHandlerPlayClient handler) {
		if(pckt.func_149169_c().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			NetworkHandler.handlePacket(pckt, handler, true);
			return true;
		}
		return false;
	}

	public static boolean onServerPacket(C17PacketCustomPayload pckt, NetHandlerPlayServer handler) {
		if(pckt.func_149559_c().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			NetworkHandler.handlePacket(pckt, handler, false);
			return true;
		}
		return false;
	}
}
