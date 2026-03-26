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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelPart;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.render.RenderTypeBuilder;
import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.common.BiomeHandlerImpl;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.network.ModelEventType;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.MojangAPI;

public class MinecraftObject implements MinecraftClientAccess {
	private final ModelDefinitionLoader<GameProfile> loader;
	private final PlayerRenderManager prm;
	private AllTagManagers tags;
	public RenderTypeBuilder<Identifier, RenderType> renderBuilder;

	public MinecraftObject() {
		MinecraftObjectHolder.setClientObject(this);
		loader = new ModelDefinitionLoader<>(PlayerProfile::new, GameProfile::id, GameProfile::name);
		prm = new PlayerRenderManager();
		renderBuilder = new RenderTypeBuilder<>();
		renderBuilder.register(RenderMode.DEFAULT, RenderTypes::entityTranslucent, 0);
		renderBuilder.register(RenderMode.GLOW, CustomRenderTypes::glowingEyes, 1);
		renderBuilder.register(RenderMode.COLOR, CustomRenderTypes::entityColorTranslucent, 0);
		renderBuilder.register(RenderMode.COLOR_GLOW, CustomRenderTypes::entityColorEyes, 1);
		renderBuilder.register(RenderMode.OUTLINE, CustomRenderTypes::linesNoDepth, 2);
	}

	public void setTags(AllTagManagers tags) {
		this.tags = tags;
	}

	@Override
	public PlayerRenderManager getPlayerRenderManager() {
		return prm;
	}

	@Override
	public ITexture createTexture() {
		return new DynTexture();
	}

	public static class DynTexture implements ITexture {
		private static int ID = 0;
		private DynamicTexture dynTex;
		private Identifier loc;
		private static Identifier bound_loc;
		private Minecraft mc = Minecraft.getInstance();

		@Override
		public void bind() {
			if (loc == null)return;//No data
			bound_loc = loc;
			if(mc.getTextureManager().getTexture(loc) == null)
				mc.getTextureManager().register(loc, dynTex);
		}

		@Override
		public void load(Image texture) {
			if (loc == null || dynTex.getTexture().getWidth(0) != texture.getWidth() || dynTex.getTexture().getHeight(0) != texture.getHeight()) {
				if (loc != null) {
					mc.getTextureManager().release(loc);
				}

				int id = ID++;
				dynTex = new DynamicTexture("CPM Dynamic Texture #" + id, texture.getWidth(), texture.getHeight(), true);
				loc = Identifier.fromNamespaceAndPath("cpm", "dyn_" + id);
				mc.getTextureManager().register(loc, dynTex);
			}

			NativeImage ni = NativeImageIO.createFromBufferedImage(texture);
			dynTex.setPixels(ni);
			dynTex.upload();
		}

		public static Identifier getBoundLoc() {
			return bound_loc;
		}

		@Override
		public void free() {
			if (loc != null)
				mc.getTextureManager().release(loc);
		}
	}

	@Override
	public void executeOnGameThread(Runnable r) {
		Minecraft.getInstance().execute(r);
	}

	@Override
	public void executeNextFrame(Runnable r) {
		Minecraft.getInstance().schedule(r);
	}

	@Override
	public ModelDefinitionLoader<GameProfile> getDefinitionLoader() {
		return loader;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.get(DefaultPlayerSkin.get(Minecraft.getInstance().getUser().getProfileId()).model().getSerializedName());
	}

	@Override
	public void setEncodedGesture(int value) {
		Set<PlayerModelPart> s = Minecraft.getInstance().options.modelParts;
		setEncPart(s, value, 0, PlayerModelPart.HAT);
		setEncPart(s, value, 1, PlayerModelPart.JACKET);
		setEncPart(s, value, 2, PlayerModelPart.LEFT_PANTS_LEG);
		setEncPart(s, value, 3, PlayerModelPart.RIGHT_PANTS_LEG);
		setEncPart(s, value, 4, PlayerModelPart.LEFT_SLEEVE);
		setEncPart(s, value, 5, PlayerModelPart.RIGHT_SLEEVE);
		Minecraft.getInstance().options.broadcastOptions();
	}

	private static void setEncPart(Set<PlayerModelPart> s, int value, int off, PlayerModelPart part) {
		if((value & (1 << off)) != 0)s.add(part);
		else s.remove(part);
	}

	@Override
	public boolean isInGame() {
		return Minecraft.getInstance().player != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return Minecraft.getInstance().getGameProfile();
	}

	@Override
	public Object getCurrentPlayerIDObject() {
		var mc = Minecraft.getInstance();
		return PlayerProfile.getPlayerProfile(mc.player);
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public ServerStatus getServerSideStatus() {
		var mc = Minecraft.getInstance();
		ClientPacketListener conn = mc.getConnection();
		return mc.player != null ? conn instanceof NetH && ((NetH)conn).cpm$hasMod() ? ServerStatus.INSTALLED : ServerStatus.SKIN_LAYERS_ONLY : ServerStatus.OFFLINE;
	}

	@Override
	public File getGameDir() {
		return Minecraft.getInstance().gameDirectory;
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		var mc = Minecraft.getInstance();
		mc.setScreen(new GuiImpl(creator, mc.screen));
	}

	@Override
	public Runnable openSingleplayer() {
		var mc = Minecraft.getInstance();
		return () -> mc.setScreen(new SelectWorldScreen(mc.screen));
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return CustomPlayerModelsClient.INSTANCE.netHandler;
	}

	@Override
	public IImageIO getImageIO() {
		return new NativeImageIO();
	}

	@Override
	public MojangAPI getMojangAPI() {
		var mc = Minecraft.getInstance();
		return new MojangAPI(mc.getUser().getName(), mc.getUser().getProfileId(), mc.getUser().getAccessToken());
	}

	@Override
	public void clearSkinCache() {
		MojangAPI.clearYggdrasilCache(Minecraft.getInstance().services().sessionService());
		//mc.getGameProfile().clear();
		//mc.getGameProfile();//refresh TODO
	}

	@Override
	public String getConnectedServer() {
		if(Minecraft.getInstance().getConnection() == null)return null;
		SocketAddress sa = Platform.getChannel(Minecraft.getInstance().getConnection().getConnection()).remoteAddress();
		if(sa instanceof InetSocketAddress)
			return ((InetSocketAddress)sa).getHostString();
		return null;
	}

	@Override
	public List<Object> getPlayers() {
		if(Minecraft.getInstance().getConnection() == null)return Collections.emptyList();
		return Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(PlayerInfo::getProfile).collect(Collectors.toList());
	}

	@Override
	public Proxy getProxy() {
		return Minecraft.getInstance().getProxy();
	}

	@Override
	public RenderTypeBuilder<?, ?> getRenderBuilder() {
		return renderBuilder;
	}

	@Override
	public AllTagManagers getBuiltinTags() {
		return tags;
	}

	@Override
	public BiomeHandler<?> getBiomeHandler() {
		return BiomeHandlerImpl.clientImpl;
	}

	@Override
	public boolean requiresSelfEventForAnimation(ModelEventType type) {
		return type == ModelEventType.FALLING;
	}
}
