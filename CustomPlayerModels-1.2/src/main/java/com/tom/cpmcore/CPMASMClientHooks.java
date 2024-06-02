package com.tom.cpmcore;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiOptions;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSelectWorld;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelBiped;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.RenderLiving;
import net.minecraft.src.RenderPlayer;
import net.minecraft.src.World;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.EmulNetwork;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.RetroGL;
import com.tom.cpm.client.SinglePlayerCommands;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.MinecraftServerAccess;
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

import cpw.mods.fml.client.FMLClientHandler;

public class CPMASMClientHooks {

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

	public static void onLogout(World world) {
		if (world == null) {
			ClientProxy.INSTANCE.onLogout();
			if (MinecraftServerAccess.get() != null)
				ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		}
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

	public static void prePlayerRender() {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
	}

	public static void onHandPre(RenderPlayer this0) {
		RetroGL.color4f(1, 1, 1, 1);
		RetroGL.renderCallLoc = 0;
		ClientProxy.INSTANCE.manager.bindHand(FMLClientHandler.instance().getClient().thePlayer, null, this0.modelBipedMain);
		ClientProxy.INSTANCE.manager.bindSkin(this0.modelBipedMain, TextureSheetType.SKIN);
	}

	public static void onHandPost(RenderPlayer this0) {
		ClientProxy.INSTANCE.manager.unbindClear(this0.modelBipedMain);
	}

	public static boolean renderCape(boolean evtRC, RenderPlayer this0, EntityPlayer player, float partialTicks) {
		if(evtRC) {
			Player<?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
			if(pl != null) {
				ModelDefinition def = pl.getModelDefinition();
				if(def != null && def.hasRoot(RootModelType.CAPE)) {
					ModelBiped model = this0.modelBipedMain;
					ClientProxy.mc.getPlayerRenderManager().rebindModel(model);
					ClientProxy.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
					ClientProxy.renderCape(player, partialTicks, model, def);
					return false;
				}
			}
		}
		return evtRC;
	}

	public static boolean onRenderPlayerModel(RenderPlayer this0, EntityLiving player0, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		EntityPlayer player = (EntityPlayer) player0;
		IPlayerRenderer this1 = (IPlayerRenderer) this0;
		boolean pBodyVisible = true;
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
		if (url.startsWith("http://s3.amazonaws.com/MinecraftSkins/") && url.endsWith(".png")) {
			String username = url.substring(39, url.length() - 4);
			return GameProfileManager.getTextureUrlSync(username, TextureType.SKIN, url);
		} else if (url.startsWith("http://s3.amazonaws.com/MinecraftCloaks/") && url.endsWith(".png")) {
			String username = url.substring(40, url.length() - 4);
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
			FMLClientHandler.instance().getClient().displayGuiScreen(new GuiImpl(EditorGui::new, screen));
		}
	}

	public static GuiScreen openGui(GuiScreen screen) {
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		if(screen == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			screen = ((GuiImpl.Overlay) minecraft.currentScreen).getGui();
		}
		if(screen instanceof GuiMainMenu && !(minecraft.currentScreen instanceof GuiSelectWorld || minecraft.currentScreen instanceof GuiMainMenu) && EditorGui.doOpenEditor()) {
			screen = new GuiImpl(EditorGui::new, screen);
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
		return screen;
	}

	public static void setLightmap(int tex, float x, float y) {
		if (tex == OpenGlHelper.lightmapTexUnit) {
			RetroGL.clx = x;
			RetroGL.cly = y;
		}
	}

	public static void startSinglePlayer(ISaveHandler sh) {
		EmulNetwork.reset();
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(sh));
	}

	public static void onSinglePlayerLogin() {
		EntityPlayer pl = FMLClientHandler.instance().getClient().thePlayer;
		if (pl != null)
			ServerHandler.netHandler.onJoin(pl);
	}

	public static boolean isChatEnabled(Minecraft mc) {
		return true;
	}

	public static boolean testCommand(Minecraft mc, String command) {
		if (!mc.isMultiplayerWorld()) {
			mc.ingameGUI.getSentMessageList().add(command);
			if (command.startsWith("/")) {
				SinglePlayerCommands.executeCommand(mc, command);
			} else {
				mc.ingameGUI.addChatMessage("<" + mc.thePlayer.username + "> " + command);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static void tabComplete(GuiChat gui) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (mc.isMultiplayerWorld()) {
			gui.completePlayerName();
		} else {

			Iterator<String> var2;
			String var3;
			if (gui.field_50060_d) {
				gui.inputField.func_50021_a(-1);
				if (gui.field_50067_h >= gui.field_50068_i.size()) {
					gui.field_50067_h = 0;
				}
			} else {
				int var1 = gui.inputField.func_50028_c(-1);
				if (gui.inputField.func_50035_h() - var1 < 1) {
					return;
				}

				gui.field_50068_i.clear();
				gui.field_50061_e = gui.inputField.getText().substring(var1);
				gui.field_50059_f = gui.field_50061_e.toLowerCase();
				var2 = SinglePlayerCommands.tabComplete(gui.inputField.getText()).iterator();

				while (var2.hasNext()) {
					var3 = var2.next();
					if (var3.startsWith(gui.field_50059_f)) {
						gui.field_50068_i.add(var3);
					}
				}

				if (gui.field_50068_i.size() == 0) {
					return;
				}

				gui.field_50060_d = true;
				gui.field_50067_h = 0;
				gui.inputField.func_50020_b(var1 - gui.inputField.func_50035_h());
			}

			if (gui.field_50068_i.size() > 1) {
				StringBuilder var4 = new StringBuilder();

				for (var2 = gui.field_50068_i.iterator(); var2.hasNext(); var4.append(var3)) {
					var3 = var2.next();
					if (var4.length() > 0) {
						var4.append(", ");
					}
				}

				mc.ingameGUI.addChatMessage(var4.toString());
			}

			gui.inputField.func_50031_b((String) gui.field_50068_i.get(gui.field_50067_h++));
		}
	}

	public static void inj_sendPacket(NetClientHandler handler, String id, byte[] data) {
		handler.addToSendQueue(ServerHandler.packet(id, data));
	}

	public static Entity inj_getEntityByID(NetClientHandler handler, int id) {
		return handler.worldClient.getEntityByID(id);
	}

	public static void clientTickEnd() {
		MCExecutor.executeAll();
	}
}
