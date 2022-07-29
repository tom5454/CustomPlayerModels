package com.tom.cpm.blockbench;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeFace;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.JsArrayE;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileReader;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import jsinterop.base.Js;

public class BlockbenchExport {
	private final Editor editor;
	private boolean box_uv;
	private JsArrayE<Group> all;
	private List<String> warnings = new ArrayList<>();
	private Map<String, List<ModelElement>> modelTree = new HashMap<>();

	public BlockbenchExport(Editor editor) {
		this.editor = editor;
	}

	@SuppressWarnings("unchecked")
	public Promise<Void> doExport() {
		box_uv = Project.box_uv;
		all = Global.getAllGroups();

		List<Promise<Void>> loadedTextures = new ArrayList<>();
		if(Texture.all.length == 1) {
			Texture skin = Texture.all.getAt(0);
			loadedTextures.add(loadTexture(skin));
		} else {
			for (int i = 0;i<Texture.all.length;i++) {
				loadedTextures.add(loadTexture(Texture.all.getAt(i)));
			}
		}

		ModelElement body = getPart(PlayerModelParts.BODY);
		Cube.all.forEach(cube -> {
			if(cube.parent == Group.ROOT) {
				Vec3f pos = cube.origin.toVecF();
				pos.x *= 1;
				pos.y *= 1;
				pos.y += 24;
				ModelElement me = new ModelElement(editor);
				me.pos = pos;
				me.texture = true;
				add(body, me);
				me.showInEditor = cube.visibility;
				me.mirror = cube.mirror_uv;
				me.rotation = cube.rotation.toVecF();
				me.rotation.x = -me.rotation.x;
				me.rotation.y = -me.rotation.y;
				me.size = cube.to.toVecF().sub(cube.from.toVecF());
				Vec3f or = cube.origin.toVecF();
				Vec3f fr = cube.from.toVecF();
				me.offset.x = or.x - me.size.x - fr.x;
				me.offset.y = or.y - me.size.y - fr.y;
				me.offset.z = fr.z - or.z;
				me.name = cube.name;
				convertUV(me, cube);
			}
		});

		all.slice().forEach(group -> {
			List<Group> subgroups = new ArrayList<>();
			int[] group_i = new int[] {all.indexOf(group)};
			group.children.forEachReverse(in -> {
				if(!(in instanceof Cube) || !((Cube)in).export)return;
				Cube cube = (Cube) in;
				if(!cube.rotation.allEqual(0)) {
					Group sub = subgroups.stream().filter(s -> {
						if(!s.rotation.equals(cube.rotation))return false;
						if(s.rotation.filter((n, __, ___) -> n).length > 1) {
							return s.origin.equals(cube.origin);
						} else {
							if(s.rotation.x == 0 && s.origin.x != cube.origin.x)return false;
							if(s.rotation.y == 0 && s.origin.y != cube.origin.y)return false;
							if(s.rotation.z == 0 && s.origin.z != cube.origin.z)return false;
							return true;
						}
					}).findFirst().orElse(null);
					if(sub == null) {
						GroupProperties g = new GroupProperties();
						g.rotation = cube.rotation;
						g.origin = cube.origin;
						g.name = cube.name + "_r1";
						sub = new Group(g);
						sub.parent = group;
						sub.is_rotation_subgroup = true;
						sub.createUniqueName(all);
						subgroups.add(sub);
						group_i[0]++;
						all.splice(group_i[0], 0, sub);
					}
					sub.children.push(cube);
				}
			});
		});

		all.forEach(group -> {
			if (!(group instanceof Group) || !group.export) return;
			if (group.parent instanceof Group) return;
			String groupName = group.name;
			VanillaModelPart part;
			if(groupName.endsWith("_dup")) {
				groupName = groupName.substring(0, groupName.length() - 4);
				part = part(groupName);
				if(part == null) {
					warnings.add("Unknown root group: " + group.name);
					return;
				}
				ModelElement e = new ModelElement(editor, ElementType.ROOT_PART, part, editor.frame.getGui());
				editor.elements.add(e);
			} else {
				part = part(groupName);
				if(part == null) {
					warnings.add("Unknown root group: " + group.name);
					return;
				}
			}
			ModelElement me = getPart(part);
			me.hidden = true;
			Vec3f o = group.origin.toVecF();
			o.x *= -1;
			o.y *= -1;
			o.y += 24;
			me.rotation.x = -group.rotation.x;
			me.rotation.y = -group.rotation.y;
			me.rotation.z = group.rotation.z;
			PartValues pv = part.getDefaultSize(SkinType.DEFAULT);
			o = o.sub(pv.getPos());
			VanillaModelPart p = part.getCopyFrom();
			if(p != null) {
				ModelElement e = getPart(p);
				me.rotation = me.rotation.sub(e.rotation);
				o = o.sub(e.pos);
			}
			me.pos = o;
			modelTree.put(group.uuid, me.children);
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				ModelElement e = new ModelElement(editor);
				e.name = cube.name;
				me.children.add(e);
				e.texture = true;
				convert(e, group, cube);
			});
		});
		all.forEach(group -> {
			if (!(group instanceof Group) || !group.export) return;
			if (!(group.parent instanceof Group)) return;
			Vec3f rot = new Vec3f();
			rot.x = -group.rotation.x;
			rot.y = -group.rotation.y;
			rot.z = group.rotation.z;
			Vec3f o = group.origin.toVecF();
			if (group.parent instanceof Group) {
				o = o.sub(group.parent.origin.toVecF());
			}
			o.x *= -1;
			o.y *= -1;
			if (!(group.parent instanceof Group))o.y += 24;
			modelTree.put(group.uuid, new ArrayList<>());
			final Vec3f fo = o;
			ModelElement[] added = new ModelElement[] {null};
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				ModelElement e = new ModelElement(editor);
				e.rotation = rot;
				e.pos = fo;
				e.name = cube.name;
				e.texture = true;
				e.showInEditor = cube.visibility;
				convert(e, group, cube);
				List<ModelElement> ch = modelTree.get(group.parent.uuid);
				if(ch == null) {
					warnings.add("Skipped cube: " + group.name + "/" + cube.name + ". Check skipped roots");
					return;
				} else {
					added[0] = e;
					ch.add(e);
				}
			});
			if(added[0] == null) {
				ModelElement e = new ModelElement(editor);
				e.rotation = rot;
				e.pos = fo;
				e.name = group.name;
				e.texture = true;
				e.size = new Vec3f();
				List<ModelElement> ch = modelTree.get(group.parent.uuid);
				if(ch == null) {
					warnings.add("Skipped cube: " + group.name + ". Check skipped roots");
					return;
				} else {
					added[0] = e;
					ch.add(e);
				}
			}
			added[0].children.addAll(modelTree.get(group.uuid));
			modelTree.put(group.uuid, added[0].children);
		});
		Promise<Void[]> pr = Promise.all(loadedTextures.stream().toArray(Promise[]::new));
		return pr.then(__ -> Promise.resolve(((Void) null)));
	}

	private Promise<Void> loadTexture(Texture t) {
		return DomGlobal.fetch(t.source).then(b -> b.blob()).
				then(blob -> new Promise<>((ResolveCallbackFn<String> res, RejectCallbackFn rej) -> {
					FileReader reader = new FileReader();
					reader.onloadend = __ -> {
						res.onInvoke(reader.result.asString());
						return null;
					};
					reader.onerror = __ -> {
						rej.onInvoke(null);
						return null;
					};
					reader.readAsDataURL(blob);
				})).then(dataUrl -> {
					String b64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
					return ImageIO.loadImage(b64, true, false);
				}).then(img -> {
					TextureSheetType tst = TextureSheetType.SKIN;
					if(Texture.all.length != 1) {
						boolean found = false;
						for(TextureSheetType s : TextureSheetType.VALUES) {
							if(t.name.equalsIgnoreCase(s.name())) {
								tst = s;
								found = true;
								break;
							}
						}
						if(!found) {
							warnings.add("Unknown texture: " + t.name);
						}
					}
					if(tst.editable) {
						ETextures e = editor.textures.get(tst);
						if(e == null) {
							e = new ETextures(editor, tst);
							editor.textures.put(tst, e);
							e.provider.size = tst.getDefSize();
							Image def = new Image(e.provider.size.x, e.provider.size.y);
							try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tst.name().toLowerCase() + ".png")) {
								def = Image.loadFrom(is);
							} catch (IOException ex) {
							}
							e.setDefaultImg(def);
							e.setImage(new Image(def));
							e.markDirty();
							e.setEdited(tst.texType == null && tst.editable);
						}
						e.setImage(img);
					}
					return Promise.resolve((Void) null);
				});
	}

	private VanillaModelPart part(String name) {
		for(PlayerModelParts pmp : PlayerModelParts.VALUES) {
			if(pmp.getName().equalsIgnoreCase(name))return pmp;
		}
		for(RootModelType pmp : RootModelType.VALUES) {
			if(pmp.getName().equalsIgnoreCase(name))return pmp;
		}
		return null;
	}

	private void convert(ModelElement elem, Group group, Cube cube) {
		convertUV(elem, cube);
		elem.mcScale = cube.inflate;
		elem.size = cube.to.toVecF().sub(cube.from.toVecF());
		elem.offset.x = group.origin.x - cube.to.x;
		elem.offset.y = -cube.from.y - elem.size.y + group.origin.y;
		elem.offset.z = cube.from.z - group.origin.z;
	}

	private void convertUV(ModelElement elem, Cube cube) {
		if(box_uv) {
			elem.u = (int) cube.uv_offset.x;
			elem.v = (int) cube.uv_offset.y;
			elem.mirror = cube.mirror_uv;
		} else {
			elem.faceUV = new PerFaceUV();
			for(Direction d : Direction.VALUES) {
				CubeFace cf = cube.faces.getFace(d);
				Face f = elem.faceUV.faces.get(d);
				f.sx = (int) cf.uv.sx;
				f.sy = (int) cf.uv.sy;
				f.ex = (int) cf.uv.ex;
				f.ey = (int) cf.uv.ey;
				if(d == Direction.UP || d == Direction.DOWN)
					f.rotation = intToRot((cf.rotation + 180) % 360);
				else
					f.rotation = intToRot(cf.rotation);
				f.autoUV = Js.isTruthy(cube.autouv) ? true : false;
			}
		}
	}

	private static Rot intToRot(int r) {
		switch (r) {
		case 0: return Rot.ROT_0;
		case 180: return Rot.ROT_180;
		case 270: return Rot.ROT_270;
		case 90: return Rot.ROT_90;
		default: throw new RuntimeException();
		}
	}

	private ModelElement getPart(VanillaModelPart pmp) {
		for(ModelElement e : editor.elements) {
			if(e.typeData == pmp)return e;
		}
		return null;
	}

	private void add(ModelElement parent, ModelElement me) {
		parent.children.add(me);
		me.parent = parent;
	}
}
