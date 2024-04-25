package com.tom.cpm.web.client;

import java.io.File;
import java.net.Proxy;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.block.entity.EntityTypeHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.render.RenderTypeBuilder;
import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.ILogger;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpl.util.Pair;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.shared.IPlayerRenderManager;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.retro.RetroGLAccess.RetroLayer;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.MdResourceIO;
import com.tom.cpm.shared.util.MojangAPI;
import com.tom.cpm.web.client.emul.BiomeHandlerImpl;
import com.tom.cpm.web.client.emul.BlockStateHandlerImpl;
import com.tom.cpm.web.client.emul.EntityTypeHandlerImpl;
import com.tom.cpm.web.client.emul.ItemStackHandlerImpl;
import com.tom.cpm.web.client.java.Base64;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.FileManagerPopup;
import com.tom.cpm.web.client.render.FullScreenButton;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.resources.Resources;
import com.tom.cpm.web.client.util.AsyncResourceException;
import com.tom.cpm.web.client.util.CPMApi;
import com.tom.cpm.web.client.util.GameProfile;
import com.tom.cpm.web.client.util.I18n;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.ExceptionUtil;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class WebMC implements MinecraftClientAccess, MinecraftCommonAccess, ILogger {
	private ModConfigFile config;
	private ModelDefinitionLoader<GameProfile> loader;
	private GameProfile profile;
	public static String platform;
	public RenderTypeBuilder<String, RetroLayer> renderBuilder = RenderTypeBuilder.setupRetro(new RenderSystem());
	private File root;
	private static WebMC instance;
	private boolean canExit, versionCheck;
	private static boolean firstOpen = true;
	private GuiImpl currentGui;
	private AllTagManagers tags;

	public WebMC(ModConfigFile config, boolean canExit, boolean versionCheck) {
		instance = this;
		this.versionCheck = versionCheck;
		MinecraftObjectHolder.setCommonObject(this);
		MinecraftObjectHolder.setClientObject(this);
		this.canExit = canExit;

		platform = buildPlatformString();
		root = new File(FS.getWorkDir());

		profile = new GameProfile(UUID.randomUUID(), "Web");
		this.config = config;
		loader = new ModelDefinitionLoader<>(PlayerProfile::new, GameProfile::getId, GameProfile::getName);

		tags = new AllTagManagers();
	}

	protected String buildPlatformString() {
		return Java.getPlatform() + " CPM " + System.getProperty("cpm.version");
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
	public void executeOnGameThread(Runnable r) {
		new Promise<>((res, rej) -> {
			RenderSystem.withContext(r);
			res.onInvoke(Js.undefined());
		});
	}

	@Override
	public void executeNextFrame(Runnable r) {
		executeOnGameThread(r);
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
				pp.addButton(panel.getGui().i18nFormat("web-button.fileManager"), () -> panel.getGui().getFrame().openPopup(new FileManagerPopup(panel.getGui(), LocalStorageFS.getInstance())));
			}
		}
		break;

		case "general":
		{
			IGui gui = panel.getGui();
			JsPropertyMap<String> mapIn = Js.<JsPropertyMap<String>>uncheckedCast(Global.JSON.parse(new String(Base64.getDecoder().decode(
					Resources.getResource("assets/cpmweb/lang-list.json")
					))));
			Map<String, String> map = new HashMap<>();
			mapIn.forEach(k -> {
				if (k.startsWith("comment"))return;
				String v = mapIn.get(k);
				map.put(k, v);
			});
			NameMapper<String> mapper = new NameMapper<>(map.keySet(), map::get);
			ListPicker<NamedElement<String>> langBtn = new ListPicker<>(gui.getFrame(), mapper.asList());
			mapper.setSetter(langBtn::setSelected);
			mapper.setValue(I18n.locale);
			langBtn.setAction(() -> {
				I18n.locale = langBtn.getSelected().getElem();
				try {
					WebStorageWindow.of(RenderSystem.getWindow()).localStorage.setItem("editorLanguage", I18n.locale);
				} catch (Exception e) {
				}
				gui.displayMessagePopup(gui.i18nFormat("web-label.language.reloadRequired.title"), gui.i18nFormat(getLangChangeDesc()));
			});
			langBtn.setBounds(new Box(5, 0, 150, 20));
			panel.addElement(langBtn);
		}
		break;

		case "editor":
			if(firstOpen) {
				firstOpen = false;
				IGui gui = panel.getGui();
				((Frame)panel).openPopup(new MessagePopup((Frame) panel, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("web-label.editorBeta")));
				String url = Java.getQueryVariable("file");
				Java.removeQueryVariable("file");
				if(url != null) {
					Editor e = ((EditorGui)panel).getEditor();
					e.setInfoMsg.accept(Pair.of(20000, gui.i18nFormat("web-label.loadFromURL")));
					CPMApi.fetch("file", url).then(f -> {
						FS.mount((String) f.get("data"), "download.cpmproject");
						e.load(new File("/mnt/download.cpmproject")).handle((v, ex) -> {
							if(ex != null) {
								Log.warn("Error loading project file", ex);
								ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.load", ex);
								e.setInfoMsg.accept(Pair.of(3000, gui.i18nFormat("label.cpm.error.load") + "\\" + ex));
							}
							return null;
						});
						return null;
					}).catch_(err -> {
						e.setInfoMsg.accept(Pair.of(3000, gui.i18nFormat("label.cpm.error.load") + "\\" + err));
						return null;
					});
				}
			}
			break;

		case "displayPopup":
		{
			PopupMenu pp = (PopupMenu) panel;
			Panel pn = ((ScrollPanel)pp.getElements().get(0)).getDisplay();
			pn.getElements().forEach(e -> {
				Box b = e.getBounds();
				e.setBounds(new Box(b.x, b.y + 20, b.w, b.h));
			});
			FullScreenButton fsb = new FullScreenButton(pp.getGui(), pp::close);
			fsb.setBounds(new Box(0, 0, 80, 20));
			pn.addElement(fsb);
		}
		break;

		default:
			break;
		}
	}

	protected String getLangChangeDesc() {
		return "web-label.language.reloadRequired.desc";
	}

	public static void close() {
		if(instance.canExit) {
			RenderSystem.getWindow().close();
		}
	}

	@Override
	public String getMCVersion() {
		return "web";
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

	public static WebMC getInstance() {
		return instance;
	}

	public void setGui(GuiImpl gui) {
		this.currentGui = gui;
	}

	public GuiImpl getGui() {
		return currentGui;
	}

	public void openURL(String url) {
		DomGlobal.window.open(url, "_blank").focus();
	}

	public String getAppID() {
		return DomGlobal.document.title;
	}

	@Override
	public BlockStateHandler<?> getBlockStateHandler() {
		return BlockStateHandlerImpl.impl;
	}

	@Override
	public ItemStackHandler<?> getItemStackHandler() {
		return ItemStackHandlerImpl.impl;
	}

	@Override
	public AllTagManagers getBuiltinTags() {
		return tags;
	}

	@Override
	public BiomeHandler<?> getBiomeHandler() {
		return BiomeHandlerImpl.impl;
	}

	@Override
	public EntityTypeHandler<?> getEntityTypeHandler() {
		return EntityTypeHandlerImpl.impl;
	}

	public Function<String, CompletableFuture<byte[]>> getNetworkFetch() {
		return MdResourceIO::jsFetch;
	}
}
