package com.tom.cpm.shared.editor.template.args;

import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.TemplateArg;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.template.args.ArgBase;
import com.tom.cpm.shared.template.args.ColorArg;

public class ColorEditorArg implements TemplateArg<ColorArg> {
	private int value;
	private String name;
	private ColorArg backingArg;

	public ColorEditorArg() {
		this.name = "color_" + Long.toString(System.nanoTime() % 65535, Character.MAX_RADIX);
	}

	@Override
	public void saveProject(Map<String, Object> m) {
		m.put("color", Integer.toHexString(value));
		m.put("name", name);
	}

	@Override
	public void loadProject(Map<String, Object> m) {
		value = Integer.parseUnsignedInt((String) m.get("color"), 16);
		name = (String) m.get("name");
	}

	@Override
	public ColorArg export() {
		return new ColorArg(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void applyArgs(Map<String, Object> data, List<ModelElement> parts) {
		List<Map<String, Object>> cubesList = (List<Map<String, Object>>) data.get("cubes");
		for (int i = 0; i < cubesList.size(); i++) {
			Map<String, Object> map = cubesList.get(i);
			int id = ((Number) map.get("id")).intValue();
			parts.forEach(e -> {
				if(e.id == id) {
					map.put("color", ArgBase.wrapName(name));
				}
			});
		}
	}

	@Override
	public void apply(List<? extends Cube> parts) {
		parts.forEach(p -> p.rgb = value);
	}

	@Override
	public void createTreeElements(List<TreeElement> c, Editor editor) {
		c.add(new TreeElement() {

			@Override
			public String getName() {
				return editor.gui().i18nFormat("label.cpm.defaultColor");
			}

			@Override
			public void setElemColor(int color) {
				value = color;
				editor.updateGui();
			}

			@Override
			public void updateGui() {
				editor.setModePanel.accept(ModeDisplayType.COLOR);
				editor.setPartColor.accept(value);
			}
		});
	}

	@Override
	public void loadTemplate(ColorArg a) {
		value = a.getColor();
		name = a.getName();
		backingArg = a;
	}

	@Override
	public void applyToArg() {
		backingArg.setColor(value);
	}
}
