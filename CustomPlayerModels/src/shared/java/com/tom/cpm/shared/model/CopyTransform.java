package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec3f;

public class CopyTransform {
	private final RenderedCube from;
	private final RenderedCube to;
	private final boolean copyPX;
	private final boolean copyPY;
	private final boolean copyPZ;
	private final boolean copyRX;
	private final boolean copyRY;
	private final boolean copyRZ;
	private final boolean copySX;
	private final boolean copySY;
	private final boolean copySZ;

	public CopyTransform(RenderedCube from, RenderedCube to, short copy) {
		this.from = from;
		this.to = to;
		copyPX = (copy & (1 << 0)) != 0;
		copyPY = (copy & (1 << 1)) != 0;
		copyPZ = (copy & (1 << 2)) != 0;
		copyRX = (copy & (1 << 3)) != 0;
		copyRY = (copy & (1 << 4)) != 0;
		copyRZ = (copy & (1 << 5)) != 0;
		copySX = (copy & (1 << 6)) != 0;
		copySY = (copy & (1 << 7)) != 0;
		copySZ = (copy & (1 << 8)) != 0;
	}

	public void apply() {
		Vec3f pf = from.getTransformPosition();
		Vec3f pt = to.getTransformPosition();
		Vec3f rf = from.getTransformRotation();
		Vec3f rt = to.getTransformRotation();
		Vec3f sf = from.getRenderScale();
		Vec3f st = to.getRenderScale();
		to.setPosition(false, copyPX ? pf.x : pt.x, copyPY ? pf.y : pt.y, copyPZ ? pf.z : pt.z);
		to.setRotation(false, copyRX ? rf.x : rt.x, copyRY ? rf.y : rt.y, copyRZ ? rf.z : rt.z);
		to.setRenderScale(false, copySX ? sf.x : st.x, copySY ? sf.y : st.y, copySZ ? sf.z : st.z);
	}
}
