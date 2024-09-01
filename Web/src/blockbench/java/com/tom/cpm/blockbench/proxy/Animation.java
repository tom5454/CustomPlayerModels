package com.tom.cpm.blockbench.proxy;

import java.util.Locale;
import java.util.function.Function;

import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Animation_$$")
public class Animation {
	public static Animation[] all;
	public String name, uuid, loop;
	public JsPropertyMap<GeneralAnimator> animators;
	public float length;
	public boolean playing;

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
		if(Js.typeof(layerDefault_) == "undefined")return 0;
		if(Js.typeof(layerDefault_) == "array") {
			float[] a = Js.cast(layerDefault_);
			return a[0];
		}
		if(Js.typeof(layerDefault_) == "object")return Js.cast(layerDefault_);
		return layerDefault;
	}

	@JsOverlay
	public final void setLayerDefault(float layerDefault) {
		this.layerDefault = layerDefault;
	}

	@JsProperty(name = "prototype.menu")
	public static Menu menu;

	public static Animation selected;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_GeneralAnimator_$$")
	public static class GeneralAnimator {

		private native Keyframe createKeyframe(Object value, float time, String channel, boolean undo, boolean select);

		@JsOverlay
		public final Keyframe createKeyframe(float time, String channel) {
			return createKeyframe(null, time, channel, false, false);
		}

	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class AnimatorChannel {
		public String name;
		public boolean mutable, transform;
		public int max_data_points;
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

	@JsOverlay
	public static String makeType(AnimationType type, IPose pose) {
		if(type == AnimationType.POSE)return ((VanillaPose) pose).name().toLowerCase(Locale.ROOT);
		return type.name().toLowerCase(Locale.ROOT);
	}

	//Ported from timeline_animators.js
	@JsOverlay
	private static JsVec3 mapAxes(Function<String, Float> cb) {
		JsVec3 v = new JsVec3();
		v.x = cb.apply("x");
		v.y = cb.apply("y");
		v.z = cb.apply("z");
		return v;
	}

	@JsOverlay
	public static boolean interpolateVisible(BoneAnimator anim, float time, boolean def) {
		if(anim == null || !anim.hasVisible())return def;
		Keyframe before = null;

		for (Keyframe keyframe : anim.visible.array()) {
			if (keyframe.time < time) {
				if (before == null || keyframe.time > before.time) {
					before = keyframe;
				}
			}
		}

		if(before == null)return def;
		return before.data_points.getAt(0).visible;
	}

	@JsOverlay
	public static JsVec3 interpolate(JsArrayE<Keyframe> keyframes, float time) {
		if(keyframes == null || keyframes.length == 0)return null;
		Keyframe before = null;
		Keyframe after = null;
		Keyframe result = null;
		float epsilon = 1/1200f;

		for (Keyframe keyframe : keyframes.array()) {
			if (keyframe.time < time) {
				if (before == null || keyframe.time > before.time) {
					before = keyframe;
				}
			} else  {
				if (after == null || keyframe.time < after.time) {
					after = keyframe;
				}
			}
		}
		if (before != null && epsilon(before.time, time, epsilon)) {
			result = before;
		} else if (after != null && epsilon(after.time, time, epsilon)) {
			result = after;
		} else if (before != null && before.interpolation.equals("step")) {
			result = before;
		} else if (before != null && after == null) {
			result = before;
		} else if (after != null && before == null) {
			result = after;
		} else if (before == null && after == null) {
			//
		} else {
			boolean no_interpolations = Blockbench.hasFlag("no_interpolations");
			float alpha = getLerp(before.time, after.time, time);

			if (no_interpolations || (
					before.interpolation.equals("linear") &&
					(after.interpolation.equals("linear") || after.interpolation.equals("step"))
					)) {
				if (no_interpolations) {
					alpha = Math.round(alpha);
				}

				final Keyframe fbefore = before;
				final Keyframe fafter = after;
				final float falpha = alpha;
				return mapAxes(axis -> fbefore.getLerp(fafter, axis, falpha, false));

			} else if (before.interpolation.equals("catmullrom") || after.interpolation.equals("catmullrom")) {
				JsArray<Keyframe> sorted = keyframes.slice().sort((kf1, kf2) -> (kf1.time - kf2.time));
				int before_index = sorted.indexOf(before);
				Keyframe before_plus = sorted.getAt(before_index-1);
				Keyframe after_plus = sorted.getAt(before_index+2);

				final Keyframe fbefore = before;
				final Keyframe fafter = after;
				final float falpha = alpha;
				return mapAxes(axis -> fbefore.getCatmullromLerp(before_plus, fbefore, fafter, after_plus, axis, falpha));

			} else if (before.interpolation.equals("bezier") || after.interpolation.equals("bezier")) {
				// Bezier
				final Keyframe fbefore = before;
				final Keyframe fafter = after;
				final float falpha = alpha;
				return mapAxes(axis -> fbefore.getBezierLerp(fbefore, fafter, axis, falpha));
			}
		}
		if (result != null && result instanceof Keyframe) {
			Keyframe keyframe = result;
			int dp_index = (keyframe.time > time || epsilon(keyframe.time, time, epsilon)) ? 0 : keyframe.data_points.length-1;

			return mapAxes(axis -> keyframe.calc(axis, dp_index));
		}
		return null;
	}

	@JsOverlay
	private static float getLerp(float a, float b, float m) {
		return (m-a) / (b-a);
	}

	@JsOverlay
	private static boolean epsilon(float a, float b, float epsilon) {
		return Math.abs(b - a) < epsilon;
	}
}
