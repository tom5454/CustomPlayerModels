package com.tom.cpmcore;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.ClientProxy;
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
			NetworkHandler.handlePacket(pckt, handler, true);
			return true;
		}
		return false;
	}

	public static boolean onServerPacket(C17PacketCustomPayload pckt, NetHandlerPlayServer handler) {
		if(pckt.getChannelName().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			NetworkHandler.handlePacket(pckt, handler, false);
			return true;
		}
		return false;
	}
}
