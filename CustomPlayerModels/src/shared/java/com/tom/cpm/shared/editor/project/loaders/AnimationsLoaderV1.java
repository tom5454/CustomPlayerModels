package com.tom.cpm.shared.editor.project.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimFrame.FrameData;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.project.IProject;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.ProjectPartLoader;
import com.tom.cpm.shared.editor.project.ProjectWriter;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;

public class AnimationsLoaderV1 implements ProjectPartLoader {
	private static final String LAYER_PREFIX = "$layer$";
	private static final String VALUE_LAYER_PREFIX = "$value$";
	private static final String SETUP_PREFIX = "$pre$";
	private static final String FINISH_PREFIX = "$post$";

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
				loadAnimation(editor, anim, data);
			}
		}
		project.jsonIfExists("anim_enc.json", data -> loadEnc(editor, data));
	}

	protected void loadAnimation(Editor editor, String anim, JsonMap data) {
		IPose pose = null;
		String displayName = data.getString("name", "Unnamed");
		AnimationType type = null;
		String[] sp = anim.split("_", 2);
		if(sp[0].equals("v")) {
			String poseName = sp[1].endsWith(".json") ? sp[1].substring(0, sp[1].length() - 5) : sp[1];
			for(VanillaPose p : VanillaPose.VALUES) {
				if(poseName.startsWith(p.name().toLowerCase(Locale.ROOT))) {
					pose = p;
					type = AnimationType.POSE;
					break;
				}
			}
		} else if(sp[0].equals("c")) {
			pose = new CustomPose(displayName, 0);
			type = AnimationType.CUSTOM_POSE;
		} else if(sp[0].equals("g")) {
			type = getType(displayName);
			displayName = cleanName(displayName);
		}
		if(type == null)return;
		EditorAnim e = new EditorAnim(editor, anim, type, false);
		e.displayName = displayName;
		e.pose = pose;
		e.add = data.getBoolean("additive");
		editor.animations.add(e);
		e.duration = data.getInt("duration");
		e.priority = data.getInt("priority", 0);
		e.loop = data.getBoolean("loop", false);
		e.intType = data.getEnum("interpolator", InterpolatorType.VALUES, InterpolatorType.POLY_LOOP);
		e.layerDefault = data.getFloat("layerDefault", 0f);
		e.order = data.getInt("order", 0);
		e.isProperty = data.getBoolean("isProperty", false);
		e.group = data.getString("group", null);
		e.command = data.getBoolean("command", false);
		e.layerControlled = data.getBoolean("layerControlled", true);
		e.maxValue = data.getInt("maxValue", 100);
		e.interpolateValue = data.getBoolean("interpolateVal", true);
		e.mustFinish = data.getBoolean("mustFinish", false);
		JsonList frames = data.getList("frames");
		frames.forEachMap(d -> initFrame(e, d));
	}

	@Override
	public void save(Editor editor, ProjectWriter project) throws IOException {
		project.clearFolder("animations");
		for (EditorAnim e : editor.animations) {
			JsonMap data = project.getJson("animations/" + e.filename);
			writeAnimation(e, data);
		}
		saveEnc(editor, project);
	}

	protected void writeAnimation(EditorAnim e, JsonMap data) {
		data.put("additive", e.add);
		data.put("name", encodeTypeInName(e.displayName, e.type));
		if(e.pose instanceof CustomPose)data.put("name", ((CustomPose)e.pose).getName());
		data.put("duration", e.duration);
		data.put("priority", e.priority);
		data.put("loop", e.loop);
		data.put("interpolator", e.intType.name().toLowerCase(Locale.ROOT));
		data.put("layerDefault", e.layerDefault);
		data.put("order", e.order);
		data.put("isProperty", e.isProperty);
		if(e.group != null && !e.group.isEmpty())data.put("group", e.group);
		data.put("command", e.command);
		data.put("layerControlled", e.layerControlled);
		data.put("maxValue", e.maxValue);
		data.put("interpolateVal", e.interpolateValue);
		data.put("mustFinish", e.mustFinish);
		data.put("frames", writeFrames(e));
	}

	protected List<Map<String, Object>> writeFrames(EditorAnim e) {
		return e.getFrames().stream().map(this::storeFrame).collect(Collectors.toList());
	}

	protected Map<String, Object> storeFrame(AnimFrame frm) {
		List<Map<String, Object>> c = new ArrayList<>();
		for(Entry<ModelElement, FrameData> e : frm.getComponents().entrySet()) {
			Map<String, Object> map = new HashMap<>();
			c.add(map);
			writePartRef(map, e.getKey());
			FrameData dt = e.getValue();
			map.put("pos", dt.getPosition().toMap());
			map.put("rotation", dt.getRotation().toMap());
			Vec3f color = dt.getColor();
			int rgb = (((int) color.x) << 16) | (((int) color.y) << 8) | ((int) color.z);
			map.put("color", Integer.toHexString(rgb));
			map.put("show", dt.isVisible());
			map.put("scale", dt.getScale().toMap());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("components", c);
		return data;
	}

	protected void writePartRef(Map<String, Object> map, ModelElement me) {
		map.put("storeID", me.storeID);
	}

	protected void initFrame(EditorAnim e, JsonMap data) {
		AnimFrame frm = new AnimFrame(e);
		loadFrame(frm, data);
		e.getFrames().add(frm);
		if(e.getSelectedFrame() == null)
			e.setSelectedFrame(frm);
	}

	protected void loadFrame(AnimFrame frm, JsonMap data) {
		JsonList c = data.getList("components");
		c.forEachMap(map -> findPartRef(frm, map, elem -> {
			FrameData dt = frm.makeData(elem);
			dt.setPos(new Vec3f(map.getMap("pos"), new Vec3f()));
			dt.setRot(new Vec3f(map.getMap("rotation"), new Vec3f()));
			int rgb = Integer.parseUnsignedInt(map.getString("color"), 16);
			int r = ((rgb & 0xff0000) >> 16);
			int g = (rgb & 0x00ff00) >> 8;
			int b =  rgb & 0x0000ff;
			dt.setColor(new Vec3f(r, g, b));
			dt.setShow(map.getBoolean("show"));
			dt.setScale(new Vec3f(map.getMap("scale"), new Vec3f(1, 1, 1)));
		}));
	}

	protected void findPartRef(AnimFrame frm, JsonMap map, Consumer<ModelElement> me) {
		long sid = map.getLong("storeID");
		Editor.walkElements(frm.getAnim().editor.elements, elem -> {
			if(elem.storeID == sid) {
				me.accept(elem);
			}
		});
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

	public static String getFileName(IPose pose, String displayName) {
		String fname = null;
		UUID newId = UUID.randomUUID();
		if(pose instanceof VanillaPose) {
			fname = "v_" + ((VanillaPose)pose).name().toLowerCase(Locale.ROOT) + "_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + newId.toString() + ".json";
		} else if(pose != null) {
			fname = "c_" + ((CustomPose) pose).getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + newId.toString() + ".json";
		} else {
			fname = "g_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + newId.toString() + ".json";
		}
		return fname;
	}

	public static AnimationType getType(String displayName) {
		if(displayName.startsWith(LAYER_PREFIX))return AnimationType.LAYER;
		if(displayName.startsWith(VALUE_LAYER_PREFIX))return AnimationType.VALUE_LAYER;
		if(displayName.startsWith(SETUP_PREFIX))return AnimationType.SETUP;
		if(displayName.startsWith(FINISH_PREFIX))return AnimationType.FINISH;
		return AnimationType.GESTURE;
	}

	public static String cleanName(String displayName) {
		if(displayName.startsWith(LAYER_PREFIX))
			return displayName.substring(LAYER_PREFIX.length());
		if(displayName.startsWith(VALUE_LAYER_PREFIX))
			return displayName.substring(VALUE_LAYER_PREFIX.length());
		if(displayName.startsWith(SETUP_PREFIX))
			return displayName.substring(SETUP_PREFIX.length());
		if(displayName.startsWith(FINISH_PREFIX))
			return displayName.substring(FINISH_PREFIX.length());
		return displayName;
	}

	public static String encodeTypeInName(String displayName, AnimationType type) {
		if(type == AnimationType.LAYER)return LAYER_PREFIX + displayName;
		if(type == AnimationType.VALUE_LAYER)return VALUE_LAYER_PREFIX + displayName;
		if(type == AnimationType.SETUP)return SETUP_PREFIX + displayName;
		if(type == AnimationType.FINISH)return FINISH_PREFIX + displayName;
		return displayName;
	}
}
