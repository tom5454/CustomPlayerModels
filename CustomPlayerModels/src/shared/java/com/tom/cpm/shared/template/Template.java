package com.tom.cpm.shared.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.editor.template.args.TexEditorArg;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.template.args.TexArg;

public class Template {
	public static final int TEMPLATE_ID_OFFSET = 10000;
	protected Link link;
	protected Map<String, IArg> templateArgs = new HashMap<>();
	protected Map<String, Object> data;
	protected Map<String, Object> appliedData;
	private List<Cube> cubes;
	private List<RenderedCube> rc;
	protected Map<VanillaModelPart, List<RenderedCube>> partToCubes;
	protected Map<Integer, RootModelType> rootIDmap = new HashMap<>();
	protected TextureProvider texture;
	protected TexArg texArg;

	@SuppressWarnings("unchecked")
	public Template(Link link, InputStreamReader rd, Map<String, Object> editorData, IOHelper args) throws IOException {
		this.link = link;
		data = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(rd, Object.class);
		List<Map<String, Object>> argsList = (List<Map<String, Object>>) data.get("args");
		if(args != null) {
			for (Map<String, Object> arg : argsList) {
				String name = (String) arg.get("name");
				IArg iarg = TemplateArgumentType.create((String) arg.get("type"));
				iarg.init((Map<String, Object>) arg.get("data"));
				iarg.load(args);
				templateArgs.put(name, iarg);
			}
		} else {
			for (Map<String, Object> arg : argsList) {
				String name = (String) arg.get("name");
				IArg iarg = TemplateArgumentType.create((String) arg.get("type"));
				iarg.init((Map<String, Object>) arg.get("data"));
				if(editorData.containsKey(name))
					iarg.load((Map<String, Object>) editorData.get(name));
				templateArgs.put(name, iarg);
			}
		}
		if(data.containsKey("texture")) {
			texture = new TextureProvider(new IOHelper((String) data.get("texture")), 128);
			texArg = new TexArg();
			if(args != null)texArg.load(args);
			else {
				if(editorData.containsKey(TexEditorArg.NAME))
					texArg.load((Map<String, Object>) editorData.get(TexEditorArg.NAME));
			}
			templateArgs.put(TexEditorArg.NAME, texArg);
		}
		Map<String, Integer> rootsMap = (Map<String, Integer>) data.get("roots");
		if(rootsMap != null) {
			for (Entry<String, Integer> e : rootsMap.entrySet()) {
				for(RootModelType rmt : RootModelType.VALUES) {
					if(rmt.getName().equals(e.getKey())) {
						rootIDmap.put(e.getValue(), rmt);
						break;
					}
				}
			}
		}
		loadModelData();
	}

	@SuppressWarnings("unchecked")
	protected void loadModelData() {
		appliedData = Util.deepCopy(data);
		templateArgs.values().forEach(a -> a.apply(appliedData));
		cubes = new ArrayList<>();
		List<Map<String, Object>> cubesList = (List<Map<String, Object>>) appliedData.get("cubes");
		Map<Integer, Map<String, Object>> cubeDataList = new HashMap<>();
		for (int i = 0; i < cubesList.size(); i++) {
			Map<String, Object> map = cubesList.get(i);
			Cube cube = Cube.loadDefinitionCube(map);
			cube.id = i + TEMPLATE_ID_OFFSET;
			cubes.add(cube);
			cubeDataList.put(cube.id, (Map<String, Object>) map.get("data"));
		}
		rc = Cube.resolveCubes(cubes);
		for (RenderedCube renderedCube : rc) {
			Map<String, Object> cd = cubeDataList.get(renderedCube.getId());
			renderedCube.hidden = (boolean) cd.get("hidden");
			renderedCube.recolor = (boolean) cd.get("recolor");
			renderedCube.glow = (boolean) cd.get("glow");
			templateArgs.values().forEach(a -> a.apply(renderedCube));
		}
	}

	protected void loadPartToCubes() {
		partToCubes = new HashMap<>();
		Map<Integer, List<RenderedCube>> roots = new HashMap<>();
		for(int i = 0;i<PlayerModelParts.VALUES.length;i++) {
			List<RenderedCube> l = new ArrayList<>();
			partToCubes.put(PlayerModelParts.VALUES[i], l);
			roots.put(i, l);
		}

		for (RenderedCube renderedCube : rc) {
			int id = renderedCube.getCube().parentId;
			List<RenderedCube> p = roots.get(id);
			if(p != null)p.add(renderedCube);
		}

		List<RenderedCube> customParts = partToCubes.remove(PlayerModelParts.CUSTOM_PART);
		for (Entry<Integer, RootModelType> e : rootIDmap.entrySet()) {
			List<RenderedCube> l = new ArrayList<>();
			partToCubes.put(e.getValue(), l);
			for (RenderedCube rc : customParts) {
				if(rc.getId() == e.getKey()) {
					l.add(rc);
				}
			}
		}
	}

	public static interface IArg {
		String getType();
		void write(Map<String, Object> map);
		void write(IOHelper h) throws IOException;

		void load(Map<String, Object> map);
		void load(IOHelper h) throws IOException;

		void init(Map<String, Object> map);
		void export(Map<String, Object> map);

		void apply(RenderedCube cube);
		void apply(Map<String, Object> map);
	}

	public Link getLink() {
		return link;
	}

	@SuppressWarnings("unchecked")
	public IOHelper writeArgs() throws IOException {
		IOHelper h = new IOHelper();
		List<Map<String, Object>> argsList = (List<Map<String, Object>>) data.get("args");
		for (Map<String, Object> arg : argsList) {
			String name = (String) arg.get("name");
			IArg iarg = templateArgs.get(name);
			iarg.write(h);
		}
		if(texArg != null)texArg.write(h);
		return h;
	}

	public List<RenderedCube> getCubes() {
		return rc;
	}

	public boolean isTextureMerged() {
		return texArg == null ? true : texArg.isTextureMerged();
	}

	public void onStitch(Vec2i uv) {
		for (RenderedCube cube : rc) {
			cube.getCube().u += uv.x;
			cube.getCube().v += uv.y;
		}
	}

	public TextureProvider getTemplateDefaultTexture() {
		return texture;
	}
}
