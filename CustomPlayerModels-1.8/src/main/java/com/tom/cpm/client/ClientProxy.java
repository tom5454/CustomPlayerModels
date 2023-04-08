package com.tom.cpm.client;

import java.util.concurrent.Executor;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.CustomPlayerModels;
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
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), EntityPlayer::getGameProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(ResourceLocation::new);
		Executor ex = minecraft::addScheduledTask;
		netHandler.setExecutor(() -> ex);
		netHandler.setSendPacketClient(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.addToSendQueue(new C17PacketCustomPayload(rl.toString(), pb)));
		netHandler.setPlayerToLoader(EntityPlayer::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(id);
			if(ent instanceof EntityPlayer) {
				return (AbstractClientPlayer) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.thePlayer);
		netHandler.setGetNet(c -> ((EntityPlayerSP)c).sendQueue);
		netHandler.setDisplayText(f -> minecraft.ingameGUI.getChatGUI().printChatMessage(f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
	}

	@Override
	public void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(EntityPlayer.class, EntityPlayer::getUniqueID).localModelApi(GameProfile::new).
		renderApi(ModelBase.class, GameProfile.class).init();
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.entityPlayer, null, event.renderer.getMainModel());
		manager.bindSkin(event.renderer.getMainModel(), TextureSheetType.SKIN);
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.unbindClear(event.renderer.getMainModel());
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.gui instanceof GuiMainMenu && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.gui instanceof GuiCustomizeSkin) {
			evt.buttonList.add(new Button(0, 0));
		}
	}

	@SubscribeEvent
	public void buttonPress(GuiScreenEvent.ActionPerformedEvent.Pre evt) {
		if(evt.button instanceof Button) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiImpl(EditorGui::new, evt.gui));
		}
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.gui == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			openGui.gui = ((GuiImpl.Overlay) minecraft.currentScreen).getGui();
		}
		if(openGui.gui instanceof GuiMainMenu && !(minecraft.currentScreen instanceof GuiSelectWorld || minecraft.currentScreen instanceof GuiMainMenu) && EditorGui.doOpenEditor()) {
			openGui.gui = new GuiImpl(EditorGui::new, openGui.gui);
		}
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

			if(minecraft.thePlayer != null && minecraft.thePlayer.onGround && minecraft.gameSettings.keyBindJump.isKeyDown()) {
				manager.jump(minecraft.thePlayer);
			}
		}

		if (minecraft.thePlayer == null || evt.phase == Phase.START)
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
		if(evt.entity instanceof AbstractClientPlayer) {
			if(!Player.isEnableNames())
				evt.setCanceled(true);
			if(Player.isEnableLoadingInfo() && canRenderName(evt.entity)) {
				FormatText st = INSTANCE.manager.getStatus(((AbstractClientPlayer) evt.entity).getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE);
				if(st != null) {
					double d0 = evt.entity.getDistanceSqToEntity(minecraft.getRenderViewEntity());
					if (d0 < 32*32) {
						Scoreboard scoreboard = ((EntityPlayer) evt.entity).getWorldScoreboard();
						ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);
						double y = evt.y;
						if (scoreobjective != null)
							y += evt.renderer.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F;

						GlStateManager.pushMatrix();
						GlStateManager.translate(0, 0.125F, 0);
						String str = ((IChatComponent) st.remap()).getFormattedText();
						GlStateManager.alphaFunc(516, 0.1F);

						if (evt.entity.isSneaking()) {
							FontRenderer fontrenderer = minecraft.fontRendererObj;
							GlStateManager.pushMatrix();
							GlStateManager.translate((float)evt.x, (float)y + evt.entity.height + 0.5F - (evt.entity.isChild() ? evt.entity.height / 2.0F : 0.0F), (float)evt.z);
							GL11.glNormal3f(0.0F, 1.0F, 0.0F);
							GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
							GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
							GlStateManager.scale(-0.02666667F / 2, -0.02666667F / 2, 0.02666667F / 2);
							GlStateManager.translate(0.0F, 9.374999F, 0.0F);
							GlStateManager.disableLighting();
							GlStateManager.depthMask(false);
							GlStateManager.enableBlend();
							GlStateManager.disableTexture2D();
							GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
							int i = fontrenderer.getStringWidth(str) / 2;
							Tessellator tessellator = Tessellator.getInstance();
							WorldRenderer worldrenderer = tessellator.getWorldRenderer();
							worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
							worldrenderer.pos(-i - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
							worldrenderer.pos(-i - 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
							worldrenderer.pos(i + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
							worldrenderer.pos(i + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
							tessellator.draw();
							GlStateManager.enableTexture2D();
							GlStateManager.depthMask(true);
							fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, 553648127);
							GlStateManager.enableLighting();
							GlStateManager.disableBlend();
							GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
							GlStateManager.popMatrix();
						} else {
							renderLivingLabel(evt.entity, str, evt.x, y - (evt.entity.isChild() ? (double)(evt.entity.height / 2.0F) : 0.0D), evt.z, 64);
						}
					}
					GlStateManager.popMatrix();
				}
			}
		}
	}

	protected boolean canRenderName(EntityLivingBase entity) {
		EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;

		if (entity instanceof EntityPlayer && entity != entityplayersp) {
			Team team = entity.getTeam();
			Team team1 = entityplayersp.getTeam();

			if (team != null) {
				Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

				switch (team$enumvisible) {
				case ALWAYS:
					return true;
				case NEVER:
					return false;
				case HIDE_FOR_OTHER_TEAMS:
					return team1 == null || team.isSameTeam(team1);
				case HIDE_FOR_OWN_TEAM:
					return team1 == null || !team.isSameTeam(team1);
				default:
					return true;
				}
			}
		}

		return Minecraft.isGuiEnabled() && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
	}

	protected void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
		double d0 = entityIn.getDistanceSqToEntity(minecraft.getRenderManager().livingPlayer);

		if (d0 <= maxDistance * maxDistance) {
			FontRenderer fontrenderer = minecraft.fontRendererObj;
			float f = 1.6F;
			float f1 = 0.016666668F * f / 2;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float)x + 0.0F, (float)y + entityIn.height + 0.5F, (float)z);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(-f1, -f1, f1);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(false);
			GlStateManager.disableDepth();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			int i = 0;

			if (str.equals("deadmau5"))i = -10;

			int j = fontrenderer.getStringWidth(str) / 2;
			GlStateManager.disableTexture2D();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}

	public static class Button extends GuiButton {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, I18n.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	//Copy from LayerCape
	public static void renderCape(AbstractClientPlayer playerIn, float partialTicks, ModelPlayer model, ModelDefinition modelDefinition) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
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
			f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
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
