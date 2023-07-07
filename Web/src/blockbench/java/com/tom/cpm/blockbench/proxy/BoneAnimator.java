package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Animation.AnimatorChannel;
import com.tom.cpm.blockbench.proxy.Animation.GeneralAnimator;
import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_BoneAnimator_$$")
public class BoneAnimator extends GeneralAnimator {
	public static BoneAnimator.Props prototype;

	public Animation animation;
	public String uuid;
	public Object muted;
	public JsArrayE<Keyframe> position;
	public JsArrayE<Keyframe> rotation;
	public JsArrayE<Keyframe> scale;

	@JsProperty(name = "cpm_visibility")
	public JsArrayE<Keyframe> visible;

	@JsProperty(name = "cpm_color")
	public JsArrayE<Keyframe> color;

	public boolean rotation_global;

	public BoneAnimator(String uuid, Animation anim) {
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class Props {
		public JsPropertyMap<AnimatorChannel> channels;
	}
}