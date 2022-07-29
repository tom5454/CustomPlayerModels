package com.tom.cpm.blockbench;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeFace;
import com.tom.cpm.blockbench.proxy.Cube.FaceUV;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec2;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.RootGroups;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;

import elemental2.core.Uint8ClampedArray;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.ImageData;
import jsinterop.base.Js;

public class BlockbenchImport {
	private Map<TextureSheetType, Texture> textures = new HashMap<>();
	private final Editor editor;

	public BlockbenchImport(Editor editor) {
		this.editor = editor;
	}

	public void doImport() {
		Vec2i tex = editor.textures.get(TextureSheetType.SKIN).provider.size;
		Project.texture_width = tex.x;
		Project.texture_height = tex.y;
		Project.box_uv = true;

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
		});

		for (ModelElement me : editor.elements) {
			VanillaModelPart part = (VanillaModelPart) me.typeData;
			PartValues pv = part.getDefaultSize(editor.skinType);
			Vec3f pos = pv.getPos().add(me.pos);
			Vec3f rot = me.rotation;//TODO validate position
			if(part.getCopyFrom() != null) {
				ModelElement c = findElementById(editor, part.getCopyFrom());
				if(c != null) {
					pos = pos.add(c.pos);
					rot = rot.add(c.rotation);
				}
			}
			String name = part.getName();
			if(me.duplicated) {
				name += "_dup";
			}
			GroupProperties gp = new GroupProperties();
			gp.name = name;
			gp.origin = JsVec3.make(0, 24, 0);
			gp.rotation = JsVec3.make(rot);
			Group gr = new Group(gp).init();
			gp = new GroupProperties();
			gp.origin = JsVec3.make(-pos.x, 24 - pos.y, pos.z);
			gr.extend(gp);

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
			}

			importChildren(me.children, gr, tst);
		}

		ProjectGenerator.updateAll();
	}

	private ModelElement findElementById(Editor editor, VanillaModelPart copyFrom) {
		for(ModelElement me : editor.elements) {
			if(me.typeData == copyFrom && !me.duplicated)return me;
		}
		return null;
	}

	private void importChildren(List<ModelElement> children, Group parent, TextureSheetType tst) {
		for (ModelElement me : children) {
			Group gr;
			Cube cube;
			{
				GroupProperties gp = new GroupProperties();
				gp.name = me.name;
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

				Cube.CubeProperties c = new Cube.CubeProperties();
				c.name = me.name;
				Vec3f o = me.offset;
				Vec3f s = new Vec3f(me.size.x * me.scale.x, me.size.y * me.scale.y, me.size.z * me.scale.z);
				c.from = JsVec3.make(gr.origin.x - o.x - s.x, gr.origin.y - o.y - s.y, gr.origin.z + o.z);
				c.inflate = me.mcScale;
				c.mirror_uv = me.mirror;
				c.uv_offset = JsVec2.make(me.u, me.v);
				c.visibility = me.showInEditor;
				cube = new Cube(c);
				c = new Cube.CubeProperties();
				c.to = JsVec3.make(cube.from.x + s.x, cube.from.y + s.y, cube.from.z + s.z);
				cube.extend(c);
			}

			importTextureUV(me, cube);

			cube.addTo(gr).init();
			cube.applyTexture(textures.get(tst), true);

			importChildren(me.children, gr, tst);
		}
	}

	private void importTextureUV(ModelElement me, Cube cube) {
		if(me.faceUV != null || me.textureSize > 1 || me.singleTex || me.extrude || me.scale.x != 1 || me.scale.y != 1 || me.scale.z != 1) {
			if (Project.box_uv) {
				Project.box_uv = false;
				Cube.all.forEach(c -> boxToPFUV(c));
			}
			cube.autouv = 0;
			if(me.faceUV != null) {
				for (Direction d : Direction.VALUES) {
					Face f = me.faceUV.faces.get(d);
					CubeFace cf = cube.faces.getFace(d);
					if(f != null) {
						cf.uv = FaceUV.make(f.sx, f.sy, f.ex, f.ey);
						if(d == Direction.UP || d == Direction.DOWN)
							cf.rotation = (rotToInt(f.rotation) + 180) % 360;
						else
							cf.rotation = rotToInt(f.rotation);
					} else {
						cf.uv = FaceUV.make(0, 0, 0, 0);
					}
				}
			} else if(me.singleTex || me.extrude) {
				if (me.mcScale == 0 && (me.size.x == 0 || me.size.y == 0 || me.size.z == 0)) {
					float texU = me.u * me.textureSize;
					float texV = me.v * me.textureSize;
					if (me.size.x == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.z * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.y * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.WEST) {
								face.uv = FaceUV.make(texU, texV, tu, tv);
							} else if (d == Direction.EAST) {
								face.uv = FaceUV.make(tu, texV, texU, tv);
							} else {
								face.uv = FaceUV.make(0, 0, 0, 0);
							}
						}
					} else if (me.size.y == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.x * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.z * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.UP) {
								face.uv = FaceUV.make(tu, texV, texU, tv);
							} else if (d == Direction.DOWN) {
								face.uv = FaceUV.make(texU, texV, tu, tv);
							} else {
								face.uv = FaceUV.make(0, 0, 0, 0);
							}
						}
					} else if (me.size.z == 0) {
						float tu = texU + ProjectConvert.ceil(me.size.x * me.textureSize);
						float tv = texV + ProjectConvert.ceil(me.size.y * me.textureSize);
						for (Direction d : Direction.VALUES) {
							CubeFace face = cube.faces.getFace(d);
							if (d == Direction.NORTH) {
								face.uv = FaceUV.make(texU, texV, tu, tv);
							} else if (d == Direction.SOUTH) {
								face.uv = FaceUV.make(tu, texV, texU, tv);
							} else {
								face.uv = FaceUV.make(0, 0, 0, 0);
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
				boxToPFUV(cube, me.textureSize, me.scale.x, me.scale.y, me.scale.z);
			}
		} else if (!Project.box_uv) {
			boxToPFUV(cube);
		}
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

	private static void boxToPFUV(Cube cube) {
		boxToPFUV(cube, 1, 1, 1, 1);
	}

	private static void boxToPFUV(Cube cube, int texSc, float scX, float scY, float scZ) {
		Vec3f d = cube.to.toVecF().sub(cube.from.toVecF()).mul(texSc);//TODO: floor and ceil
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
		cube.faces.up.uv = FaceUV.make(f6, f11, f5, f10);
		cube.faces.down.uv = FaceUV.make(f7, f10, f6, f11);
		cube.faces.east.uv = FaceUV.make(f4, f11, f5, f12);
		cube.faces.north.uv = FaceUV.make(f5, f11, f6, f12);
		cube.faces.west.uv = FaceUV.make(f6, f11, f8, f12);
		cube.faces.south.uv = FaceUV.make(f8, f11, f9, f12);
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

}
