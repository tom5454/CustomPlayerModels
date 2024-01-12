package com.tom.cpm.shared.editor.util;

import java.util.List;
import java.util.function.Consumer;

import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;

public class ExportHelper {

	public static void flattenElements(List<ModelElement> elems, int[] id, List<Cube> flatList) {
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
			flattenElements(me.children, id, flatList);
		}
	}

	public static void walkElements(List<ModelElement> elems, Consumer<ModelElement> c) {
		for (ModelElement me : elems) {
			c.accept(me);
			walkElements(me.children, c);
		}
	}
}
