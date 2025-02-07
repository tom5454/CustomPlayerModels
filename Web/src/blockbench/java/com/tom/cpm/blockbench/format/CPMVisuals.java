package com.tom.cpm.blockbench.format;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.tom.cpm.blockbench.PluginStart;
import com.tom.cpm.blockbench.proxy.Animation;
import com.tom.cpm.blockbench.proxy.Animation.GeneralAnimator;
import com.tom.cpm.blockbench.proxy.BoneAnimator;
import com.tom.cpm.blockbench.proxy.Canvas;
import com.tom.cpm.blockbench.proxy.Codecs;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Keyframe;
import com.tom.cpm.blockbench.proxy.KeyframeDataPoint;
import com.tom.cpm.blockbench.proxy.Modes;
import com.tom.cpm.blockbench.proxy.NodePreviewController.NodePreviewEvent;
import com.tom.cpm.blockbench.proxy.OutlinerElement;
import com.tom.cpm.blockbench.proxy.OutlinerNode;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Timeline;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.blockbench.proxy.jq.JQueryNode;
import com.tom.cpm.blockbench.proxy.jq.JQueryNode.SpectrumInit;
import com.tom.cpm.blockbench.proxy.three.MeshBasicMaterial;
import com.tom.cpm.blockbench.proxy.three.ThreeColor;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;

public class CPMVisuals {

	public static void init() {
		PluginStart.addEventListener(Codecs.project, "parsed", __ -> {
			if (Project.format == CPMCodec.format) {
				Cube.all.forEach(CPMVisuals::updateCubeMaterial);
			}
		});

		PluginStart.addEventListener(Cube.preview_controller, "update_geometry update_faces", dt -> {
			if (Project.format == CPMCodec.format) {
				OutlinerElement elem = NodePreviewEvent.getElement(dt);
				if(elem instanceof Cube) {
					Cube c = (Cube) elem;
					updateCubeMaterial(c);
				}
			}
		});

		PluginStart.addEventListener("update_keyframe_selection", __ -> updateKeyframeSelection());

		PluginStart.addEventListener("display_animation_frame", __ -> displayAnimation());
	}

	private static void updateCubeMaterial(Cube c) {
		if (c.getRecolor() != -1) {
			if (c.mesh.material.isColor()) {
				((MeshBasicMaterial) c.mesh.material).color = ThreeColor.make(c.getRecolor());
			} else {
				c.mesh.material = c.mesh.material.makeRecolor(c.getRecolor(), c.isColorCube());
			}
		} else if (c.mesh.material.isColor()) {
			c.mesh.material = c.mesh.material.getOriginal();
		}
		c.mesh.material = c.glow ? c.mesh.material.getGlow() : c.mesh.material.getNormal();
	}

	private static void setCubeColor(Cube c, float r, float g, float b) {
		if (c.mesh.material.isColor())
			((MeshBasicMaterial) c.mesh.material).color = ThreeColor.make(r, g, b);
	}

	private static void setCubeColor(Cube c, int color) {
		if (c.mesh.material.isColor())
			((MeshBasicMaterial) c.mesh.material).color = ThreeColor.make(color);
	}

	private static void displayAnimation() {
		if (Project.format == CPMCodec.format) {
			if (Modes.animate != null) {
				Map<String, BoneAnimator> animsV = new HashMap<>();
				Map<String, BoneAnimator> animsC = new HashMap<>();
				Arrays.stream(Animation.all).
				filter(a -> a.playing && !(a.loop.equals("once") && Timeline.time > a.length && a.length > 0)).
				sorted(Comparator.comparingInt(Animation::getPriority)).forEach(a -> {
					a.animators.forEach(k -> {
						GeneralAnimator ga = a.animators.get(k);
						if(ga instanceof BoneAnimator) {
							BoneAnimator ba = (BoneAnimator) ga;
							if(ba.hasVisible())animsV.put(k, ba);
							if(ba.hasColor())animsC.put(k, ba);
						}
					});
				});

				for (int i = 0; i < Group.all.length; i++) {
					Group g = Group.all[i];
					if (g.parent == Group.ROOT)
						g.currentVisible = true;
					else
						g.currentVisible = Animation.interpolateVisible(animsV.get(g.uuid), Timeline.time, !g.hidden);

					BoneAnimator cAn = animsC.get(g.uuid);
					if (cAn != null) {
						JsVec3 color = Animation.interpolate(cAn.color, Timeline.time);
						for (OutlinerNode e : g.children.array()) {
							if (e instanceof Cube) {
								Cube c = (Cube) e;
								if (c.canRecolor()) {
									setCubeColor(c, color.x / 255f, color.y / 255f, color.z / 255f);
								}
							}
						}
					}

					/*if (g.copyTransform != null && !g.copyTransform.isEmpty()) {//TODO
						JsonMap s = JsonUtil.fromJson(g.copyTransform);
						Group from = Group.uuids.get((String) s.get("uuid"));
						if (from != null) {
							Vec3f addPos = new Vec3f();
							Vec3f addRot = new Vec3f();
							if(from.parent == Group.ROOT) {
								VanillaModelPart part = from.parent.getRootType();
								if(part != null) {

								}
							}
							if(s.getBoolean("px", false))g.mesh.position.x = from.mesh.position.x + addPos.x;
							if(s.getBoolean("py", false))g.mesh.position.y = from.mesh.position.y - addPos.y;
							if(s.getBoolean("pz", false))g.mesh.position.z = from.mesh.position.z - addPos.z;
							if(s.getBoolean("rx", false))g.mesh.rotation.x = from.mesh.rotation.x + (float) Math.toRadians(addRot.x);
							if(s.getBoolean("ry", false))g.mesh.rotation.y = from.mesh.rotation.y + (float) Math.toRadians(addRot.y);
							if(s.getBoolean("rz", false))g.mesh.rotation.z = from.mesh.rotation.z - (float) Math.toRadians(addRot.z);
							if(s.getBoolean("sx", false))g.mesh.scale.x = from.mesh.scale.x;
							if(s.getBoolean("sy", false))g.mesh.scale.y = from.mesh.scale.y;
							if(s.getBoolean("sz", false))g.mesh.scale.z = from.mesh.scale.z;
							if(s.getBoolean("cv", false))g.currentVisible = from.currentVisible;
						}
					}*/
				}

				for (int i = 0; i < Cube.all.length; i++) {
					Cube c = Cube.all.getAt(i);
					if(!c.animatorInit) {
						c.animatorInit = true;
						c.defaultVisible = c.visibility;
					}
				}

				for (int i = 0; i < Group.all.length; i++) {
					Group g = Group.all[i];
					if(g.parent != Group.ROOT)continue;
					g.applyVisible(true);
				}
			} else {
				for (int i = 0; i < Group.all.length; i++) {
					Group g = Group.all[i];
					if(g.animatorInit) {
						g.animatorInit = false;
						g.visibility = g.defaultVisible;
					}
				}

				for (int i = 0; i < Cube.all.length; i++) {
					Cube c = Cube.all.getAt(i);
					if(c.animatorInit) {
						c.animatorInit = false;
						c.visibility = c.defaultVisible;

						if (c.canRecolor()) {
							setCubeColor(c, c.getRecolor());
						}
					}
				}
			}
			Canvas.updateVisibility();
		}
	}

	private static void updateKeyframeSelection() {
		JQueryNode v = JQueryNode.jq("#keyframe_bar_cpm_visible");
		if(v.length != 0) {
			JQueryNode cb = v.find("#keyframe_bar_cpm_visible_cb");
			if(cb.length == 0) {
				HTMLDivElement el = (HTMLDivElement) v.getAt(0);
				HTMLInputElement inputElement = (HTMLInputElement) el.querySelector("input[type=text]");
				HTMLInputElement checkboxElement = (HTMLInputElement) DomGlobal.document.createElement("input");
				checkboxElement.type = "checkbox";
				checkboxElement.checked = "true".equalsIgnoreCase(inputElement.value);
				checkboxElement.id = "keyframe_bar_cpm_visible_cb";
				checkboxElement.addEventListener("change", event -> {
					inputElement.value = Boolean.toString(checkboxElement.checked);
					inputElement.dispatchEvent(new Event("input"));
				});
				inputElement.style.display = "none";
				el.append(checkboxElement);
			} else {
				HTMLInputElement inputElement = (HTMLInputElement) v.find("input[type=text]").getAt(0);
				HTMLInputElement checkboxElement = (HTMLInputElement) cb.getAt(0);
				checkboxElement.checked = "true".equalsIgnoreCase(inputElement.value);
			}
		}
		v = JQueryNode.jq("#keyframe_bar_cpm_color_picker_place");
		if(v.length != 0 && Keyframe.selected.length != 0) {
			KeyframeDataPoint kdp = Keyframe.selected.getAt(0).data_points.getAt(0);
			SpectrumInit si = new SpectrumInit();
			si.preferredFormat = "hex";
			si.showAlpha = false;
			si.showInput = true;
			si.color = String.format("#%02X%02X%02X", (int) kdp.x, (int) kdp.y, (int) kdp.z);
			si.change = si.hide = si.move = ci -> {
				String c = ci.toHexString();
				kdp.x = Integer.valueOf(c.substring(1, 3), 16);
				kdp.y = Integer.valueOf(c.substring(3, 5), 16);
				kdp.z = Integer.valueOf(c.substring(5, 7), 16);
			};
			v.find("input[type=text]").spectrum(si);

			JQueryNode.jq("#keyframe_bar_x > label").text("R");
			JQueryNode.jq("#keyframe_bar_y > label").text("G");
			JQueryNode.jq("#keyframe_bar_z > label").text("B");
		} else {
			JQueryNode.jq("#keyframe_bar_x > label").text("X");
			JQueryNode.jq("#keyframe_bar_y > label").text("Y");
			JQueryNode.jq("#keyframe_bar_z > label").text("Z");
		}
	}
}
