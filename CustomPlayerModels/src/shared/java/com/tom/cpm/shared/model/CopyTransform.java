package com.tom.cpm.shared.model;

import com.tom.cpl.math.Rotation;
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
	private final boolean copyVis;
	private final boolean copyColor;
	private final boolean additive;
	private final float multiply;

	public CopyTransform(RenderedCube from, RenderedCube to, short copy, float multiply) {
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
		copyVis = (copy & (1 << 9)) != 0;
		copyColor = (copy & (1 << 10)) != 0;
		additive = (copy & (1 << 11)) != 0;
		//bit 14: multiply
		this.multiply = multiply;
	}

	public void apply() {
		Vec3f pf = from.getTransformPosition();
		Vec3f pt;
		Rotation rf = from.getTransformRotation();
		Rotation rt;
		Vec3f sf = from.getRenderScale();
		Vec3f st;
		if (additive) {
			pt = Vec3f.ZERO;
			rt = Rotation.ZERO;
			st = Vec3f.ZERO;
		} else {
			pt = to.getTransformPosition();
			rt = to.getTransformRotation();
			st = to.getRenderScale();
		}
		to.setPosition(additive, copyPX ? pf.x * multiply : pt.x, copyPY ? pf.y * multiply : pt.y, copyPZ ? pf.z * multiply : pt.z);
		to.setRotation(additive, copyRX ? rf.x * multiply : rt.x, copyRY ? rf.y * multiply : rt.y, copyRZ ? rf.z * multiply : rt.z);
		to.setRenderScale(additive, copySX ? sf.x * multiply : st.x, copySY ? sf.y * multiply : st.y, copySZ ? sf.z * multiply : st.z);
		if(copyVis)to.setVisible(from.isVisible());
		if(copyColor)to.setColor(from.getRGB());
	}
}
