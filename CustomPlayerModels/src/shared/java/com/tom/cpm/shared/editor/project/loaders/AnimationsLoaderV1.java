package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.AnimationType;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;

public class AnimationsLoaderV1 implements ProjectPartLoader {

	@Override
	public String getId() {
		return "anims";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public void load(Editor editor, IProject project) throws IOException {
		List<String> anims = project.listEntires("animations");
		if(anims != null) {
			for (String anim : anims) {
				JsonMap data = project.getJson("animations/" + anim);
				IPose pose = null;
				String displayName = data.getString("name", "Unnamed");
				AnimationType type = null;
				String[] sp = anim.split("_", 2);
				if(sp[0].equals("v")) {
					String poseName = sp[1].endsWith(".json") ? sp[1].substring(0, sp[1].length() - 5) : sp[1];
					for(VanillaPose p : VanillaPose.VALUES) {
						if(poseName.startsWith(p.name().toLowerCase())) {
							pose = p;
							type = AnimationType.POSE;
							break;
						}
					}
				} else if(sp[0].equals("c")) {
					pose = new CustomPose(displayName);
					type = AnimationType.POSE;
				} else if(sp[0].equals("g")) {
					type = AnimationType.GESTURE;
				}
				if(type == null)continue;
				EditorAnim e = new EditorAnim(editor, anim, type, false);
				e.displayName = displayName;
				e.pose = pose;
				e.add = data.getBoolean("additive");
				editor.animations.add(e);
				e.duration = data.getInt("duration");
				e.priority = data.getInt("priority", 0);
				e.loop = (data.getBoolean("loop", false));
				JsonList frames = data.getList("frames");
				frames.forEachMap(e::loadFrame);
			}
		}
		project.jsonIfExists("anim_enc.json", data -> loadEnc(editor, data));
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		project.clearFolder("animations");
		for (EditorAnim e : editor.animations) {
			JsonMap data = project.getJson("animations/" + e.filename);
			data.put("additive", e.add);
			data.put("name", e.displayName);
			if(e.pose instanceof CustomPose)data.put("name", ((CustomPose)e.pose).getName());
			data.put("duration", e.duration);
			data.put("priority", e.priority);
			data.put("loop", e.loop);
			data.put("frames", e.writeFrames());
		}
		saveEnc(editor, project);
	}

	protected void loadEnc(Editor editor, JsonMap data) {
		editor.animEnc = new AnimationEncodingData();
		data.getList("freeLayers").forEach(v -> editor.animEnc.freeLayers.add(PlayerSkinLayer.getLayer((String) v)));
		data.getMap("defaultValues").forEach((k, v) -> editor.animEnc.defaultLayerValue.put(PlayerSkinLayer.getLayer(k), (Boolean) v));
	}

	protected void saveEnc(Editor editor, ProjectWriter project) {
		if(editor.animEnc != null) {
			JsonMap data = project.getJson("anim_enc.json");
			data.put("freeLayers", editor.animEnc.freeLayers.stream().map(l -> l.getLowerName()).collect(Collectors.toList()));
			data.put("defaultValues", editor.animEnc.defaultLayerValue.entrySet().stream().map(e -> Pair.of(e.getKey().getLowerName(), e.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
		}
	}
}
