package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
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
	public static final Identifier DEFAULT_CAPE = Identifier.parse("cpm:textures/template/cape.png");
	public static boolean optifineLoaded, irisLoaded, vrLoaded;
	public static MinecraftObject mc;
	public RenderManager<GameProfile, Avatar, Model, Void> manager;
	public NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, Avatar, ClientPacketListener> netHandler;

	public void init0() {
		mc = new MinecraftObject();
		optifineLoaded = OFDetector.doApply();
		vrLoaded = Platform.isModLoaded("vivecraft");
		irisLoaded = IrisDetector.doApply();
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		if(vrLoaded)Log.info("ViveCraft detected, enabling ViveCraft compatibility");
		if(irisLoaded)Log.info("Iris detected, enabling iris compatibility");
	}

	public void init1() {
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerProfile::getPlayerProfile);
		manager.setGPGetters(GameProfile::properties, Property::value);
		netHandler = new NetHandler<>((k, v) -> new CustomPacketPayload.Type<>(Identifier.tryBuild(k, v)));
		netHandler.setExecutor(Minecraft::getInstance);
		netHandler.setSendPacketClient(Function.identity(), (c, rl, pb) -> c.send(new ServerboundCustomPayloadPacket(new ByteArrayPayload(rl, pb))));
		netHandler.setPlayerToLoader(PlayerProfile::getPlayerProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getInstance().level.getEntity(id);
			if(ent instanceof Avatar a) {
				return a;
			}
			return null;
		});
		netHandler.setGetClient(() -> Minecraft.getInstance().player);
		netHandler.setGetNet(c -> ((LocalPlayer)c).connection);
		netHandler.setDisplayText(t -> Minecraft.getInstance().player.displayClientMessage(t.remap(), false));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		if (irisLoaded)IrisPipelineSetup.setup();
	}

	public static void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(net.minecraft.world.entity.player.Player.class, net.minecraft.world.entity.player.Player::getUUID).
		//renderApi(Model.class, Identifier.class, RenderType.class, MultiBufferSource.class, GameProfile.class, ModelTexture::new).
		localModelApi(GameProfile::new).init();
	}

	public void playerRenderPre(PlayerRenderStateAccess sa, PlayerModel model, AvatarRenderState renderState) {
		CustomPlayerModelsClient.INSTANCE.manager.bindPlayerState(sa.cpm$getPlayer(), null, model, null);
		model.setupAnim(renderState);
		mc.getPlayerRenderManager().setModelPose(model);
	}

	public void playerRenderPost(PlayerModel model) {
		manager.unbindClear(model);
	}

	public void renderHand(PlayerModel model) {
		renderHand(Minecraft.getInstance().player, model);
	}

	public void renderHand(AbstractClientPlayer pl, PlayerModel model) {
		manager.bindHand(pl, null, model);
	}

	public void renderHandPost(HumanoidModel model) {
		manager.unbindClear(model);
	}

	public void renderElytra(HumanoidModel<HumanoidRenderState> player, ElytraModel model) {
		manager.bindElytra(player, model);
	}

	public void renderArmor(ArmorModelSet<HumanoidModel<HumanoidRenderState>> modelSet,
			HumanoidModel<HumanoidRenderState> player) {
		manager.bindArmor(player, modelSet.head(), 1);
		manager.bindArmor(player, modelSet.legs(), 2);
		manager.bindArmor(player, modelSet.chest(), 3);
		manager.bindArmor(player, modelSet.feet(), 4);
	}

	public void updateJump() {
		var minecraft = Minecraft.getInstance();
		if(minecraft.player.onGround() && minecraft.player.input.keyPresses.jump()) {
			manager.jump(minecraft.player);
		}
	}
}
