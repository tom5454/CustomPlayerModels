package com.tom.cpm.shared.editor;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.CreateGistPopup;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.effects.EffectColor;
import com.tom.cpm.shared.effects.EffectGlow;
import com.tom.cpm.shared.effects.EffectHide;
import com.tom.cpm.shared.effects.EffectScale;
import com.tom.cpm.shared.io.ChecksumOutputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.SkinDataOutputStream;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimation;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartDupRoot;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartListIcon;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartPlayerPos;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartScale;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.parts.ModelPartTemplate;
import com.tom.cpm.shared.parts.ModelPartUUIDLockout;
import com.tom.cpm.shared.template.Template;

public class Exporter {
	public static final String TEMP_MODEL = ".temp.cpmmodel";
	public static final Gson sgson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

	public static void exportSkin(Editor e, EditorGui gui, File f, boolean forceOut) {
		if(e.vanillaSkin == null) {
			gui.openPopup(new MessagePopup(gui.getGui(), "Unknown Error", "Couldn't load vanilla skin"));
			return;
		}
		if(e.vanillaSkin.getWidth() != 64 || e.vanillaSkin.getHeight() != 64) {
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("error.cpm.vanillaSkinSize")));
			return;
		}
		Image img = new Image(e.vanillaSkin);
		exportSkin0(e, gui, new Result(() -> new SkinDataOutputStream(img, MinecraftClientAccess.get().getPlayerRenderManager().getLoader().getTemplate(), e.skinType.getChannel()), () -> {
			img.storeTo(f);
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.export_success"), gui.getGui().i18nFormat("label.cpm.export_success.desc", f.getName())));
		}, (d, c) -> handleGistOverflow(d, c, gui)), forceOut);
	}

	public static void exportB64(Editor e, EditorGui gui, Consumer<String> b64Out, boolean forceOut) {
		byte[] buffer = new byte[200];
		int[] size = new int[] {0};
		exportSkin0(e, gui, new Result(() -> {
			size[0] = 0;
			return new BAOS(buffer, size);
		}, () -> b64Out.accept(Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, size[0]))),
				(d, c) -> handleGistOverflow(d, c, gui)), forceOut);
	}

	public static void exportGistUpdate(Editor e, EditorGui gui, Consumer<String> b64Out) {
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
			System.out.println(b64);
			b64Out.accept(b64);
		} catch (ExportException ex) {
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void exportModel(Editor e, EditorGui gui, File f, ModelDescription desc) {
		ModelWriter wr = new ModelWriter(gui, f);
		wr.setDesc(desc.name, desc.desc, desc.icon);
		exportSkin0(e, gui, new Result(wr::getOut, () -> {
			if(wr.finish())
				gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.export_success"), gui.getGui().i18nFormat("label.cpm.export_success.desc", f.getName())));
		}, (d, c) -> handleGistOverflow(d, l -> {
			wr.setOverflow(d, l);
			c.accept(l);
		}, gui)), false);
	}

	public static boolean exportTempModel(Editor e, EditorGui gui) {
		File models = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		models.mkdirs();
		ModelWriter wr = new ModelWriter(gui, new File(models, TEMP_MODEL));
		wr.setDesc("Test model", "", null);
		return exportSkin0(e, gui, new Result(wr::getOut, wr::finish,
				(d, c) -> {
					Link l = new Link("local:test");
					wr.setOverflow(d, l);
					c.accept(l);
				}), false);
	}

	private static ModelPartDefinition prepareDefinition(Editor e) throws IOException {
		List<Cube> flatList = new ArrayList<>();
		walkElements(e.elements, new int[] {10}, flatList);
		ModelPartDefinition def = new ModelPartDefinition(e.skinProvider.isEdited() ? new ModelPartSkin(e) : null, flatList);
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
			}
		});
		for (ModelElement el : e.elements) {
			if(el.type == ElementType.ROOT_PART && el.duplicated) {
				if(!el.show)otherParts.add(new ModelPartRenderEffect(new EffectHide(el.id)));
				otherParts.add(new ModelPartDupRoot(el.id, (PlayerModelParts) el.typeData));
			}
		}
		if(!e.animations.isEmpty()) {
			otherParts.add(new ModelPartAnimation(e));
		}
		if(e.listIconProvider != null) {
			otherParts.add(new ModelPartListIcon(e));
		}
		for (EditorTemplate et : e.templates) {
			otherParts.add(new ModelPartTemplate(et));
		}
		if(e.scaling != 0 && e.scaling != 1)otherParts.add(new ModelPartScale(e.scaling));
		if(e.description != null) {
			switch (e.description.copyProtection) {
			case CLONEABLE:
				otherParts.add(new ModelPartCloneable());
				break;
			case NORMAL:
				break;
			case UUID_LOCK:
				otherParts.add(new ModelPartUUIDLockout(MinecraftClientAccess.get().getClientPlayer().getUUID()));
				break;
			default:
				break;
			}
		}
		def.setOtherParts(otherParts);
		if(MinecraftObjectHolder.DEBUGGING)System.out.println(def);
		return def;
	}

	private static boolean exportSkin0(Editor e, EditorGui gui, Result result, boolean forceOut) {
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
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private static void writeOut(Editor e, EditorGui gui, ModelPartDefinition def, Result result) throws Exception {
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
				gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", ex.getMessage())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public static void exportTemplate(Editor e, EditorGui gui, ModelDescription desc, Consumer<String> templateOut) {
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
				map.put("type", a.type.baseType.name().toLowerCase());
				map.put("elem_type", a.type.name().toLowerCase());
				Map<String, Object> d = new HashMap<>();
				a.handler.export().export(d);
				map.put("data", d);
				a.handler.applyArgs(data, a.effectedElems);
			}
			if(e.skinProvider.isEdited()) {
				IOHelper h = new IOHelper();
				e.skinProvider.write(h);
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
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
		} catch (Exception ex) {
			ex.printStackTrace();
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
				if(me.duplicated) {
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

	public static boolean check(Editor editor, EditorGui editorGui, Runnable next) {
		if(!editor.animations.isEmpty() && editor.animEnc == null && editor.animations.stream().anyMatch(EditorAnim::isCustom)) {
			editorGui.openPopup(new AnimEncConfigPopup(editorGui.getGui(), editor, next));
			return false;
		}
		return true;
	}

	private static void handleGistOverflow(byte[] data, Consumer<Link> linkC, EditorGui gui) {
		String b64 = Base64.getEncoder().encodeToString(data);
		System.out.println(b64);
		gui.openPopup(new CreateGistPopup(gui, gui.getGui(), "skinOverflow", b64, linkC));
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
		private final EditorGui gui;
		private byte[] buffer = new byte[2*1024];
		private int[] size = new int[] {0};
		private byte[] overflow;
		private File out;
		private String name, desc;
		private Link l;
		private Image icon;

		public ModelWriter(EditorGui gui, File out) {
			this.gui = gui;
			this.out = out;
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
				gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return false;
		}
	}
}
