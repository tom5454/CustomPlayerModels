package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.util.Image;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import io.netty.buffer.Unpooled;

public class ClientProxy extends CommonProxy {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	private Minecraft minecraft;
	public static ClientProxy INSTANCE;
	private RenderManager<GameProfile, EntityPlayer, ModelBase, Void> manager;
	public NetH netHandler;

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
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), loader, EntityPlayer::getGameProfile);
		netHandler = new NetH();
		netHandler.setNewNbt(NBTTagCompound::new);
		netHandler.setNewPacketBuffer(() -> new PacketBuffer(Unpooled.buffer()));
		netHandler.setWriteCompound((t, u) -> {
			try {
				t.writeNBTTagCompoundToBuffer(u);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, t -> {
			try {
				return t.readNBTTagCompoundFromBuffer();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		netHandler.setNBTSetters(NBTTagCompound::setBoolean, NBTTagCompound::setByteArray, NBTTagCompound::setFloat);
		netHandler.setNBTGetters(NBTTagCompound::getBoolean, NBTTagCompound::getByteArray, NBTTagCompound::getFloat);
		netHandler.setContains(NBTTagCompound::hasKey);
		Executor ex = minecraft::func_152344_a;
		netHandler.setExecutor(() -> ex);
		netHandler.setSendPacket((c, rl, pb) -> c.addToSendQueue(new C17PacketCustomPayload(rl.toString(), pb)), null);
		netHandler.setPlayerToLoader(EntityPlayer::getGameProfile);
		netHandler.setReadPlayerId(PacketBuffer::readVarIntFromBuffer, id -> {
			Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(id);
			if(ent instanceof EntityPlayer) {
				return (EntityPlayer) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.thePlayer);
		netHandler.setGetNet(c -> ((EntityClientPlayerMP) c).sendQueue);
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.entityPlayer, null);
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.tryUnbind();
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.gui instanceof GuiMainMenu && ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.gui instanceof GuiOptions) {
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
		if(openGui.gui instanceof GuiMainMenu && EditorGui.doOpenEditor()) {
			openGui.gui = new GuiImpl(EditorGui::new, openGui.gui);
		}
	}

	@SubscribeEvent
	public void renderHand(RenderHandEvent evt) {
		manager.bindHand(Minecraft.getMinecraft().thePlayer, null);
	}

	public void renderSkull(ModelBase skullModel, GameProfile profile) {
		manager.bindSkull(profile, null, skullModel);
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

		if (minecraft.thePlayer == null || evt.phase == Phase.START)
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
	public void onRenderName(RenderLivingEvent.Specials.Pre evt) {
		if(evt.entity instanceof AbstractClientPlayer) {
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

	public static class NetH extends NetHandler<ResourceLocation, NBTTagCompound, EntityPlayer, PacketBuffer, NetHandlerPlayClient> {

		public NetH() {
			super(ResourceLocation::new);
		}

		@Override
		public void receiveClient(ResourceLocation key, PacketBuffer data, com.tom.cpm.shared.network.NetH net) {
			if(key.equals(setLayer)) {
				int entId = data.readVarIntFromBuffer();
				int layer = data.readByte();
				Minecraft.getMinecraft().func_152344_a(() -> {
					Entity ent = Minecraft.getMinecraft().theWorld.getEntityByID(entId);
					if(ent instanceof EntityPlayer) {
						PlayerProfile profile = (PlayerProfile) ClientProxy.INSTANCE.loader.loadPlayer(((EntityPlayer) ent).getGameProfile());
						profile.encGesture = layer;
					}
				});
			} else
				super.receiveClient(key, data, net);
		}

		public void sendLayer(int value) {
			if(hasModClient()) {
				PacketBuffer pb = newPacketBuffer.get();
				pb.writeByte(value);
				sendPacket.accept(getClientNet(), setLayer, pb);
			}
		}
	}

	public void unbind(ModelBase skullModel) {
		manager.tryUnbind(skullModel);
	}

	public void unbind() {
		manager.tryUnbindPlayer(Minecraft.getMinecraft().thePlayer);
	}
}
