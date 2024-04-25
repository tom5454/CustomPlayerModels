package com.tom.cpm.client;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.modificationstation.stationapi.api.util.Identifier;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.common.ServerNetworkImpl;
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
	public RenderManager<GameProfile, PlayerEntity, EntityModel, Void> manager;
	public static NetHandlerExt<Identifier, PlayerEntity, ClientNetworkImpl> netHandler = new NetHandlerExt<>((a, b) -> Identifier.of(a + ":" + b));

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		minecraft = Minecraft.INSTANCE;
		CustomPlayerModels.proxy = this;
		mc = new MinecraftObject(minecraft);
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), e -> GameProfileManager.getProfile(e.name));
		netHandler.setExecutor(() -> MCExecutor.ex);
		netHandler.setSendPacketClient(ClientNetworkImpl::cpm$sendPacket);
		netHandler.setPlayerToLoader(e -> GameProfileManager.getProfile(e.name));
		netHandler.setGetPlayerById(id -> {
			Entity ent = EmulNetwork.getClient(minecraft.player).cpm$getEntityByID(id);
			if(ent instanceof PlayerEntity) {
				return (PlayerEntity) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(EmulNetwork::getClient);
		netHandler.setDisplayText(f -> minecraft.inGameHud.addChatMessage(f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		apiInit();
		Lang.init();
	}

	public void apiInit() {
		CustomPlayerModels.api.buildClient().localModelApi(GameProfile::new).
		renderApi(EntityModel.class, GameProfile.class).init();
	}

	public void playerRenderPre(PlayerEntityRenderer renderer, PlayerEntity entityPlayer) {
		manager.bindPlayer(entityPlayer, null, renderer.bipedModel);
		manager.bindSkin(renderer.bipedModel, TextureSheetType.SKIN);
		BipedEntityModel model = renderer.bipedModel;
		manager.bindArmor(model, renderer.field_295, 1);
		manager.bindArmor(model, renderer.field_296, 2);
		manager.bindSkin(renderer.field_295, TextureSheetType.ARMOR1);
		manager.bindSkin(renderer.field_296, TextureSheetType.ARMOR2);
	}

	public void playerRenderPost(PlayerEntityRenderer renderer) {
		manager.unbind(renderer.field_296);
		manager.unbind(renderer.field_295);
		manager.unbindClear(renderer.bipedModel);
	}

	public void clientTickStart() {
		if(!minecraft.paused) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();

			if(minecraft.player != null && minecraft.player.field_1623 && Keyboard.isKeyDown(minecraft.options.jumpKey.code)) {
				manager.jump(minecraft.player);
			}
		}
	}

	public void clientTickEnd() {
		if (minecraft.player == null)
			return;

		if (minecraft.currentScreen == null) {
			if(Keyboard.isKeyDown(KeyBindings.gestureMenuBinding.code)) {
				minecraft.setScreen(new GuiImpl(GestureGui::new, null));
			}

			if(Keyboard.isKeyDown(KeyBindings.renderToggleBinding.code)) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
		}
	}

	public boolean onRenderName(LivingEntityRenderer renderer, LivingEntity entity, double xIn, double yIn, double zIn) {
		boolean res = false;
		if(entity instanceof PlayerEntity) {
			if(!Player.isEnableNames())
				res = true;
			if(Player.isEnableLoadingInfo() && canRenderName(entity)) {
				GameProfile gp = GameProfileManager.getProfile(((PlayerEntity) entity).name);
				FormatText st = INSTANCE.manager.getStatus(gp, ModelDefinitionLoader.PLAYER_UNIQUE);
				if(st != null) {
					float f = 1.6F;
					float f1 = 0.016666668F * f / 2;
					double d3 = entity.method_1352(EntityRenderDispatcher.field_2489.field_2496);

					if (d3 < 32*32) {
						double y = yIn;
						GL11.glPushMatrix();
						GL11.glTranslated(0, 0.125F, 0);
						String s = st.remap();

						if (entity.method_1373()) {
							TextRenderer fontrenderer = minecraft.textRenderer;
							GL11.glPushMatrix();
							GL11.glTranslatef((float)xIn + 0.0F, (float)y + entity.spacingY + 0.5F, (float)zIn);
							GL11.glNormal3f(0.0F, 1.0F, 0.0F);
							GL11.glRotatef(-EntityRenderDispatcher.field_2489.field_2497, 0.0F, 1.0F, 0.0F);
							GL11.glRotatef(EntityRenderDispatcher.field_2489.field_2498, 1.0F, 0.0F, 0.0F);
							GL11.glScalef(-f1, -f1, f1);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
							GL11.glDepthMask(false);
							GL11.glEnable(GL11.GL_BLEND);
							RetroGL.glBlendFunc(770, 771, 1, 0);
							Tessellator tessellator = Tessellator.INSTANCE;
							GL11.glDisable(GL11.GL_TEXTURE_2D);
							tessellator.startQuads();
							int i = fontrenderer.getWidth(s) / 2;
							tessellator.color(0.0F, 0.0F, 0.0F, 0.25F);
							tessellator.vertex(-i - 1, -1.0D, 0.0D);
							tessellator.vertex(-i - 1, 8.0D, 0.0D);
							tessellator.vertex(i + 1, 8.0D, 0.0D);
							tessellator.vertex(i + 1, -1.0D, 0.0D);
							tessellator.draw();
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL11.glDepthMask(true);
							fontrenderer.draw(s, -fontrenderer.getWidth(s) / 2, 0, 553648127);
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

	protected boolean canRenderName(LivingEntity p_110813_1_) {
		return Minecraft.method_2146() && p_110813_1_.field_1594 == null;
	}

	protected void renderLivingLabel(LivingEntity p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {
		if (p_96449_1_.method_943())this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
		else this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_, p_96449_6_, 64);
	}

	protected void renderLivingLabel0(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_, int p_147906_9_) {
		double d3 = p_147906_1_.method_1352(EntityRenderDispatcher.field_2489.field_2496);

		if (d3 <= p_147906_9_ * p_147906_9_) {
			TextRenderer fontrenderer = minecraft.textRenderer;
			float f = 1.6F;
			float f1 = 0.016666668F * f / 2;
			GL11.glPushMatrix();
			GL11.glTranslatef((float)p_147906_3_ + 0.0F, (float)p_147906_5_ + p_147906_1_.spacingY + 0.5F, (float)p_147906_7_);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-EntityRenderDispatcher.field_2489.field_2497, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(EntityRenderDispatcher.field_2489.field_2498, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-f1, -f1, f1);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			RetroGL.glBlendFunc(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.INSTANCE;
			byte b0 = 0;

			if (p_147906_2_.equals("deadmau5"))b0 = -10;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tessellator.startQuads();
			int j = fontrenderer.getWidth(p_147906_2_) / 2;
			tessellator.color(0.0F, 0.0F, 0.0F, 0.25F);
			tessellator.vertex(-j - 1, -1 + b0, 0.0D);
			tessellator.vertex(-j - 1, 8 + b0, 0.0D);
			tessellator.vertex(j + 1, 8 + b0, 0.0D);
			tessellator.vertex(j + 1, -1 + b0, 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			fontrenderer.draw(p_147906_2_, -fontrenderer.getWidth(p_147906_2_) / 2, b0, 553648127);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			fontrenderer.draw(p_147906_2_, -fontrenderer.getWidth(p_147906_2_) / 2, b0, -1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, Lang.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	//Copy from PlayerEntityRenderer.method_827
	public static void renderCape(PlayerEntity playerIn, float partialTicks, BipedEntityModel model, ModelDefinition modelDefinition) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, 0.125F);
		float f5, f6, f7;
		if(playerIn != null) {
			double d3 = playerIn.field_530 + (playerIn.field_533 - playerIn.field_530) * partialTicks
					- (playerIn.prevX + (playerIn.x - playerIn.prevX) * partialTicks);
			double d4 = playerIn.field_531 + (playerIn.field_534 - playerIn.field_531) * partialTicks
					- (playerIn.prevY + (playerIn.y - playerIn.prevY) * partialTicks);
			double d0 = playerIn.field_532 + (playerIn.field_535 - playerIn.field_532) * partialTicks
					- (playerIn.prevZ + (playerIn.z - playerIn.prevZ) * partialTicks);
			float f4 = playerIn.field_1013 + (playerIn.field_1012 - playerIn.field_1013) * partialTicks;

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

			float var18 = playerIn.field_524 + (playerIn.field_525 - playerIn.field_524) * partialTicks;
			f5 += MathHelper.sin((playerIn.field_1634 + (playerIn.field_1635 - playerIn.field_1634) * partialTicks) * 6.0F) * 32.0F * var18;
			if (playerIn.method_1373()) {
				f5 += 25.0F;
			}
		} else {
			f5 = 0;
			f6 = 0;
			f7 = 0;
		}

		model.cape.pitch = (float) -Math.toRadians(6.0F + f6 / 2.0F + f5);
		model.cape.yaw = (float) Math.toRadians(180.0F - f7 / 2.0F);
		model.cape.roll = (float) Math.toRadians(f7 / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.cape.pitch = 0;
		model.cape.yaw = 0;
		model.cape.roll = 0;
		model.renderCape(0.0625F);
		GL11.glPopMatrix();
	}

	public static SidedHandler makeProxy() {
		return new CustomPlayerModelsClient();
	}

	@Override
	public void getTracking(PlayerEntity player, Consumer<PlayerEntity> f) {
	}

	@Override
	public Set<PlayerEntity> getTrackingPlayers(Entity entity) {
		return Collections.emptySet();
	}

	@Override
	public List<PlayerEntity> getPlayersOnline() {
		return Collections.singletonList(minecraft.player);
	}

	@Override
	public ServerNetworkImpl getServer(PlayerEntity pl) {
		return EmulNetwork.emulServer;
	}
}
