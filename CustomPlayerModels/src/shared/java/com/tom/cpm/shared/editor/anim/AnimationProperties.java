package com.tom.cpm.shared.editor.anim;

import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;

public class AnimationProperties {
	public IPose pose;
	public String displayName;
	public AnimationType type;
	public boolean add;
	public boolean loop;
	public InterpolatorType it;
	public boolean command;
	public boolean layerCtrl;
	public boolean mustFinish;
	public boolean hidden;

	@Deprecated
	public AnimationProperties(IPose pose, String displayName, AnimationType type, boolean add, boolean loop,
			InterpolatorType it, boolean command, boolean layerCtrl) {
		this.pose = pose;
		this.displayName = displayName;
		this.type = type;
		this.add = add;
		this.loop = loop;
		this.it = it;
		this.command = command;
		this.layerCtrl = layerCtrl;
	}

	public AnimationProperties(IPose pose, String displayName, AnimationType type, boolean add, boolean loop,
			InterpolatorType it, boolean command, boolean layerCtrl, boolean mustFinish, boolean hidden) {
		this.pose = pose;
		this.displayName = displayName;
		this.type = type;
		this.add = add;
		this.loop = loop;
		this.it = it;
		this.command = command;
		this.layerCtrl = layerCtrl;
		this.mustFinish = mustFinish;
		this.hidden = hidden;
	}
}
