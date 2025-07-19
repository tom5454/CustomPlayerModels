package com.tom.cpm.shared.editor;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.text.I18n;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.project.JsonMap;

public class CopyTransformEffect {
	public final ModelElement to;
	public long storeID;
	public ModelElement from;
	public boolean copyPX;
	public boolean copyPY;
	public boolean copyPZ;
	public boolean copyRX;
	public boolean copyRY;
	public boolean copyRZ;
	public boolean copySX;
	public boolean copySY;
	public boolean copySZ;
	public boolean copyVis;
	public boolean copyColor;
	public boolean additive;
	public float multiply = 1f;

	public CopyTransformEffect(ModelElement to) {
		this.to = to;
	}

	public void load(JsonMap data) {
		storeID = data.getLong("storeID", -1L);
		copyPX = data.getBoolean("px", false);
		copyPY = data.getBoolean("py", false);
		copyPZ = data.getBoolean("pz", false);
		copyRX = data.getBoolean("rx", false);
		copyRY = data.getBoolean("ry", false);
		copyRZ = data.getBoolean("rz", false);
		copySX = data.getBoolean("sx", false);
		copySY = data.getBoolean("sy", false);
		copySZ = data.getBoolean("sz", false);
		copyVis = data.getBoolean("cv", false);
		copyColor = data.getBoolean("r", false);
		additive = data.getBoolean("additive", false);
		multiply = data.getFloat("multiply", 1f);
	}

	public short toShort() {
		short r = 0;
		if(copyPX)r |= (1 << 0);
		if(copyPY)r |= (1 << 1);
		if(copyPZ)r |= (1 << 2);
		if(copyRX)r |= (1 << 3);
		if(copyRY)r |= (1 << 4);
		if(copyRZ)r |= (1 << 5);
		if(copySX)r |= (1 << 6);
		if(copySY)r |= (1 << 7);
		if(copySZ)r |= (1 << 8);
		if(copyVis)r |= (1 << 9);
		if(copyColor)r |= (1 << 10);
		if(additive)r |= (1 << 11);
		if(Math.abs(multiply - 1f) > 0.01f)r |= (1 << 14);
		return r;
	}

	public void apply() {
		if(from != null) {
			Vec3f pf = from.rc.getTransformPosition();
			Vec3f pt;
			Rotation rf = from.rc.getTransformRotation();
			Rotation rt;
			Vec3f sf = from.rc.getRenderScale();
			Vec3f st;
			if (additive) {
				pt = Vec3f.ZERO;
				rt = Rotation.ZERO;
				st = Vec3f.ZERO;
			} else {
				pt = to.rc.getTransformPosition();
				rt = to.rc.getTransformRotation();
				st = to.rc.getRenderScale();
			}
			to.rc.setPosition(additive, copyPX ? pf.x * multiply : pt.x, copyPY ? pf.y * multiply : pt.y, copyPZ ? pf.z * multiply : pt.z);
			to.rc.setRotation(additive, copyRX ? rf.x * multiply : rt.x, copyRY ? rf.y * multiply : rt.y, copyRZ ? rf.z * multiply : rt.z);
			to.rc.setRenderScale(additive, copySX ? sf.x * multiply : st.x, copySY ? sf.y * multiply : st.y, copySZ ? sf.z * multiply : st.z);
			if(copyVis)to.rc.setVisible(to.rc.doDisplay() && from.rc.isVisible());
			if(copyColor)to.rc.setColor(from.rc.getRGB());
		}
	}

	public Map<String, Object> toMap() {
		Map<String, Object> r = new HashMap<>();
		if(from != null)r.put("storeID", from.storeID);
		r.put("px", copyPX);
		r.put("py", copyPY);
		r.put("pz", copyPZ);
		r.put("rx", copyRX);
		r.put("ry", copyRY);
		r.put("rz", copyRZ);
		r.put("sx", copySX);
		r.put("sy", copySY);
		r.put("sz", copySZ);
		r.put("cv", copyVis);
		r.put("color", copyColor);
		r.put("additive", additive);
		r.put("multiply", multiply);
		return r;
	}

	public void load(Editor editor) {
		Editor.walkElements(editor.elements, elem -> {
			if(elem.storeID == storeID) {
				from = elem;
			}
		});
	}

	public String getTooltip(I18n gui) {
		StringBuilder sb = new StringBuilder();
		sb.append(gui.i18nFormat("label.cpm.copyTransform"));
		if(from != null) {
			sb.append("\\ ");
			sb.append(gui.i18nFormat("label.cpm.copyTransform.from", from.getElemName()));
		}
		boolean p = createXYZ(sb, gui.i18nFormat("label.cpm.position"), copyPX, copyPY, copyPZ);
		boolean r = createXYZ(sb, gui.i18nFormat("label.cpm.rotation"), copyRX, copyRY, copyRZ);
		boolean s = createXYZ(sb, gui.i18nFormat("label.cpm.scale"), copySX, copySY, copySZ);
		if(copyVis) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("label.cpm.visible"));
		}
		if(copyColor) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("action.cpm.color"));
		}
		if(additive) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("label.cpm.anim_additive"));
		}
		if(Math.abs(multiply - 1f) > 0.01f) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("tooltip.cpm.copyTransform.multiply", String.format("%.2f", multiply)));
		}
		if(!(p || r || s || copyVis || copyColor)) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("tooltip.cpm.noCopyTransforms"));
		}
		return sb.toString();
	}

	private boolean createXYZ(StringBuilder sb, String name, boolean x, boolean y, boolean z) {
		if(x || y || z) {
			sb.append("\\  ");
			sb.append(name);
			sb.append(": ");
			if(x)sb.append('X');
			if(y && x)sb.append(", Y");
			else if(y)sb.append('Y');
			if(z && (x || y))sb.append(", Z");
			else if(z)sb.append('Z');
			return true;
		}
		return false;
	}

	public void setAll(boolean v) {
		copyPX = v;
		copyPY = v;
		copyPZ = v;
		copyRX = v;
		copyRY = v;
		copyRZ = v;
		copySX = v;
		copySY = v;
		copySZ = v;
		copyVis = v;
		copyColor = v;
	}
}
