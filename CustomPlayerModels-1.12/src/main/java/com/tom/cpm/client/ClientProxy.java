package com.tom.cpm.client;

import java.util.concurrent.Executor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.lefix.FixSSL;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ClientProxy extends CommonProxy {
	public static final ResourceLocation DEFAULT_CAPE = new ResourceLocation("cpm:textures/template/cape.png");
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public static ClientProxy INSTANCE;
	public RenderManager<GameProfile, EntityPlayer, ModelBase, Void> manager;
	public NetHandler<ResourceLocation, EntityPlayer, NetHandlerPlayClient> netHandler;

	@Override
	public void init() {
		super.init();
		FixSSL.fixup();
		INSTANCE = this;
		minecraft = Minecraft.getMinecraft();
		mc = new MinecraftObject(minecraft);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerProfile::getPlayerProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(ResourceLocation::new);
		Executor ex = minecraft::addScheduledTask;
		netHandler.setExecutor(() -> ex);
		netHandler.setSendPacketClient(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.sendPacket(new CPacketCustomPayload(rl.toString(), pb)));
		netHandler.setPlayerToLoader(EntityPlayer::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getMinecraft().world.getEntityByID(id);
			if(ent instanceof EntityPlayer) {
				return (EntityPlayer) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(c -> ((EntityPlayerSP)c).connection);
		netHandler.setDisplayText(f -> minecraft.ingameGUI.addChatMessage(ChatType.SYSTEM, f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		new Command(ClientCommandHandler.instance::registerCommand, true);
	}

	@Override
	public void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(EntityPlayer.class, EntityPlayer::getUniqueID).localModelApi(GameProfile::new).
		renderApi(ModelBase.class, GameProfile.class).init();
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.getEntityPlayer(), null, event.getRenderer().getMainModel());
		manager.bindSkin(event.getRenderer().getMainModel(), TextureSheetType.SKIN);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void playerRenderPreC(RenderPlayerEvent.Pre event) {
		if(event.isCanceled())manager.unbindClear(event.getRenderer().getMainModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.unbindClear(event.getRenderer().getMainModel());
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.getGui() instanceof GuiMainMenu && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getGui() instanceof GuiCustomizeSkin) {
			evt.getButtonList().add(new Button(0, 0));
		}
	}

	@SubscribeEvent
	public void buttonPress(GuiScreenEvent.ActionPerformedEvent.Pre evt) {
		if(evt.getButton() instanceof Button) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiImpl(EditorGui::new, evt.getGui()));
		}
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.getGui() == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			openGui.setGui(((GuiImpl.Overlay) minecraft.currentScreen).getGui());
		}
		if(openGui.getGui() instanceof GuiMainMenu && EditorGui.doOpenEditor()) {
			openGui.setGui(new GuiImpl(EditorGui::new, openGui.getGui()));
		}
		if(openGui.getGui() instanceof GuiImpl)((GuiImpl)openGui.getGui()).onOpened();
	}

	@SubscribeEvent
	public void drawGuiPre(DrawScreenEvent.Pre evt) {
		PlayerProfile.inGui = true;
	}

	@SubscribeEvent
	public void drawGuiPost(DrawScreenEvent.Post evt) {
		PlayerProfile.inGui = false;
	}

	public void renderSkull(ModelBase skullModel, GameProfile profile) {
		manager.bindSkull(profile, null, skullModel);
		manager.bindSkin(skullModel, TextureSheetType.SKIN);
	}

	public void renderElytra(ModelBiped player, ModelElytra model) {
		manager.bindElytra(player, model);
		manager.bindSkin(model, TextureSheetType.ELYTRA);
	}

	public void renderArmor(ModelBase modelArmor, ModelBase modelLeggings,
			ModelBiped player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
		manager.bindSkin(modelArmor, TextureSheetType.ARMOR1);
		manager.bindSkin(modelLeggings, TextureSheetType.ARMOR2);
	}

	@SubscribeEvent
	public void renderTick(RenderTickEvent evt) {
		if(evt.phase == Phase.START) {
			mc.getPlayerRenderManager().getAnimationEngine().update(evt.renderTickTime);
		}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if(evt.phase == Phase.START && !minecraft.isGamePaused()) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();
		}

		if (minecraft.player == null || evt.phase == Phase.START)
			return;

		if(KeyBindings.gestureMenuBinding.isPressed()) {
			minecraft.displayGuiScreen(new GuiImpl(GestureGui::new, null));
		}

		if(KeyBindings.renderToggleBinding.isPressed()) {
			Player.setEnableRendering(!Player.isEnableRendering());
		}

		mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
	}

	@SubscribeEvent
	public void onRenderName(RenderLivingEvent.Specials.Pre<AbstractClientPlayer> evt) {
		if(evt.getEntity() instanceof AbstractClientPlayer) {
			if(!Player.isEnableNames())
				evt.setCanceled(true);
			if(Player.isEnableLoadingInfo() && canRenderName(evt.getEntity())) {
				FormatText st = INSTANCE.manager.getStatus(((AbstractClientPlayer) evt.getEntity()).getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE);
				if(st != null) {
					double d0 = evt.getEntity().getDistanceSq(minecraft.getRenderViewEntity());
					if (d0 < 32*32) {
						Scoreboard scoreboard = ((EntityPlayer) evt.getEntity()).getWorldScoreboard();
						ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);
						double y = evt.getY();
						if (scoreobjective != null)
							y += evt.getRenderer().getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F;
						GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0.25F, 0);
						String str = ((ITextComponent) st.remap()).getFormattedText();
						renderLivingLabel(evt.getEntity(), str, evt.getX(), y, evt.getZ(), 64);
						GlStateManager.popMatrix();
					}
				}
			}
		}
	}

	protected boolean canRenderName(Entity entity) {
		EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
		boolean flag = !entity.isInvisibleToPlayer(entityplayersp);

		if (entity != entityplayersp) {
			Team team = entity.getTeam();
			Team team1 = entityplayersp.getTeam();

			if (team != null) {
				Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

				switch (team$enumvisible) {
				case ALWAYS:
					return flag;
				case NEVER:
					return false;
				case HIDE_FOR_OTHER_TEAMS:
					return team1 == null ? flag : team.isSameTeam(team1) && (team.getSeeFriendlyInvisiblesEnabled() || flag);
				case HIDE_FOR_OWN_TEAM:
					return team1 == null ? flag : !team.isSameTeam(team1) && flag;
				default:
					return true;
				}
			}
		}

		return Minecraft.isGuiEnabled() && flag && !entity.isBeingRidden();
	}

	protected void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
		double d0 = entityIn.getDistanceSq(minecraft.getRenderManager().renderViewEntity);

		if (d0 <= maxDistance * maxDistance) {
			boolean flag = entityIn.isSneaking();
			float f = minecraft.getRenderManager().playerViewY;
			float f1 = minecraft.getRenderManager().playerViewX;
			boolean flag1 = minecraft.getRenderManager().options.thirdPersonView == 2;
			float f2 = entityIn.height + 0.5F - (flag ? 0.25F : 0.0F);
			int i = "deadmau5".equals(str) ? -10 : 10;
			drawNameplate(minecraft.fontRenderer, str, (float)x, (float)y + f2, (float)z, i, f, f1, flag1, flag);
		}
	}

	public static void drawNameplate(FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-0.025F / 2, -0.025F / 2, 0.025F / 2);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);

		if (!isSneaking)
		{
			GlStateManager.disableDepth();
		}

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		int i = fontRendererIn.getStringWidth(str) / 2;
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(-i - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos(-i - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos(i + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos(i + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();

		if (!isSneaking)
		{
			fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
			GlStateManager.enableDepth();
		}

		GlStateManager.depthMask(true);
		fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	public static class Button extends GuiButton {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, I18n.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	@SubscribeEvent
	public void updateJump(PlayerSPPushOutOfBlocksEvent evt) {
		if(minecraft.player.onGround && minecraft.player.movementInput.jump) {
			manager.jump(minecraft.player);
		}
	}

	//Copy from LayerCape
	public static void renderCape(AbstractClientPlayer playerIn, float partialTicks, ModelPlayer model, ModelDefinition modelDefinition) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		float f1, f2, f3;

		if(playerIn != null) {
			double lvt_10_1_ = playerIn.prevChasingPosX
					+ (playerIn.chasingPosX - playerIn.prevChasingPosX) * partialTicks
					- (playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * partialTicks);
			double lvt_12_1_ = playerIn.prevChasingPosY
					+ (playerIn.chasingPosY - playerIn.prevChasingPosY) * partialTicks
					- (playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * partialTicks);
			double lvt_14_1_ = playerIn.prevChasingPosZ
					+ (playerIn.chasingPosZ - playerIn.prevChasingPosZ) * partialTicks
					- (playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * partialTicks);
			float lvt_16_1_ = playerIn.prevRenderYawOffset
					+ (playerIn.renderYawOffset - playerIn.prevRenderYawOffset) * partialTicks;
			double lvt_17_1_ = MathHelper.sin(lvt_16_1_ * 0.017453292F);
			double lvt_19_1_ = (-MathHelper.cos(lvt_16_1_ * 0.017453292F));
			f1 = (float) lvt_12_1_ * 10.0F;
			f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
			f2 = (float) (lvt_10_1_ * lvt_17_1_ + lvt_14_1_ * lvt_19_1_) * 100.0F;
			f3 = (float) (lvt_10_1_ * lvt_19_1_ - lvt_14_1_ * lvt_17_1_) * 100.0F;
			if (f2 < 0.0F) {
				f2 = 0.0F;
			}

			float lvt_24_1_ = playerIn.prevCameraYaw
					+ (playerIn.cameraYaw - playerIn.prevCameraYaw) * partialTicks;
			f1 += MathHelper.sin((playerIn.prevDistanceWalkedModified
					+ (playerIn.distanceWalkedModified - playerIn.prevDistanceWalkedModified) * partialTicks)
					* 6.0F) * 32.0F * lvt_24_1_;
			if (playerIn.isSneaking()) {
				f1 += 25.0F;
				model.bipedCape.rotationPointY = 2.0F;
			} else {
				model.bipedCape.rotationPointY = 0.0F;
			}
		} else {
			f1 = 0;
			f2 = 0;
			f3 = 0;
		}

		model.bipedCape.rotateAngleX = (float) -Math.toRadians(6.0F + f2 / 2.0F + f1);
		model.bipedCape.rotateAngleY = (float) Math.toRadians(180.0F - f3 / 2.0F);
		model.bipedCape.rotateAngleZ = (float) Math.toRadians(f3 / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.bipedCape.rotateAngleX = 0;
		model.bipedCape.rotateAngleY = 0;
		model.bipedCape.rotateAngleZ = 0;
		model.renderCape(0.0625F);
		GlStateManager.popMatrix();
	}
}
