package com.tom.cpm.client;

import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.CommonProxy;
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

public class ClientProxy extends CommonProxy {
	public static final ResourceLocation DEFAULT_CAPE = new ResourceLocation("cpm:textures/template/cape.png");
	public static boolean optifineLoaded;
	public static ClientProxy INSTANCE = null;
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public RenderManager<GameProfile, PlayerEntity, Model, IRenderTypeBuffer> manager;
	public NetHandler<ResourceLocation, CompoundNBT, PlayerEntity, PacketBuffer, ClientPlayNetHandler> netHandler;

	@Override
	public void init() {
		super.init();
		INSTANCE = this;
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft);
		optifineLoaded = OFDetector.doApply();
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerEntity::getGameProfile);
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setNewNbt(CompoundNBT::new);
		netHandler.setNewPacketBuffer(() -> new PacketBuffer(Unpooled.buffer()));
		netHandler.setWriteCompound(PacketBuffer::writeNbt, PacketBuffer::readNbt);
		netHandler.setNBTSetters(CompoundNBT::putBoolean, CompoundNBT::putByteArray, CompoundNBT::putFloat);
		netHandler.setNBTGetters(CompoundNBT::getBoolean, CompoundNBT::getByteArray, CompoundNBT::getFloat);
		netHandler.setContains(CompoundNBT::contains);
		netHandler.setExecutor(() -> minecraft);
		netHandler.setSendPacket((c, rl, pb) -> c.send(new CCustomPayloadPacket(rl, pb)), null);
		netHandler.setPlayerToLoader(PlayerEntity::getGameProfile);
		netHandler.setReadPlayerId(PacketBuffer::readVarInt, id -> {
			Entity ent = Minecraft.getInstance().level.getEntity(id);
			if(ent instanceof PlayerEntity) {
				return (PlayerEntity) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(c -> ((ClientPlayerEntity)c).connection);
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, scr) -> new GuiImpl(SettingsGui::new, scr));
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.getPlayer(), event.getBuffers());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.tryUnbind();
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.getGui() instanceof MainMenuScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getGui() instanceof CustomizeSkinScreen) {
			evt.addWidget(new Button(0, 0, () -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, evt.getGui()))));
		}
	}

	public void renderHand(IRenderTypeBuffer buffer) {
		manager.bindHand(Minecraft.getInstance().player, buffer);
	}

	public void renderSkull(Model skullModel, GameProfile profile, IRenderTypeBuffer buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}

	public void renderElytra(PlayerEntity player, IRenderTypeBuffer buffer, ElytraModel<LivingEntity> model) {
		manager.bindElytra(player, buffer, model);
	}

	public void renderArmor(BipedModel<LivingEntity> modelArmor, BipedModel<LivingEntity> modelLeggings,
			PlayerEntity player, IRenderTypeBuffer bufferIn) {
		manager.bindArmor(player, bufferIn, modelArmor, 1);
		manager.bindArmor(player, bufferIn, modelLeggings, 2);
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

		for (Entry<Integer, KeyBinding> e : KeyBindings.quickAccess.entrySet()) {
			if(e.getValue().consumeClick()) {
				mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
			}
		}
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.getGui() == null && minecraft.screen instanceof GuiImpl.Overlay) {
			openGui.setGui(((GuiImpl.Overlay) minecraft.screen).getGui());
		}
		if(openGui.getGui() instanceof MainMenuScreen && EditorGui.doOpenEditor()) {
			openGui.setGui(new GuiImpl(EditorGui::new, openGui.getGui()));
		}
	}

	public static class Button extends net.minecraft.client.gui.widget.button.Button {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslationTextComponent("button.cpm.open_editor"), b -> r.run());
		}

	}

	@SubscribeEvent
	public void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
		mc.getDefinitionLoader().clearServerData();
	}

	public void unbind(Model model) {
		manager.tryUnbind(model);
	}

	//Copy from CapeLayer
	public static void renderCape(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn,
			AbstractClientPlayerEntity playerIn, float partialTicks, PlayerModel<AbstractClientPlayerEntity> model,
			ModelDefinition modelDefinition) {
		matrixStackIn.pushPose();

		float f1, f2, f3;

		if(playerIn != null) {
			double d0 = MathHelper.lerp(partialTicks, playerIn.xCloakO,
					playerIn.xCloak)
					- MathHelper.lerp(partialTicks, playerIn.xo, playerIn.getX());
			double d1 = MathHelper.lerp(partialTicks, playerIn.yCloakO,
					playerIn.yCloak)
					- MathHelper.lerp(partialTicks, playerIn.yo, playerIn.getY());
			double d2 = MathHelper.lerp(partialTicks, playerIn.zCloakO,
					playerIn.zCloak)
					- MathHelper.lerp(partialTicks, playerIn.zo, playerIn.getZ());
			float f = playerIn.yBodyRotO
					+ (playerIn.yBodyRot - playerIn.yBodyRotO);
			double d3 = MathHelper.sin(f * 0.017453292F);
			double d4 = (-MathHelper.cos(f * 0.017453292F));
			f1 = (float) d1 * 10.0F;
			f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
			f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
			f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
			f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
			f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
			if (f2 < 0.0F) {
				f2 = 0.0F;
			}

			float f4 = MathHelper.lerp(partialTicks, playerIn.oBob, playerIn.bob);
			f1 += MathHelper.sin(MathHelper.lerp(partialTicks, playerIn.walkDistO,
					playerIn.walkDist) * 6.0F) * 32.0F * f4;
			if (playerIn.isCrouching()) {
				f1 += 25.0F;
			}
			if (playerIn.getItemBySlot(EquipmentSlotType.CHEST).isEmpty()) {
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
}
