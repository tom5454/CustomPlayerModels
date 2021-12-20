package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.model.SkinType;

public class PropertiesLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "prop";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		JsonMap data = project.getJson("config.json");
		if(data.containsKey("skinType")) {
			editor.customSkinType = true;
			editor.skinType = SkinType.get(data.getString("skinType"));
		}
		editor.scalingElem.entityScaling = data.getFloat("scaling", 0);
		editor.hideHeadIfSkull = data.getBoolean("hideHeadIfSkull", true);
		editor.removeArmorOffset = data.getBoolean("removeArmorOffset", !editor.elements.stream().anyMatch(e -> e.duplicated));
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		JsonMap data = project.getJson("config.json");
		data.put("skinType", editor.skinType.getName());
		data.put("scaling", editor.scalingElem.entityScaling);
		data.put("hideHeadIfSkull", editor.hideHeadIfSkull);
		data.put("removeArmorOffset", editor.removeArmorOffset);
	}

}
