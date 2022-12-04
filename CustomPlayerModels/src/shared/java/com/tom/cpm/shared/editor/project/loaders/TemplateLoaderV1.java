package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.template.TemplateArgType;
import com.tom.cpm.shared.editor.template.TemplateSettings;

public class TemplateLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "template";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		project.jsonIfExists("templates.json", data -> {
			JsonList lst = data.getList("templates");
			lst.forEachMap(map -> {
				editor.templates.add(EditorTemplate.load(editor, map));
			});
		});
		project.jsonIfExists("template_settings.json", data -> {
			if(editor.templateSettings == null)editor.templateSettings = new TemplateSettings(editor);
			editor.templateSettings.hasTex = data.getBoolean("texture");
			JsonList lst = data.getList("args");
			lst.forEachMap(map -> {
				TemplateArgType type = TemplateArgType.lookup(map.getString("type"));
				TemplateArgHandler arg = new TemplateArgHandler(editor, map.getString("name"), map.getString("desc"), type);
				editor.templateSettings.templateArgs.add(arg);
				arg.handler.loadProject(map.getMap("data").asMap());
				if(arg.handler.requiresParts() && arg.effectedElems != null) {
					JsonList partList = map.getList("parts");
					partList.<Number>forEach(e -> Editor.walkElements(editor.elements, elem -> {
						if(elem.storeID == e.longValue()) {
							arg.effectedElems.add(elem);
						}
					}));
				}
			});
			List<Long> templElems = data.getList("displayElems").<Number>stream().map(Number::longValue).collect(Collectors.toList());
			Editor.walkElements(editor.elements, e -> {
				if(templElems.contains(e.storeID)) {
					e.templateElement = true;
				}
			});
		});
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		if(!editor.templates.isEmpty()) {
			JsonMap data = project.getJson("templates.json");
			JsonList lst = data.putList("templates");
			for (EditorTemplate templ : editor.templates) {
				Map<String, Object> t = new HashMap<>();
				lst.add(t);
				templ.store(t);
			}
		}
		if(editor.templateSettings != null) {
			JsonMap data = project.getJson("template_settings.json");
			JsonList lst = data.putList("args");
			data.put("texture", editor.templateSettings.hasTex);
			for(TemplateArgHandler arg : editor.templateSettings.templateArgs) {
				Map<String, Object> map = new HashMap<>();
				lst.add(map);
				map.put("name", arg.name);
				map.put("desc", arg.desc);
				map.put("type", arg.type.name().toLowerCase(Locale.ROOT));
				if(arg.handler.requiresParts() && arg.effectedElems != null) {
					List<Number> partList = new ArrayList<>();
					arg.effectedElems.forEach(e -> partList.add(e.storeID));
					map.put("parts", partList);
				}
				Map<String, Object> m = new HashMap<>();
				map.put("data", m);
				arg.handler.saveProject(m);
			}
			List<Object> dispIds = new ArrayList<>();
			data.put("displayElems", dispIds);
			Editor.walkElements(editor.elements, e -> {
				if(e.templateElement) {
					dispIds.add(e.storeID);
				}
			});
		}
	}

	@Override
	public int getLoadOrder() {
		return 1;
	}
}
