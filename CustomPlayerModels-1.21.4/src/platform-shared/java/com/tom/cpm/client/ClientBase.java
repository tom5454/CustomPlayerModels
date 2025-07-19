package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.mixinplugin.IrisDetector;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

public abstract class ClientBase {
	public static final ResourceLocation DEFAULT_CAPE = ResourceLocation.parse("cpm:textures/template/cape.png");
	public static boolean optifineLoaded, irisLoaded, vrLoaded;
	public static MinecraftObject mc;
	protected Minecraft minecraft;
	public RenderManager<GameProfile, net.minecraft.world.entity.player.Player, Model, MultiBufferSource> manager;
	public NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, net.minecraft.world.entity.player.Player, ClientPacketListener> netHandler;

	public void init0() {
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft);
		optifineLoaded = OFDetector.doApply();
		vrLoaded = Platform.isModLoaded("vivecraft");
		irisLoaded = IrisDetector.doApply();
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		if(vrLoaded)Log.info("ViveCraft detected, enabling ViveCraft compatibility");
		if(irisLoaded)Log.info("Iris detected, enabling iris compatibility");
	}

	public void init1() {
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerProfile::getPlayerProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::value);
		netHandler = new NetHandler<>((k, v) -> new CustomPacketPayload.Type<>(ResourceLocation.tryBuild(k, v)));
		netHandler.setExecutor(() -> minecraft);
		netHandler.setSendPacketClient(Function.identity(), (c, rl, pb) -> c.send(new ServerboundCustomPayloadPacket(new ByteArrayPayload(rl, pb))));
		netHandler.setPlayerToLoader(net.minecraft.world.entity.player.Player::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getInstance().level.getEntity(id);
			if(ent instanceof net.minecraft.world.entity.player.Player) {
				return (net.minecraft.world.entity.player.Player) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(c -> ((LocalPlayer)c).connection);
		netHandler.setDisplayText(t -> minecraft.player.displayClientMessage(t.remap(), false));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
	}

	public static void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(net.minecraft.world.entity.player.Player.class, net.minecraft.world.entity.player.Player::getUUID).
		renderApi(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class, ModelTexture::new).
		localModelApi(GameProfile::new).init();
	}

	public void playerRenderPost(MultiBufferSource buffer, PlayerModel model) {
		manager.unbindClear(model);
	}

	public void renderHand(MultiBufferSource buffer, PlayerModel model) {
		renderHand(minecraft.player, buffer, model);
	}

	public void renderHand(AbstractClientPlayer pl, MultiBufferSource buffer, PlayerModel model) {
		manager.bindHand(pl, buffer, model);
	}

	public void renderHandPost(MultiBufferSource buffer, HumanoidModel model) {
		manager.unbindClear(model);
	}

	public void renderSkull(Model skullModel, GameProfile profile, MultiBufferSource buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}

	public void renderSkullPost(MultiBufferSource buffer, Model model) {
		manager.unbindFlush(model);
	}

	public void renderElytra(HumanoidModel<HumanoidRenderState> player, ElytraModel model) {
		manager.bindElytra(player, model);
	}

	public void renderArmor(HumanoidModel<HumanoidRenderState> modelArmor, HumanoidModel<HumanoidRenderState> modelLeggings,
			HumanoidModel<HumanoidRenderState> player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
	}

	public void updateJump() {
		if(minecraft.player.onGround() && minecraft.player.input.keyPresses.jump()) {
			manager.jump(minecraft.player);
		}
	}
}
