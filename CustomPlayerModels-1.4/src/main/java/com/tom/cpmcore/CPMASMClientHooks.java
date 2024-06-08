package com.tom.cpmcore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.RetroGL;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.skin.TextureType;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, String profile) {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
		if(profile != null) {
			ClientProxy.INSTANCE.renderSkull(skullModel, GameProfileManager.getProfile(profile));
		}
	}

	public static void renderSkullPost(ModelBase skullModel, String profile) {
		if(profile != null) {
			ClientProxy.INSTANCE.manager.unbind(skullModel);
		}
	}

	public static void playerRenderPre(RenderPlayer renderer, EntityPlayer entityPlayer) {
		ClientProxy.INSTANCE.playerRenderPre(renderer, entityPlayer);
	}

	public static void playerRenderPost(RenderPlayer renderer) {
		ClientProxy.INSTANCE.playerRenderPost(renderer);
	}

	public static void renderPass(ModelBase model, Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_, RenderLiving r, int callLoc) {
		RetroGL.renderCallLoc = callLoc;
		if(r instanceof RenderPlayer && model instanceof ModelBiped) {
			RenderPlayer rp = (RenderPlayer) r;
			if(model == rp.modelArmor || model == rp.modelArmorChestplate) {
				PlayerRenderManager m = ClientProxy.mc.getPlayerRenderManager();
				ModelBiped player = rp.modelBipedMain;
				ModelBiped armor = (ModelBiped) model;
				m.copyModelForArmor(player.bipedBody, armor.bipedBody);
				m.copyModelForArmor(player.bipedHead, armor.bipedHead);
				m.copyModelForArmor(player.bipedLeftArm, armor.bipedLeftArm);
				m.copyModelForArmor(player.bipedLeftLeg, armor.bipedLeftLeg);
				m.copyModelForArmor(player.bipedRightArm, armor.bipedRightArm);
				m.copyModelForArmor(player.bipedRightLeg, armor.bipedRightLeg);
				CPMClientAccess.setNoSetup(armor, true);
			}
		}
		model.render(p_78088_1_, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_);
	}

	public static void onLogout(WorldClient world) {
		if (world == null)
			ClientProxy.INSTANCE.onLogout();
	}

	public static boolean onClientPacket(Packet250CustomPayload pckt, NetClientHandler handler) {
		if(pckt.channel.startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			ClientProxy.INSTANCE.netHandler.receiveClient(pckt.channel, new FastByteArrayInputStream(pckt.data), (NetH) handler);
			return true;
		}
		return false;
	}

	public static void glColor4f(float r, float g, float b, float a) {
		RetroGL.color4f(r, g, b, a);
	}

	public static void glColor3f(float r, float g, float b) {
		RetroGL.color4f(r, g, b, 1);
	}

	public static void prePlayerRender() {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
	}

	public static void onHandPre(RenderPlayer this0, EntityPlayer player) {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
		ClientProxy.INSTANCE.manager.bindHand(player, null, this0.modelBipedMain);
		ClientProxy.INSTANCE.manager.bindSkin(this0.modelBipedMain, TextureSheetType.SKIN);
	}

	public static void onHandPost(RenderPlayer this0, EntityPlayer player) {
		ClientProxy.INSTANCE.manager.unbindClear(this0.modelBipedMain);
	}

	public static boolean renderCape(boolean evtRC, RenderPlayer this0, EntityPlayer player, float partialTicks) {
		if(evtRC) {
			Player<?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
			if(pl != null) {
				ModelDefinition def = pl.getModelDefinition();
				if(def != null && def.hasRoot(RootModelType.CAPE)) {
					if(!player.getHasActivePotion() && !player.getHideCape()) {
						ModelBiped model = this0.modelBipedMain;
						ClientProxy.mc.getPlayerRenderManager().rebindModel(model);
						ClientProxy.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
						ClientProxy.renderCape(player, partialTicks, model, def);
					}
					return false;
				}
			}
		}
		return evtRC;
	}

	public static boolean onRenderPlayerModel(RenderPlayer this0, EntityLiving player0, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		EntityPlayer player = (EntityPlayer) player0;
		IPlayerRenderer this1 = (IPlayerRenderer) this0;
		boolean pBodyVisible = !player.getHasActivePotion();
		boolean pTranslucent = !pBodyVisible;
		if(!pBodyVisible && ClientProxy.mc.getPlayerRenderManager().isBound(this0.modelBipedMain)) {
			boolean r = ClientProxy.mc.getPlayerRenderManager().getHolderSafe(this0.modelBipedMain, null, h -> h.setInvisState(), false, false);
			if(pTranslucent)return false;
			if(!r)return false;
			this1.cpm$bindEntityTexture(player);

			ClientProxy.mc.getPlayerRenderManager().getHolderSafe(this0.modelBipedMain, null, h -> h.setInvis(false), false);
			this0.modelBipedMain.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
			return true;
		}
		return false;
	}

	public static String fixSkinURL(String url) {
		if (url.startsWith("http://skins.minecraft.net/MinecraftSkins/") && url.endsWith(".png")) {
			String username = url.substring(42, url.length() - 4);
			return GameProfileManager.getTextureUrlSync(username, TextureType.SKIN, url);
		} else if (url.startsWith("http://skins.minecraft.net/MinecraftCloaks/") && url.endsWith(".png")) {
			String username = url.substring(43, url.length() - 4);
			return GameProfileManager.getTextureUrlSync(username, TextureType.CAPE, url);
		}
		return url;
	}

	public static boolean onRenderName(RenderPlayer renderer, EntityPlayer entity, double xIn, double yIn, double zIn) {
		return ClientProxy.INSTANCE.onRenderName(renderer, entity, xIn, yIn, zIn);
	}

	public static void onDrawScreenPre() {
		PlayerProfile.inGui = true;
	}

	public static void onDrawScreenPost() {
		PlayerProfile.inGui = false;
	}

	public static void onInitScreen(GuiScreen screen) {
		if((screen instanceof GuiMainMenu && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof GuiOptions) {
			screen.controlList.add(new ClientProxy.Button(0, 0));
		}
	}

	public static void onGuiButtonClick(GuiButton button, GuiScreen screen) {
		if(button instanceof ClientProxy.Button) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiImpl(EditorGui::new, screen));
		}
	}

	public static GuiScreen openGui(GuiScreen screen) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if(screen == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			screen = ((GuiImpl.Overlay) minecraft.currentScreen).getGui();
		}
		if(screen instanceof GuiMainMenu && !(minecraft.currentScreen instanceof GuiSelectWorld || minecraft.currentScreen instanceof GuiMainMenu) && EditorGui.doOpenEditor()) {
			screen = new GuiImpl(EditorGui::new, screen);
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
		return screen;
	}
}
