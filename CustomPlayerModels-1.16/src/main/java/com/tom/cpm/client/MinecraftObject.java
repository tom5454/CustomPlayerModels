package com.tom.cpm.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.MojangSkinUploadAPI;

public class MinecraftObject implements MinecraftClientAccess {
	private final Minecraft mc;
	private final ModelDefinitionLoader<GameProfile> loader;
	private final PlayerRenderManager prm;

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

	public static class DynTexture implements ITexture {
		private final DynamicTexture dynTex;
		private final ResourceLocation loc;
		private final Minecraft mc;
		private static ResourceLocation bound_loc;

		public DynTexture(Minecraft mc) {
			dynTex = new DynamicTexture(1, 1, true);
			this.mc = mc;
			loc = mc.getTextureManager().register("cpm", dynTex);
		}

		@Override
		public void bind() {
			bound_loc = loc;
			if(mc.getTextureManager().getTexture(loc) == null)
				mc.getTextureManager().register(loc, dynTex);
		}

		@Override
		public void load(Image texture) {
			NativeImage ni = NativeImageIO.createFromBufferedImage(texture);
			dynTex.setPixels(ni);
			TextureUtil.prepareImage(dynTex.getId(), ni.getWidth(), ni.getHeight());
			dynTex.upload();
		}

		public static ResourceLocation getBoundLoc() {
			return bound_loc;
		}

		@Override
		public void free() {
			mc.getTextureManager().release(loc);
		}

		public DynamicTexture getDynTex() {
			return dynTex;
		}
	}

	@Override
	public void executeLater(Runnable r) {
		mc.execute(r);
	}

	@Override
	public ModelDefinitionLoader<GameProfile> getDefinitionLoader() {
		return loader;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.get(DefaultPlayerSkin.getSkinModelName(mc.getUser().getGameProfile().getId()));
	}

	@Override
	public void setEncodedGesture(int value) {
		Set<PlayerModelPart> s = mc.options.modelParts;
		setEncPart(s, value, 0, PlayerModelPart.HAT);
		setEncPart(s, value, 1, PlayerModelPart.JACKET);
		setEncPart(s, value, 2, PlayerModelPart.LEFT_PANTS_LEG);
		setEncPart(s, value, 3, PlayerModelPart.RIGHT_PANTS_LEG);
		setEncPart(s, value, 4, PlayerModelPart.LEFT_SLEEVE);
		setEncPart(s, value, 5, PlayerModelPart.RIGHT_SLEEVE);
		mc.options.broadcastOptions();
	}

	private static void setEncPart(Set<PlayerModelPart> s, int value, int off, PlayerModelPart part) {
		if((value & (1 << off)) != 0)s.add(part);
		else s.remove(part);
	}

	@Override
	public boolean isInGame() {
		return mc.player != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return mc.getUser().getGameProfile();
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
	public ServerStatus getServerSideStatus() {
		return mc.player != null ? ((NetH)mc.getConnection()).cpm$hasMod() ? ServerStatus.INSTALLED : ServerStatus.SKIN_LAYERS_ONLY : ServerStatus.OFFLINE;
	}

	@Override
	public File getGameDir() {
		return mc.gameDirectory;
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		mc.setScreen(new GuiImpl(creator, mc.screen));
	}

	@Override
	public Runnable openSingleplayer() {
		return () -> mc.setScreen(new WorldSelectionScreen(mc.screen));
	}

	@Override
	public NetHandler<?, ?, ?, ?, ?> getNetHandler() {
		return ClientProxy.INSTANCE.netHandler;
	}

	@Override
	public IImageIO getImageIO() {
		return new NativeImageIO();
	}

	@Override
	public MojangSkinUploadAPI getUploadAPI() {
		return new MojangSkinUploadAPI(mc.getUser().getGameProfile().getId(), mc.getUser().getAccessToken());
	}

	@Override
	public void clearSkinCache() {
		MojangSkinUploadAPI.clearYggdrasilCache(mc.getMinecraftSessionService());
		mc.getProfileProperties().clear();
		mc.getProfileProperties();//refresh
	}

	@Override
	public String getConnectedServer() {
		if(mc.getConnection() == null)return null;
		SocketAddress sa = mc.getConnection().getConnection().channel().remoteAddress();
		if(sa instanceof InetSocketAddress)
			return ((InetSocketAddress)sa).getHostString();
		return null;
	}

	@Override
	public List<Object> getPlayers() {
		if(mc.getConnection() == null)return Collections.emptyList();
		return mc.getConnection().getOnlinePlayers().stream().map(NetworkPlayerInfo::getProfile).collect(Collectors.toList());
	}
}
