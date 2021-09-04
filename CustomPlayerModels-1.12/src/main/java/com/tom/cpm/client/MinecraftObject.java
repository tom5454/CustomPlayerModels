package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.util.AWTImageIO;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.MojangSkinUploadAPI;

public class MinecraftObject implements MinecraftClientAccess {
	private final Minecraft mc;
	private final PlayerRenderManager prm;
	private final ModelDefinitionLoader<GameProfile> loader;

	public MinecraftObject(Minecraft mc) {
		this.mc = mc;
		MinecraftObjectHolder.setClientObject(this);
		loader = new ModelDefinitionLoader<>(PlayerProfile::new, GameProfile::getId, GameProfile::getName);
		prm = new PlayerRenderManager();
	}

	@Override
	public PlayerRenderManager getPlayerRenderManager() {
		return prm;
	}

	@Override
	public ITexture createTexture() {
		return new DynTexture(mc);
	}

	public static class DynTexture extends DynamicTexture implements ITexture {
		private final ResourceLocation loc;
		private final Minecraft mc;

		public DynTexture(Minecraft mc) {
			super(1, 1);
			loc = mc.getTextureManager().getDynamicTextureLocation("cpm", this);
			this.mc = mc;
		}

		private static ResourceLocation bound_loc;

		@Override
		public void bind() {
			GlStateManager.bindTexture(getGlTextureId());
			bound_loc = loc;
			if(mc.getTextureManager().getTexture(loc) == null)
				mc.getTextureManager().loadTexture(loc, this);
		}

		@Override
		public void load(Image image) {
			this.deleteGlTexture();
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), AWTImageIO.toBufferedImage(image), false, false);
		}

		@Override
		public void free() {
			this.deleteGlTexture();
			mc.getTextureManager().deleteTexture(loc);
		}

		@Override
		public void loadTexture(IResourceManager resourceManager) throws IOException {}

		public static ResourceLocation getBoundLoc() {
			return bound_loc;
		}
	}

	@Override
	public void executeLater(Runnable r) {
		mc.addScheduledTask(r);
	}

	@Override
	public ModelDefinitionLoader<GameProfile> getDefinitionLoader() {
		return loader;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.get(DefaultPlayerSkin.getSkinType(mc.getSession().getProfile().getId()));
	}

	@Override
	public void setEncodedGesture(int value) {
		Set<EnumPlayerModelParts> s = ObfuscationReflectionHelper.getPrivateValue(GameSettings.class, mc.gameSettings, "field_178882_aU");
		setEncPart(s, value, 0, EnumPlayerModelParts.HAT);
		setEncPart(s, value, 1, EnumPlayerModelParts.JACKET);
		setEncPart(s, value, 2, EnumPlayerModelParts.LEFT_PANTS_LEG);
		setEncPart(s, value, 3, EnumPlayerModelParts.RIGHT_PANTS_LEG);
		setEncPart(s, value, 4, EnumPlayerModelParts.LEFT_SLEEVE);
		setEncPart(s, value, 5, EnumPlayerModelParts.RIGHT_SLEEVE);
		mc.gameSettings.sendSettingsToServer();
	}

	private static void setEncPart(Set<EnumPlayerModelParts> s, int value, int off, EnumPlayerModelParts part) {
		if((value & (1 << off)) != 0)s.add(part);
		else s.remove(part);
	}

	@Override
	public boolean isInGame() {
		return mc.player != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return mc.getSession().getProfile();
	}

	@Override
	public Object getCurrentPlayerIDObject() {
		return mc.player != null ? mc.player.getGameProfile() : null;
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public File getGameDir() {
		return mc.mcDataDir;
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		mc.displayGuiScreen(new GuiImpl(creator, mc.currentScreen));
	}

	@Override
	public Runnable openSingleplayer() {
		return () -> mc.displayGuiScreen(new GuiWorldSelection(mc.currentScreen));
	}

	@Override
	public NetHandler<?, ?, ?, ?, ?> getNetHandler() {
		return ClientProxy.INSTANCE.netHandler;
	}

	@Override
	public IImageIO getImageIO() {
		return new AWTImageIO();
	}

	@Override
	public MojangSkinUploadAPI getUploadAPI() {
		return new MojangSkinUploadAPI(mc.getSession().getProfile().getId(), mc.getSession().getToken());
	}

	@Override
	public void clearSkinCache() {
		MojangSkinUploadAPI.clearYggdrasilCache(mc.getSessionService());
		mc.getProfileProperties().clear();
		mc.getProfileProperties();//refresh
	}

	@Override
	public String getConnectedServer() {
		if(mc.getConnection() == null)return null;
		SocketAddress sa = mc.getConnection().getNetworkManager().channel().remoteAddress();
		if(sa instanceof InetSocketAddress)
			return ((InetSocketAddress)sa).getHostString();
		return null;
	}

	@Override
	public List<Object> getPlayers() {
		if(mc.getConnection() == null)return Collections.emptyList();
		return mc.getConnection().getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).collect(Collectors.toList());
	}
}
