package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.editor.tags.EditorTagManager;

public class TagsLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "tags";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		readTags(project, editor.tags.getItemTags(), "items");
		readTags(project, editor.tags.getBlockTags(), "blocks");
		readTags(project, editor.tags.getEntityTags(), "entities");
		readTags(project, editor.tags.getBiomeTags(), "biomes");
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		writeTags(project, editor.tags.getItemTags(), "items");
		writeTags(project, editor.tags.getBlockTags(), "blocks");
		writeTags(project, editor.tags.getEntityTags(), "entities");
		writeTags(project, editor.tags.getBiomeTags(), "biomes");
	}

	protected <T> void readTags(IProject project, EditorTagManager<T> mngr, String name) throws IOException {
		project.jsonIfExists("tags/" + name + ".json", data -> {
			JsonList list = data.getList("tags");
			list.forEachMap(d -> {
				String id = d.getString("id");
				if (id == null)return;
				id = EditorTagManager.formatTag(id);
				if (id == null)return;
				String[] elems = d.getList("elements").stream().map(String::valueOf).toArray(String[]::new);
				mngr.load(id, elems);
			});
		});
	}

	protected <T> void writeTags(ProjectWriter project, EditorTagManager<T> mngr, String name) throws IOException {
		String path = "tags/" + name + ".json";
		if (mngr.hasTags()) {
			JsonMap data = project.getJson(path);
			JsonList list = data.putList("tags");
			mngr.getTags().forEach(t -> {
				JsonMap d = list.addMap();
				d.put("id", t.getRawId());
				JsonList l = d.putList("elements");
				t.getEntries().forEach(l::add);
			});
		} else {
			project.delete(path);
		}
	}
}
