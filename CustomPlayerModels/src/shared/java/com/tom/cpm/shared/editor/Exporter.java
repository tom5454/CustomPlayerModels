package com.tom.cpm.shared.editor;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.cpl.gui.UI;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.ExportStringResultPopup;
import com.tom.cpm.shared.editor.gui.popup.OverflowPopup;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.effects.EffectColor;
import com.tom.cpm.shared.effects.EffectCopyTransform;
import com.tom.cpm.shared.effects.EffectDisableVanilla;
import com.tom.cpm.shared.effects.EffectExtrude;
import com.tom.cpm.shared.effects.EffectFirstPersonHandPos;
import com.tom.cpm.shared.effects.EffectGlow;
import com.tom.cpm.shared.effects.EffectHide;
import com.tom.cpm.shared.effects.EffectHideSkull;
import com.tom.cpm.shared.effects.EffectInvisGlow;
import com.tom.cpm.shared.effects.EffectModelScale;
import com.tom.cpm.shared.effects.EffectPerFaceUV;
import com.tom.cpm.shared.effects.EffectRemoveArmorOffset;
import com.tom.cpm.shared.effects.EffectRemoveBedOffset;
import com.tom.cpm.shared.effects.EffectRenderItem;
import com.tom.cpm.shared.effects.EffectScale;
import com.tom.cpm.shared.effects.EffectScaling;
import com.tom.cpm.shared.effects.EffectSingleTexture;
import com.tom.cpm.shared.effects.EffectUV;
import com.tom.cpm.shared.io.ChecksumOutputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.io.SkinDataOutputStream;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimatedTexture;
import com.tom.cpm.shared.parts.ModelPartAnimation;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartDupRoot;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartPlayerPos;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartRoot;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.parts.ModelPartTemplate;
import com.tom.cpm.shared.parts.ModelPartTexture;
import com.tom.cpm.shared.parts.ModelPartUUIDLockout;
import com.tom.cpm.shared.paste.PastePopup;
import com.tom.cpm.shared.template.Template;
import com.tom.cpm.shared.util.Log;

public class Exporter {
	public static final Gson sgson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

	public static void exportSkin(Editor e, UI gui, File f, boolean forceOut) {
		exportSkin(e, gui, img -> {
			img.storeTo(f);
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.export_success"), gui.i18nFormat("label.cpm.export_success.desc", f.getName()));
		}, forceOut);
	}

	public static void exportSkin(Editor e, UI gui, ThrowingConsumer<Image, IOException> out, boolean forceOut) {
		if(e.vanillaSkin == null) {
			gui.displayMessagePopup("Unknown Error", "Couldn't load vanilla skin");
			return;
		}
		if(e.vanillaSkin.getWidth() != 64 || e.vanillaSkin.getHeight() != 64) {
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.vanillaSkinSize"));
			return;
		}
		Image img = new Image(e.vanillaSkin);
		exportSkin0(e, gui,
				new Result(() -> new SkinDataOutputStream(img, MinecraftClientAccess.get().getDefinitionLoader().getTemplate(), e.skinType.getChannel()),
						() -> out.accept(img), (d, c) -> handleOverflow(d, c, "skin", gui, e)), forceOut);
	}

	public static void exportB64(Editor e, UI gui, Consumer<String> b64Out, boolean forceOut) {
		byte[] buffer = new byte[200];
		int[] size = new int[] {0};
		exportSkin0(e, gui, new Result(() -> {
			size[0] = 0;
			return new BAOS(buffer, size);
		}, () -> b64Out.accept(Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, size[0]))),
				(d, c) -> handleOverflow(d, c, "b64", gui, e)), forceOut);
	}

	public static void exportUpdate(Editor e, UI gui, Link linkToUpdate) {
		try {
			ModelPartDefinition def = prepareDefinition(e);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			{
				baos.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(baos);
				def.write(new IOHelper(cos));
				cos.close();
			}
			String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
			Log.info(b64);
			if(linkToUpdate.getLoader().equals("p")) {
				PastePopup.runRequest(gui, c -> c.updateFile(linkToUpdate.getPath(), b64.getBytes(StandardCharsets.UTF_8)),
						() -> {
							gui.displayPopup(frame -> {
								PopupPanel popup = new MessagePopup(frame, gui.i18nFormat("label.cpm.export_success"), gui.i18nFormat("label.cpm.paste.updatedModel"));
								popup.setOnClosed(MinecraftClientAccess.get().getDefinitionLoader()::clearCache);
								return popup;
							});
						}, () -> {}, "uploading");
			} else {
				gui.displayPopup(frame -> new ExportStringResultPopup(frame, "skin_update", b64));
			}
		} catch (ExportException ex) {
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.export_error", gui.i18nFormat(ex.getMessage())));
		} catch (Exception ex) {
			gui.onGuiException("Error while exporting", ex, false);
		}
	}

	public static void exportModel(Editor e, UI gui, File f, ModelDescription desc, boolean skinCompat) {
		ModelWriter wr = new ModelWriter(gui, f, skinCompat);
		wr.setDesc(desc.name, desc.desc, desc.icon);
		exportSkin0(e, gui, new Result(wr::getOut, () -> {
			if(wr.finish())
				gui.displayMessagePopup(gui.i18nFormat("label.cpm.export_success"), gui.i18nFormat("label.cpm.export_success.desc", f.getName()));
		}, (d, c) -> handleOverflow(d, l -> {
			wr.setOverflow(d, l);
			c.accept(l);
		}, "model", gui, e)), false);
	}

	public static boolean exportTempModel(Editor e, UI gui) {
		File models = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		models.mkdirs();
		ModelWriter wr = new ModelWriter(gui, new File(models, TestIngameManager.TEST_MODEL_NAME), false);
		wr.setDesc("Test model", "", null);
		return exportSkin0(e, gui, new Result(wr::getOut, wr::finish,
				(d, c) -> {
					Link l = new Link("local:test" + System.nanoTime());
					wr.setOverflow(d, l);
					c.accept(l);
				}), false);
	}

	private static ModelPartDefinition prepareDefinition(Editor e) throws IOException {
		List<Cube> flatList = new ArrayList<>();
		walkElements(e.elements, new int[] {10}, flatList);
		ModelPartDefinition def = new ModelPartDefinition(e.textures.get(TextureSheetType.SKIN).isEdited() ? new ModelPartSkin(e) : null, flatList);
		ModelPartPlayer player = new ModelPartPlayer(e);
		def.setPlayer(player);
		List<IModelPart> otherParts = new ArrayList<>();
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p && !el.duplicated) {
					if(Math.abs(el.pos.x) >= 0.1f || Math.abs(el.pos.y) >= 0.1f || Math.abs(el.pos.z) >= 0.1f ||
							Math.abs(el.rotation.x) >= 0.1f || Math.abs(el.rotation.y) >= 0.1f || Math.abs(el.rotation.z) >= 0.1f) {
						otherParts.add(new ModelPartPlayerPos(p.getId(el.rc), el.pos, el.rotation));
					}
				}
			}
		}
		List<IModelPart> otherParts2 = new ArrayList<>();
		walkElements(e.elements, el -> {
			if(el.type == ElementType.NORMAL) {
				if(el.glow) {
					otherParts.add(new ModelPartRenderEffect(new EffectGlow(el.id)));
				}
				if(Math.abs(el.mcScale) > 0.0001f || Math.abs(el.scale.x - 1) > 0.01f || Math.abs(el.scale.y - 1) > 0.01f || Math.abs(el.scale.z - 1) > 0.01f) {
					otherParts.add(new ModelPartRenderEffect(new EffectScale(el.id, el.scale, el.mcScale)));
				}
				if(el.hidden) {
					otherParts.add(new ModelPartRenderEffect(new EffectHide(el.id)));
				}
				if(el.recolor) {
					otherParts.add(new ModelPartRenderEffect(new EffectColor(el.id, el.rgb)));
				}
				if(el.singleTex) {
					otherParts.add(new ModelPartRenderEffect(new EffectSingleTexture(el.id)));
				}
				if(el.extrude) {
					otherParts.add(new ModelPartRenderEffect(new EffectExtrude(el.id)));
				}
				if(el.faceUV != null) {
					otherParts.add(new ModelPartRenderEffect(new EffectPerFaceUV(el.id, el.faceUV)));
				} else if(el.u > 255 || el.v > 255) {
					otherParts.add(new ModelPartRenderEffect(new EffectUV(el.id, el.u, el.v)));
				}
				if(el.itemRenderer != null) {
					otherParts.add(new ModelPartRenderEffect(new EffectRenderItem(el.id, el.itemRenderer.slot, el.itemRenderer.slotID)));
				}
				if(el.copyTransform != null && el.copyTransform.from != null) {
					otherParts2.add(new ModelPartRenderEffect(new EffectCopyTransform(el.copyTransform.from.id, el.id, el.copyTransform.toShort())));
				}
			}
		});
		for (ModelElement el : e.elements) {
			if(el.type == ElementType.ROOT_PART) {
				if(el.duplicated && el.typeData instanceof PlayerModelParts) {
					if(el.hidden)otherParts.add(new ModelPartRenderEffect(new EffectHide(el.id)));
					otherParts.add(new ModelPartDupRoot(el.id, (PlayerModelParts) el.typeData));
				} else if(el.typeData instanceof RootModelType){
					if(el.hidden)otherParts.add(new ModelPartRenderEffect(new EffectHide(el.id)));
					otherParts.add(new ModelPartRoot(el.id, (RootModelType) el.typeData));
				}
				if(el.disableVanillaAnim) {
					otherParts.add(new ModelPartRenderEffect(new EffectDisableVanilla(el.id)));
				}
			}
		}
		otherParts.addAll(otherParts2);
		if(!e.animations.isEmpty()) {
			otherParts.add(new ModelPartAnimation(e, otherParts));
		}
		e.textures.forEach((type, tex) -> {
			if(type.editable) {
				if(type != TextureSheetType.SKIN)
					otherParts.add(new ModelPartTexture(e, type));
				if(!tex.animatedTexs.isEmpty())
					tex.animatedTexs.forEach(at -> otherParts.add(new ModelPartAnimatedTexture(type, at)));
			}
		});
		for (EditorTemplate et : e.templates) {
			otherParts.add(new ModelPartTemplate(et));
		}
		if(e.scalingElem.enabled) {
			otherParts.add(new ModelPartRenderEffect(new EffectScaling(e.scalingElem.scaling)));
			if(e.scalingElem.hasTransform())
				otherParts.add(new ModelPartRenderEffect(new EffectModelScale(e.scalingElem.pos, e.scalingElem.rotation, e.scalingElem.scale)));
		}
		if(!e.hideHeadIfSkull)otherParts.add(new ModelPartRenderEffect(new EffectHideSkull(e.hideHeadIfSkull)));
		if(e.removeArmorOffset)otherParts.add(new ModelPartRenderEffect(new EffectRemoveArmorOffset(e.removeArmorOffset)));
		if(e.removeBedOffset)otherParts.add(new ModelPartRenderEffect(new EffectRemoveBedOffset()));
		if(e.enableInvisGlow)otherParts.add(new ModelPartRenderEffect(new EffectInvisGlow()));
		if(e.leftHandPos.isChanged() || e.rightHandPos.isChanged())
			otherParts.add(new ModelPartRenderEffect(new EffectFirstPersonHandPos(e.leftHandPos, e.rightHandPos)));

		if(e.description != null) {
			switch (e.description.copyProtection) {
			case CLONEABLE:
				otherParts.add(new ModelPartCloneable(e.description.name, e.description.desc, e.description.icon));
				break;
			case NORMAL:
				break;
			case UUID_LOCK:
				otherParts.add(new ModelPartUUIDLockout(e.description.uuid != null ? e.description.uuid : MinecraftClientAccess.get().getClientPlayer().getUUID()));
				break;
			default:
				break;
			}
		}
		def.setOtherParts(otherParts);
		if(MinecraftObjectHolder.DEBUGGING)Log.info(def);
		return def;
	}

	private static boolean exportSkin0(Editor e, UI gui, Result result, boolean forceOut) {
		try {
			ModelPartDefinition def = prepareDefinition(e);
			if(forceOut) {
				writeOut(e, gui, def, result);
				return true;
			}
			try(OutputStream out = result.get()) {
				out.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(out);
				try(IOHelper dout = new IOHelper(cos)) {
					dout.writeObjectBlock(new ModelPartSkinType(e.skinType));
					dout.writeObjectBlock(def);
					dout.writeObjectBlock(ModelPartEnd.END);
				}
			} catch (EOFException unusedException) {
				writeOut(e, gui, def, result);
				return true;
			} catch (IOException e1) {
				throw new ExportException("error.cpm.unknownError", e1);
			}
			result.close();
			return true;
		} catch (ExportException ex) {
			Log.error("Export exception", ex);
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.export_error", gui.i18nFormat(ex.getMessage())));
			return false;
		} catch (Exception ex) {
			gui.onGuiException("Error while exporting", ex, false);
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("tooltip.cpm.errorTooltip", gui.i18nFormat("error.cpm.unknownError")));
			return false;
		}
	}

	private static void writeOut(Editor e, UI gui, ModelPartDefinition def, Result result) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		{
			baos.write(ModelDefinitionLoader.HEADER);
			ChecksumOutputStream cos = new ChecksumOutputStream(baos);
			def.write(new IOHelper(cos));
			cos.close();
		}
		result.overflowWriter.accept(baos.toByteArray(), link -> {
			try {
				ModelPartDefinitionLink defLink = new ModelPartDefinitionLink(link);
				try(OutputStream out = result.get()) {
					out.write(ModelDefinitionLoader.HEADER);
					ChecksumOutputStream cos = new ChecksumOutputStream(out);
					try(IOHelper dout = new IOHelper(cos)) {
						dout.writeObjectBlock(new ModelPartSkinType(e.skinType));
						dout.writeObjectBlock(defLink);
						dout.writeObjectBlock(ModelPartEnd.END);
					}
				}
				result.close();
			} catch (ExportException ex) {
				gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.export_error", ex.getMessage()));
			} catch (Exception ex) {
				gui.onGuiException("Error while exporting", ex, false);
			}
		});
	}

	public static void exportTemplate(Editor e, UI gui, ModelDescription desc, Consumer<String> templateOut) {
		try {
			List<Cube> flatList = new ArrayList<>();
			walkElements(e.elements, new int[] {Template.TEMPLATE_ID_OFFSET}, flatList);
			Map<String, Object> data = new HashMap<>();
			List<Map<String, Object>> cubesList = new ArrayList<>();
			data.put("cubes", cubesList);
			Map<Integer, Map<String, Object>> cubeDataList = new HashMap<>();
			flatList.sort((a, b) -> Integer.compare(a.id, b.id));
			for (Cube cube : flatList) {
				Map<String, Object> m = new HashMap<>();
				Cube.saveDefinitionCube(m, cube);
				cubesList.add(m);
				Map<String, Object> dtMap = new HashMap<>();
				m.put("data", dtMap);
				m.put("id", cube.id);
				cubeDataList.put(cube.id, dtMap);
			}
			walkElements(e.elements, el -> {
				if(el.type == ElementType.NORMAL && !el.templateElement) {
					Map<String, Object> dt = cubeDataList.get(el.id);
					dt.put("hidden", el.hidden);
					dt.put("recolor", el.recolor);
					dt.put("glow", el.glow);
				}
			});
			List<Map<String, Object>> argsList = new ArrayList<>();
			data.put("args", argsList);
			for(TemplateArgHandler a : e.templateSettings.templateArgs) {
				Map<String, Object> map = new HashMap<>();
				argsList.add(map);
				map.put("name", a.name);
				map.put("desc", a.desc);
				map.put("type", a.type.baseType.name().toLowerCase(Locale.ROOT));
				map.put("elem_type", a.type.name().toLowerCase(Locale.ROOT));
				Map<String, Object> d = new HashMap<>();
				a.handler.export().export(d);
				map.put("data", d);
				a.handler.applyArgs(data, a.effectedElems);
			}
			if(e.textures.get(TextureSheetType.SKIN).isEdited()) {
				IOHelper h = new IOHelper();
				e.textures.get(TextureSheetType.SKIN).write(h);
				data.put("texture", h.toB64());
			}
			data.put("name", desc.name);
			data.put("desc", desc.desc);
			if(desc.icon != null) {
				try(IOHelper icon = new IOHelper()) {
					icon.writeImage(desc.icon);
					data.put("icon", icon.toB64());
				}
			}
			String result = sgson.toJson(data);
			templateOut.accept(result);
		} catch (ExportException ex) {
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.export_error", gui.i18nFormat(ex.getMessage())));
		} catch (Exception ex) {
			gui.onGuiException("Error while exporting", ex, false);
		}
	}

	private static void walkElements(List<ModelElement> elems, int[] id, List<Cube> flatList) {
		for (ModelElement me : elems) {
			if(me.templateElement)continue;
			switch (me.type) {
			case NORMAL:
				me.id = id[0]++;
				flatList.add(me);
				break;
			case ROOT_PART:
				if(me.duplicated || me.typeData instanceof RootModelType) {
					Cube fake = Cube.newFakeCube();
					me.id = id[0]++;
					fake.id = me.id;
					fake.pos = me.pos;
					fake.rotation = me.rotation;
					flatList.add(fake);
				} else me.id = ((RootModelElement)me.rc).getPart().getId(me.rc);
				break;
			default:
				break;
			}
			if(me.parent != null)me.parentId = me.parent.id;
			walkElements(me.children, id, flatList);
		}
	}

	private static void walkElements(List<ModelElement> elems, Consumer<ModelElement> c) {
		for (ModelElement me : elems) {
			c.accept(me);
			walkElements(me.children, c);
		}
	}

	public static boolean check(Editor editor, UI gui, Runnable next) {
		if(!editor.animations.isEmpty() && editor.animEnc == null && editor.animations.stream().anyMatch(EditorAnim::isCustom)) {
			gui.displayPopup(f -> new AnimEncConfigPopup(f.getGui(), editor, next));
			return false;
		}
		return true;
	}

	private static void handleOverflow(byte[] data, Consumer<Link> linkC, String reason, UI gui, Editor e) {
		String b64 = Base64.getEncoder().encodeToString(data);
		Log.info(b64);
		gui.displayPopup(f -> new OverflowPopup(f, e, b64, reason, linkC));
	}

	public static class ExportException extends RuntimeException {
		private static final long serialVersionUID = 3255847899314886673L;

		public ExportException(String message, Throwable cause) {
			super(message, cause);
		}

		public ExportException(String message) {
			super(message);
		}
	}

	private static class Result implements Supplier<OutputStream>, Closeable {
		private Supplier<OutputStream> out;
		private Closeable finish;
		private BiConsumer<byte[], Consumer<Link>> overflowWriter;

		public Result(Supplier<OutputStream> out, Closeable finish, BiConsumer<byte[], Consumer<Link>> overflowWriter) {
			this.out = out;
			this.finish = finish;
			this.overflowWriter = overflowWriter;
		}

		@Override
		public OutputStream get() {
			return out.get();
		}

		@Override
		public void close() throws IOException {
			finish.close();
		}
	}

	private static class BAOS extends OutputStream {
		private byte[] buffer;
		private int[] size;
		public BAOS(byte[] buffer, int[] size) {
			this.buffer = buffer;
			this.size = size;
		}

		@Override
		public void write(int b) throws IOException {
			if(buffer.length > size[0]) {
				buffer[size[0]++] = (byte) b;
			} else {
				throw new EOFException();
			}
		}
	}

	private static class ModelWriter {
		private final UI gui;
		private byte[] buffer;
		private int[] size = new int[] {0};
		private byte[] overflow;
		private File out;
		private String name, desc;
		private Link l;
		private Image icon;

		public ModelWriter(UI gui, File out, boolean skinCompat) {
			this.gui = gui;
			this.out = out;
			buffer = new byte[skinCompat ? 2*1024 : 30*1024];
		}

		public void setDesc(String name, String desc, Image icon) {
			this.name = name;
			this.desc = desc;
			this.icon = icon;
		}

		public OutputStream getOut() {
			size[0] = 0;
			return new BAOS(buffer, size);
		}

		public void setOverflow(byte[] d, Link l) {
			this.overflow = d;
			this.l = l;
		}

		public boolean finish() {
			try (FileOutputStream fout = new FileOutputStream(out)){
				fout.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(fout);
				IOHelper h = new IOHelper(cos);
				h.writeUTF(name);
				h.writeUTF(desc);
				h.writeVarInt(size[0]);
				h.write(buffer, 0, size[0]);
				if(overflow != null) {
					h.writeByteArray(overflow);
					l.write(h);
				} else {
					h.writeVarInt(0);
				}
				if(icon != null) {
					h.writeImage(icon);
				} else {
					h.writeVarInt(0);
				}
				cos.close();
				return true;
			} catch (ExportException ex) {
				gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.export_error", gui.i18nFormat(ex.getMessage())));
			} catch (Exception ex) {
				gui.onGuiException("Error while exporting", ex, false);
			}
			return false;
		}
	}

	public static void convert(ModelFile file, Image imgIn, SkinType type, Consumer<Image> outCons, Runnable error) {
		Image img = new Image(imgIn);
		try(SkinDataOutputStream out = new SkinDataOutputStream(img, MinecraftClientAccess.get().getDefinitionLoader().getTemplate(), type.getChannel())) {
			out.write(file.getDataBlock());
		} catch (IOException e) {
			Log.error("Failed to convert model file to skin", e);
			error.run();
			return;
		}
		outCons.accept(img);
	}
}
