package com.tom.cpm.client;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.client.render.entity.LivingRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.helper.MathHelper;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.lefix.FixSSL;
import com.tom.cpm.retro.GameProfile;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.retro.NetHandlerExt;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

import turniplabs.halplibe.helper.CommandHelper;

public class CustomPlayerModelsClient implements ClientModInitializer, SidedHandler {
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public static CustomPlayerModelsClient INSTANCE;
	public RenderManager<GameProfile, EntityPlayer, ModelBase, Void> manager;
	public static NetHandlerExt<String, EntityPlayer, ClientNetworkImpl> netHandler = new NetHandlerExt<>((a, b) -> a + ":" + b);

	@Override
	public void onInitializeClient() {
		FixSSL.fixup();
		INSTANCE = this;
		minecraft = Minecraft.INSTANCE;
		CustomPlayerModels.proxy = this;
		mc = new MinecraftObject(minecraft);
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), e -> GameProfileManager.getProfile(e.username));
		netHandler.setExecutor(() -> MCExecutor.ex);
		netHandler.setSendPacketClient(ClientNetworkImpl::cpm$sendPacket);
		netHandler.setPlayerToLoader(e -> GameProfileManager.getProfile(e.username));
		netHandler.setGetPlayerById(id -> {
			Entity ent = EmulNetwork.getClient(minecraft.thePlayer).cpm$getEntityByID(id);
			if(ent instanceof EntityPlayer) {
				return (EntityPlayer) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.thePlayer);
		netHandler.setGetNet(EmulNetwork::getClient);
		netHandler.setDisplayText(f -> minecraft.ingameGUI.addChatMessage(f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		apiInit();
	}

	public void postInit() {
		KeyBindings.init();
		RetroGL.init();
		new Command(c -> CommandHelper.createClientCommand(__ -> c), true);
	}

	public void apiInit() {
		CustomPlayerModels.api.buildClient().localModelApi(GameProfile::new).
		renderApi(ModelBase.class, GameProfile.class).init();
	}

	public void playerRenderPre(PlayerRenderer renderer, EntityPlayer entityPlayer) {
		manager.bindPlayer(entityPlayer, null, renderer.modelBipedMain);
		manager.bindSkin(renderer.modelBipedMain, TextureSheetType.SKIN);
		ModelBiped model = renderer.modelBipedMain;
		manager.bindArmor(model, renderer.modelArmorChestplate, 1);
		manager.bindArmor(model, renderer.modelArmor, 2);
		manager.bindSkin(renderer.modelArmorChestplate, TextureSheetType.ARMOR1);
		manager.bindSkin(renderer.modelArmor, TextureSheetType.ARMOR2);
	}

	public void playerRenderPost(PlayerRenderer renderer) {
		manager.unbind(renderer.modelArmor);
		manager.unbind(renderer.modelArmorChestplate);
		manager.unbindClear(renderer.modelBipedMain);
	}

	public void clientTickStart() {
		if(!minecraft.isGamePaused) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();

			if(minecraft.thePlayer != null && minecraft.thePlayer.onGround && Keyboard.isKeyDown(minecraft.gameSettings.keyJump.getKeyCode())) {
				manager.jump(minecraft.thePlayer);
			}
		}
	}

	public void clientTickEnd() {
		if (minecraft.thePlayer == null)
			return;

		if (minecraft.currentScreen == null) {
			if(KeyBindings.gestureMenuBinding.isPressed()) {
				minecraft.displayGuiScreen(new GuiImpl(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.isPressed()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
		}
	}

	public boolean onRenderName(LivingRenderer renderer, EntityLiving entity, double xIn, double yIn, double zIn) {
		boolean res = false;
		if(entity instanceof EntityPlayer) {
			if(!Player.isEnableNames())
				res = true;
			if(Player.isEnableLoadingInfo() && canRenderName(entity)) {
				GameProfile gp = GameProfileManager.getProfile(((EntityPlayer) entity).username);
				FormatText st = INSTANCE.manager.getStatus(gp, ModelDefinitionLoader.PLAYER_UNIQUE);
				if(st != null) {
					float f = 1.6F;
					float f1 = 0.016666668F * f / 2;
					double d3 = EntityRenderDispatcher.instance.camera.distanceToSqr(entity.x, entity.y, entity.z);

					if (d3 < 32*32) {
						double y = yIn;
						GL11.glPushMatrix();
						GL11.glTranslated(0, 0.125F, 0);
						String s = st.remap();

						if (entity.isSneaking()) {
							FontRenderer fontrenderer = minecraft.fontRenderer;
							GL11.glPushMatrix();
							GL11.glTranslatef((float)xIn + 0.0F, (float)y + entity.bbHeight + 0.5F, (float)zIn);
							GL11.glNormal3f(0.0F, 1.0F, 0.0F);
							GL11.glRotatef(-EntityRenderDispatcher.instance.viewLerpYaw, 0.0F, 1.0F, 0.0F);
							GL11.glRotatef(EntityRenderDispatcher.instance.viewLerpPitch, 1.0F, 0.0F, 0.0F);
							GL11.glScalef(-f1, -f1, f1);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
							GL11.glDepthMask(false);
							GL11.glEnable(GL11.GL_BLEND);
							RetroGL.glBlendFunc(770, 771, 1, 0);
							Tessellator tessellator = Tessellator.instance;
							GL11.glDisable(GL11.GL_TEXTURE_2D);
							tessellator.startDrawingQuads();
							int i = fontrenderer.getStringWidth(s) / 2;
							tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
							tessellator.addVertex(-i - 1, -1.0D, 0.0D);
							tessellator.addVertex(-i - 1, 8.0D, 0.0D);
							tessellator.addVertex(i + 1, 8.0D, 0.0D);
							tessellator.addVertex(i + 1, -1.0D, 0.0D);
							tessellator.draw();
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL11.glDepthMask(true);
							fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
							GL11.glEnable(GL11.GL_LIGHTING);
							GL11.glDisable(GL11.GL_BLEND);
							GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
							GL11.glPopMatrix();
						} else {
							this.renderLivingLabel(entity, xIn, y, zIn, s, f1, d3);
						}
						GL11.glPopMatrix();
					}
				}
			}
		}
		return res;
	}

	protected boolean canRenderName(EntityLiving p_110813_1_) {
		return Minecraft.INSTANCE.gameSettings.immersiveMode.drawNames() && p_110813_1_.vehicle == null;
	}

	protected void renderLivingLabel(EntityLiving p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {
		if (p_96449_1_.isPlayerSleeping())this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
		else this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_, p_96449_6_, 64);
	}

	protected void renderLivingLabel0(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_, int p_147906_9_) {
		double d3 = EntityRenderDispatcher.instance.camera.distanceToSqr(p_147906_1_.x, p_147906_1_.y, p_147906_1_.z);

		if (d3 <= p_147906_9_ * p_147906_9_) {
			FontRenderer fontrenderer = minecraft.fontRenderer;
			float f = 1.6F;
			float f1 = 0.016666668F * f / 2;
			GL11.glPushMatrix();
			GL11.glTranslatef((float)p_147906_3_ + 0.0F, (float)p_147906_5_ + p_147906_1_.bbHeight + 0.5F, (float)p_147906_7_);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-EntityRenderDispatcher.instance.viewLerpYaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(EntityRenderDispatcher.instance.viewLerpPitch, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-f1, -f1, f1);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			RetroGL.glBlendFunc(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.instance;
			byte b0 = 0;

			if (p_147906_2_.equals("deadmau5"))b0 = -10;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tessellator.startDrawingQuads();
			int j = fontrenderer.getStringWidth(p_147906_2_) / 2;
			tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
			tessellator.addVertex(-j - 1, -1 + b0, 0.0D);
			tessellator.addVertex(-j - 1, 8 + b0, 0.0D);
			tessellator.addVertex(j + 1, 8 + b0, 0.0D);
			tessellator.addVertex(j + 1, -1 + b0, 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			fontrenderer.drawString(p_147906_2_, -fontrenderer.getStringWidth(p_147906_2_) / 2, b0, 553648127);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			fontrenderer.drawString(p_147906_2_, -fontrenderer.getStringWidth(p_147906_2_) / 2, b0, -1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public static class Button extends GuiButton {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, Lang.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	//Copy from PlayerRenderer.renderSpecials
	public static void renderCape(EntityPlayer playerIn, float partialTicks, ModelBiped model, ModelDefinition modelDefinition) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, 0.125F);
		float f5, f6, f7;
		if(playerIn != null) {
			double d3 = playerIn.xdO
					+ (playerIn.xd - playerIn.xdO) * partialTicks
					- (playerIn.xo + (playerIn.x - playerIn.xo) * partialTicks);
			double d4 = playerIn.ydO
					+ (playerIn.yd - playerIn.ydO) * partialTicks
					- (playerIn.yo + (playerIn.y - playerIn.yo) * partialTicks);
			double d0 = playerIn.zd0
					+ (playerIn.zd - playerIn.zd0) * partialTicks
					- (playerIn.zo + (playerIn.z - playerIn.zo) * partialTicks);
			float f4 = playerIn.prevRenderYawOffset + (playerIn.renderYawOffset - playerIn.prevRenderYawOffset) * partialTicks;

			double d1 = MathHelper.sin(f4 * (float)Math.PI / 180.0F);
			double d2 = (-MathHelper.cos(f4 * (float)Math.PI / 180.0F));
			f5 = (float)d4 * 10.0F;

			if (f5 < -6.0F)
			{
				f5 = -6.0F;
			}

			if (f5 > 32.0F)
			{
				f5 = 32.0F;
			}

			f6 = (float)(d3 * d1 + d0 * d2) * 100.0F;
			f7 = (float)(d3 * d2 - d0 * d1) * 100.0F;

			if (f6 < 0.0F)
			{
				f6 = 0.0F;
			}

			float f8 = playerIn.prevRenderYawOffset + (playerIn.renderYawOffset - playerIn.prevRenderYawOffset) * partialTicks;
			f5 += MathHelper.sin((playerIn.walkDistO + (playerIn.walkDist - playerIn.walkDistO) * partialTicks) * 6.0F) * 32.0F * f8;

			if (playerIn.isSneaking()) {
				f5 += 25.0F;
			}
		} else {
			f5 = 0;
			f6 = 0;
			f7 = 0;
		}

		model.bipedCloak.rotateAngleX = (float) -Math.toRadians(6.0F + f6 / 2.0F + f5);
		model.bipedCloak.rotateAngleY = (float) Math.toRadians(180.0F - f7 / 2.0F);
		model.bipedCloak.rotateAngleZ = (float) Math.toRadians(f7 / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.bipedCloak.rotateAngleX = 0;
		model.bipedCloak.rotateAngleY = 0;
		model.bipedCloak.rotateAngleZ = 0;
		model.renderCloak(0.0625F);
		GL11.glPopMatrix();
	}

	public static SidedHandler makeProxy() {
		return new CustomPlayerModelsClient();
	}

	@Override
	public void getTracking(EntityPlayer player, Consumer<EntityPlayer> f) {
	}

	@Override
	public Set<EntityPlayer> getTrackingPlayers(Entity entity) {
		return Collections.emptySet();
	}

	@Override
	public List<EntityPlayer> getPlayersOnline() {
		return Collections.singletonList(minecraft.thePlayer);
	}

	@Override
	public ServerNetworkImpl getServer(EntityPlayer pl) {
		return EmulNetwork.emulServer;
	}
}
