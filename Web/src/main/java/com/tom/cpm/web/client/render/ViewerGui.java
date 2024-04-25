package com.tom.cpm.web.client.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.Pair;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.io.ChecksumOutputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.io.SkinDataInputStream;
import com.tom.cpm.shared.io.SkinDataOutputStream;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.MojangAPI;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.PlayerProfile;
import com.tom.cpm.web.client.PlayerProfile.PlayerInfo;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.util.AsyncResourceException;
import com.tom.cpm.web.client.util.CPMApi;
import com.tom.cpm.web.client.util.GameProfile;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.UGWTContext;

import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class ViewerGui extends Frame implements IModelDisplayPanel {
	public static final List<Promise<?>> bgLoading = new ArrayList<>();
	private String name;
	private GameProfile profile;
	private ViewportCamera cam;
	private AnimationHandler animHandler;
	private CompletableFuture<ModelDefinition> def;
	private Player<?> player;
	private TextureProvider vanillaSkin;
	private Map<String, Object> state = new HashMap<>();
	private ModelFile fileData;

	public ViewerGui(IGui gui) {
		super(gui);
		UGWTContext.setContext(DomGlobal.window);
		name = Java.getQueryVariable("name");
		animHandler = new AnimationHandler(this::getSelectedDefinition, AnimationMode.GUI);
		if (name == null)
			throw new IllegalArgumentException("Missing query parameter: name");
		if(name.startsWith("https:")) {
			String url = name;
			name = "Web";
			profile = new GameProfile(UUID.randomUUID(), "Web");
			if(url.startsWith("https://cdn.discordapp.com/attachments/"))
				CPMApi.fetch("file", url).then(f -> {
					try {
						fileData = ModelFile.load(new ByteArrayInputStream(Base64.getDecoder().decode((String) f.get("data"))));
						initModel(null);
					} catch (IOException e1) {
						errorLoading(e1);
						e1.printStackTrace();
					}
					return null;
				}).catch_(e -> {
					errorLoading(e);
					return null;
				});
			else
				errorLoading(gui.i18nFormat("web-label.viewer.invaidURL"));
		} else {
			CPMApi.fetch("search", name).then(this::loadProfile).then(this::initModel).catch_(e -> {
				errorLoading(e);
				return null;
			});
		}
		cam = new ViewportCamera() {

			@Override
			public void reset() {
				camDist = getQuerryValue("dist", 64);
				position = getQuerryValue("pos", new Vec3f(0.5f, 1, 0.5f));
				look = getQuerryValue("look", new Vec3f(0.25f, 0.5f, 0.25f));
			}
		};
		cam.reset();
		DomGlobal.window.addEventListener("message", ev -> {
			MessageEvent<String> e = Js.uncheckedCast(ev);
			receiveData((Map<String, Object>) MinecraftObjectHolder.gson.fromJson(e.data, Object.class));
		});
	}

	private void errorLoading(Object e) {
		def = CompletableFuture.completedFuture(new ModelDefinition(new IOException("Failed to load model: " + e), player));
		if(e instanceof Throwable)
			Log.error("Failed to load model: ", (Throwable) e);
		else
			DomGlobal.console.error("Failed to load model: ", e);
		state.clear();
		state.put("error", gui.i18nFormat("label.cpm.errorLoadingModel", e.toString()));
		updateState();
	}

	private final String CLONE_POPUP_HTML = "<div>"
			+ "<h1>" + gui.i18nFormat("web-label.viewer.selectVanilla") + "</h1>"
			+ "<label for=\"cpmv_upload\"><button onclick=\"document.getElementById('cpmv_upload').click()\">" + gui.i18nFormat("web-button.viewer.add") + "</button></label> "
			+ "<input type=\"file\" id=\"cpmv_upload\" name=\"cpmv_upload\" style=\"display: none;\" accept=\"image/png\" onchange=\"uploadChange()\">"
			+ "<input id=\"nameSearch\" placeholder=\"" + gui.i18nFormat("web-label.viewer.searchGhost") + "\"> "
			+ "<button onclick=\"E('clone:skinSearch', document.getElementById('nameSearch').value)\">" + gui.i18nFormat("web-button.viewer.search") + "</button>"
			+ "</div>";

	private final String SKIN_TYPE_POPUP = "<h1>" + gui.i18nFormat("web-label.viewer.skinType") + "</h1>"
			+ Arrays.stream(SkinType.VANILLA_TYPES).map(this::makeSkinTypeButton).collect(Collectors.joining())
			+ "<p id=\"cpmv_skinImageData\" style=\"display: none;\">$</p>";

	//Javac stack overflows if this is inlined
	private String makeSkinTypeButton(SkinType t) {
		return "<button onclick=\"setSkinType('" + t.getName() + "')\">" + gui.i18nFormat("label.cpm.skin_type." + t.getName()) + "</button> ";
	}

	private final String SKIN_OUTPUT_POPUP = "<h1>" + gui.i18nFormat("web-label.viewer.useSkin") + "</h1>"
			+ "<button onclick=\"useSkinDl()\">" + gui.i18nFormat("web-button.viewer.download") + "</button> "
			+ "<button onclick=\"useSkinMc()\">" + gui.i18nFormat("web-button.viewer.uploadToMc") + "</button>"
			+ "<p id=\"cpmv_skinImageData\" style=\"display: none;\">$</p>";

	private void receiveData(Map<String, Object> data) {
		switch ((String) data.get("id")) {
		case "clone:model":
		{
			cloneModel(fout -> {
				DomGlobal.fetch("data:application/octet-binary;base64," + fout.toB64()).then(Response::blob).then(b -> {
					LocalStorageFS.saveAs(b, "cloned_model.cpmmodel");
					return null;
				});
			}, this::skinErrPopup, true);
		}
		break;

		case "clone:skin":
			openPopup(CLONE_POPUP_HTML);
			break;

		case "clone:skinSearch":
			writeToSkin(CPMApi.fetch("search", (String) data.get("value")).then(mapIn -> {
				String id = (String) mapIn.get("id");
				String name = (String) mapIn.get("name");
				state.put("name", name);
				state.put("id", id);
				profile = new GameProfile(fromString(id), name);
				Map<TextureType, String> textures = new HashMap<>();
				String skinType = "default";
				List<Map<String, Object>> pr = (List<Map<String, Object>>) mapIn.get("properties");
				for (Map<String, Object> map : pr) {
					if("textures".equals(map.get("name"))) {
						String val = new String(Base64.getDecoder().decode((String) map.get("value")));
						Map<String, Object> p = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(val, Object.class);
						Map<String, Map<String, Object>> tex = (Map<String, Map<String, Object>>) p.get("textures");
						for (Entry<String, Map<String, Object>> entry : tex.entrySet()) {
							TextureType tt = TextureType.valueOf(entry.getKey());
							String url = (String) entry.getValue().get("url");
							Object meta = entry.getValue().get("metadata");
							if(tt == TextureType.SKIN) {
								if(meta != null)
									skinType = ((Map<String, String>)meta).get("model");
								if (skinType == null) {
									skinType = "default";
								}
								if(url.startsWith("http://textures.minecraft.net/texture/")) {
									String link = url.substring("http://textures.minecraft.net/texture/".length());
									final String st = skinType;
									return CPMApi.fetch("texture", link).
											then(img -> Promise.resolve(Pair.of((String) img.get("texture"), st)));
								}
							}
						}
					}
				}
				return Promise.reject("Not found");
			}));
			break;

		case "clone:skinOpen":
		{
			String v = (String) data.get("value");
			v = v.substring(v.indexOf(',') + 1);
			openPopup(SKIN_TYPE_POPUP.replace("$", v));
		}
		break;

		case "clone:skinType":
		{
			Map<String, Object> d = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson((String) data.get("value"), Object.class);
			writeToSkin(Promise.resolve(Pair.of((String) d.get("skin"), (String) d.get("type"))));
		}
		break;

		case "clone:skinUse":
		{
			System.out.println(data);
			Map<String, Object> d = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson((String) data.get("value"), Object.class);
			String skin = (String) d.get("skin");
			switch ((String) d.get("type")) {
			case "mc":
			{
				MojangAPI.upload(Base64.getDecoder().decode(skin), url -> {
					Map<String, Object> r = new HashMap<>();
					r.put("link", url);
					updateCallback(data, r);
				}).catch_(this::skinErrPopup);
			}
			break;

			case "dl":
			{
				Map<String, Object> r = new HashMap<>();
				DomGlobal.fetch("data:application/octet-binary;base64," + skin).then(Response::blob).then(b -> {
					LocalStorageFS.saveAs(b, "cloned_model.png");
					return null;
				});
				updateCallback(data, r);
			}
			break;

			default:
				break;
			}
		}
		break;

		default:
			System.out.println(data);
			break;
		}
	}

	private Promise<Void> skinErrPopup(Object err) {
		String msg;
		if(err instanceof Throwable) {
			((Throwable)err).printStackTrace();
			msg = err.toString();
		} else {
			msg = String.valueOf(err);
		}
		openPopup("<h1>" + gui.i18nFormat("web-label.viewer.errCloning") + "</h1><p>" + msg + "</p>");
		return Promise.resolve((Void) null);
	}

	@SuppressWarnings("resource")
	private void writeToSkin(Promise<Pair<String, String>> skinF) {
		skinF.then(p -> new Promise<>((res, rej) -> {
			Image img;
			try {
				img = ImageIO.load(new IOHelper(p.getKey()).getDin());
			} catch (IOException e) {
				rej.onInvoke(e);
				return;
			}
			String skinType = p.getValue();
			cloneModel(h -> {
				try (SkinDataOutputStream out = new SkinDataOutputStream(img, MinecraftClientAccess.get().getDefinitionLoader().getTemplate(), SkinType.get(skinType).getChannel())) {
					out.write(h.toBytes());
				}
				IOHelper o = new IOHelper();
				ImageIO.save(img, o.getDout());
				openPopup(SKIN_OUTPUT_POPUP.replace("$", o.toB64()));
				res.onInvoke("");
			}, rej::onInvoke, false);
		})).catch_(this::skinErrPopup);
	}

	private void cloneModel(ThrowingConsumer<IOHelper, IOException> writer, Consumer<IOException> ex, boolean file) {
		ModelDefinition d = player.getModelDefinition();
		if(d != null && d.cloneable != null) {
			String desc = d.cloneable.desc;
			Image icon = d.cloneable.icon;
			if(fileData != null) {
				if(!fileData.convertable() && !file) {
					ex.accept(new IOException("Model not convertable to skin"));
					return;
				}
				try {
					byte[] fd = fileData.getDataBlock();
					if(file) {
						IOHelper fout = new IOHelper();
						fout.write(ModelDefinitionLoader.HEADER);
						ChecksumOutputStream cos = new ChecksumOutputStream(fout.getDout());
						IOHelper h = new IOHelper(cos);
						h.writeUTF("Cloned model");
						h.writeUTF(desc != null ? desc : "");
						h.writeVarInt(fd.length);
						h.write(fd);
						h.writeVarInt(0);
						if(icon != null) {
							h.writeImage(icon);
						} else {
							h.writeVarInt(0);
						}
						cos.close();
						writer.accept(fout);
					} else
						writer.accept(new IOHelper(fd));
				} catch (IOException e) {
					ex.accept(e);
				}
			} else {
				player.getTextures().load().thenCompose(v -> player.getTextures().getTexture(TextureType.SKIN)).thenAccept(skin -> {
					try(SkinDataInputStream in = new SkinDataInputStream(skin, MinecraftClientAccess.get().getDefinitionLoader().getTemplate(), player.getSkinType().getChannel())) {
						IOHelper ioh = new IOHelper();
						IOHelper.copy(in, ioh.getDout());
						if(ioh.flip().read() != ModelDefinitionLoader.HEADER)throw new IOException();
						if(file) {
							byte[] dt = ioh.toBytes();
							IOHelper fout = new IOHelper();
							fout.write(ModelDefinitionLoader.HEADER);
							ChecksumOutputStream cos = new ChecksumOutputStream(fout.getDout());
							IOHelper h = new IOHelper(cos);
							h.writeUTF(name + "'s model");
							h.writeUTF(desc != null ? desc : "");
							h.writeVarInt(dt.length);
							h.write(dt);
							h.writeVarInt(0);
							if(icon != null) {
								h.writeImage(icon);
							} else {
								h.writeVarInt(0);
							}
							cos.close();
							writer.accept(fout);
						} else
							writer.accept(ioh);
					} catch (IOException e) {
						ex.accept(e);
					}
				});
			}
		}
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

	private int initCalled = 2;
	private boolean initRunning, controlsSent;

	@SuppressWarnings("unchecked")
	private Promise<Object> initModel(Object[] unused) {
		if(fileData != null)MinecraftClientAccess.get().getDefinitionLoader().setModel(profile, fileData.getDataBlock(), false);
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

	private void updateState() {
		if(DomGlobal.window.parent != null)
			DomGlobal.window.parent.postMessage(MinecraftObjectHolder.gson.toJson(state), "*");
	}

	private void updateCallback(Map<String, Object> event, Map<String, Object> value) {
		Map<String, Object> d = new HashMap<>();
		d.put("callback", event.get("cbid"));
		d.put("value", value);
		if(DomGlobal.window.parent != null)
			DomGlobal.window.parent.postMessage(MinecraftObjectHolder.gson.toJson(d), "*");
	}

	private void openPopup(String html) {
		Map<String, Object> d = new HashMap<>();
		d.put("popup", html);
		if(DomGlobal.window.parent != null)
			DomGlobal.window.parent.postMessage(MinecraftObjectHolder.gson.toJson(d), "*");
	}

	@SuppressWarnings("unchecked")
	private Promise<Object[]> loadProfile(Map<String, Object> data) {
		state.clear();
		List<Promise<?>> texLoad = new ArrayList<>();
		if(data.containsKey("error")) {
			String err = (String) data.get("error");
			state.put("error", err);
			texLoad.add(Promise.reject(err));
		} else {
			String id = (String) data.get("id");
			String name = (String) data.get("name");
			state.put("name", name);
			state.put("id", id);
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
							texLoad.add(CPMApi.fetch("texture", link).
									then(img -> {
										textures.put(tt, "data:image/png;base64," + img.get("texture"));
										return null;
									}));
						}
					}
				}
			}
			PlayerInfo info = new PlayerInfo(skinType, textures);
			PlayerProfile.infos.put(profile, info);
		}
		updateState();
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
							if(d.getError() instanceof AsyncResourceException)setLoadingText(gui.i18nFormat("label.cpm.loading") + getProgress());
							else setLoadingText(gui.i18nFormat("label.cpm.errorLoadingModel", d.getError().toString()));
							break;
						case LOADED:
							break;
						case NEW:
						case RESOLVING:
							setLoadingText(gui.i18nFormat("label.cpm.loading") + getProgress());
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
		modelPanel.setLoadingText(gui.i18nFormat("label.cpm.loading") + getProgress());
		modelPanel.setBackgroundColor(gui.getColors().panel_background);
		modelPanel.setBounds(new Box(0, 0, width, height));
		addElement(modelPanel);
	}

	private String getProgress() {
		return " (" + (2 - initCalled) + "/2)";
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
			} else if (def != null && def.getResolveState() == ModelLoadingState.LOADED && !controlsSent) {
				sendControls(def);
				updateState();
				controlsSent = true;
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

	private void sendControls(ModelDefinition def) {
		StringBuilder sb = new StringBuilder();
		boolean skin = fileData == null || fileData.convertable();
		if(def.cloneable != null)makeDropdown(sb, "clone", gui.i18nFormat("web-label.viewer.clone"), skin ? "skin" : null, gui.i18nFormat("web-label.viewer.mode.skin"), "model", gui.i18nFormat("web-label.viewer.mode.model"));
		state.put("ctrl", sb.toString());
	}

	private static void makeDropdown(StringBuilder sb, String id, String name, String... args) {
		sb.append("<div class=\"cpmv_dropdown\"><button onclick=\"dropDown('");
		sb.append(id);
		sb.append("')\" class=\"cpmv_dropbtn\">");
		sb.append(name);
		sb.append("</button><div id=\"");
		sb.append(id);
		sb.append("\" class=\"dropdown-content\">");
		for (int i = 0; i < args.length; i+=2) {
			String oid = args[i];
			String oname = args[i + 1];
			if(oid == null) {
				sb.append("<p>");
				sb.append(oname);
				sb.append("</p>");
			} else {
				sb.append("<a href=\"javascript:void(0)\" onclick=\"E('");
				sb.append(id);
				sb.append(':');
				sb.append(oid);
				sb.append("')\">");
				sb.append(oname);
				sb.append("</a>");
			}
		}
		sb.append("</div></div>");
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
