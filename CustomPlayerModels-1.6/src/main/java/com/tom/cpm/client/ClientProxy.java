package com.tom.cpm.client;

import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.lefix.FixSSL;
import com.tom.cpm.retro.GameProfile;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.retro.NetHandlerExt;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {
	public static MinecraftObject mc;
	private Minecraft minecraft;
	public static ClientProxy INSTANCE;
	public RenderManager<GameProfile, EntityPlayer, ModelBase, Void> manager;
	public NetHandlerExt<ResourceLocation, EntityPlayer, NetClientHandler> netHandler;

	@Override
	public void init() {
		super.init();
		FixSSL.fixup();
		INSTANCE = this;
		minecraft = Minecraft.getMinecraft();
		mc = new MinecraftObject(minecraft);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), e -> GameProfileManager.getProfile(e.username));
		netHandler = new NetHandlerExt<>(ResourceLocation::new);
		netHandler.setExecutor(() -> MCExecutor.ex);
		netHandler.setSendPacketClient((c, rl, pb) -> c.addToSendQueue(new Packet250CustomPayload(rl.toString(), pb)));
		netHandler.setPlayerToLoader(e -> GameProfileManager.getProfile(e.username));
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(id);
			if(ent instanceof EntityPlayer) {
				return (EntityPlayer) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.thePlayer);
		netHandler.setGetNet(c -> ((EntityClientPlayerMP) c).sendQueue);
		netHandler.setDisplayText(f -> minecraft.ingameGUI.getChatGUI().printChatMessage(f.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		TickRegistry.registerTickHandler(new ITickHandler() {

			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.CLIENT, TickType.RENDER);
			}

			@Override
			public void tickStart(EnumSet<TickType> var1, Object... var2) {
				if (var1.contains(TickType.CLIENT)) {
					clientTickStart();
				}
				if (var1.contains(TickType.RENDER)) {
					renderTick((float) var2[0]);
				}
			}

			@Override
			public void tickEnd(EnumSet<TickType> var1, Object... var2) {
				if (var1.contains(TickType.CLIENT)) {
					clientTickEnd();
					MCExecutor.executeAll();
				}
			}

			@Override
			public String getLabel() {
				return "CPM";
			}
		}, Side.CLIENT);
		new Command(ClientCommandHandler.instance::registerCommand, true);
	}

	@Override
	public void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(EntityPlayer.class, EntityPlayer::getUniqueID).localModelApi(GameProfile::new).
		renderApi(ModelBase.class, GameProfile.class).init();
	}

	@ForgeSubscribe
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.entityPlayer, null, event.renderer.modelBipedMain);
		manager.bindSkin(event.renderer.modelBipedMain, TextureSheetType.SKIN);
		ModelBiped model = event.renderer.modelBipedMain;
		manager.bindArmor(model, event.renderer.modelArmorChestplate, 1);
		manager.bindArmor(model, event.renderer.modelArmor, 2);
		manager.bindSkin(event.renderer.modelArmorChestplate, TextureSheetType.ARMOR1);
		manager.bindSkin(event.renderer.modelArmor, TextureSheetType.ARMOR2);
	}

	@ForgeSubscribe(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void playerRenderPreC(RenderPlayerEvent.Pre event) {
		if(event.isCanceled()) {
			manager.unbind(event.renderer.modelArmor);
			manager.unbind(event.renderer.modelArmorChestplate);
			manager.unbindClear(event.renderer.modelBipedMain);
		}
	}

	@ForgeSubscribe
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.unbind(event.renderer.modelArmor);
		manager.unbind(event.renderer.modelArmorChestplate);
		manager.unbindClear(event.renderer.modelBipedMain);
	}

	@ForgeSubscribe
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.gui == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			openGui.gui = ((GuiImpl.Overlay) minecraft.currentScreen).getGui();
		}
		if(openGui.gui instanceof GuiMainMenu && !(minecraft.currentScreen instanceof GuiSelectWorld || minecraft.currentScreen instanceof GuiMainMenu) && EditorGui.doOpenEditor()) {
			openGui.gui = new GuiImpl(EditorGui::new, openGui.gui);
		}
		if(openGui.gui instanceof GuiImpl)((GuiImpl)openGui.gui).onOpened();
	}

	public void renderSkull(ModelBase skullModel, GameProfile profile) {
		manager.bindSkull(profile, null, skullModel);
		manager.bindSkin(skullModel, TextureSheetType.SKIN);
	}

	public void renderTick(float partial) {
		mc.getPlayerRenderManager().getAnimationEngine().update(partial);
	}

	public void clientTickStart() {
		if(!minecraft.isGamePaused) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();

			if(minecraft.thePlayer != null && minecraft.thePlayer.onGround && minecraft.gameSettings.keyBindJump.pressed) {
				manager.jump(minecraft.thePlayer);
			}
		}
	}

	public void clientTickEnd() {
		if (minecraft.thePlayer == null)
			return;

		if(KeyBindings.gestureMenuBinding.isPressed()) {
			minecraft.displayGuiScreen(new GuiImpl(GestureGui::new, null));
		}

		if(KeyBindings.renderToggleBinding.isPressed()) {
			Player.setEnableRendering(!Player.isEnableRendering());
		}

		mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
	}

	public boolean onRenderName(RendererLivingEntity renderer, EntityLivingBase entity, double xIn, double yIn, double zIn) {
		boolean res = false;
		if(entity instanceof AbstractClientPlayer) {
			if(!Player.isEnableNames())
				res = true;
			if(Player.isEnableLoadingInfo() && canRenderName(entity)) {
				GameProfile gp = GameProfileManager.getProfile(((AbstractClientPlayer) entity).username);
				FormatText st = INSTANCE.manager.getStatus(gp, ModelDefinitionLoader.PLAYER_UNIQUE);
				if(st != null) {
					float f = 1.6F;
					float f1 = 0.016666668F * f / 2;
					double d3 = entity.getDistanceSqToEntity(net.minecraft.client.renderer.entity.RenderManager.instance.livingPlayer);

					if (d3 < 32*32) {
						Scoreboard scoreboard = ((EntityPlayer) entity).getWorldScoreboard();
						ScoreObjective scoreobjective = scoreboard.func_96539_a(2);
						double y = yIn;
						if (scoreobjective != null)
							y += renderer.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F;
						GL11.glPushMatrix();
						GL11.glTranslated(0, 0.125F, 0);
						String s = st.remap();

						if (entity.isSneaking()) {
							FontRenderer fontrenderer = minecraft.fontRenderer;
							GL11.glPushMatrix();
							GL11.glTranslatef((float)xIn + 0.0F, (float)y + entity.height + 0.5F, (float)zIn);
							GL11.glNormal3f(0.0F, 1.0F, 0.0F);
							GL11.glRotatef(-net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
							GL11.glRotatef(net.minecraft.client.renderer.entity.RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
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

	protected boolean canRenderName(EntityLivingBase p_110813_1_) {
		return Minecraft.isGuiEnabled() && !p_110813_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && p_110813_1_.riddenByEntity == null;
	}

	protected void renderLivingLabel(EntityLivingBase p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {
		if (p_96449_1_.isPlayerSleeping())this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
		else this.renderLivingLabel0(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_, p_96449_6_, 64);
	}

	protected void renderLivingLabel0(Entity p_147906_1_, String p_147906_2_, double p_147906_3_, double p_147906_5_, double p_147906_7_, int p_147906_9_) {
		double d3 = p_147906_1_.getDistanceSqToEntity(net.minecraft.client.renderer.entity.RenderManager.instance.livingPlayer);

		if (d3 <= p_147906_9_ * p_147906_9_) {
			FontRenderer fontrenderer = minecraft.fontRenderer;
			float f = 1.6F;
			float f1 = 0.016666668F * f / 2;
			GL11.glPushMatrix();
			GL11.glTranslatef((float)p_147906_3_ + 0.0F, (float)p_147906_5_ + p_147906_1_.height + 0.5F, (float)p_147906_7_);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(net.minecraft.client.renderer.entity.RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
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
			super(99, x, y, 100, 20, I18n.getString("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	//Copy from RenderPlayer.renderEquippedItems
	public static void renderCape(AbstractClientPlayer playerIn, float partialTicks, ModelBiped model, ModelDefinition modelDefinition) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, 0.125F);
		float f5, f6, f7;
		if(playerIn != null) {
			double d3 = playerIn.field_71091_bM + (playerIn.field_71094_bP - playerIn.field_71091_bM) * partialTicks - (playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * partialTicks);
			double d4 = playerIn.field_71096_bN + (playerIn.field_71095_bQ - playerIn.field_71096_bN) * partialTicks - (playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * partialTicks);
			double d0 = playerIn.field_71097_bO + (playerIn.field_71085_bR - playerIn.field_71097_bO) * partialTicks - (playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * partialTicks);
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

			float f8 = playerIn.prevCameraYaw + (playerIn.cameraYaw - playerIn.prevCameraYaw) * partialTicks;
			f5 += MathHelper.sin((playerIn.prevDistanceWalkedModified + (playerIn.distanceWalkedModified - playerIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f8;

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
}
