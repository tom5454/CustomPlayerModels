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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.UI;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.tag.TagType;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.ExportStringResultPopup;
import com.tom.cpm.shared.editor.gui.popup.OverflowPopup;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.util.ExportHelper;
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
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimatedTexture;
import com.tom.cpm.shared.parts.ModelPartAnimation;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDupRoot;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartPlayerPos;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartRoot;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.parts.ModelPartTags;
import com.tom.cpm.shared.parts.ModelPartTemplate;
import com.tom.cpm.shared.parts.ModelPartTexture;
import com.tom.cpm.shared.parts.ModelPartUUIDLockout;
import com.tom.cpm.shared.parts.PartCollection;
import com.tom.cpm.shared.paste.PastePopup;
import com.tom.cpm.shared.util.Log;

public class Exporter {

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
			PartCollection def = prepareExport(e);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			{
				baos.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(baos);
				def.writePackage(new IOHelper(cos));
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
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), ex.toString(gui));
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

	private static PartCollection prepareExport(Editor e) throws IOException {
		if (ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false)) {
			return ExporterImpl.prepareExport(e);
		} else {
			return prepareDefinition(e);
		}
	}

	@Deprecated
	private static ModelPartDefinition prepareDefinition(Editor e) throws IOException {
		List<Cube> flatList = new ArrayList<>();
		ExportHelper.flattenElements(e.elements, new int[] {10}, flatList);
		ModelPartDefinition def = new ModelPartDefinition(flatList);
		ModelPartPlayer player = new ModelPartPlayer(e);
		List<IModelPart> otherParts = new ArrayList<>();
		otherParts.add(player);
		if (e.textures.get(TextureSheetType.SKIN).isEdited()) {
			otherParts.add(new ModelPartSkin(e));
		}
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p && !el.duplicated) {
					if(!el.pos.epsilon(0.1f) || !el.rotation.epsilon(0.1f)) {
						otherParts.add(new ModelPartPlayerPos(p.getId(el.rc), el.pos, el.rotation));
					}
				}
			}
		}
		List<IModelPart> otherParts2 = new ArrayList<>();
		ExportHelper.walkElements(e.elements, el -> {
			if(el.type == ElementType.NORMAL) {
				if(el.glow) {
					otherParts.add(new ModelPartRenderEffect(new EffectGlow(el.id)));
				}
				if(Math.abs(el.mcScale) > 0.0001f || Math.abs(el.meshScale.x - 1) > 0.01f || Math.abs(el.meshScale.y - 1) > 0.01f || Math.abs(el.meshScale.z - 1) > 0.01f) {
					otherParts.add(new ModelPartRenderEffect(new EffectScale(el.id, el.meshScale, el.mcScale)));
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
		for (TagType t : TagType.VALUES) {
			if (e.tags.getByType(t).hasTags()) {
				otherParts.add(new ModelPartTags(t, e.tags.getByType(t)));
			}
		}

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
			PartCollection def = prepareExport(e);
			if(forceOut) {
				writeOut(e, gui, def, result);
				return true;
			}
			try(OutputStream out = result.get()) {
				out.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(out);
				try(IOHelper dout = new IOHelper(cos)) {
					dout.writeObjectBlock(new ModelPartSkinType(e.skinType));
					def.writeBlocks(dout);
					dout.writeObjectBlock(ModelPartEnd.END);
				}
			} catch (EOFException unusedException) {
				writeOut(e, gui, def, result);
				return true;
			} catch (IOException e1) {
				throw new ExportException(new FormatText("error.cpm.unknownError"), e1);
			}
			result.close();
			return true;
		} catch (ExportException ex) {
			Log.error("Export exception", ex);
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), ex.toString(gui));
			return false;
		} catch (Exception ex) {
			gui.onGuiException("Error while exporting", ex, false);
			gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("tooltip.cpm.errorTooltip", gui.i18nFormat("error.cpm.unknownError")));
			return false;
		}
	}

	private static void writeOut(Editor e, UI gui, PartCollection def, Result result) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		{
			baos.write(ModelDefinitionLoader.HEADER);
			ChecksumOutputStream cos = new ChecksumOutputStream(baos);
			def.writePackage(new IOHelper(cos));
			cos.close();
		}
		result.overflowWriter.accept(baos.toByteArray(), link -> {
			try {
				IModelPart defLink = def.toLink(link);
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
				gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), ex.toString(gui));
			} catch (Exception ex) {
				gui.onGuiException("Error while exporting", ex, false);
			}
		});
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
				gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), ex.toString(gui));
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
