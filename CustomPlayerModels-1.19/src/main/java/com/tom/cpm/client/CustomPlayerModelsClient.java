package com.tom.cpm.client;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.IText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.gui.SettingsGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

import io.netty.buffer.Unpooled;

public class CustomPlayerModelsClient {
	public static final ResourceLocation DEFAULT_CAPE = new ResourceLocation("cpm:textures/template/cape.png");
	public static boolean optifineLoaded;
	public static final CustomPlayerModelsClient INSTANCE = new CustomPlayerModelsClient();
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public RenderManager<GameProfile, net.minecraft.world.entity.player.Player, Model, MultiBufferSource> manager;
	public NetHandler<ResourceLocation, net.minecraft.world.entity.player.Player, ClientPacketListener> netHandler;

	public void init() {
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft);
		optifineLoaded = OFDetector.doApply();
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerShaders);
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), net.minecraft.world.entity.player.Player::getGameProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setExecutor(() -> minecraft);
		netHandler.setSendPacket(d -> new FriendlyByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new ServerboundCustomPayloadPacket(rl, pb)), null);
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
		netHandler.setDisplayText(this::onMessage);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, scr) -> new GuiImpl(SettingsGui::new, scr)));
	}

	private void onMessage(IText f) {
		Registry<ChatType> registry = minecraft.level.registryAccess().registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
		ChatType messageType = registry.get(ChatType.SYSTEM);
		minecraft.gui.handleSystemChat(messageType, f.remap());
	}

	public static void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(net.minecraft.world.entity.player.Player.class).
		renderApi(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class, ModelTexture::new).
		localModelApi(GameProfile::new).init();
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.getEntity(), event.getMultiBufferSource(), event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.unbindClear(event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void initGui(ScreenEvent.Init.Post evt) {
		if((evt.getScreen() instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getScreen() instanceof SkinCustomizationScreen) {
			Button btn = new Button(0, 0, () -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, evt.getScreen())));
			evt.addListener(btn);
			((List) evt.getScreen().children()).add(btn);
		}
	}

	public void renderHand(MultiBufferSource buffer, PlayerModel model) {
		manager.bindHand(Minecraft.getInstance().player, buffer, model);
	}

	public void renderSkull(Model skullModel, GameProfile profile, MultiBufferSource buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}

	public void renderElytra(HumanoidModel<LivingEntity> player, ElytraModel<LivingEntity> model) {
		manager.bindElytra(player, model);
	}

	public void renderArmor(HumanoidModel<LivingEntity> modelArmor, HumanoidModel<LivingEntity> modelLeggings,
			HumanoidModel<LivingEntity> player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
	}

	@SubscribeEvent
	public void renderTick(RenderTickEvent evt) {
		if(evt.phase == Phase.START) {
			mc.getPlayerRenderManager().getAnimationEngine().update(evt.renderTickTime);
		}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if(evt.phase == Phase.START && !minecraft.isPaused()) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();
		}
		if (minecraft.player == null || evt.phase == Phase.START)
			return;

		if(KeyBindings.gestureMenuBinding.consumeClick()) {
			Minecraft.getInstance().setScreen(new GuiImpl(GestureGui::new, null));
		}

		if(KeyBindings.renderToggleBinding.consumeClick()) {
			Player.setEnableRendering(!Player.isEnableRendering());
		}

		for (Entry<Integer, KeyMapping> e : KeyBindings.quickAccess.entrySet()) {
			if(e.getValue().consumeClick()) {
				mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
			}
		}
	}

	@SubscribeEvent
	public void openGui(ScreenEvent.Opening openGui) {
		if(openGui.getScreen() == null && minecraft.screen instanceof GuiImpl.Overlay) {
			openGui.setNewScreen(((GuiImpl.Overlay) minecraft.screen).getGui());
		}
		if(openGui.getScreen() instanceof TitleScreen && EditorGui.doOpenEditor()) {
			openGui.setNewScreen(new GuiImpl(EditorGui::new, openGui.getScreen()));
		}
	}

	private void registerShaders(RegisterShadersEvent evt) {
	}

	private void registerShader(RegisterShadersEvent evt, String name, VertexFormat vertexFormat, Consumer<ShaderInstance> finish) {
		try {
			evt.registerShader(new ShaderInstance(evt.getResourceManager(), new ResourceLocation("cpm", name), vertexFormat), finish);
		} catch (IOException e) {
			Log.error("Failed to load cpm '" + name + "' shader", e);
		}
	}

	public static class Button extends net.minecraft.client.gui.components.Button {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, Component.translatable("button.cpm.open_editor"), b -> r.run());
		}

	}

	@SubscribeEvent
	public void onLogout(ClientPlayerNetworkEvent.LoggingOut evt) {
		mc.onLogOut();
	}

	public void updateJump() {
		if(minecraft.player.isOnGround() && minecraft.player.input.jumping) {
			manager.jump(minecraft.player);
		}
	}

	//Copy from CapeLayer
	public static void renderCape(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn,
			AbstractClientPlayer playerIn, float partialTicks, PlayerModel<AbstractClientPlayer> model,
			ModelDefinition modelDefinition) {
		matrixStackIn.pushPose();

		float f1, f2, f3;

		if(playerIn != null) {
			double d0 = Mth.lerp(partialTicks, playerIn.xCloakO,
					playerIn.xCloak)
					- Mth.lerp(partialTicks, playerIn.xo, playerIn.getX());
			double d1 = Mth.lerp(partialTicks, playerIn.yCloakO,
					playerIn.yCloak)
					- Mth.lerp(partialTicks, playerIn.yo, playerIn.getY());
			double d2 = Mth.lerp(partialTicks, playerIn.zCloakO,
					playerIn.zCloak)
					- Mth.lerp(partialTicks, playerIn.zo, playerIn.getZ());
			float f = playerIn.yBodyRotO
					+ (playerIn.yBodyRot - playerIn.yBodyRotO);
			double d3 = Mth.sin(f * 0.017453292F);
			double d4 = (-Mth.cos(f * 0.017453292F));
			f1 = (float) d1 * 10.0F;
			f1 = Mth.clamp(f1, -6.0F, 32.0F);
			f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
			f2 = Mth.clamp(f2, 0.0F, 150.0F);
			f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
			f3 = Mth.clamp(f3, -20.0F, 20.0F);
			if (f2 < 0.0F) {
				f2 = 0.0F;
			}

			float f4 = Mth.lerp(partialTicks, playerIn.oBob, playerIn.bob);
			f1 += Mth.sin(Mth.lerp(partialTicks, playerIn.walkDistO,
					playerIn.walkDist) * 6.0F) * 32.0F * f4;
			if (playerIn.isCrouching()) {
				f1 += 25.0F;
			}
			if (playerIn.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
				if (playerIn.isCrouching()) {
					model.cloak.z = 1.4F + 0.125F * 3;
					model.cloak.y = 1.85F + 1 - 0.125F * 4;
				} else {
					model.cloak.z = 0.0F + 0.125F * 16f;
					model.cloak.y = 0.0F;
				}
			} else if (playerIn.isCrouching()) {
				model.cloak.z = 0.3F + 0.125F * 16f;
				model.cloak.y = 0.8F + 0.3f;
			} else {
				model.cloak.z = -1.1F + 0.125F * 32f;
				model.cloak.y = -0.85F + 1;
			}
		} else {
			f1 = 0;
			f2 = 0;
			f3 = 0;
		}

		model.cloak.xRot = (float) -Math.toRadians(6.0F + f2 / 2.0F + f1);
		model.cloak.yRot = (float) Math.toRadians(180.0F - f3 / 2.0F);
		model.cloak.zRot = (float) Math.toRadians(f3 / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.cloak.xRot = 0;
		model.cloak.yRot = 0;
		model.cloak.zRot = 0;
		model.renderCloak(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

	public static interface PlayerNameTagRenderer<E extends Entity> {
		void cpm$renderNameTag(E entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn);
		EntityRenderDispatcher cpm$entityRenderDispatcher();
	}

	public static <E extends Entity> void renderNameTag(PlayerNameTagRenderer<E> r, E entityIn, GameProfile gprofile, String unique, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		double d0 = r.cpm$entityRenderDispatcher().distanceToSqr(entityIn);
		if (d0 < 100.0D) {
			FormatText st = INSTANCE.manager.getStatus(gprofile, unique);
			if(st != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0.0D, 1.3F, 0.0D);
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				r.cpm$renderNameTag(entityIn, st.remap(), matrixStackIn, bufferIn, packedLightIn);
				matrixStackIn.popPose();
			}
		}
	}
}
