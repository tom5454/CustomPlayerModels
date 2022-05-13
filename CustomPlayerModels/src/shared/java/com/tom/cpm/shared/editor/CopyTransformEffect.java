package com.tom.cpm.shared.editor;

import java.util.HashMap;
import java.util.Map;

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

	public CopyTransformEffect(ModelElement to) {
		this.to = to;
	}

	public void load(JsonMap data) {
		storeID = data.getLong("storeID", 0);
		copyPX = data.getBoolean("px", false);
		copyPY = data.getBoolean("py", false);
		copyPZ = data.getBoolean("pz", false);
		copyRX = data.getBoolean("rx", false);
		copyRY = data.getBoolean("ry", false);
		copyRZ = data.getBoolean("rz", false);
		copySX = data.getBoolean("sx", false);
		copySY = data.getBoolean("sy", false);
		copySZ = data.getBoolean("sz", false);
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
		return r;
	}

	public void apply() {
		if(from != null) {
			to.rc.setPosition(false,
					copyPX ? from.rc.getPosition().x : to.rc.getPosition().x,
							copyPY ? from.rc.getPosition().y : to.rc.getPosition().y,
									copyPZ ? from.rc.getPosition().z : to.rc.getPosition().z);
			to.rc.setRotation(false,
					copyRX ? from.rc.getRotation().x : to.rc.getRotation().x,
							copyRY ? from.rc.getRotation().y : to.rc.getRotation().y,
									copyRZ ? from.rc.getRotation().z : to.rc.getRotation().z);
			to.rc.setRenderScale(false,
					copySX ? from.rc.getRenderScale().x : to.rc.getRenderScale().x,
							copySY ? from.rc.getRenderScale().y : to.rc.getRenderScale().y,
									copySZ ? from.rc.getRenderScale().z : to.rc.getRenderScale().z);
		}
	}

	public Map<String, Object> toMap() {
		Map<String, Object> r = new HashMap<>();
		if(from != null) {
			from.editor.storeIDgen.setID(from);
			r.put("storeID", from.storeID);
			r.put("px", copyPX);
			r.put("py", copyPY);
			r.put("pz", copyPZ);
			r.put("rx", copyRX);
			r.put("ry", copyRY);
			r.put("rz", copyRZ);
			r.put("sx", copySX);
			r.put("sy", copySY);
			r.put("sz", copySZ);
		}
		return r;
	}

	public void load(Editor editor) {
		Editor.walkElements(editor.elements, elem -> {
			if(elem.storeID == storeID) {
				from = elem;
			}
		});
	}
}
