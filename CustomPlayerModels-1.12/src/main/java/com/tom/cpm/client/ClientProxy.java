package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.util.Image;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpmcore.CPMASMClientHooks;

import io.netty.buffer.Unpooled;

public class ClientProxy extends CommonProxy {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	private Minecraft minecraft;
	public static ClientProxy INSTANCE;
	private RenderManager<GameProfile, EntityPlayer, ModelBase, Void> manager;

	@Override
	public void init() {
		super.init();
		try(InputStream is = ClientProxy.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(Image.loadFrom(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		INSTANCE = this;
		minecraft = Minecraft.getMinecraft();
		mc = new MinecraftObject(minecraft, loader);
		MinecraftObjectHolder.setClientObject(mc);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), loader, EntityPlayer::getGameProfile);
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.getEntityPlayer(), null);
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.tryUnbind();
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.getGui() instanceof GuiMainMenu && ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
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
	}

	@SubscribeEvent
	public void renderHand(RenderHandEvent evt) {
		manager.bindHand(Minecraft.getMinecraft().player, null, PlayerRenderManager::unbindHand);
	}

	public void renderSkull(ModelBase skullModel, GameProfile profile) {
		manager.bindSkull(profile, null, PlayerRenderManager::unbindSkull, skullModel);
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

		for (Entry<Integer, KeyBinding> e : KeyBindings.quickAccess.entrySet()) {
			if(e.getValue().isPressed()) {
				mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
			}
		}
	}

	@SubscribeEvent
	public void onRenderName(RenderLivingEvent.Specials.Pre<AbstractClientPlayer> evt) {
		if(evt.getEntity() instanceof AbstractClientPlayer) {
			if(!Player.isEnableNames())
				evt.setCanceled(true);
		}
	}

	public static class Button extends GuiButton {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, I18n.format("button.cpm.open_editor"));
		}

	}

	public void onLogout() {
		loader.clearServerData();
	}

	public void receivePacket(SPacketCustomPayload packet, NetHandlerPlayClient h) {
		ResourceLocation rl = new ResourceLocation(packet.getChannelName());
		if(NetworkHandler.helloPacket.equals(rl)) {
			PacketBuffer pb = packet.getBufferData();
			try {
				NBTTagCompound nbt = pb.readCompoundTag();
				Minecraft.getMinecraft().addScheduledTask(() -> {
					CPMASMClientHooks.setHasMod(h, true);
					loader.clearServerData();
					h.sendPacket(new CPacketCustomPayload(NetworkHandler.helloPacket.toString(), new PacketBuffer(Unpooled.EMPTY_BUFFER)));
				});
			} catch (IOException e) {}
		} else if(NetworkHandler.setSkin.equals(rl)) {
			PacketBuffer pb = packet.getBufferData();
			int entId = pb.readVarInt();
			try {
				NBTTagCompound data = pb.readCompoundTag();
				Minecraft.getMinecraft().addScheduledTask(() -> {
					Entity ent = Minecraft.getMinecraft().world.getEntityByID(entId);
					if(ent instanceof EntityPlayer) {
						loader.setModel(((EntityPlayer)ent).getGameProfile(), data.hasKey("data") ? data.getByteArray("data") : null, data.getBoolean("forced"));
					}
				});
			} catch (IOException e) {}
		} else if(NetworkHandler.getSkin.equals(rl)) {
			sendSkinData(h);
		}
	}

	public void sendSkinData(NetHandlerPlayClient h) {
		String model = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL, null);
		if(model != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			try {
				ModelFile file = ModelFile.load(new File(modelsDir, model));
				PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
				NBTTagCompound data = new NBTTagCompound();
				data.setByteArray("data", file.getDataBlock());
				pb.writeCompoundTag(data);
				h.sendPacket(new CPacketCustomPayload(NetworkHandler.setSkin.toString(), pb));
			} catch (IOException e) {
			}
		} else {
			PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
			NBTTagCompound data = new NBTTagCompound();
			pb.writeCompoundTag(data);
			h.sendPacket(new CPacketCustomPayload(NetworkHandler.setSkin.toString(), pb));
		}
	}
}
