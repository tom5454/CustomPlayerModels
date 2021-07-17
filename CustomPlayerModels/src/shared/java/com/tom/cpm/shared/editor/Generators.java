package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.actions.ImageAction;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.render.VanillaModelPart;

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
		ActionBuilder ab = e.action("i", "button.cpm.tools.add_skin_layer2");
		ab.onUndo(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					ModelElement elem = new ModelElement(e);
					ab.addToList(el.children, elem);
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
		ab.execute();
		e.updateGui();
	}

	public static void convertModel(Editor e) {
		ActionBuilder ab = e.action("i", "button.cpm.tools.convert_model_custom");
		ab.onUndo(() -> e.selectedElement = null);
		for (ModelElement el : e.elements) {
			if(el.type == ElementType.ROOT_PART) {
				if(el.show) {
					ModelElement elem = new ModelElement(e);
					ab.addToList(el.children, elem);
					elem.parent = el;
					PartValues val = ((VanillaModelPart) el.typeData).getDefaultSize(e.skinType);
					elem.size = val.getSize();
					elem.offset = val.getOffset();
					elem.texture = true;
					elem.mcScale = val.getMCScale();
					elem.mirror = val.isMirror();
					Vec2i uv = val.getUV();
					elem.u = uv.x;
					elem.v = uv.y;
					elem.name = el.name;
					elem.generated = true;
					ab.updateValueOp(el, true, false, (a, b) -> a.show = b);
				}
			}
		}
		ab.execute();
		e.updateGui();
	}

	public static void setupTemplateModel(Editor e) {
		ActionBuilder ab = e.action("i", "button.cpm.tools.convert2template");
		ab.onUndo(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(el.show) {
						ModelElement elem = new ModelElement(e);
						ab.addToList(el.children, elem);
						elem.parent = el;
						PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
						elem.size = val.getSize();
						elem.offset = val.getOffset();
						elem.texture = false;
						elem.rgb = 0xffffff;
						elem.name = el.name;
						elem.templateElement = true;
						ab.updateValueOp(el, true, false, (a, b) -> a.show = b);
					}
					break;
				}
			}
		}
		ab.execute();
	}

	private static void convertTemplate(EditorGui eg) {
		Editor editor = eg.getEditor();
		IGui gui = eg.getGui();
		if (editor.templateSettings == null) {
			if (editor.dirty) {
				eg.openPopup(new MessagePopup(eg, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.must_save")));
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
			int ts = Math.abs(me.texSize);
			int bx = me.u * ts;
			int by = me.v * ts;
			int dx = MathHelper.ceil(me.size.x * ts);
			int dy = MathHelper.ceil(me.size.y * ts);
			int dz = MathHelper.ceil(me.size.z * ts);
			editor.action("i", "button.cpm.tools.fillUV").
			action(new ImageAction(me.getTexture().getImage(), box, img -> {
				img.fill(bx + dx + dz, by + dz, dz, dy, 0xffff0000);
				img.fill(bx, by + dz, dz, dy, 0xffdd0000);
				img.fill(bx + dz, by, dx, dz, 0xff00ff00);
				img.fill(bx + dz + dx, by, dx, dz, 0xff00dd00);
				img.fill(bx + dz, by + dz, dx, dy, 0xff0000ff);
				img.fill(bx + dz * 2 + dx, by + dz, dx, dy, 0xff0000dd);
			})).onAction(me.getTexture()::markDirty).
			execute();
		}
	}
}
