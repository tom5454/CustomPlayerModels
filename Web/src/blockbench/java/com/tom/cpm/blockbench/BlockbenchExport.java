package com.tom.cpm.blockbench;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeFace;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.util.BBPartValues;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimatedTex;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.loaders.AnimationsLoaderV1;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.model.PartPosition;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.util.ScalingOptions;
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
	private JsArrayE<Group> all;
	private List<String> warnings = new ArrayList<>();
	private Map<String, CPMElem> modelTree = new HashMap<>();
	private Map<TextureSheetType, UVMul> uvMul = new HashMap<>();
	private List<Runnable> postConvert = new ArrayList<>();
	private Map<String, ModelElement> uuidLookup = new HashMap<>();

	public BlockbenchExport(Editor editor) {
		this.editor = editor;
	}

	private static void log(String msg) {
		DomGlobal.console.log(msg);
	}

	@SuppressWarnings("unchecked")
	public Promise<Void> doExport() {
		log("Exporting");
		all = Global.getAllGroups();

		List<Promise<Void>> loadedTextures = new ArrayList<>();
		UVMul skinUV = new UVMul();
		uvMul.put(TextureSheetType.SKIN, skinUV);
		if(Texture.all.length == 1) {
			Texture skin = Texture.all.getAt(0);
			loadedTextures.add(loadTexture(skin, TextureSheetType.SKIN));
		} else {
			for (int i = 0;i<Texture.all.length;i++) {
				Texture tex = Texture.all.getAt(i);
				TextureSheetType tst = TextureSheetType.SKIN;
				if(Texture.all.length != 1) {
					boolean found = false;
					for(TextureSheetType s : TextureSheetType.VALUES) {
						if(tex.name.equalsIgnoreCase(s.name())) {
							tst = s;
							found = true;
							break;
						}
					}
					if(!found) {
						warnings.add("Unknown texture: " + tex.name);
						continue;
					}
				}
				loadedTextures.add(loadTexture(tex, tst));
				if(tst != TextureSheetType.SKIN) {
					UVMul m = new UVMul();
					if(!tst.editable) {
						m.x = tst.getDefSize().x / (float) Project.texture_width;
						m.y = tst.getDefSize().y / (float) Project.texture_height;
					}
					uvMul.put(tst, m);
				}
			}
		}
		log("Exported textures");

		//Throw all loose cubes into the body
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
		log("Exported loose cubes");

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

		log("Generated rotation subgroups");

		all.forEach(group -> {
			if ((!(group instanceof Group) && !group.is_catch_bone) || !group.export) return;
			if (group.parent != Group.ROOT) return;
			String groupName = group.name;
			VanillaModelPart part;
			ModelElement me;
			if(groupName.endsWith("_dup")) {
				groupName = groupName.substring(0, groupName.length() - 4);
				part = part(groupName);
				if(part == null) {
					warnings.add("Unknown root group: " + group.name);
					return;
				}
				ModelElement e = new ModelElement(editor, ElementType.ROOT_PART, part, editor.frame.getGui());
				editor.elements.add(e);
				me = e;
			} else {
				part = part(groupName);
				if(part == null) {
					warnings.add("Unknown root group: " + group.name);
					return;
				}
				me = getPart(part);
			}
			me.hidden = true;
			me.disableVanillaAnim = group.disableVanillaAnim;
			uuidLookup.put(group.uuid, me);
			Vec3f o = group.origin.toVecF();
			o.x *= -1;
			o.y *= -1;
			o.y += 24;
			me.rotation.x = -group.rotation.x;
			me.rotation.y = -group.rotation.y;
			me.rotation.z = group.rotation.z;
			PartValues pv;
			if(part instanceof RootModelType)pv = BBParts.getPart((RootModelType) part);
			else pv = part.getDefaultSize(SkinType.DEFAULT);
			o = o.sub(pv.getPos());
			VanillaModelPart p = part.getCopyFrom();
			if(p != null) {
				ModelElement e = getPart(p);
				me.rotation = me.rotation.sub(e.rotation);
				o = o.sub(e.pos);
			} else if(pv instanceof BBPartValues) {
				me.rotation = me.rotation.sub(((BBPartValues)pv).getRotation());
			}
			me.pos = o;
			modelTree.put(group.uuid, new CPMElem(me));
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				ModelElement e = new ModelElement(editor);
				add(me, e);
				e.texture = true;
				convert(e, group, cube);
			});
		});

		log("Exported roots");

		all.forEach(group -> {
			if ((!(group instanceof Group) && !group.is_catch_bone) || !group.export) return;
			if (group.parent == Group.ROOT) return;
			Vec3f rot = new Vec3f();
			rot.x = -group.rotation.x;
			rot.y = -group.rotation.y;
			rot.z = group.rotation.z;
			Vec3f o = group.origin.toVecF().sub(group.parent.origin.toVecF());
			o.x *= -1;
			o.y *= -1;
			final Vec3f fo = o;
			List<CPMElem> children = new ArrayList<>();
			CPMElem ch = modelTree.get(group.parent.uuid);
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				CPMElem e = new CPMElem(group, cube, rot, fo);

				if(ch == null) {
					warnings.add("Skipped cube: " + group.name + "/" + cube.name + ". Check skipped roots");
					return;
				} else {
					children.add(e);
				}
			});
			if(children.isEmpty()) {
				CPMElem e = new CPMElem(group, null, rot, fo);
				if(ch == null) {
					warnings.add("Skipped cube: " + group.name + ". Check skipped roots");
					return;
				} else {
					children.add(e);
				}
			} else if(children.size() > 1) {
				CPMElem e = new CPMElem(group, null, rot, fo);
				children.forEach(CPMElem::resetTransform);
				e.children.addAll(children);
				children.clear();
				children.add(e);
			}
			ch.children.add(children.get(0));
			modelTree.put(group.uuid, children.get(0));
		});

		log("Exported model parts");

		modelTree.values().stream().filter(e -> e.part != null).forEach(CPMElem::finishConvert);
		postConvert.forEach(Runnable::run);

		log("Constructed model parts hierarchy");

		Promise<?> pr = Promise.all(loadedTextures.stream().toArray(Promise[]::new));
		return pr.then(__ -> {
			log("Textures finished");
			finishExport();
			return Promise.resolve(((Void) null));
		});
	}

	private void finishExport() {
		uvMul.entrySet().forEach(e -> {
			UVMul m = e.getValue();
			TextureSheetType tst = e.getKey();
			if(tst.editable) {
				ETextures tex = editor.textures.get(tst);
				while(tex.provider.size.x < 16384 && m.elems.stream().anyMatch(DecimalFixOp::needsFix)) {
					tex.provider.size.x *= 2;
					tex.provider.size.y *= 2;
					m.elems.forEach(d -> d.baseMul *= 2);
				}
			}
			m.elems.forEach(DecimalFixOp::apply);
		});
		log("Applied UV decimal fixer");

		if(Project.pluginData != null && !Project.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(Project.pluginData);

			if(s.containsKey("anims")) {
				new AnimEnc().load(s.getMap("anims"));
			}

			if(s.containsKey("scaling")) {
				JsonMap scl = s.getMap("scaling");

				for(ScalingOptions opt : ScalingOptions.VALUES) {
					float v = scl.getFloat(opt.name().toLowerCase(Locale.ROOT), 0);
					if(v != 0)
						editor.scalingElem.scaling.put(opt, v);
				}

				editor.scalingElem.pos = new Vec3f(scl.getMap("render_position"), new Vec3f());
				editor.scalingElem.rotation = new Vec3f(scl.getMap("render_rotation"), new Vec3f());
				editor.scalingElem.scale = new Vec3f(scl.getMap("render_scale"), new Vec3f());
			}

			editor.leftHandPos = loadPartPos(s, "left_hand");
			editor.rightHandPos = loadPartPos(s, "right_hand");
			if(s.containsKey("modelId")) {
				editor.modelId = s.getString("modelId");
			}
			editor.removeArmorOffset = s.getBoolean("removeArmorOffset", !editor.elements.stream().anyMatch(e -> e.duplicated));
			if(s.containsKey("tex")) {
				JsonMap tex = s.getMap("tex");
				for(TextureSheetType tx : TextureSheetType.VALUES) {
					ETextures eTex = editor.textures.get(tx);
					String name = tx.name().toLowerCase(Locale.ROOT);
					if(eTex != null && eTex.isEditable() && tex.containsKey(name)) {
						JsonMap map = tex.getMap(name);
						JsonList list = map.getList("anim");
						list.forEachMap(elem -> eTex.animatedTexs.add(new AnimatedTex(editor, tx, elem)));
					}
				}
			}
			if(s.containsKey("desc")) {
				JsonMap desc = s.getMap("desc");
				editor.description = new ModelDescription();
				editor.description.name = desc.getString("name");
				editor.description.desc = desc.getString("desc");
				editor.description.camera.camDist = desc.getFloat("zoom");
				editor.description.camera.look = new Vec3f(desc.getMap("look"), editor.description.camera.look);
				editor.description.camera.position = new Vec3f(desc.getMap("pos"), editor.description.camera.position);
				editor.description.copyProtection = CopyProtection.lookup(desc.getString("copyProt", "normal"));
			}
		}
		editor.hideHeadIfSkull = Project.hideHeadIfSkull;
		editor.removeBedOffset = Project.removeBedOffset;
	}

	protected static PartPosition loadPartPos(JsonMap fpHand, String name) {
		PartPosition p = new PartPosition();
		JsonMap map = fpHand.getMap(name);
		if(map != null) {
			Vec3f pos = new Vec3f(map.getMap("position"), new Vec3f());
			Vec3f rotation = new Vec3f(map.getMap("rotation"), new Vec3f());
			Vec3f scale = new Vec3f(map.getMap("scale"), new Vec3f());
			p.setRenderScale(pos, rotation, scale);
		}
		return p;
	}

	private class AnimEnc extends AnimationsLoaderV1 {

		public void load(JsonMap data) {
			JsonMap an = data.getMap("anims");
			an.asMap().keySet().forEach(a -> loadAnimation(editor, a, an.getMap(a)));
			editor.animEnc = new AnimationEncodingData();
			data.getList("free").forEach(v -> editor.animEnc.freeLayers.add(PlayerSkinLayer.getLayer((String) v)));
			data.getMap("def").forEach((k, v) -> editor.animEnc.defaultLayerValue.put(PlayerSkinLayer.getLayer(k), (Boolean) v));
		}

		@Override
		protected void findPartRef(AnimFrame frm, JsonMap map, Consumer<ModelElement> me) {
			ModelElement el = uuidLookup.get(map.getString("uuid"));
			if(el != null)me.accept(el);
		}
	}

	private Promise<Void> loadTexture(Texture t, TextureSheetType tst) {
		ETextures e = editor.textures.get(tst);
		if(e == null) {
			e = new ETextures(editor, tst);
			editor.textures.put(tst, e);
			Image def = new Image(tst.getDefSize().x, tst.getDefSize().y);
			e.provider.size = new Vec2i(tst.getDefSize());
			try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tst.name().toLowerCase() + ".png")) {
				def = Image.loadFrom(is);
			} catch (IOException ex) {
			}
			e.setDefaultImg(def);
			e.setImage(new Image(def));
		}
		if(tst.editable) {
			final ETextures fe = e;
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
						fe.provider.size = new Vec2i(Project.texture_width, Project.texture_height);
						fe.setImage(img);
						fe.setEdited(true);
						return Promise.resolve((Void) null);
					});
		}
		return Promise.resolve((Void) null);
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
		elem.name = cube.name;
		elem.mcScale = cube.inflate;
		elem.size = cube.to.toVecF().sub(cube.from.toVecF());
		elem.offset.x = group.origin.x - cube.to.x;
		elem.offset.y = -cube.from.y - elem.size.y + group.origin.y;
		elem.offset.z = cube.from.z - group.origin.z;
		elem.glow = cube.glow;
		elem.recolor = cube.recolor != -1;
		if(cube.recolor != -1)elem.rgb = cube.recolor;
		elem.extrude = cube.extrude;
		if(cube.pluginData != null && !cube.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(cube.pluginData);
			if(s.containsKey("extrude")) {
				elem.extrude = true;
				elem.u = s.getInt("u");
				elem.v = s.getInt("v");
				elem.textureSize = s.getInt("ts");
				elem.faceUV = null;
			}
		}
	}

	private void convertGroup(ModelElement elem, Group group) {
		uuidLookup.put(group.uuid, elem);
		elem.hidden = group.hidden;
		if(group.copyTransform != null && !group.copyTransform.isEmpty()) {
			postConvert.add(() -> {
				JsonMap s = JsonUtil.fromJson(group.copyTransform);
				ModelElement me = uuidLookup.get(s.get("uuid"));
				if(me != null) {
					CopyTransformEffect ct = new CopyTransformEffect(elem);
					ct.load(s);
					ct.from = me;
					elem.copyTransform = ct;
				}
			});
		}
		if(group.pluginData != null && !group.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(group.pluginData);
			if(s.containsKey("item")) {
				String name = s.getString("item");
				for(ItemSlot slot : ItemSlot.VALUES) {
					if(name.equalsIgnoreCase(slot.name())) {
						elem.itemRenderer = new ItemRenderer(slot, 0);
						elem.size = new Vec3f();
						elem.faceUV = null;
						break;
					}
				}
			}
		}
	}

	private void convertUV(ModelElement elem, Cube cube) {
		VanillaModelPart part = (VanillaModelPart) elem.getRoot().typeData;
		TextureSheetType tst = TextureSheetType.SKIN;
		if(part instanceof RootModelType) {
			tst = RootGroups.getGroup((RootModelType) part).getTexSheet((RootModelType) part);
		}
		UVMul m = uvMul.get(tst);
		if(cube.box_uv) {
			m.elems.add(new DecimalFixOp(cube.uv_offset.x, 0, m, a -> elem.u = a));
			m.elems.add(new DecimalFixOp(cube.uv_offset.y, 1, m, a -> elem.v = a));
			m.elems.add(new DecimalFixOp(1, 4, m, a -> elem.textureSize = a));
			elem.mirror = cube.mirror_uv;
		} else {
			elem.faceUV = new PerFaceUV();
			for(Direction d : Direction.VALUES) {
				CubeFace cf = cube.faces.getFace(d);
				Face f = elem.faceUV.faces.get(d);
				m.elems.add(new DecimalFixOp(cf.uv.sx, 2, m, a -> f.sx = a));
				m.elems.add(new DecimalFixOp(cf.uv.sy, 3, m, a -> f.sy = a));
				m.elems.add(new DecimalFixOp(cf.uv.ex, 2, m, a -> f.ex = a));
				m.elems.add(new DecimalFixOp(cf.uv.ey, 3, m, a -> f.ey = a));
				if(d == Direction.UP || d == Direction.DOWN)
					f.rotation = intToRot((cf.rotation + 180) % 360);
				else
					f.rotation = intToRot(cf.rotation);
				f.autoUV = Js.isTruthy(cube.autouv) ? true : false;
			}
		}
	}

	private static class DecimalFixOp {
		private float val;
		private int mode;
		private UVMul mul;
		private IntConsumer set;
		private int baseMul = 1;

		public DecimalFixOp(float val, int mode, UVMul mul, IntConsumer set) {
			this.val = val;
			this.mode = mode;
			this.mul = mul;
			this.set = set;
		}

		public boolean needsFix() {
			if((mode & 4) != 0)return false;
			float m = val * baseMul * ((mode & 1) != 0 ? mul.y : mul.x);
			return (m - Math.floor(m)) > 0.05f;
		}

		public void apply() {
			if((mode & 4) != 0) {
				set.accept(baseMul);
			} else
				set.accept((int) (val * baseMul * ((mode & 1) != 0 ? mul.y : mul.x)));
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
		ModelElement e = new ModelElement(editor, ElementType.ROOT_PART, pmp, editor.frame.getGui());
		editor.elements.add(e);
		if(pmp instanceof RootModelType) {
			RootGroups rg = RootGroups.getGroup((RootModelType) pmp);
			if(rg != null) {

			}
		}
		return e;
	}

	private void add(ModelElement parent, ModelElement me) {
		parent.children.add(me);
		me.parent = parent;
	}

	private static class UVMul {
		private float x = 1, y = 1;
		private Set<DecimalFixOp> elems = new HashSet<>();
	}

	private class CPMElem {
		private Group group;
		private Cube cube;
		private List<CPMElem> children = new ArrayList<>();
		private ModelElement part;
		private Vec3f rotation;
		private Vec3f pos;
		private boolean reGrouped;

		public CPMElem(Group group, Cube cube, Vec3f rotation, Vec3f pos) {
			this.group = group;
			this.cube = cube;
			this.rotation = rotation;
			this.pos = pos;
		}

		public CPMElem(ModelElement part) {
			this.part = part;
		}

		public void finishConvert() {
			children.forEach(c -> {
				c.makePart(part);
				c.finishConvert();
			});
		}

		private void makePart(ModelElement parent) {
			if(part == null) {
				part = new ModelElement(editor);
				add(parent, part);
				part.rotation = rotation;
				part.pos = pos;
				part.texture = true;
				if(cube != null) {
					part.showInEditor = cube.visibility;
					convert(part, group, cube);
					if(!reGrouped)convertGroup(part, group);
				} else {
					part.name = group.name;
					part.size = new Vec3f();
					convertGroup(part, group);
				}
			}
		}

		public void resetTransform() {
			rotation = new Vec3f();
			pos = new Vec3f();
			reGrouped = true;
		}
	}
}
