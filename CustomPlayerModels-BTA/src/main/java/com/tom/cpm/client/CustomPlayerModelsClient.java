package com.tom.cpm.client;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.Font;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.SidedHandler;
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

public class CustomPlayerModelsClient implements ClientModInitializer, SidedHandler {
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public static CustomPlayerModelsClient INSTANCE;
	public RenderManager<GameProfile, net.minecraft.core.entity.player.Player, ModelBase, Void> manager;
	public static NetHandlerExt<String, net.minecraft.core.entity.player.Player, ClientNetworkImpl> netHandler = new NetHandlerExt<>((a, b) -> a + ":" + b);

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
			if(ent instanceof net.minecraft.core.entity.player.Player) {
				return (net.minecraft.core.entity.player.Player) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.thePlayer);
		netHandler.setGetNet(EmulNetwork::getClient);
		netHandler.setDisplayText(f -> minecraft.hudIngame.addChatMessage(f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		apiInit();
	}

	public void postInit() {
		KeyBindings.init();
		RetroGL.init();
		//new Command(c -> CommandHelper.createClientCommand(__ -> c), true);
	}

	public void apiInit() {
		CustomPlayerModels.api.buildClient().localModelApi(GameProfile::new).
		renderApi(ModelBase.class, GameProfile.class).init();
	}

	public void playerRenderPre(MobRendererPlayer renderer, net.minecraft.core.entity.player.Player entityPlayer, ModelBiped model) {
		manager.bindPlayer(entityPlayer, null, model);
		manager.bindSkin(model, TextureSheetType.SKIN);
		manager.bindArmor(model, renderer.modelArmorChestplate, 1);
		manager.bindArmor(model, renderer.modelArmor, 2);
		manager.bindSkin(renderer.modelArmorChestplate, TextureSheetType.ARMOR1);
		manager.bindSkin(renderer.modelArmor, TextureSheetType.ARMOR2);
	}

	public void playerRenderPost(MobRendererPlayer renderer) {
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
				minecraft.displayScreen(new GuiImpl(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.isPressed()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
		}
	}

	public void onRenderName(MobRenderer renderer, net.minecraft.core.entity.player.Player entity, double xIn, double yIn, double zIn) {
		if(Player.isEnableLoadingInfo()) {
			GameProfile gp = GameProfileManager.getProfile(entity.username);
			FormatText st = INSTANCE.manager.getStatus(gp, ModelDefinitionLoader.PLAYER_UNIQUE);
			if(st != null) {
				float f = 1.6F;
				float f1 = 0.016666668F * f / 2;
				double d3 = EntityRenderDispatcher.instance.camera.distanceToSqr(entity.x, entity.y, entity.z);

				if (d3 < 32*32) {
					double y = yIn;
					GL11.glPushMatrix();
					GL11.glTranslated(0, -0.15F, 0);
					String s = st.remap();

					if (entity.isSneaking()) {
						Font fontrenderer = minecraft.font;
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

	protected void renderLivingLabel(Mob p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {
		if (p_96449_1_.isPlayerSleeping())this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
		else this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_, p_96449_6_, 64);
	}

	protected void renderLivingLabel0(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_, int p_147906_9_) {
		double d3 = EntityRenderDispatcher.instance.camera.distanceToSqr(p_147906_1_.x, p_147906_1_.y, p_147906_1_.z);

		if (d3 <= p_147906_9_ * p_147906_9_) {
			Font fontrenderer = minecraft.font;
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

	public static class Button extends ButtonElement {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, Lang.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	//Copy from PlayerRenderer.renderSpecials
	public static void renderCape(net.minecraft.core.entity.player.Player player, float partialTick, ModelBiped model, ModelDefinition modelDefinition) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, 0.125F);
		float f;
		if(player != null) {
			float yawOff = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * partialTick;
			float bodyAngle = 5.0F;
			if (player.isSneaking()) {
				bodyAngle += 25.0F;
			}

			double _xd = player.vehicle instanceof Entity ? ((Entity)player.vehicle).xd : MathHelper.lerp(player.xdO, player.xd, partialTick);
			double _yd = Math.min(player.vehicle instanceof Entity ? ((Entity)player.vehicle).yd : MathHelper.lerp(player.ydO, player.yd, partialTick), 0.0);
			double _zd = player.vehicle instanceof Entity ? ((Entity)player.vehicle).zd : MathHelper.lerp(player.zd0, player.zd, partialTick);
			double vel = -1.0 / (3.0 * Math.hypot(_xd, _zd) + 1.0) + 1.0;
			double moveAng = Math.atan2(_xd, _zd);
			double yawRad = Math.toRadians(yawOff);
			double multiplier = 1.0 - Math.abs((Math.cos(yawRad) + 1.0 - (Math.cos(moveAng) + 1.0) + 2.0) / 2.0 - 1.0);
			player.wobbleTimer = player.wobbleTimer
					+ (float)((player.tickCount + partialTick - player.lastRenderTick) / (30.0 - 29.0 * MathHelper.clamp(vel, 0.0, 1.0)));
			player.lastRenderTick = player.tickCount;
			double wobble = Math.sin(player.wobbleTimer) * (1.5 + 4.5 * vel * multiplier);

			f = (float) MathHelper.clamp(MathHelper.clamp(bodyAngle + vel * 100.0 * multiplier, bodyAngle, 100.0) + wobble - _yd * 60.0, 0.0, 180.0);
		} else {
			f = 0;
		}

		model.cloak.xRot = (float) -Math.toRadians(f);
		model.cloak.yRot = (float) Math.toRadians(180.0F);
		model.cloak.zRot = 0f;
		mc.getPlayerRenderManager().setModelPose(model);
		model.cloak.xRot = 0;
		model.cloak.yRot = 0;
		model.cloak.zRot = 0;
		model.renderCloak(0.0625F);
		GL11.glPopMatrix();
	}

	public static SidedHandler makeProxy() {
		return new CustomPlayerModelsClient();
	}

	@Override
	public void getTracking(net.minecraft.core.entity.player.Player player, Consumer<net.minecraft.core.entity.player.Player> f) {
	}

	@Override
	public Set<net.minecraft.core.entity.player.Player> getTrackingPlayers(Entity entity) {
		return Collections.emptySet();
	}

	@Override
	public List<net.minecraft.core.entity.player.Player> getPlayersOnline() {
		return Collections.singletonList(minecraft.thePlayer);
	}

	@Override
	public ServerNetworkImpl getServer(net.minecraft.core.entity.player.Player pl) {
		return EmulNetwork.emulServer;
	}
}
