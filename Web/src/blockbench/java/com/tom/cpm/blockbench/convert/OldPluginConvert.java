package com.tom.cpm.blockbench.convert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.blockbench.format.CubeData;
import com.tom.cpm.blockbench.format.GroupData;
import com.tom.cpm.blockbench.format.ProjectData;
import com.tom.cpm.blockbench.format.ProjectGenerator;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.util.PopupDialogs;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.JsonMapImpl;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.util.ScalingOptions;
import com.tom.cpm.web.client.resources.Resources;
import com.tom.cpm.web.client.util.I18n;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class OldPluginConvert {
	private static final String CPM_DT_GROUP = "CPM_data_DO_NOT_EDIT";
	private static final String CPM_DT_MARKER = "|CPM:";

	@SuppressWarnings("unchecked")
	public static void convert() {
		if(Arrays.stream(Group.all).anyMatch(gr -> gr.parent == Group.ROOT && gr.name.equals(CPM_DT_GROUP)) || Cube.all.asList().stream().anyMatch(c -> c.name.contains(CPM_DT_MARKER))) {
			try {
				new ArrayList<>(Cube.all.asList()).stream().filter(c -> c.name.contains(CPM_DT_MARKER)).forEach(c -> {
					int ind = c.name.lastIndexOf(CPM_DT_MARKER);
					String data = c.name.substring(ind + 5);
					c.name = c.name.substring(0, ind);
					OldCubeData odt = Js.uncheckedCast(Global.JSON.parse(new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8)));
					CubeData dt = c.getData();
					if(odt.extrude != null) {
						dt.setExtrude(true).setU(odt.extrude.u).setV(odt.extrude.v).setTs(odt.extrude.ts);
					}
					if(odt.glow)c.glow = true;
					if(odt.recolor) {
						c.recolor = odt.color;
					}
					if(odt.item != null) {
						if(c.parent.children.length == 1) {
							GroupData gd = c.parent.getData();
							gd.setItemRenderer(odt.item).flush();
						} else {
							GroupProperties grp = new GroupProperties();
							grp.name = I18n.format("label.cpm.elem.item." + odt.item);
							Group gr = new Group(grp);
							gr.getData().setItemRenderer(odt.item).flush();
							gr.origin = c.parent.origin;
							gr.init().addTo(c.parent);
						}
						c.remove();
					}
					dt.flush();
				});
				Arrays.stream(Group.all).filter(gr -> gr.parent == Group.ROOT && gr.name.equals(CPM_DT_GROUP)).findFirst().ifPresent(dtGroup -> {
					List<FileData> files = new ArrayList<>();
					Map<String, String> storeIDtoUUID = new HashMap<>();
					ProjectData pd = Project.getData();
					dtGroup.children.asList().stream().forEach(c -> {
						if(c instanceof Cube) {
							String[] sp = ((Cube)c).name.split("_");
							OldProjectDataExt extDt = Js.uncheckedCast(Global.JSON.parse(new String(Base64.getDecoder().decode(sp[sp.length - 1]), StandardCharsets.UTF_8)));
							if(extDt.export.mode.equals("ids")) {
								extDt.storeId.forEach(k -> storeIDtoUUID.put(k, extDt.storeId.get(k)));
							} else if(extDt.export.mode.equals("store")) {
								JsonMap map = new JsonMapImpl((Map<String, Object>) convertObj(extDt.storeObject));
								files.add(new FileData(extDt.export.file, map));
							} else if(extDt.export.mode.equals("conf")) {
								JsonMap map = new JsonMapImpl((Map<String, Object>) convertObj(extDt.storeObject));
								if(map.containsKey("hideHeadIfSkull"))Project.hideHeadIfSkull = map.getBoolean("hideHeadIfSkull");
								if(map.containsKey("removeArmorOffset"))pd.removeArmorOffset = map.getBoolean("removeArmorOffset");
								if(map.containsKey("textures")) {
									JsonMap texDt = map.getMap("textures");
									for(TextureSheetType tex : TextureSheetType.VALUES) {
										String name = tex.name().toLowerCase(Locale.ROOT);
										if(texDt.containsKey(name)) {
											pd.animTex.put(tex, texDt.getMap(name).asMap());
										}
									}
								}
								if(map.containsKey("scaling"))pd.scalingOpt.put(ScalingOptions.ENTITY, map.getFloat("scaling"));
								if(map.containsKey("scalingEx")) {
									JsonMap scaling = map.getMap("scalingEx");
									for(ScalingOptions opt : ScalingOptions.VALUES) {
										if(opt == ScalingOptions.ENTITY)continue;
										float v = scaling.getFloat(opt.name().toLowerCase(Locale.ROOT), 0);
										if(v != 0)
											pd.scalingOpt.put(opt, v);
									}
									pd.renderPos = new Vec3f(scaling.getMap("render_position"), new Vec3f());
									pd.renderRot = new Vec3f(scaling.getMap("render_rotation"), new Vec3f());
									pd.renderScl = new Vec3f(scaling.getMap("render_scale"), new Vec3f());
								}
							}
						}
					});
					DomGlobal.console.log(storeIDtoUUID.toString());
					files.forEach(fd -> {
						if(fd.name.equals("anim_enc.json")) {
							pd.animations.put("free", fd.data.getList("freeLayers").stream().collect(Collectors.toList()));
							pd.animations.put("def", fd.data.getMap("defaultValues").asMap());
						} else if(fd.name.equals("description.json")) {
							JsonMap data = fd.data;
							pd.description = new ModelDescription();
							pd.description.name = data.getString("name");
							pd.description.desc = data.getString("desc");
							JsonMap map = data.getMap("cam");
							pd.description.camera.camDist = map.getFloat("zoom");
							pd.description.camera.look = new Vec3f(map.getMap("look"), pd.description.camera.look);
							pd.description.camera.position = new Vec3f(map.getMap("pos"), pd.description.camera.position);
							pd.description.copyProtection = CopyProtection.lookup(map.getString("copyProt", "normal"));
						} else if(fd.name.startsWith("animations/")) {
							Map<String, Object> l = (Map<String, Object>) pd.animations.computeIfAbsent("anims", k -> new HashMap<>());
							String fname = fd.name.substring(11);
							JsonList frames = fd.data.getList("frames");
							frames.forEachMap(d -> {
								JsonList c = d.getList("components");
								c.forEachMap(map -> {
									Number n = map.getLong("storeID");
									String sid = String.valueOf(n.doubleValue());
									String uuid = storeIDtoUUID.get(sid);
									if(n.intValue() >= 0 && n.intValue() < PlayerModelParts.VALUES.length) {
										PlayerModelParts part = PlayerModelParts.VALUES[n.intValue()];
										for(Group g : Group.all) {
											if (g.parent == Group.ROOT && g.name.equalsIgnoreCase(part.getName())) {
												uuid = g.uuid;
											}
										}
									}
									DomGlobal.console.log(sid, uuid);
									if(uuid != null) {
										map.asMap().remove("storeID");
										map.put("uuid", uuid);
									}
								});
							});
							l.put(fname, fd.data.asMap());
						}
					});

					pd.flush();
					dtGroup.remove();
				});
				for (int i = 0;i<Texture.all.length;i++) {
					Texture tex = Texture.all.getAt(i);

					for(TextureSheetType s : TextureSheetType.VALUES) {
						if(!s.editable && (tex.name.equalsIgnoreCase(s.name()) || tex.name.equalsIgnoreCase(s.name() + ".png"))) {
							Vec2i sz = s.getDefSize();
							Cube.all.forEach(c -> {
								c.faces.forEach(cf -> {
									if(cf.texture == tex.uuid) {
										if(c.box_uv)c.box_uv = false;
										cf.uv.mul(Project.texture_width / (float) sz.x, Project.texture_height / (float) sz.y);
									}
								});
							});

							tex.fromDataURL("data:image/png;base64," + Resources.getResource("assets/cpm/textures/template/" + s.name().toLowerCase(Locale.ROOT) + ".png"));
							break;
						}
					}
				}

				ProjectGenerator.updateAll();
				PopupDialogs.displayMessage(I18n.formatBr("bb-label.convertedFromOld"));
			} catch (Exception e) {
				ProjectGenerator.updateAll();
				PopupDialogs.displayError(I18n.get("bb-label.error.oldConvertFail"), e);
			}
		}
	}

	private static Object convertObj(Object v) {
		if(JsArray.isArray(v)) {
			JsArrayE<Object> ar = Js.uncheckedCast(v);
			List<Object> l = new ArrayList<>();
			ar.forEach(o -> l.add(convertObj(o)));
			return l;
		} else if(Js.typeof(v).equals("object")) {
			Map<String, Object> map = new HashMap<>();
			JsPropertyMap<Object> o = Js.uncheckedCast(v);
			o.forEach(k -> map.put(k, convertObj(o.get(k))));
			return map;
		} else if(Js.typeof(v).equals("string") || Js.typeof(v).equals("number") || Js.typeof(v).equals("boolean")) {
			return v;
		}
		DomGlobal.console.error("Unknown type: " + Js.typeof(v));
		return null;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	private static class OldCubeData {
		public OldCubeDataExtrude extrude;
		public boolean glow;
		public boolean recolor;
		public int color;
		public String item;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	private static class OldCubeDataExtrude {
		public int u, v, ts;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	private static class OldProjectDataExt {
		private OldProjectDataExport export;

		@JsProperty(name = "data")
		private JsPropertyMap<String> storeId;

		@JsProperty(name = "data")
		private JsPropertyMap<Object> storeObject;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	private static class OldProjectDataExport {
		private String mode, file;
	}

	private static class FileData {
		private JsonMap data;
		private String name;

		public FileData(String name, JsonMap data) {
			this.data = data;
			this.name = name;
		}
	}
}
