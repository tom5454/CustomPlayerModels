package com.tom.cpm.web.client;

import java.io.File;
import java.net.Proxy;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.render.RenderTypeBuilder;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.ILogger;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.shared.IPlayerRenderManager;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.retro.RetroGLAccess.RetroLayer;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.MojangAPI;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.FileManagerPopup;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.AsyncResourceException;
import com.tom.cpm.web.client.util.GameProfile;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.ExceptionUtil;

import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class WebMC implements MinecraftClientAccess, MinecraftCommonAccess, ILogger {
	private ModConfigFile config;
	private ModelDefinitionLoader<GameProfile> loader;
	private GameProfile profile;
	public static String platform;
	public RenderTypeBuilder<String, RetroLayer> renderBuilder = RenderTypeBuilder.setupRetro(new RenderSystem());
	private File root;
	private static WebMC instance;
	private boolean canExit, versionCheck;
	private static boolean betaWarning = true;

	public WebMC(ModConfigFile config, boolean canExit, boolean versionCheck) {
		this.versionCheck = versionCheck;
		MinecraftObjectHolder.setCommonObject(this);
		MinecraftObjectHolder.setClientObject(this);
		instance = this;
		this.canExit = canExit;

		platform = Java.getPlatform() + " CPM " + System.getProperty("cpm.version");
		root = new File("/");

		profile = new GameProfile(UUID.randomUUID(), "Web");
		this.config = config;
		loader = new ModelDefinitionLoader<>(PlayerProfile::new, GameProfile::getId, GameProfile::getName);
		/*try {
			ImageTest.runTest();
		} catch (Exception e) {
		}*/
	}

	public static void setProfile(GameProfile profile) {
		instance.profile = profile;
	}

	@Override
	public ModConfigFile getConfig() {
		return config;
	}

	@Override
	public ILogger getLogger() {
		return this;
	}

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return EnumSet.of(PlatformFeature.EDITOR_SUPPORTED);
	}

	@Override
	public String getPlatformVersionString() {
		return platform;
	}

	@Override
	public TextRemapper<?> getTextRemapper() {
		return null;
	}

	@Override
	public CPMApiManager getApi() {
		return null;
	}

	@Override
	public IPlayerRenderManager getPlayerRenderManager() {
		return ViewportPanelBase3d.manager;
	}

	@Override
	public ModelDefinitionLoader getDefinitionLoader() {
		return loader;
	}

	@Override
	public ITexture createTexture() {
		return new Texture();
	}

	public static class Texture implements ITexture {
		public static Texture bound;
		private String id;

		public Texture() {
			id = RenderSystem.newTexture();
		}

		@Override
		public void bind() {
			bound = this;
		}

		@Override
		public void load(Image image) {
			RenderSystem.upload(id, image);
		}

		@Override
		public void free() {
			RenderSystem.freeTexture(id);
		}

		public String getId() {
			return id;
		}
	}

	@Override
	public void executeLater(Runnable r) {
		new Promise<>((res, rej) -> {
			r.run();
			res.onInvoke(Js.undefined());
		});
	}

	@Override
	public Object getPlayerIDObject() {
		return profile;
	}

	@Override
	public Object getCurrentPlayerIDObject() {
		return null;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.DEFAULT;
	}

	@Override
	public void setEncodedGesture(int value) {

	}

	@Override
	public boolean isInGame() {
		return false;
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return Collections.emptyList();
	}

	@Override
	public File getGameDir() {
		return root;
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return null;
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
	}

	@Override
	public IImageIO getImageIO() {
		return new ImageIO();
	}

	@Override
	public MojangAPI getMojangAPI() {
		return new MojangAPI();
	}

	@Override
	public void clearSkinCache() {
	}

	@Override
	public String getConnectedServer() {
		return null;
	}

	@Override
	public List<Object> getPlayers() {
		return null;
	}

	@Override
	public Proxy getProxy() {
		return null;
	}

	@Override
	public RenderTypeBuilder<?, ?> getRenderBuilder() {
		return renderBuilder;
	}

	@Override
	public void info(String text) {
		DomGlobal.console.info(text);
	}

	@Override
	public void info(String text, Throwable thr) {
		if(thr instanceof AsyncResourceException)return;
		StringBuilder sb = new StringBuilder();
		sb.append(text);
		sb.append('\n');
		sb.append(ExceptionUtil.getStackTrace(thr));
		DomGlobal.console.info(sb.toString());
	}

	@Override
	public void error(String text) {
		DomGlobal.console.error(text);
	}

	@Override
	public void error(String text, Throwable thr) {
		if(thr instanceof AsyncResourceException)return;
		StringBuilder sb = new StringBuilder();
		sb.append(text);
		sb.append('\n');
		sb.append(ExceptionUtil.getStackTrace(thr));
		DomGlobal.console.error(sb.toString());
	}

	@Override
	public void warn(String text) {
		DomGlobal.console.warn(text);
	}

	@Override
	public void warn(String text, Throwable thr) {
		if(thr instanceof AsyncResourceException)return;
		StringBuilder sb = new StringBuilder();
		sb.append(text);
		sb.append('\n');
		sb.append(ExceptionUtil.getStackTrace(thr));
		DomGlobal.console.warn(sb.toString());
	}

	@Override
	public void populatePlatformSettings(String group, Panel panel) {
		switch (group) {
		case "filePopup":
		{
			PopupMenu pp = (PopupMenu) panel;
			if(!canExit) {
				ScrollPanel sp = (ScrollPanel) pp.getElements().get(0);
				sp.getDisplay().getElements().remove(sp.getDisplay().getElements().size() - 1);
			}
			if(FS.needFileManager()) {
				pp.addButton("File Manager", () -> panel.getGui().getFrame().openPopup(new FileManagerPopup(panel.getGui())));
			}
		}
		break;

		case "general":

			break;

		case "editor":
			if(betaWarning) {
				betaWarning = false;
				IGui gui = panel.getGui();
				((Frame)panel).openPopup(new MessagePopup((Frame) panel, gui.i18nFormat("label.cpm.warning"), "The web version of the editor is in Beta\\You may experience minor visual glitches with the editor.\\\\Your models will be saved inside your browser\\use the File/File Manager to download them, or use the ... button in the file chooser.\\Exporting models with the web editor is not recommended."));
			}
			break;

		default:
			break;
		}
	}

	public static void close() {
		if(instance.canExit) {
			RenderSystem.getWindow().close();
		}
	}

	@Override
	public String getMCVersion() {
		return "1.16.5";
	}

	@Override
	public String getMCBrand() {
		return "Web";
	}

	@Override
	public String getModVersion() {
		return System.getProperty("cpm.version");
	}

	@Override
	public IVersionCheck getVersionCheck() {
		if(versionCheck)
			return MinecraftCommonAccess.super.getVersionCheck();
		else {
			return new IVersionCheck() {

				@Override
				public boolean isOutdated() {
					return false;
				}

				@Override
				public Map<String, String> getChanges() {
					return null;
				}
			};
		}
	}
}
