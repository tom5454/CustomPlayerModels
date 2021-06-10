package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;

public class Generators {
	public static List<Generators> generators = new ArrayList<>();

	static {
		generators.add(new Generators("button.cpm.tools.convert_model_custom", "tooltip.cpm.tools.convert_model_custom", eg -> convertModel(eg.getEditor())));
		generators.add(new Generators("button.cpm.tools.add_skin_layer2", null, eg -> addSkinLayer(eg.getEditor())));
		generators.add(new Generators("button.cpm.tools.convert2template", null, Generators::convertTemplate));
		generators.add(new Generators("button.cpm.tools.fillUV", null, Generators::fillUV));
	}

	public String name, tooltip;
	public Consumer<EditorGui> func;

	public Generators(String name, String tooltip, Consumer<EditorGui> func) {
		this.name = name;
		this.tooltip = tooltip;
		this.func = func;
	}

	private static void addSkinLayer(Editor e) {
		List<Runnable> l = new ArrayList<>();
		List<Runnable> u = new ArrayList<>();
		u.add(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					ModelElement elem = new ModelElement(e);
					l.add(() -> el.children.add(elem));
					u.add(() -> el.children.remove(elem));
					elem.parent = el;
					PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
					elem.size = val.getSize();
					elem.offset = val.getOffset();
					elem.texture = true;
					elem.u = val.u2;
					elem.v = val.v2;
					elem.name = e.frame.getGui().i18nFormat("label.cpm.layer_" + val.layer.getLowerName());
					elem.mcScale = 0.25F;
					break;
				}
			}
		}
		e.runOp(l);
		e.addUndo(u);
		e.markDirty();
		e.updateGui();
	}

	public static void convertModel(Editor e) {
		List<Runnable> l = new ArrayList<>();
		List<Runnable> u = new ArrayList<>();
		u.add(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(el.show) {
						ModelElement elem = new ModelElement(e);
						l.add(() -> el.children.add(elem));
						u.add(() -> el.children.remove(elem));
						elem.parent = el;
						PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
						elem.size = val.getSize();
						elem.offset = val.getOffset();
						elem.texture = true;
						elem.u = val.u;
						elem.v = val.v;
						elem.name = el.name;
						elem.generated = true;
						l.add(() -> el.show = false);
						u.add(() -> el.show = true);
					}
					break;
				}
			}
		}
		e.runOp(l);
		e.addUndo(u);
		e.markDirty();
		e.updateGui();
	}

	public static void setupTemplateModel(Editor e) {
		List<Runnable> l = new ArrayList<>();
		List<Runnable> u = new ArrayList<>();
		u.add(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(el.show) {
						ModelElement elem = new ModelElement(e);
						l.add(() -> el.children.add(elem));
						u.add(() -> el.children.remove(elem));
						elem.parent = el;
						PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
						elem.size = val.getSize();
						elem.offset = val.getOffset();
						elem.texture = false;
						elem.rgb = 0xffffff;
						elem.name = el.name;
						elem.templateElement = true;
						l.add(() -> el.show = false);
						u.add(() -> el.show = true);
					}
					break;
				}
			}
		}
		e.runOp(l);
		e.addUndo(u);
	}

	private static void convertTemplate(EditorGui eg) {
		Editor editor = eg.getEditor();
		IGui gui = eg.getGui();
		if (editor.templateSettings == null) {
			if (editor.dirty) {
				eg.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.must_save")));
			} else {
				if(editor.file == null)
					setupTemplate(editor);
				else
					eg.openPopup(new ConfirmPopup(eg, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.warn_c2t"), () -> setupTemplate(editor), null));
			}
		}
	}

	private static void setupTemplate(Editor editor) {
		editor.templateSettings = new TemplateSettings(editor);
		setupTemplateModel(editor);
		editor.markDirty();
		editor.updateGui();
	}

	private static void fillUV(EditorGui eg) {
		Editor editor = eg.getEditor();
		ModelElement me = editor.getSelectedElement();
		if(me != null && me.type == ElementType.NORMAL && me.texture) {
			Box box = me.getTextureBox();
			Image undo = new Image(box.w, box.h);
			undo.draw(me.getTexture().getImage(), -box.x, -box.y);
			editor.addUndo(() -> {
				me.getTexture().getImage().draw(undo, box.x, box.y);
				me.getTexture().markDirty();
				editor.refreshTexture(me.getTexture());
			});
			Image img = me.getTexture().getImage();
			int ts = Math.abs(me.texSize);
			int bx = me.u * ts;
			int by = me.v * ts;
			int dx = MathHelper.ceil(me.size.x * ts);
			int dy = MathHelper.ceil(me.size.y * ts);
			int dz = MathHelper.ceil(me.size.z * ts);
			editor.runOp(() -> {
				img.fill(bx + dx + dz, by + dz, dz, dy, 0xffff0000);
				img.fill(bx, by + dz, dz, dy, 0xffdd0000);
				img.fill(bx + dz, by, dx, dz, 0xff00ff00);
				img.fill(bx + dz + dx, by, dx, dz, 0xff00dd00);
				img.fill(bx + dz, by + dz, dx, dy, 0xff0000ff);
				img.fill(bx + dz * 2 + dx, by + dz, dx, dy, 0xff0000dd);
				me.getTexture().markDirty();
				editor.refreshTexture(me.getTexture());
			});
		}
	}
}
