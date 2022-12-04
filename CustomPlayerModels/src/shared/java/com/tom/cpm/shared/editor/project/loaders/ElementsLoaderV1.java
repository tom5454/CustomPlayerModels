package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class ElementsLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "elements";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		JsonMap data = project.getJson("config.json");
		JsonList lst = data.getList("elements");
		lst.forEachMap(map -> {
			String key = map.getString("id");
			ModelElement elem = null;
			if (map.getBoolean("customPart", false)) {
				for (RootModelType rmt : RootModelType.VALUES) {
					if(rmt.getName().equalsIgnoreCase(key)) {
						elem = new ModelElement(editor, ElementType.ROOT_PART, rmt, editor.gui());
						break;
					}
				}
				editor.elements.add(elem);
				elem.storeID = map.getLong("storeID", 0);
			} else if (map.getBoolean("dup", false)) {
				for (ModelElement e : editor.elements) {
					if(((VanillaModelPart) e.typeData).getName().equalsIgnoreCase(key)) {
						elem = new ModelElement(editor, ElementType.ROOT_PART, e.typeData, editor.gui());
						elem.duplicated = true;
						elem.storeID = map.getLong("storeID", 0);
						if(e.typeData instanceof PlayerModelParts && elem.storeID == ((PlayerModelParts)e.typeData).ordinal()) {
							//Fix duplicated parts broken storeID
							elem.storeID = Math.abs(new Random().nextLong());
						}
						editor.elements.add(elem);
						break;
					}
				}
			} else {
				for (ModelElement e : editor.elements) {
					if(((VanillaModelPart) e.typeData).getName().equalsIgnoreCase(key)) {
						elem = e;
						break;
					}
				}
			}
			if(elem != null) {
				elem.hidden = !map.getBoolean("show");
				elem.showInEditor = map.getBoolean("showInEditor", true);
				if(map.containsKey("children")) {
					loadChildren(map.getList("children"), elem, editor);
				}
				elem.pos = new Vec3f(map.getMap("pos"), new Vec3f(0, 0, 0));
				elem.rotation = new Vec3f(map.getMap("rotation"), new Vec3f(0, 0, 0));
				elem.disableVanillaAnim = map.getBoolean("disableVanillaAnim", false);
			}
		});
		Editor.walkElements(editor.elements, e -> {
			if(e.copyTransform != null)e.copyTransform.load(editor);
		});
	}

	@Override
	public void save(Editor editor, ProjectWriter project) {
		JsonMap data = project.getJson("config.json");
		JsonList lst = data.putList("elements");
		for (ModelElement elem : editor.elements) {
			JsonMap map = lst.addMap();
			map.put("id", ((VanillaModelPart) elem.typeData).getName());
			if(elem.typeData instanceof RootModelType)map.put("customPart", true);
			map.put("show", !elem.hidden);
			map.put("showInEditor", elem.showInEditor);
			if(!elem.children.isEmpty()) {
				saveChildren(elem, map.putList("children"), editor);
			}
			map.put("pos", elem.pos.toMap());
			map.put("rotation", elem.rotation.toMap());
			map.put("dup", elem.duplicated);
			map.put("disableVanillaAnim", elem.disableVanillaAnim);
			if(elem.duplicated || elem.typeData instanceof RootModelType) {
				map.put("storeID", elem.storeID);
			}
		}
	}

	protected void loadChildren(JsonList list, ModelElement parent, Editor editor) {
		list.forEachMap(map -> {
			ModelElement elem = new ModelElement(editor);
			elem.parent = parent;
			parent.children.add(elem);

			loadElement(elem, map, editor);
			elem.storeID = map.getLong("storeID", 0);

			if(map.containsKey("children")) {
				loadChildren(map.getList("children"), elem, editor);
			}
		});
	}

	protected void saveChildren(ModelElement modelElement, JsonList lst, Editor editor) {
		for (ModelElement elem : modelElement.children) {
			JsonMap map = lst.addMap();

			saveElement(elem, map, editor);
			map.put("storeID", elem.storeID);

			if(!elem.children.isEmpty()) {
				saveChildren(elem, map.putList("children"), editor);
			}
		}
	}

	protected void saveElement(ModelElement elem, JsonMap map, Editor editor) {
		map.put("name", elem.name);
		map.put("show", elem.showInEditor);
		map.put("texture", elem.texture);
		map.put("textureSize", elem.textureSize);
		map.put("offset", elem.offset.toMap());
		map.put("pos", elem.pos.toMap());
		map.put("rotation", elem.rotation.toMap());
		map.put("size", elem.size.toMap());
		map.put("scale", elem.scale.toMap());
		map.put("u", elem.u);
		map.put("v", elem.v);
		map.put("color", Integer.toHexString(elem.rgb));
		map.put("mirror", elem.mirror);
		map.put("mcScale", elem.mcScale);
		map.put("glow", elem.glow);
		map.put("recolor", elem.recolor);
		map.put("hidden", elem.hidden);
		map.put("singleTex", elem.singleTex);
		map.put("extrude", elem.extrude);
		if(elem.faceUV != null)map.put("faceUV", elem.faceUV.toMap());
		if(elem.itemRenderer != null)map.put("itemRenderer", elem.itemRenderer.slot.name().toLowerCase(Locale.ROOT));
		if(elem.copyTransform != null)map.put("copyTransform", elem.copyTransform.toMap());
	}

	protected void loadElement(ModelElement elem, JsonMap map, Editor editor) {
		elem.name = map.getString("name");
		elem.showInEditor = map.getBoolean("show");
		elem.texture = map.getBoolean("texture");
		elem.textureSize = map.getInt("textureSize");
		elem.offset = new Vec3f(map.getMap("offset"), new Vec3f());
		elem.pos = new Vec3f(map.getMap("pos"), new Vec3f());
		elem.rotation = new Vec3f(map.getMap("rotation"), new Vec3f());
		elem.size = new Vec3f(map.getMap("size"), new Vec3f(1, 1, 1));
		elem.scale = new Vec3f(map.getMap("scale"), new Vec3f(1, 1, 1));
		elem.u = map.getInt("u");
		elem.v = map.getInt("v");
		elem.rgb = Integer.parseUnsignedInt(map.getString("color"), 16);
		elem.mirror = map.getBoolean("mirror");
		elem.mcScale = map.getFloat("mcScale");
		elem.glow = map.getBoolean("glow", false);
		elem.recolor = map.getBoolean("recolor", false);
		elem.hidden = map.getBoolean("hidden", false);
		elem.singleTex = map.getBoolean("singleTex", false);
		elem.extrude = map.getBoolean("extrude", false);
		if(map.containsKey("faceUV"))elem.faceUV = new PerFaceUV(map.getMap("faceUV"));
		if(map.containsKey("itemRenderer")) {
			String name = map.getString("itemRenderer");
			for(ItemSlot slot : ItemSlot.VALUES) {
				if(name.equalsIgnoreCase(slot.name())) {
					elem.itemRenderer = new ItemRenderer(slot, 0);
					break;
				}
			}
		}
		if(map.containsKey("copyTransform")) {
			elem.copyTransform = new CopyTransformEffect(elem);
			elem.copyTransform.load(map.getMap("copyTransform"));
		}
	}

	@Override
	public int getLoadOrder() {
		return -1;
	}
}
