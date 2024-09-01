package com.tom.cpm.blockbench.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.format.CPMCodec;
import com.tom.cpm.blockbench.format.CubeData;
import com.tom.cpm.blockbench.format.GroupData;
import com.tom.cpm.blockbench.format.ProjectData;
import com.tom.cpm.blockbench.format.ProjectGenerator;
import com.tom.cpm.blockbench.proxy.Animation;
import com.tom.cpm.blockbench.proxy.BoneAnimator;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeFace;
import com.tom.cpm.blockbench.proxy.Cube.FaceUV;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Keyframe;
import com.tom.cpm.blockbench.proxy.KeyframeDataPoint;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec2;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.blockbench.util.JsonUtil;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.JsonMapImpl;
import com.tom.cpm.shared.editor.project.loaders.AnimationsLoaderV1;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.DirectPartValues;
import com.tom.cpm.shared.model.render.DirectParts;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.web.client.util.I18n;

import elemental2.core.Uint8ClampedArray;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.ImageData;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class BlockbenchImport {
	private Map<TextureSheetType, Texture> textures = new HashMap<>();
	private Map<TextureSheetType, UVMul> uvMul = new HashMap<>();
	private Map<ModelElement, Group> me2group = new HashMap<>();
	private List<Runnable> postConvert = new ArrayList<>();
	private final Editor editor;
	private Function<List<WarnEntry>, Promise<Void>> openWarn;
	private List<WarnEntry> warnings = new ArrayList<>();
	private boolean copyTrWarn, importAnims;
	private Map<String, Integer> groupNames = new HashMap<>();

	public BlockbenchImport(Editor editor, Function<List<WarnEntry>, Promise<Void>> openWarn) {
		this.editor = editor;
		this.openWarn = openWarn;
	}

	public Promise<Void> doImport() {
		for (ModelElement me : editor.elements) {
			if(!me.hidden)
				warnings.add(new WarnEntry(I18n.format("bb-label.warn.nonConvertedPartNotSupported", me.getElemName())));

			checkCompat(me.children);
		}

		Promise<Void> warns = !warnings.isEmpty() ? openWarn.apply(warnings) : Promise.resolve((Void) null);

		if(!editor.animations.isEmpty()) {
			warns = warns.then(__ -> new Promise<>((res, rej) -> {
				editor.ui.displayConfirm("Import Animations", "Convert CPM animations into Blockbench animations or keep CPM animations\\The animation importer is in Beta and may not import every animation correctly.\\Using the keep option will store the animations separately in the CPM format inside the bbmodel.", () -> {
					importAnims = true;
					res.onInvoke((Void) null);
				}, () -> res.onInvoke((Void) null), "Import", "Keep");
			}));
		}

		return warns.then(__ -> {
			Global.newProject(CPMCodec.format);
			doImport0();
			return null;
		});
	}

	private void checkCompat(List<ModelElement> children) {
		for (ModelElement me : children) {
			if(!me.texture) {
				warnings.add(new WarnEntry(I18n.format("bb-label.warn.coloredCubeNotSupported", me.getElemName())));
			}

			if(me.copyTransform != null && !copyTrWarn) {
				warnings.add(new WarnEntry(I18n.format("bb-label.warn.copyTransformNotVis")));
				copyTrWarn = true;
			}

			checkCompat(me.children);
		}
	}

	private void doImport0() {
		Vec2i tex = editor.textures.get(TextureSheetType.SKIN).provider.size;
		Project.texture_width = tex.x;
		Project.texture_height = tex.y;
		Project.box_uv = true;
		uvMul.put(TextureSheetType.SKIN, new UVMul());

		editor.textures.entrySet().forEach(e -> {
			Texture.TextureProperties c = new Texture.TextureProperties();
			c.mode = "bitmap";
			c.name = e.getKey().name().toLowerCase();
			Texture t = new Texture(c);
			Image img = e.getValue().provider.getImage();
			HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
			canvas.width = img.getWidth();
			canvas.height = img.getHeight();
			CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
			Uint8ClampedArray i = new Uint8ClampedArray(img.getData().buffer);
			ImageData im = new ImageData(i, img.getWidth(), img.getHeight());
			ctx.putImageData(im, 0, 0);
			t.fromDataURL(canvas.toDataURL()).add(false);
			textures.put(e.getKey(), t);
			UVMul m = new UVMul();
			m.x = Project.texture_width / (float) e.getValue().provider.size.x;
			m.y = Project.texture_height / (float) e.getValue().provider.size.y;
			uvMul.put(e.getKey(), m);
		});

		for (ModelElement me : editor.elements) {
			VanillaModelPart part = (VanillaModelPart) me.typeData;
			PartValues pv = DirectParts.getPartOverrides(part, editor.skinType);
			Vec3f pos = pv.getPos().add(me.pos);
			Vec3f rot = me.rotation;
			if(part.getCopyFrom() != null) {
				ModelElement c = findElementById(editor, part.getCopyFrom());
				if(c != null) {
					pos = pos.add(c.pos);
					rot = rot.add(c.rotation);
				}
			} else if(pv instanceof DirectPartValues) {
				rot = rot.add(((DirectPartValues)pv).getRotation());
			}
			String name = part.getName();
			if(me.duplicated) {
				name += "_dup";
			}
			GroupProperties gp = new GroupProperties();
			gp.name = name;
			gp.origin = JsVec3.make(0, 24, 0);
			gp.rotation = JsVec3.make(-rot.x, -rot.y, rot.z);
			Group gr = new Group(gp).init();
			gr.disableVanillaAnim = me.disableVanillaAnim;
			gp = new GroupProperties();
			gp.origin = JsVec3.make(-pos.x, 24 - pos.y, pos.z);
			gr.extend(gp);
			me2group.put(me, gr);

			TextureSheetType tst = part instanceof RootModelType ? RootGroups.getGroup((RootModelType) part).getTexSheet((RootModelType) part) : TextureSheetType.SKIN;

			if(!me.hidden) {
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

				cube.applyTexture(textures.get(tst), true);
				UVMul uvm = uvMul.get(tst);
				if(uvm.needsPF())boxToPFUV(cube, uvm);
			}

			importChildren(me.children, gr, tst);
		}

		ProjectData pd = Project.getData();

		if(!editor.animations.isEmpty()) {
			if(importAnims)editor.animations.forEach(this::convertAnim);
			else new AnimEnc().write(pd.animations);
		}

		pd.scalingOpt.putAll(editor.scalingElem.scaling);
		pd.renderPos = editor.scalingElem.pos;
		pd.renderRot = editor.scalingElem.rotation;
		pd.renderScl = editor.scalingElem.scale;

		pd.leftHandPos = editor.leftHandPos;
		pd.rightHandPos = editor.rightHandPos;
		pd.modelId = editor.modelId;
		pd.removeArmorOffset = editor.removeArmorOffset;
		Project.hideHeadIfSkull = editor.hideHeadIfSkull;
		Project.removeBedOffset = editor.removeBedOffset;
		Project.invisGlow = editor.enableInvisGlow;

		for(TextureSheetType tx : TextureSheetType.VALUES) {
			ETextures eTex = editor.textures.get(tx);
			if(eTex != null) {
				if(eTex.isEditable()) {
					Map<String, Object> t = new HashMap<>();
					JsonMap map = new JsonMapImpl(t);
					JsonList l = map.putList("anim");
					eTex.animatedTexs.forEach(v -> v.save(l.addMap()));
					if(l.size() != 0) {
						pd.animTex.put(tx, t);
					}
				}
			}
		}

		pd.description = editor.description;

		pd.flush();

		postConvert.forEach(Runnable::run);

		ProjectGenerator.updateAll();
	}

	private class AnimEnc extends AnimationsLoaderV1 {

		public void write(Map<String, Object> an) {
			Map<String, Object> l = new HashMap<>();
			for (EditorAnim e : editor.animations) {
				Map<String, Object> d = new HashMap<>();
				l.put(e.filename, d);
				JsonMap data = new JsonMapImpl(d);
				writeAnimation(e, data);
			}
			an.put("anims", l);
			if(editor.animEnc != null) {
				an.put("free", editor.animEnc.freeLayers.stream().map(v -> v.getLowerName()).collect(Collectors.toList()));
				an.put("def", editor.animEnc.defaultLayerValue.entrySet().stream().map(e -> Pair.of(e.getKey().getLowerName(), e.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
			}
		}

		@Override
		protected void writePartRef(Map<String, Object> map, ModelElement me) {
			map.put("uuid", me2group.get(me).uuid);
		}
	}

	private void convertAnim(EditorAnim ea) {
		Animation a = new Animation().add(false);
		Animation.selected = a;
		a.name = ea.toString();
		if (ea.type.isStaged()) {
			a.name = ea.displayName;
			a.loop = "once";
		} else if (ea.type.isLayer())a.loop = "loop";
		else a.loop = ea.loop ? "loop" : "once";
		a.type = Animation.makeType(ea.type, ea.pose);
		a.additive = ea.add;
		a.commandCtrl = ea.command;
		a.layerCtrl = ea.layerControlled;
		a.setPriority(ea.priority);
		a.setOrder(ea.order);
		a.group = ea.group;
		a.setLayerDefault(ea.layerDefault);
		a.isProperty = ea.isProperty;
		a.length = ea.duration / 1000f;
		Map<ModelElement, BoneAnimator> an = new HashMap<>();
		me2group.forEach((e, g) -> {
			BoneAnimator be = new BoneAnimator(g.uuid, a);
			an.put(e, be);
			a.animators.set(g.uuid, be);
		});
		float ft = (ea.getFrames().size() < 2 ? ea.duration : ea.duration / (float) ea.getFrames().size()) / 1000f;
		List<AnimFrame> fr = ea.getFrames();
		List<ModelElement> components = ea.getComponentsFiltered();
		float[][][] data = new float[fr.size()][components.size()][InterpolatorChannel.VALUES.length];
		for (int component = 0; component < components.size(); component++) {
			for (InterpolatorChannel channel : InterpolatorChannel.VALUES) {
				float[] vals = AnimFrame.toArray(ea, components.get(component), channel);
				DoubleUnaryOperator setup = channel.createInterpolatorSetup();

				for (int i = 0; i < vals.length; i++) {
					data[i][component][channel.ordinal()] = (float) setup.applyAsDouble(vals[i]);
				}
			}
		}
		for (int i = 0; i < data.length; i++) {
			AnimFrame f = fr.get(i);
			float[][] compDt = data[i];
			for (int component = 0; component < components.size(); component++) {
				float[] v = compDt[component];
				ModelElement me = components.get(component);

				Vec3f addPos = me.pos;
				Vec3f addRot = me.rotation;
				if(!ea.add && me.type == ElementType.ROOT_PART) {
					VanillaModelPart part = (VanillaModelPart) me.typeData;
					PartValues pv = DirectParts.getPartOverrides(part, editor.skinType);
					if(pv instanceof DirectPartValues) {
						addRot = addRot.add(((DirectPartValues)pv).getRotation());
					}
					addPos = addPos.add(pv.getPos());
				}
				Vec3f pos = new Vec3f(
						v[InterpolatorChannel.POS_X.ordinal()],
						v[InterpolatorChannel.POS_Y.ordinal()],
						v[InterpolatorChannel.POS_Z.ordinal()]
						);
				Vec3f rot = new Vec3f(
						(float) Math.toDegrees(v[InterpolatorChannel.ROT_X.ordinal()]),
						(float) Math.toDegrees(v[InterpolatorChannel.ROT_Y.ordinal()]),
						(float) Math.toDegrees(v[InterpolatorChannel.ROT_Z.ordinal()])
						);
				Vec3f scl = new Vec3f(
						v[InterpolatorChannel.SCALE_X.ordinal()],
						v[InterpolatorChannel.SCALE_Y.ordinal()],
						v[InterpolatorChannel.SCALE_Z.ordinal()]
						);
				if(!ea.add) {
					pos = pos.sub(addPos);
					rot = rot.sub(addRot);
				}
				pos.y *= -1;

				BoneAnimator be = an.get(me);
				Keyframe kp = be.createKeyframe(ft * i, "position");
				Keyframe kr = be.createKeyframe(ft * i, "rotation");
				Keyframe ks = me.type == ElementType.ROOT_PART ? null : be.createKeyframe(ft * i, "scale");
				Keyframe kv = be.createKeyframe(ft * i, CPMCodec.VISIBILITY);
				Keyframe kc = be.createKeyframe(ft * i, CPMCodec.COLOR);
				KeyframeDataPoint dp = kp.data_points.getAt(0);
				KeyframeDataPoint dr = kr.data_points.getAt(0);
				KeyframeDataPoint ds = me.type == ElementType.ROOT_PART ? null : ks.data_points.getAt(0);
				KeyframeDataPoint dv = kv.data_points.getAt(0);
				KeyframeDataPoint dc = kc.data_points.getAt(0);

				dp.set(pos);
				dr.set(rot);
				if(me.type != ElementType.ROOT_PART)ds.set(scl);
				dv.visible = f.getVisible(me);
				dc.x = (int) (v[InterpolatorChannel.COLOR_R.ordinal()] * 255);
				dc.y = (int) (v[InterpolatorChannel.COLOR_G.ordinal()] * 255);
				dc.z = (int) (v[InterpolatorChannel.COLOR_B.ordinal()] * 255);
			}
		}

		Animation.selected = null;
	}

	private ModelElement findElementById(Editor editor, VanillaModelPart copyFrom) {
		for(ModelElement me : editor.elements) {
			if(me.typeData == copyFrom && !me.duplicated)return me;
		}
		return null;
	}

	private void importChildren(List<ModelElement> children, Group parent, TextureSheetType tst) {
		UVMul uvm = uvMul.get(tst);
		for (ModelElement me : children) {
			Group gr;
			Cube cube;

			GroupProperties gp = new GroupProperties();
			int d = groupNames.merge(me.name, 1, Integer::sum) - 1;
			gp.name = me.name + (d > 0 ? d : "");
			gp.origin = JsVec3.make(0, 24, 0);
			gr = new Group(gp).init();
			gp = new GroupProperties();
			gp.origin = JsVec3.make(-me.pos.x, 24 - me.pos.y, me.pos.z);
			gr.extend(gp);
			gr.origin.V3_add(parent.origin);
			gr.origin.y -= 24;
			gr.addTo(parent);
			gp = new GroupProperties();
			gp.rotation = JsVec3.make(-me.rotation.x, -me.rotation.y, me.rotation.z);
			gr.extend(gp);
			gr.visibility = me.showInEditor;
			gr.hidden = me.hidden;
			me2group.put(me, gr);
			if(me.copyTransform != null) {
				postConvert.add(() -> {
					Map<String, Object> s = me.copyTransform.toMap();
					Group g = me2group.get(me.copyTransform.from);
					if(g != null) {
						s.remove("storeID");
						s.put("uuid", g.uuid);
						gr.copyTransform = JsonUtil.toJson(s);
					}
				});
			}
			GroupData gd = gr.getData();
			if(me.itemRenderer != null) {
				gd.setItemRenderer(me.itemRenderer.slot.name().toLowerCase());
			}
			gd.flush();

			Cube.CubeProperties c = new Cube.CubeProperties();
			c.name = me.name;
			Vec3f o = me.offset;
			Vec3f s = new Vec3f(me.size.x * me.meshScale.x, me.size.y * me.meshScale.y, me.size.z * me.meshScale.z);
			c.from = JsVec3.make(gr.origin.x - o.x - s.x, gr.origin.y - o.y - s.y, gr.origin.z + o.z);
			c.inflate = me.mcScale;
			c.mirror_uv = me.mirror;
			c.uv_offset = JsVec2.make(me.u, me.v);
			c.visibility = me.showInEditor;
			cube = new Cube(c);
			cube.glow = me.glow;
			cube.extrude = me.extrude;
			cube.setRecolor(me.recolor ? me.rgb : -1);
			c = new Cube.CubeProperties();
			c.to = JsVec3.make(cube.from.x + s.x, cube.from.y + s.y, cube.from.z + s.z);
			cube.extend(c);

			importTextureUV(me, cube, uvm, s.x < 1 || s.y < 1 || s.z < 1);

			cube.addTo(gr).init();
			cube.applyTexture(textures.get(tst), true);

			CubeData cd = cube.getData();

			if(me.extrude) {
				cd.setExtrude(true).setU(me.u).setV(me.v).setTs(me.textureSize);
			}
			cd.flush();

			importChildren(me.children, gr, tst);
		}
	}

	private void importTextureUV(ModelElement me, Cube cube, UVMul uvm, boolean forcePF) {
		if(!me.texture)return;
		if(me.faceUV != null || forcePF || uvm.needsPF() || me.textureSize > 1 || me.singleTex || me.extrude || me.meshScale.x != 1 || me.meshScale.y != 1 || me.meshScale.z != 1) {
			cube.box_uv = false;
			cube.autouv = 0;
			if(me.faceUV != null) {
				for (Direction d : Direction.VALUES) {
					Face f = me.faceUV.faces.get(d);
					CubeFace cf = cube.faces.getFace(d);
					if(f != null) {
						cf.uv = makeFaceUV(f.sx, f.sy, f.ex, f.ey, uvm);
						if(d == Direction.UP || d == Direction.DOWN)
							cf.rotation = (rotToInt(f.rotation) + 180) % 360;
						else
							cf.rotation = rotToInt(f.rotation);
					} else {
						cf.uv = makeFaceUV(0, 0, 0, 0, uvm);
					}
				}
			} else if(me.singleTex || me.extrude) {
				if ((me.mcScale == 0 || me.extrude) && (me.size.x == 0 || me.size.y == 0 || me.size.z == 0)) {
					float texU = me.u * me.textureSize;
					float texV = me.v * me.textureSize;
					if (me.size.x == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.z * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.y * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.WEST) {
								face.uv = makeFaceUV(texU, texV, tu, tv, uvm);
							} else if (d == Direction.EAST) {
								face.uv = makeFaceUV(tu, texV, texU, tv, uvm);
							} else {
								face.uv = makeFaceUV(0, 0, 0, 0, uvm);
							}
						}
					} else if (me.size.y == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.x * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.z * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.UP) {
								face.uv = makeFaceUV(tu, texV, texU, tv, uvm);
							} else if (d == Direction.DOWN) {
								face.uv = makeFaceUV(texU, texV, tu, tv, uvm);
							} else {
								face.uv = makeFaceUV(0, 0, 0, 0, uvm);
							}
						}
					} else if (me.size.z == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.x * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.y * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.NORTH) {
								face.uv = makeFaceUV(texU, texV, tu, tv, uvm);
							} else if (d == Direction.SOUTH) {
								face.uv = makeFaceUV(tu, texV, texU, tv, uvm);
							} else {
								face.uv = makeFaceUV(0, 0, 0, 0, uvm);
							}
						}
					}
				} else {
					float size = Math.max(me.size.x, Math.max(me.size.y, me.size.z));
					for (Direction d : Direction.VALUES) {
						CubeFace face = cube.faces.getFace(d);
						face.uv.sx = me.u * me.textureSize;
						face.uv.sy = me.v * me.textureSize;
						face.uv.ex = (me.u + size) * me.textureSize;
						face.uv.ey = (me.v + size) * me.textureSize;
					}
				}
				if (me.mirror) {
					for (Direction d : Direction.VALUES) {
						CubeFace face = cube.faces.getFace(d);
						float t = face.uv.sx;
						face.uv.sx = face.uv.ex;
						face.uv.ex = t;
					}
					FaceUV uv = cube.faces.east.uv;
					cube.faces.east.uv = cube.faces.west.uv;
					cube.faces.west.uv = uv;
				}
			} else {
				boxToPFUV(cube, me.textureSize, me.meshScale.x, me.meshScale.y, me.meshScale.z, uvm);
			}
		} else {
			cube.box_uv = true;
		}
	}

	private static FaceUV makeFaceUV(float sx, float sy, float ex, float ey, UVMul mul) {
		FaceUV uv = FaceUV.make(sx, sy, ex, ey);
		if(mul != null)uv.mul(mul.x, mul.y);
		return uv;
	}

	private static int rotToInt(Rot r) {
		switch (r) {
		case ROT_0: return 0;
		case ROT_180: return 180;
		case ROT_270: return 270;
		case ROT_90: return 90;
		default: throw new RuntimeException();
		}
	}

	private static void boxToPFUV(Cube cube, UVMul mul) {
		boxToPFUV(cube, 1, 1, 1, 1, mul);
	}

	public static void boxToPFUV(Cube cube, int texSc, float scX, float scY, float scZ, UVMul mul) {
		Vec3f d = cube.to.toVecF().sub(cube.from.toVecF()).mul(texSc);
		d.mul(scX == 0 ? 1 : 1f / scX, scY == 0 ? 1 : 1f / scY, scZ == 0 ? 1 : 1f / scZ);
		float dx = ProjectConvert.ceil(d.x);
		float dy = ProjectConvert.ceil(d.y);
		float dz = ProjectConvert.ceil(d.z);
		float u = ProjectConvert.floor(cube.uv_offset.x * texSc);
		float v = ProjectConvert.floor(cube.uv_offset.y * texSc);
		float f4 = u;
		float f5 = u + dz;
		float f6 = u + dz + dx;
		float f7 = u + dz + dx + dx;
		float f8 = u + dz + dx + dz;
		float f9 = u + dz + dx + dz + dx;
		float f10 = v;
		float f11 = v + dz;
		float f12 = v + dz + dy;
		cube.faces.up.uv = makeFaceUV(f6, f11, f5, f10, mul);
		cube.faces.down.uv = makeFaceUV(f7, f10, f6, f11, mul);
		cube.faces.east.uv = makeFaceUV(f4, f11, f5, f12, mul);
		cube.faces.north.uv = makeFaceUV(f5, f11, f6, f12, mul);
		cube.faces.west.uv = makeFaceUV(f6, f11, f8, f12, mul);
		cube.faces.south.uv = makeFaceUV(f8, f11, f9, f12, mul);
		if(cube.mirror_uv) {
			cube.faces.forEach(face -> {
				float f = face.uv.sx;
				face.uv.sx = face.uv.ex;
				face.uv.ex = f;
			});
			FaceUV uv = cube.faces.east.uv;
			cube.faces.east.uv = cube.faces.west.uv;
			cube.faces.west.uv = uv;
		}
		cube.autouv = 0;
	}

	public static class UVMul {
		public float x = 1, y = 1;

		private boolean needsPF() {
			return x != 1 || y != 1;
		}
	}
}
