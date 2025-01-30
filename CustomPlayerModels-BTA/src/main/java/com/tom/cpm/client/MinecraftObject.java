package com.tom.cpm.client;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocalMultiplayer;
import net.minecraft.client.gui.ScreenSelectWorld;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.render.RenderTypeBuilder;
import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpl.util.AWTImageIO;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.common.BiomeHandlerImpl;
import com.tom.cpm.retro.GameProfile;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.retro.RetroGLAccess.RetroLayer;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.MojangAPI;

public class MinecraftObject implements MinecraftClientAccess {
	private final Minecraft mc;
	private final PlayerRenderManager prm;
	private final AllTagManagers tags;
	private final ModelDefinitionLoader<GameProfile> loader;
	private final RenderTypeBuilder<net.minecraft.client.render.texture.Texture, RetroLayer> renderBuilder = RenderTypeBuilder.setupRetro(new RetroGL());

	public MinecraftObject(Minecraft mc) {
		this.mc = mc;
		MinecraftObjectHolder.setClientObject(this);
		loader = new ModelDefinitionLoader<>(PlayerProfile::new, GameProfile::getId, GameProfile::getName);
		prm = new PlayerRenderManager();
		tags = new AllTagManagers(null, CPMTagLoader::new);
	}

	@Override
	public PlayerRenderManager getPlayerRenderManager() {
		return prm;
	}

	@Override
	public ITexture createTexture() {
		return new Texture();
	}

	public static class Texture implements ITexture {
		public static Texture bound;
		private net.minecraft.client.render.texture.Texture mcTex;

		public Texture() {
			mcTex = new net.minecraft.client.render.texture.Texture() {

				@Override
				protected void finalize() throws Throwable {
					// Fix rare JVM crash due to finalizer not running in the game thread
					if (isGenerated()) {
						MCExecutor.addScheduledTask(this::delete);
					}
				}
			};
			mcTex.generate();
		}

		@Override
		public void bind() {
			bound = this;
		}

		@Override
		public void load(Image image) {
			mcTex.setupTexture(AWTImageIO.toBufferedImage(image));
		}

		@Override
		public void free() {
			mcTex.delete();
		}

		public net.minecraft.client.render.texture.Texture getMcTex() {
			return mcTex;
		}
	}

	@Override
	public void executeOnGameThread(Runnable r) {
		MCExecutor.addScheduledTask(r);
	}

	@Override
	public void executeNextFrame(Runnable r) {
		MCExecutor.tell(r);
	}

	@Override
	public ModelDefinitionLoader<GameProfile> getDefinitionLoader() {
		return loader;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.DEFAULT;
	}

	@Override
	public void setEncodedGesture(int value) {
		CustomPlayerModelsClient.netHandler.sendLayer(value);
	}

	@Override
	public boolean isInGame() {
		return mc.thePlayer != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return GameProfileManager.getProfile(mc.session.username);
	}

	@Override
	public Object getCurrentPlayerIDObject() {
		return mc.thePlayer != null ? GameProfileManager.getProfile(mc.thePlayer.username) : null;
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public ServerStatus getServerSideStatus() {
		return isInGame() ? getNetHandler().hasModClient() ? ServerStatus.INSTALLED : ServerStatus.UNAVAILABLE : ServerStatus.OFFLINE;
	}

	@Override
	public File getGameDir() {
		return Minecraft.INSTANCE.getMinecraftDir();
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		mc.displayScreen(new GuiImpl(creator, mc.currentScreen));
	}

	@Override
	public Runnable openSingleplayer() {
		return () -> mc.displayScreen(new ScreenSelectWorld(mc.currentScreen));
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return CustomPlayerModelsClient.netHandler;
	}

	@Override
	public IImageIO getImageIO() {
		return new AWTImageIO();
	}

	@Override
	public MojangAPI getMojangAPI() {
		GameProfile gp = GameProfileManager.getProfile(mc.session.username);
		return new MojangAPI(gp.getName(), gp.getId(), mc.session.sessionId);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void clearSkinCache() {
		GameProfileManager.clear();

		try {
			// Clear Ears cache
			Class<?> earsLegacy = Class.forName("com.unascribed.ears.legacy.LegacyHelper");
			Field f = earsLegacy.getDeclaredField("skinUrls");
			f.setAccessible(true);
			((Map) f.get(null)).clear();
			f = earsLegacy.getDeclaredField("cache");
			f.setAccessible(true);
			((Map) f.get(null)).clear();
			Log.info("Cleared Ears Cache");
		} catch (Throwable e) {
		}
	}

	@Override
	public String getConnectedServer() {
		if (mc.thePlayer == null)return null;
		return EmulNetwork.getClient(mc.thePlayer).cpm$getConnectedServer();
	}

	@Override
	public List<Object> getPlayers() {
		if(mc.thePlayer == null)return Collections.emptyList();
		Stream<String> players;
		if (mc.thePlayer instanceof PlayerLocalMultiplayer) {
			PlayerLocalMultiplayer mp = (PlayerLocalMultiplayer) mc.thePlayer;
			players = mp.sendQueue.players.stream().map(e -> e.playerName);
		} else {
			players = Stream.of(mc.thePlayer.username);
		}
		return players.map(e -> GameProfileManager.getProfile(e)).collect(Collectors.toList());
	}

	@Override
	public Proxy getProxy() {
		return Proxy.NO_PROXY;
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
		return BiomeHandlerImpl.impl;
	}
}
