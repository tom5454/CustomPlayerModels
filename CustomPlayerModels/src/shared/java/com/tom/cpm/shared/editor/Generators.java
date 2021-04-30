package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;

public class Generators {

	public static void addSkinLayer(Editor e) {
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
}
