package com.tom.cpm.shared.editor.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.cpl.gui.UI;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Exporter.ExportException;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.util.ExportHelper;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.template.Template;

public class TemplateExporter {
	public static final Gson sgson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

	public static void exportTemplate(Editor e, UI gui, ModelDescription desc, Consumer<String> templateOut) {
		try {
			List<Cube> flatList = new ArrayList<>();
			ExportHelper.flattenElements(e.elements, new int[] {Template.TEMPLATE_ID_OFFSET}, flatList);
			Map<String, Object> data = new HashMap<>();
			List<Map<String, Object>> cubesList = new ArrayList<>();
			data.put("cubes", cubesList);
			Map<Integer, Map<String, Object>> cubeDataList = new HashMap<>();
			flatList.sort((a, b) -> Integer.compare(a.id, b.id));
			for (Cube cube : flatList) {
				Map<String, Object> m = new HashMap<>();
				Cube.saveTemplateCube(m, cube);
				cubesList.add(m);
				Map<String, Object> dtMap = new HashMap<>();
				m.put("data", dtMap);
				m.put("id", cube.id);
				cubeDataList.put(cube.id, dtMap);
			}
			ExportHelper.walkElements(e.elements, el -> {
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
}
