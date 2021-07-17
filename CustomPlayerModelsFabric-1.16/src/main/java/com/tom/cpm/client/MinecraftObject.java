package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

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
	private static final Identifier STEVE_SKIN = new Identifier("textures/entity/steve.png");
	private static final Identifier ALEX_SKIN = new Identifier("textures/entity/alex.png");

	private final MinecraftClient mc;
	private final PlayerRenderManager prm;
	private final ModelDefinitionLoader loader;
	public MinecraftObject(MinecraftClient mc) {
		this.mc = mc;
		MinecraftObjectHolder.setClientObject(this);
		loader = new ModelDefinitionLoader(PlayerProfile::create);
		prm = new PlayerRenderManager(loader);
	}

	@Override
	public Image getVanillaSkin(SkinType skinType) {
		Identifier loc;
		switch (skinType) {
		case SLIM:
			loc = ALEX_SKIN;
			break;

		case DEFAULT:
		case UNKNOWN:
		default:
			loc = STEVE_SKIN;
			break;
		}
		try(Resource r = mc.getResourceManager().getResource(loc)) {
			return Image.loadFrom(r.getInputStream());
		} catch (IOException e) {
		}
		return null;
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
			TextureUtil.allocate(this.getGlId(), ni.getWidth(), ni.getHeight());
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
	public ModelDefinitionLoader getDefinitionLoader() {
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
		mc.options.onPlayerModelPartChange();
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
		mc.openScreen(new GuiImpl(creator, mc.currentScreen));
	}

	@Override
	public Runnable openSingleplayer() {
		return () -> mc.openScreen(new SelectWorldScreen(mc.currentScreen));
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
	public MojangSkinUploadAPI getUploadAPI() {
		return new MojangSkinUploadAPI(mc.getSession().getProfile().getId(), mc.getSession().getAccessToken());
	}

	@Override
	public void clearSkinCache() {
		MojangSkinUploadAPI.clearYggdrasilCache(mc.getSessionService());
		mc.getSessionProperties().clear();
		mc.getSessionProperties();//refresh
	}
}
