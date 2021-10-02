package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.util.Log;

public class DescriptionLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "desc";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		project.jsonIfExists("description.json", data -> loadDesc(editor, data));
		project.ifExists("desc_icon.png", Image::loadFrom, img -> loadIcon(editor, img));
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		if(editor.description != null) {
			JsonMap data = project.getJson("description.json");
			data.put("name", editor.description.name);
			data.put("desc", editor.description.desc);
			JsonMap map = data.putMap("cam");
			map.put("zoom", editor.description.camera.camDist);
			map.put("look", editor.description.camera.look.toMap());
			map.put("pos", editor.description.camera.position.toMap());
			map.put("copyProt", editor.description.copyProtection.name().toLowerCase());
			if(editor.description.icon != null) {
				project.putFile("desc_icon.png", editor.description.icon, Image::storeTo);
			}
		}
	}

	protected void loadDesc(Editor editor, JsonMap data) {
		editor.description = new ModelDescription();
		editor.description.name = data.getString("name");
		editor.description.desc = data.getString("desc");
		JsonMap map = data.getMap("cam");
		editor.description.camera.camDist = map.getFloat("zoom");
		editor.description.camera.look = new Vec3f(map.getMap("look"), editor.description.camera.look);
		editor.description.camera.position = new Vec3f(map.getMap("pos"), editor.description.camera.position);
		editor.description.copyProtection = CopyProtection.lookup(map.getString("copyProt", "normal"));
	}

	protected void loadIcon(Editor editor, Image img) {
		if(editor.description == null)editor.description = new ModelDescription();
		editor.description.icon = img;
		if(editor.description.icon.getWidth() != 256 || editor.description.icon.getHeight() != 256) {
			editor.description.icon = null;
			Log.error("Illegal image size for model/template icon must be 256x256");
		}
	}
}
