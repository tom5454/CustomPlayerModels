package com.tom.cpm.blockbench.format;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.util.JsonUtil;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.model.PartPosition;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.util.ScalingOptions;

public class ProjectData {
	public Map<ScalingOptions, Float> scalingOpt = new HashMap<>();
	public Vec3f renderPos, renderRot, renderScl;
	public PartPosition leftHandPos, rightHandPos;
	public String modelId;
	public boolean removeArmorOffset = true;
	public Map<TextureSheetType, Map<String, Object>> animTex = new HashMap<>();
	public ModelDescription description;
	public Map<String, Object> animations = new HashMap<>();

	public ProjectData(boolean init) {
		if(init && Project.pluginData != null && !Project.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(Project.pluginData);

			if(s.containsKey("anims")) {
				animations = s.getMap("anims").asMap();
			}

			if(s.containsKey("scaling")) {
				JsonMap scl = s.getMap("scaling");

				for(ScalingOptions opt : ScalingOptions.VALUES) {
					float v = scl.getFloat(opt.name().toLowerCase(Locale.ROOT), 0);
					if(v != 0)
						scalingOpt.put(opt, v);
				}

				renderPos = new Vec3f(scl.getMap("render_position"), new Vec3f());
				renderRot = new Vec3f(scl.getMap("render_rotation"), new Vec3f());
				renderScl = new Vec3f(scl.getMap("render_scale"), new Vec3f());
			}

			leftHandPos = loadPartPos(s, "left_hand");
			rightHandPos = loadPartPos(s, "right_hand");
			if(s.containsKey("modelId")) {
				modelId = s.getString("modelId");
			}

			if(s.containsKey("tex")) {
				JsonMap tex = s.getMap("tex");
				for(TextureSheetType tx : TextureSheetType.VALUES) {
					if(tx.editable) {
						String name = tx.name().toLowerCase(Locale.ROOT);
						if (tex.containsKey(name)) {
							animTex.put(tx, tex.getMap(name).asMap());
						}
					}
				}
			}

			if(s.containsKey("desc")) {
				JsonMap desc = s.getMap("desc");
				description = new ModelDescription();
				description.name = desc.getString("name");
				description.desc = desc.getString("desc");
				description.camera.camDist = desc.getFloat("zoom");
				description.camera.look = new Vec3f(desc.getMap("look"), description.camera.look);
				description.camera.position = new Vec3f(desc.getMap("pos"), description.camera.position);
				description.copyProtection = CopyProtection.lookup(desc.getString("copyProt", "normal"));
			}
		}
	}

	public void flush() {
		Map<String, Object> pluginDt = new HashMap<>();

		if(!animations.isEmpty())pluginDt.put("anims", animations);
		{
			Map<String, Object> scl = new HashMap<>();
			for(ScalingOptions opt : ScalingOptions.VALUES) {
				if(scalingOpt.containsKey(opt))
					scl.put(opt.name().toLowerCase(Locale.ROOT), scalingOpt.get(opt));
			}
			if(renderPos != null)scl.put("render_position", renderPos.toMap());
			if(renderRot != null)scl.put("render_rotation", renderRot.toMap());
			if(renderScl != null)scl.put("render_scale", renderScl.toMap());

			if(!scl.isEmpty())pluginDt.put("scaling", scl);
		}
		if(leftHandPos != null)putPartPos(leftHandPos, pluginDt, "left_hand");
		if(rightHandPos != null)putPartPos(rightHandPos, pluginDt, "right_hand");
		if(modelId != null) {
			pluginDt.put("modelId", modelId);
		}
		if(!removeArmorOffset)
			pluginDt.put("removeArmorOffset", false);

		{
			boolean hasTex = false;
			Map<String, Object> ant = new HashMap<>();
			for(TextureSheetType tx : TextureSheetType.VALUES) {
				if(tx.editable) {
					String name = tx.name().toLowerCase(Locale.ROOT);
					if(animTex.containsKey(tx)) {
						ant.put(name, animTex.get(tx));
					}
				}
			}
			if(hasTex)pluginDt.put("tex", ant);
		}

		if(this.description != null) {
			Map<String, Object> desc = new HashMap<>();
			desc.put("name", this.description.name);
			desc.put("desc", this.description.desc);
			desc.put("zoom", this.description.camera.camDist);
			desc.put("look", this.description.camera.look.toMap());
			desc.put("pos", this.description.camera.position.toMap());
			desc.put("copyProt", this.description.copyProtection.name().toLowerCase(Locale.ROOT));
			pluginDt.put("desc", desc);
		}

		if(!pluginDt.isEmpty())Project.pluginData = JsonUtil.toJson(pluginDt);
		else Project.pluginData = null;
	}

	private static void putPartPos(PartPosition pos, Map<String, Object> fpHand, String name) {
		Map<String, Object> map = new HashMap<>();
		fpHand.put(name, map);
		map.put("position", pos.getRPos().toMap());
		map.put("rotation", pos.getRRotation().toMap3());
		map.put("scale", pos.getRScale().toMap());
	}

	private static PartPosition loadPartPos(JsonMap fpHand, String name) {
		PartPosition p = new PartPosition();
		JsonMap map = fpHand.getMap(name);
		if(map != null) {
			Vec3f pos = new Vec3f(map.getMap("position"), new Vec3f());
			Vec3f rotation = new Vec3f(map.getMap("rotation"), new Vec3f());
			Vec3f scale = new Vec3f(map.getMap("scale"), new Vec3f());
			p.setRenderScale(pos, new Rotation(rotation, true), scale);
		}
		return p;
	}
}
