package com.tom.cpm.shared.editor.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.TemplateArg;
import com.tom.cpm.shared.editor.template.args.TexEditorArg;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.gui.elements.Tooltip;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.template.Template;
import com.tom.cpm.shared.util.TextureStitcher;

public class EditorTemplate extends Template implements TreeElement {
	private Editor editor;
	private String name;
	private List<TreeElement> elems;
	private List<TemplateArg<?>> editorArgs;
	private EditorTexture tex;
	private TexEditorArg textureArg;
	private Tooltip tooltip;

	@SuppressWarnings("unchecked")
	private EditorTemplate(Editor editor, Link link, InputStreamReader rd, Map<String, Object> data) throws IOException {
		super(link, rd, data, null);
		this.editor = editor;
		loadPartToCubes();
		name = (String) this.data.get("name");
		if(this.data.containsKey("desc")) {
			tooltip = new Tooltip(editor.frame, (String) this.data.get("desc"));
		}
		elems = new ArrayList<>();
		editorArgs = new ArrayList<>();
		List<Map<String, Object>> argsList = (List<Map<String, Object>>) this.data.get("args");
		Map<String, TemplateArgType> types = new HashMap<>();
		types.put(TexEditorArg.NAME, TemplateArgType.TEX);
		for (Map<String, Object> arg : argsList) {
			String name = (String) arg.get("name");
			types.put(name, TemplateArgType.lookup((String) arg.get("elem_type")));
		}
		templateArgs.forEach((n, arg) -> {
			TemplateArgType type = types.get(n);
			TemplateArg<?> a = type.factory.get();
			if(type == TemplateArgType.TEX)textureArg = (TexEditorArg) a;
			loadArg(a, arg);
			editorArgs.add(a);
			List<TreeElement> options = new ArrayList<>();
			a.createTreeElements(options, editor);
			elems.add(new TreeElement() {

				@Override
				public String getName() {
					if(n.equals(TexEditorArg.NAME))return editor.gui().i18nFormat("label.cpm.template_arg_tex");
					return n;
				}

				@Override
				public void getTreeElements(Consumer<TreeElement> c) {
					options.forEach(c);
				}
			});
		});
		if(texture != null) {
			tex = new EditorTexture(texture);
			textureArg.bind(this);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends IArg> void loadArg(TemplateArg<T> arg, IArg value) {
		arg.loadTemplate((T) value);
	}

	@SuppressWarnings("unchecked")
	public static EditorTemplate load(Editor editor, Map<String, Object> data) throws IOException {
		Link link = new Link((String) data.get("link"));
		try (InputStreamReader rd = new InputStreamReader(MinecraftClientAccess.get().getDefinitionLoader().load(link, ResourceEncoding.NO_ENCODING))) {
			return new EditorTemplate(editor, link, rd, (Map<String, Object>) data.get("data"));
		}
	}

	public static EditorTemplate create(Editor editor, String linkIn) throws IOException {
		Link link = new Link(linkIn);
		try (InputStreamReader rd = new InputStreamReader(MinecraftClientAccess.get().getDefinitionLoader().load(link, ResourceEncoding.NO_ENCODING))) {
			return new EditorTemplate(editor, link, rd, new HashMap<>());
		}
	}

	@Override
	public String getName() {
		return editor.gui().i18nFormat("label.cpm.template_name", name);
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		elems.forEach(c);
	}

	public List<RenderedCube> getForPart(ModelPart type) {
		return partToCubes.getOrDefault(type, Collections.emptyList());
	}

	public void store(Map<String, Object> map) {
		editorArgs.forEach(TemplateArg::applyToArg);
		Map<String, Object> data = new HashMap<>();
		map.put("data", data);
		map.put("link", link.toString());
		for (Entry<String, IArg> e : templateArgs.entrySet()) {
			Map<String, Object> dt = new HashMap<>();
			data.put(e.getKey(), dt);
			e.getValue().write(dt);
		}
	}

	public void applyToModel() {
		editorArgs.forEach(TemplateArg::applyToArg);
		loadModelData();
		loadPartToCubes();
		if(texArg != null && !texArg.isTextureMerged() && tex != null && tex.stitchPos != null) {
			getCubes().forEach(c -> {
				c.getCube().u += tex.stitchPos.x;
				c.getCube().v += tex.stitchPos.y;
			});
		}
	}

	@Override
	public EditorTexture getTexture() {
		return texArg != null ? (texArg.isTextureMerged() ? editor.skinProvider : tex) : null;
	}

	public EditorTexture getTemplateTexture() {
		return tex;
	}

	public void stitch(TextureStitcher stitcher) {
		if(texArg != null && !texArg.isTextureMerged()) {
			stitcher.stitchImage(tex);
		}
	}

	@Override
	public void delete() {
		editor.addUndo(() -> {
			editor.templates.add(this);
			editor.restitchTexture();
		});
		editor.runOp(() -> {
			editor.templates.remove(this);
			editor.restitchTexture();
		});
		editor.selectedElement = null;
		editor.markDirty();
		editor.updateGui();
	}

	@Override
	public void updateGui() {
		editor.setDelEn.accept(true);
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}
}
