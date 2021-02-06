package com.tom.cpm.shared.editor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

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
import com.tom.cpm.shared.model.PlayerModelElement;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimation;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartPlayerPos;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinType;

public class Exporter {

	public static void exportSkin(Editor e, EditorGui gui, File f, boolean forceOut) {
		try {
			List<Cube> flatList = new ArrayList<>();
			walkElements(e.elements, new int[] {10}, flatList);
			ModelPartDefinition def = new ModelPartDefinition(e.skinProvider.isEdited() ? new ModelPartSkin(e) : null, flatList);
			ModelPartPlayer player = new ModelPartPlayer(e);
			def.setPlayer(player);
			List<IModelPart> otherParts = new ArrayList<>();
			for(PlayerModelParts p : PlayerModelParts.VALUES) {
				for (ModelElement el : e.elements) {
					if(el.type == ElementType.PLAYER_PART && el.typeData == p) {
						if(Math.abs(el.pos.x) >= 0.1f || Math.abs(el.pos.y) >= 0.1f || Math.abs(el.pos.z) >= 0.1f ||
								Math.abs(el.rotation.x) >= 0.1f || Math.abs(el.rotation.y) >= 0.1f || Math.abs(el.rotation.z) >= 0.1f) {
							otherParts.add(new ModelPartPlayerPos(p, el.pos, el.rotation));
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
			def.setOtherParts(otherParts);
			if(e.vanillaSkin == null) {
				gui.openPopup(new MessagePopup(gui.getGui(), "Unknown Error", "Couldn't load vanilla skin"));
				return;
			}
			if(MinecraftObjectHolder.DEBUGGING)System.out.println(def);
			if(forceOut) {
				writeOut(e, gui, def, f);
				return;
			}
			BufferedImage image = copyImage(e.vanillaSkin);
			try(SkinDataOutputStream out = new SkinDataOutputStream(image, MinecraftClientAccess.get().getPlayerRenderManager().getLoader().getTemplate(), e.skinType)) {
				out.write(ModelDefinitionLoader.HEADER);
				ChecksumOutputStream cos = new ChecksumOutputStream(out);
				try(IOHelper dout = new IOHelper(cos)) {
					dout.writeObjectBlock(new ModelPartSkinType(e.skinType));
					dout.writeObjectBlock(def);
					dout.writeObjectBlock(ModelPartEnd.END);
				}
			} catch (EOFException unusedException) {
				writeOut(e, gui, def, f);
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			ImageIO.write(image, "PNG", f);
		} catch (ExportException ex) {
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("label.cpm.export_error", gui.getGui().i18nFormat(ex.getMessage()))));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void writeOut(Editor e, EditorGui gui, ModelPartDefinition def, File f) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		{
			baos.write(ModelDefinitionLoader.HEADER);
			ChecksumOutputStream cos = new ChecksumOutputStream(baos);
			def.write(new IOHelper(cos));
			cos.close();
		}
		String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
		System.out.println(b64);
		gui.openPopup(new ExportOverflowPopup(gui.getGui(), b64, link -> {
			try {
				BufferedImage img = copyImage(e.vanillaSkin);
				ModelPartDefinitionLink defLink = new ModelPartDefinitionLink(link);
				try(SkinDataOutputStream out = new SkinDataOutputStream(img, MinecraftClientAccess.get().getPlayerRenderManager().getLoader().getTemplate(), e.skinType)) {
					out.write(ModelDefinitionLoader.HEADER);
					ChecksumOutputStream cos = new ChecksumOutputStream(out);
					try(IOHelper dout = new IOHelper(cos)) {
						dout.writeObjectBlock(new ModelPartSkinType(e.skinType));
						dout.writeObjectBlock(defLink);
						dout.writeObjectBlock(ModelPartEnd.END);
					}
				}
				ImageIO.write(img, "PNG", f);
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
			case PLAYER_PART:
				me.id = (byte) ((PlayerModelElement)me.rc).getPart().ordinal();
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

	public static BufferedImage copyImage(BufferedImage source){
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
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
}
