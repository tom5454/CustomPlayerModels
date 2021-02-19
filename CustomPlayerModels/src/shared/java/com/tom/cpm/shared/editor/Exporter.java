package com.tom.cpm.shared.editor;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.ExportOverflowPopup;
import com.tom.cpm.shared.effects.EffectColor;
import com.tom.cpm.shared.effects.EffectGlow;
import com.tom.cpm.shared.effects.EffectHide;
import com.tom.cpm.shared.effects.EffectScale;
import com.tom.cpm.shared.gui.elements.MessagePopup;
import com.tom.cpm.shared.io.ChecksumOutputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.SkinDataOutputStream;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimation;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartListIcon;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartPlayerPos;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.util.Image;

public class Exporter {

	public static void exportSkin(Editor e, EditorGui gui, File f, boolean forceOut) {
		if(e.vanillaSkin == null) {
			gui.openPopup(new MessagePopup(gui.getGui(), "Unknown Error", "Couldn't load vanilla skin"));
			return;
		}
		Image img = new Image(e.vanillaSkin);
		exportSkin0(e, gui, new Result(() -> new SkinDataOutputStream(img, MinecraftClientAccess.get().getPlayerRenderManager().getLoader().getTemplate(), e.skinType), () -> {
			img.storeTo(f);
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.export_success"), gui.getGui().i18nFormat("label.cpm.export_success.desc", f.getName())));
		}), forceOut);
	}

	public static void exportSkin(Editor e, EditorGui gui, Consumer<String> b64Out, boolean forceOut) {
		byte[] buffer = new byte[200];
		int[] size = new int[] {0};
		exportSkin0(e, gui, new Result(() -> new BAOS(buffer, size), () -> b64Out.accept(Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, size[0])))), forceOut);
	}

	private static void exportSkin0(Editor e, EditorGui gui, Result result, boolean forceOut) {
		try {
			List<Cube> flatList = new ArrayList<>();
			walkElements(e.elements, new int[] {10}, flatList);
			ModelPartDefinition def = new ModelPartDefinition(e.skinProvider.isEdited() ? new ModelPartSkin(e) : null, flatList);
			ModelPartPlayer player = new ModelPartPlayer(e);
			def.setPlayer(player);
			List<IModelPart> otherParts = new ArrayList<>();
			for(PlayerModelParts p : PlayerModelParts.VALUES) {
				for (ModelElement el : e.elements) {
					if(el.type == ElementType.ROOT_PART && el.typeData == p) {
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
			if(!e.animations.isEmpty()) {
				otherParts.add(new ModelPartAnimation(e));
			}
			if(e.listIconProvider != null) {
				otherParts.add(new ModelPartListIcon(e));
			}
			def.setOtherParts(otherParts);
			if(MinecraftObjectHolder.DEBUGGING)System.out.println(def);
			if(forceOut) {
				writeOut(e, gui, def, result);
				return;
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
				return;
			} catch (IOException e1) {
				throw new ExportException("error.cpm.unknownError", e1);
			}
			result.close();
		} catch (ExportException ex) {
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
		} catch (Exception ex) {
			ex.printStackTrace();
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
		String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
		System.out.println(b64);
		gui.openPopup(new ExportOverflowPopup(gui, gui.getGui(), b64, link -> {
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
		}));
	}

	private static void walkElements(List<ModelElement> elems, int[] id, List<Cube> flatList) {
		for (ModelElement me : elems) {
			switch (me.type) {
			case NORMAL:
				me.id = (byte) id[0]++;
				flatList.add(me);
				break;
			case ROOT_PART:
				me.id = (byte) ((RootModelElement)me.rc).getPart().getId(me.rc);
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

		public Result(Supplier<OutputStream> out, Closeable finish) {
			this.out = out;
			this.finish = finish;
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
}
