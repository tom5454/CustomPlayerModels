package com.tom.cpm.blockbench;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Canvas;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Dialog;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.MenuBar;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.proxy.TextureGenerator;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec2;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.web.client.java.JsBuilder;

import elemental2.dom.BaseRenderingContext2D.FillStyleUnionType;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class ProjectGenerator {
	public static Dialog dialogNew;

	public static void initDialog() {
		Dialog.DialogProperties dctr = new Dialog.DialogProperties();
		dctr.id = "image_editor";
		dctr.title = Global.translate("dialog.skin.title");
		Dialog.FormSelectElement selRes = Dialog.FormSelectElement.make();
		selRes.label = "dialog.create_texture.resolution";
		selRes.value = 64;
		selRes.options = new JsBuilder<>().put("64", "64x64").put("128", "128x128").put("256", "256x256").build();
		Dialog.FormSelectElement selType = Dialog.FormSelectElement.make();
		selType.label = "Type";
		selType.value = "default";
		selType.options = new JsBuilder<>().put("default", "Steve (Default)").put("slim", "Alex (Slim)").build();
		Dialog.FormFileElement file = Dialog.FormFileElement.make();
		file.label = "dialog.skin.texture";
		file.extensions = new String[] {"png"};
		file.readtype = "image";
		file.filetype = "PNG";
		dctr.form = new JsBuilder<>().put("resolution", selRes).put("texture", file).put("type", selType).build();
		dctr.draggable = true;
		dctr.onConfirm = rIn -> {
			makeNewProject(Js.uncheckedCast(rIn));
			dialogNew.hide();
		};
		dctr.onCancel = () -> {
			dialogNew.hide();
		};
		dialogNew = new Dialog(dctr);
		PluginStart.format.new_ = () -> {
			dialogNew.show();
			return true;
		};

		Action.ActionProperties a = new Action.ActionProperties();
		a.name = "Add Parts";
		a.description = "";
		a.icon = "icon-player";
		a.category = "edit";
		a.condition = new Action.Condition();
		a.condition.formats = new String[] {"cpm"};
		/*a.children = () -> {
			return new Action[0];
		};*/
		a.click = e -> {
			//new Menu(a.children.children()).open(e.target);
		};
		Action addParts = new Action("add_parts", a);
		MenuBar.addAction(addParts, "edit.7");
		PluginStart.cleanup.add(addParts::delete);
	}

	public static void makeNewProject(DialogResult result) {
		if(Global.newProject(PluginStart.format)) {
			Project.texture_width = result.resolution;
			Project.texture_height = result.resolution;
			SkinType type = SkinType.get(result.type);
			for(PlayerModelParts part : PlayerModelParts.VALUES) {
				if(part == PlayerModelParts.CUSTOM_PART)continue;
				addPart(part, type);
			}
			Texture tex;
			if(result.texture != null && !result.texture.isEmpty()) {
				tex = new Texture();
				tex.name = "skin";
				tex.add(false);
			} else {
				Texture.TextureProperties c = new Texture.TextureProperties();
				c.mode = "bitmap";
				c.name = "skin";
				tex = new Texture(c);
				HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
				CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
				canvas.width = result.resolution;
				canvas.height = result.resolution;
				Cube.all.forEach(cube -> {
					TextureGenerator.paintCubeBoxTemplate(cube, tex, canvas, null, false);
				});
				ctx.fillStyle = FillStyleUnionType.of("#cdefff");
				ctx.fillRect(9, 11, 2, 2);
				ctx.fillRect(13, 11, 2, 2);
				tex.fromDataURL(canvas.toDataURL()).add(false);
			}
			Cube.all.forEach(cube -> cube.applyTexture(tex, true));
			updateAll();
		}
		dialogNew.hide();
	}

	public static Cube addPart(VanillaModelPart part, SkinType type) {
		PartValues pv = part.getDefaultSize(type);
		Group.GroupProperties pr = new Group.GroupProperties();
		pr.name = part.getName();
		pr.origin = JsVec3.make(0, 24, 0);
		Group gr = new Group(pr).init();
		pr = new Group.GroupProperties();
		Vec3f p = pv.getPos();
		pr.origin = JsVec3.make(-p.x, 24 - p.y, p.z);
		gr.extend(pr);
		gr.isOpen = true;
		Cube.CubeProperties c = new Cube.CubeProperties();
		c.name = part.getName();
		c.origin = gr.origin;
		Vec3f o = pv.getOffset();
		Vec3f s = pv.getSize();
		c.from = JsVec3.make(gr.origin.x - o.x - s.x, gr.origin.y - o.y - s.y, gr.origin.z + o.z);
		c.rotation = JsVec3.make(0, 0, 0);
		c.uv_offset = JsVec2.make(pv.getUV());
		Cube cube = new Cube(c);
		c = new Cube.CubeProperties();
		c.to = JsVec3.make(cube.from.x + s.x, cube.from.y + s.y, cube.from.z + s.z);
		cube.extend(c);
		cube.addTo(gr).init();
		return cube;
	}

	public static void updateAll() {
		Global.loadTextureDraggable();
		Canvas.updateAllBones();
		Canvas.updateVisibility();
		Global.setProjectTitle();
		Global.updateSelection();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogResult {
		public int resolution;
		public String texture, type;
	}
}
