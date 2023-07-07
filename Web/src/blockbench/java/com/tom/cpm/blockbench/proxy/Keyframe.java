package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Keyframe_$$")
public class Keyframe {
	public static JsArrayE<Keyframe> selected;

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