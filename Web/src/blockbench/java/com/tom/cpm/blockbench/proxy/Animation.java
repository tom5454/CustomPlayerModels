package com.tom.cpm.blockbench.proxy;

import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.JsObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Animation_$$")
public class Animation {
	public String name, uuid, loop;
	public JsPropertyMap<GeneralAnimator> animators;
	public float length;

	@JsProperty(name = "cpm_type")
	public String type;

	@JsProperty(name = "cpm_additive")
	public boolean additive;

	@JsProperty(name = "cpm_layerCtrl")
	public boolean layerCtrl;

	@JsProperty(name = "cpm_commandCtrl")
	public boolean commandCtrl;

	@JsProperty(name = "cpm_priority")
	private int priority;

	@JsProperty(name = "cpm_priority")
	private JsObject priority_;

	@JsOverlay
	public final int getPriority() {
		return Js.typeof(priority_) == "undefined" ? 0 : Js.typeof(priority_) == "object" ? Js.cast(priority_) : priority;
	}

	@JsOverlay
	public final void setPriority(int priority) {
		this.priority = priority;
	}

	@JsProperty(name = "cpm_order")
	private int order;

	@JsProperty(name = "cpm_order")
	private JsObject order_;

	@JsOverlay
	public final int getOrder() {
		return Js.typeof(order_) == "undefined" ? 0 : Js.typeof(order_) == "object" ? Js.cast(order_) : order;
	}

	@JsOverlay
	public final void setOrder(int order) {
		this.order = order;
	}

	@JsProperty(name = "cpm_isProperty")
	public boolean isProperty;

	@JsProperty(name = "cpm_group")
	public String group;

	@JsProperty(name = "cpm_layerDefault")
	private float layerDefault;

	@JsProperty(name = "cpm_layerDefault")
	private JsObject layerDefault_;

	@JsOverlay
	public final float getLayerDefault() {
		return Js.typeof(layerDefault_) == "undefined" ? 0 : Js.typeof(layerDefault_) == "object" ? Js.cast(layerDefault_) : layerDefault;
	}

	@JsOverlay
	public final void setLayerDefault(float layerDefault) {
		this.layerDefault = layerDefault;
	}

	public static Props properties;

	@JsProperty(name = "prototype.menu")
	public static Menu menu;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_GeneralAnimator_$$")
	public static class GeneralAnimator {
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_BoneAnimator_$$")
	public static class BoneAnimator extends GeneralAnimator {
		public Animation animation;
		public String uuid;
		public Object muted;
		public JsArrayE<Keyframe> position;
		public JsArrayE<Keyframe> rotation;
		public JsArrayE<Keyframe> scale;
		public boolean rotation_global;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class Props {
		public Property saved, path, anim_time_update, blend_weight, start_delay, loop_delay;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Keyframe_$$")
	public static class Keyframe {
		public BoneAnimator animator;
		public String uuid, interpolation, channel;
		public boolean transform, bezier_linked;
		public float time;
		public JsVec3 bezier_left_time, bezier_left_value, bezier_right_time, bezier_right_value;
		public int color;
		public JsArrayE<KeyframeDataPoint> data_points;

		public native float getLerp(Keyframe after, String axis, float alpha, boolean allow_exp);
		public native float getBezierLerp(Keyframe before, Keyframe after, String axis, float alpha);
		public native float getCatmullromLerp(Keyframe before_plus, Keyframe before, Keyframe after, Keyframe after_plus, String axis, float alpha);
		public native float calc(String axis, int dp_index);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_KeyframeDataPoint_$$")
	public static class KeyframeDataPoint {
		public Keyframe keyframe;
		public float x, y, z;
	}

	public native Animation add(boolean undo);

	@JsOverlay
	public static Pair<AnimationType, VanillaPose> parseType(String typeIn) {
		AnimationType type = null;
		for(AnimationType t : AnimationType.VALUES) {
			if(typeIn.equalsIgnoreCase(t.name())) {
				type = t;
				break;
			}
		}
		VanillaPose pose = VanillaPose.GLOBAL;
		if(type == null) {
			type = AnimationType.POSE;
			for (VanillaPose p : VanillaPose.VALUES) {
				if(typeIn.equalsIgnoreCase(p.name())) {
					pose = p;
					break;
				}
			}
		}
		return Pair.of(type, pose);
	}
}
