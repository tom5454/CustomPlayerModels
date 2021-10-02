package com.tom.cpm.shared.editor.project;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.loaders.AnimationsLoaderV1;
import com.tom.cpm.shared.editor.project.loaders.DescriptionLoaderV1;
import com.tom.cpm.shared.editor.project.loaders.ElementsLoaderV1;
import com.tom.cpm.shared.editor.project.loaders.PropertiesLoaderV1;
import com.tom.cpm.shared.editor.project.loaders.TemplateLoaderV1;
import com.tom.cpm.shared.editor.project.loaders.TexturesLoaderV1;

public class ProjectIO {
	public static final int projectFileVersion = 1;

	public static Map<Integer, ProjectIO> loaders;
	public Map<String, ProjectPartLoader> partLoaders;

	static {
		loaders = new HashMap<>();
		Set<ProjectPartLoader> l = new HashSet<>();
		load(l);
		TreeMap<Integer, Set<ProjectPartLoader>> ls = l.stream().collect(Collectors.toMap(ProjectPartLoader::getVersion, v -> {
			Set<ProjectPartLoader> s = new HashSet<>();
			s.add(v);
			return s;
		}, (a, b) -> {
			a.addAll(b);
			return a;
		}, TreeMap::new));
		ProjectIO loader = new ProjectIO();
		for(Entry<Integer, Set<ProjectPartLoader>> e : ls.entrySet()) {
			loaders.put(e.getKey(), loader);
			final ProjectIO fpl = loader;
			e.getValue().forEach(p -> fpl.partLoaders.put(p.getId(), p));
			loader = new ProjectIO(loader);
		}
	}

	public ProjectIO() {
		partLoaders = new HashMap<>();
	}

	private static void load(Set<ProjectPartLoader> l) {
		l.add(new AnimationsLoaderV1());
		l.add(new DescriptionLoaderV1());
		l.add(new ElementsLoaderV1());
		l.add(new PropertiesLoaderV1());
		l.add(new TemplateLoaderV1());
		l.add(new TexturesLoaderV1());
	}

	public ProjectIO(ProjectIO pl) {
		partLoaders = new HashMap<>(pl.partLoaders);
	}

	public static void loadProject(Editor editor, IProject project) throws IOException {
		JsonMap data = project.getJson("config.json");
		int version = data.getInt("version");
		ProjectIO loader = loaders.get(version);
		Iterator<ProjectPartLoader> itr = loader.partLoaders.values().stream().
				sorted(Comparator.comparing(ProjectPartLoader::getLoadOrder)).iterator();
		while (itr.hasNext()) {
			ProjectPartLoader p = itr.next();
			p.load(editor, project);
		}
	}

	public static void saveProject(Editor editor, ProjectFile project) throws IOException {
		ProjectWriter writer = new ProjectWriter.Impl(project);
		ProjectIO loader = loaders.get(projectFileVersion);
		JsonMap data = writer.getJson("config.json");
		data.put("version", projectFileVersion);
		for (ProjectPartLoader p : loader.partLoaders.values()) {
			p.save(editor, writer);
		}
		writer.flush();
	}
}
