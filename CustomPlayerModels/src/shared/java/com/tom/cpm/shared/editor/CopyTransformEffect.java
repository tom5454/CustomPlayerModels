package com.tom.cpm.shared.editor;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.gui.IGui;
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
		return r;
	}

	public void apply() {
		if(from != null) {
			to.rc.setPosition(false,
					copyPX ? from.rc.getTransformPosition().x : to.rc.getTransformPosition().x,
							copyPY ? from.rc.getTransformPosition().y : to.rc.getTransformPosition().y,
									copyPZ ? from.rc.getTransformPosition().z : to.rc.getTransformPosition().z);
			to.rc.setRotation(false,
					copyRX ? from.rc.getTransformRotation().x : to.rc.getTransformRotation().x,
							copyRY ? from.rc.getTransformRotation().y : to.rc.getTransformRotation().y,
									copyRZ ? from.rc.getTransformRotation().z : to.rc.getTransformRotation().z);
			to.rc.setRenderScale(false,
					copySX ? from.rc.getRenderScale().x : to.rc.getRenderScale().x,
							copySY ? from.rc.getRenderScale().y : to.rc.getRenderScale().y,
									copySZ ? from.rc.getRenderScale().z : to.rc.getRenderScale().z);
			if(copyVis)to.rc.setVisible(from.rc.isVisible());
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
		return r;
	}

	public void load(Editor editor) {
		Editor.walkElements(editor.elements, elem -> {
			if(elem.storeID == storeID) {
				from = elem;
			}
		});
	}

	public String getTooltip(IGui gui) {
		StringBuilder sb = new StringBuilder();
		sb.append(gui.i18nFormat("label.cpm.copyTransform"));
		if(from != null) {
			sb.append("\\ ");
			sb.append(gui.i18nFormat("label.cpm.copyTransform.from", from.getElemName()));
		}
		boolean p = createXYZ(sb, gui.i18nFormat("label.cpm.position"), copyPX, copyPX, copyPZ);
		boolean r = createXYZ(sb, gui.i18nFormat("label.cpm.rotation"), copyRX, copyRX, copyRZ);
		boolean s = createXYZ(sb, gui.i18nFormat("label.cpm.scale"), copySX, copySX, copySZ);
		if(copyVis) {
			sb.append("\\  ");
			sb.append(gui.i18nFormat("label.cpm.visible"));
		}
		if(!(p || r || s || copyVis)) {
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
}
