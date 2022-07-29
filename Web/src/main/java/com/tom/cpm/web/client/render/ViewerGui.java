package com.tom.cpm.web.client.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.io.SkinDataInputStream;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.web.client.PlayerProfile;
import com.tom.cpm.web.client.PlayerProfile.PlayerInfo;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.util.AsyncResourceException;
import com.tom.cpm.web.client.util.GameProfile;
import com.tom.ugwt.client.UGWTContext;

import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;

public class ViewerGui extends Frame implements IModelDisplayPanel {
	public static final List<Promise<?>> bgLoading = new ArrayList<>();
	private String name;
	private GameProfile profile;
	private ViewportCamera cam;
	private AnimationHandler animHandler;
	private CompletableFuture<ModelDefinition> def;
	private Player<?> player;
	private TextureProvider vanillaSkin;

	@SuppressWarnings("unchecked")
	public static Promise<Map<String, Object>> fetch(String api, String value) {
		return DomGlobal.fetch(System.getProperty("cpm.webApiEndpoint") + "/" + api + "?v=" + value).then(v -> {
			DomGlobal.console.info("Fetched: " + api);
			return v.text();//.then(Response::text)
		}).then(r -> {
			DomGlobal.console.info(r);
			Map<String, Object> data = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(r, Object.class);
			if(data.containsKey("error")) {
				String err = (String) data.get("error");
				return Promise.reject(err);
			}
			return Promise.resolve(data);
		});
	}

	public ViewerGui(IGui gui) {
		super(gui);
		UGWTContext.setContext(DomGlobal.window);
		name = Java.getQueryVariable("name");
		animHandler = new AnimationHandler(this::getSelectedDefinition);
		fetch("search", name).then(this::loadProfile).then(this::initModel).catch_(e -> {
			def = CompletableFuture.completedFuture(new ModelDefinition(new IOException("Failed to load model: " + e), player));
			Map<String, Object> msg = new HashMap<>();
			if(e instanceof Throwable)
				Log.error("Failed to load model: ", (Throwable) e);
			else
				DomGlobal.console.error("Failed to load model: ", e);
			msg.put("error", "Failed to load model: " + e);
			if(DomGlobal.window.parent != null)
				DomGlobal.window.parent.postMessage(MinecraftObjectHolder.gson.toJson(msg), "*");
			return null;
		});
		cam = new ViewportCamera() {

			@Override
			public void reset() {
				camDist = getQuerryValue("dist", 64);
				position = getQuerryValue("pos", new Vec3f(0.5f, 1, 0.5f));
				look = getQuerryValue("look", new Vec3f(0.25f, 0.5f, 0.25f));
			}
		};
		cam.reset();
	}

	private static Vec3f getQuerryValue(String name, Vec3f def) {
		float x = getQuerryValue(name + "x", def.x);
		float y = getQuerryValue(name + "y", def.y);
		float z = getQuerryValue(name + "z", def.z);
		return new Vec3f(x, y, z);
	}

	private static float getQuerryValue(String name, float def) {
		try {
			String q = Java.getQueryVariable(name);
			if(q != null) {
				return Float.parseFloat(q);
			}
		} catch (NumberFormatException e) {
		}
		return def;
	}

	private int initCalled = 5;
	private boolean initRunning;

	@SuppressWarnings("unchecked")
	private Promise<Object> initModel(Object[] unused) {
		player = MinecraftClientAccess.get().getDefinitionLoader().loadPlayer(profile, "gui");

		ModelDefinition def = player.getModelDefinition0();
		if(def == null) {
			this.def = player.getTextures().load().thenCompose(_v -> {
				return player.getTextures().getTexture(TextureType.SKIN).
						thenApply(i -> {
							if(i == null)i = player.getSkinType().getSkinTexture();
							vanillaSkin = new TextureProvider(i, new Vec2i(64, 64));
							return ModelDefinition.createVanilla(() -> vanillaSkin, player.getSkinType());
						});
			});
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Promise<Object[]> loadProfile(Map<String, Object> data) {
		DomGlobal.console.info(data);
		Map<String, Object> msg = new HashMap<>();
		List<Promise<?>> texLoad = new ArrayList<>();
		if(data.containsKey("error")) {
			String err = (String) data.get("error");
			msg.put("error", err);
			texLoad.add(Promise.reject(err));
		} else {
			String id = (String) data.get("id");
			String name = (String) data.get("name");
			msg.put("name", name);
			msg.put("id", id);
			profile = new GameProfile(fromString(id), name);
			Map<TextureType, String> textures = new HashMap<>();
			String skinType = "default";
			List<Map<String, Object>> pr = (List<Map<String, Object>>) data.get("properties");
			for (Map<String, Object> map : pr) {
				if("textures".equals(map.get("name"))) {
					String val = new String(Base64.getDecoder().decode((String) map.get("value")));
					Map<String, Object> p = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(val, Object.class);
					Map<String, Map<String, Object>> tex = (Map<String, Map<String, Object>>) p.get("textures");
					for (Entry<String, Map<String, Object>> entry : tex.entrySet()) {
						TextureType tt = TextureType.valueOf(entry.getKey());
						String url = (String) entry.getValue().get("url");
						Object meta = entry.getValue().get("metadata");
						if(tt == TextureType.SKIN && meta != null) {
							skinType = ((Map<String, String>)meta).get("model");
						}
						if (skinType == null) {
							skinType = "default";
						}
						if(url.startsWith("http://textures.minecraft.net/texture/")) {
							String link = url.substring("http://textures.minecraft.net/texture/".length());
							if(tt == TextureType.SKIN)link += "&d=" + skinType;
							texLoad.add(fetch("texture", link).
									then(img -> {
										textures.put(tt, "data:image/png;base64," + img.get("texture"));
										if(img.containsKey("dec")) {
											SkinDataInputStream.decodedURL.put("data:image/png;base64," + img.get("texture"), (String) img.get("dec"));
										}
										return null;
									}));
						}
					}
				}
			}
			PlayerInfo info = new PlayerInfo(skinType, textures);
			PlayerProfile.infos.put(profile, info);
		}
		if(DomGlobal.window.parent != null)
			DomGlobal.window.parent.postMessage(MinecraftObjectHolder.gson.toJson(msg), "*");
		Promise<?>[] prs = texLoad.toArray(new Promise[0]);
		return Promise.all(prs);
	}

	private static UUID fromString(final String input) {
		return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	@Override
	public void initFrame(int width, int height) {
		ModelDisplayPanel modelPanel = new ModelDisplayPanel(this, this) {
			@Override
			public void draw(MouseEvent event, float partialTicks) {
				if(player != null) {
					ModelDefinition d = player.getModelDefinition0();
					if(d != null) {
						switch (d.getResolveState()) {
						case CLEANED_UP:
							break;
						case ERRORRED:
							if(d.getError() instanceof AsyncResourceException)setLoadingText(gui.i18nFormat("label.cpm.loading") + " (" + (5 - initCalled) + "/5)");
							else setLoadingText(gui.i18nFormat("label.cpm.errorLoadingModel", d.getError().toString()));
							break;
						case LOADED:
							break;
						case NEW:
						case RESOLVING:
							setLoadingText(gui.i18nFormat("label.cpm.loading") + " (" + (5 - initCalled) + "/5)");
							break;
						case SAFETY_BLOCKED:
							setLoadingText(gui.i18nFormat("label.cpm.safetyBlocked"));
							break;
						default:
							break;
						}
					}
				}
				super.draw(event, partialTicks);
			}

			@Override
			protected boolean isRotate(int btn) {
				if(btn == 0)RenderSystem.passEvent();
				return super.isRotate(btn);
			}

			@Override
			protected boolean isDrag(int btn) {
				return btn == 1 || gui.isShiftDown();
			}
		};
		modelPanel.setLoadingText(gui.i18nFormat("label.cpm.loading") + " (" + (5 - initCalled) + "/5)");
		modelPanel.setBackgroundColor(gui.getColors().panel_background);
		modelPanel.setBounds(new Box(0, 0, width, height));
		addElement(modelPanel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelDefinition getSelectedDefinition() {
		if(player == null)return null;
		if (initCalled > 0 && !initRunning) {
			ModelDefinition def = player.getModelDefinition0();
			if(def != null && def.getResolveState() == ModelLoadingState.ERRORRED) {
				initCalled--;
				initRunning = true;
				DomGlobal.console.log("Model loading finished waiting for bg resources");
				Promise.all(bgLoading.stream().toArray(Promise[]::new)).then(v -> {
					bgLoading.clear();
					DomGlobal.console.log("Bg resources loaded, reloading models");
					MinecraftClientAccess.get().getDefinitionLoader().clearCache();
					initModel(null);
					initRunning = false;
					return null;
				});
			}
		}
		ModelDefinition d = player.getModelDefinition();
		if(d != null) {
			return d;
		}
		d = player.getModelDefinition0();
		if(d != null) {
			ModelLoadingState st = d.getResolveState();
			if(st == ModelLoadingState.NEW || st == ModelLoadingState.RESOLVING || st == ModelLoadingState.ERRORRED ||
					d.getError() instanceof AsyncResourceException)
				return null;
		}
		if(def != null)return def.getNow(null);
		return null;
	}

	public static void addBgLoad(CompletableFuture<?> cf) {
		bgLoading.add(new Promise<>((res, rej) -> {
			cf.handle((v, e) -> {
				if(e != null)rej.onInvoke(e);
				else res.onInvoke(v);
				return null;
			});
		}));
	}

	@Override
	public ViewportCamera getCamera() {
		return cam;
	}

	@Override
	public void preRender() {
		if(getSelectedDefinition() != null) {
			MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().handleGuiAnimation(animHandler, getSelectedDefinition());
		}
	}

	@Override
	public boolean doRender() {
		return true;
	}
}
