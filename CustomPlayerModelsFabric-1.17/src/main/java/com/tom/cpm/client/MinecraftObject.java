package com.tom.cpm.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.TextureUtil;

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
import com.tom.cpm.shared.util.MojangAPI;

public class MinecraftObject implements MinecraftClientAccess {
	private final MinecraftClient mc;
	private final PlayerRenderManager prm;
	private final ModelDefinitionLoader<GameProfile> loader;

	public MinecraftObject(MinecraftClient mc) {
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

	public static class DynTexture extends NativeImageBackedTexture implements ITexture {
		private final Identifier loc;
		private final MinecraftClient mc;
		private static Identifier bound_loc;

		public DynTexture(MinecraftClient mc) {
			super(1, 1, true);
			this.mc = mc;
			loc = mc.getTextureManager().registerDynamicTexture("cpm", this);
		}

		@Override
		public void bind() {
			bound_loc = loc;
			if(mc.getTextureManager().getTexture(loc) == null)
				mc.getTextureManager().registerTexture(loc, this);
		}

		@Override
		public void load(Image texture) {
			NativeImage ni = NativeImageIO.createFromBufferedImage(texture);
			setImage(ni);
			TextureUtil.prepareImage(this.getGlId(), ni.getWidth(), ni.getHeight());
			upload();
		}

		public static Identifier getBoundLoc() {
			return bound_loc;
		}

		@Override
		public void free() {
			mc.getTextureManager().destroyTexture(loc);
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
		return SkinType.get(DefaultSkinHelper.getModel(mc.getSession().getProfile().getId()));
	}

	@Override
	public void setEncodedGesture(int value) {
		Set<PlayerModelPart> s = mc.options.enabledPlayerModelParts;
		setEncPart(s, value, 0, PlayerModelPart.HAT);
		setEncPart(s, value, 1, PlayerModelPart.JACKET);
		setEncPart(s, value, 2, PlayerModelPart.LEFT_PANTS_LEG);
		setEncPart(s, value, 3, PlayerModelPart.RIGHT_PANTS_LEG);
		setEncPart(s, value, 4, PlayerModelPart.LEFT_SLEEVE);
		setEncPart(s, value, 5, PlayerModelPart.RIGHT_SLEEVE);
		mc.options.sendClientSettings();
	}

	private static void setEncPart(Set<PlayerModelPart> s, int value, int off, PlayerModelPart part) {
		if((value & (1 << off)) != 0)s.add(part);
		else s.remove(part);
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
	public boolean isInGame() {
		return mc.player != null;
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public ServerStatus getServerSideStatus() {
		return mc.player != null ? ((NetH)mc.getNetworkHandler()).cpm$hasMod() ? ServerStatus.INSTALLED : ServerStatus.SKIN_LAYERS_ONLY : ServerStatus.OFFLINE;
	}

	@Override
	public File getGameDir() {
		return mc.runDirectory;
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		mc.setScreen(new GuiImpl(creator, mc.currentScreen));
	}

	@Override
	public Runnable openSingleplayer() {
		return () -> mc.setScreen(new SelectWorldScreen(mc.currentScreen));
	}

	@Override
	public NetHandler<?, ?, ?, ?, ?> getNetHandler() {
		return CustomPlayerModelsClient.INSTANCE.netHandler;
	}

	@Override
	public IImageIO getImageIO() {
		return new NativeImageIO();
	}

	@Override
	public MojangAPI getMojangAPI() {
		return new MojangAPI(mc.getSession().getProfile().getName(), mc.getSession().getProfile().getId(), mc.getSession().getAccessToken());
	}

	@Override
	public void clearSkinCache() {
		MojangAPI.clearYggdrasilCache(mc.getSessionService());
		mc.getSessionProperties().clear();
		mc.getSessionProperties();//refresh
	}

	@Override
	public String getConnectedServer() {
		if(mc.getNetworkHandler() == null)return null;
		SocketAddress sa = mc.getNetworkHandler().getConnection().channel.remoteAddress();
		if(sa instanceof InetSocketAddress)
			return ((InetSocketAddress)sa).getHostString();
		return null;
	}

	@Override
	public List<Object> getPlayers() {
		if(mc.getNetworkHandler() == null)return Collections.emptyList();
		return mc.getNetworkHandler().getPlayerList().stream().map(PlayerListEntry::getProfile).collect(Collectors.toList());
	}

	@Override
	public Proxy getProxy() {
		return mc.getNetworkProxy();
	}
}
