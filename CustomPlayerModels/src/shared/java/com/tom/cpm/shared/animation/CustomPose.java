package com.tom.cpm.shared.animation;

import com.tom.cpl.text.I18n;

public class CustomPose implements IPose {
	private String name;
	public int order;
	public boolean command;
	public boolean layerCtrl;

	public CustomPose(String name, int order) {
		this.name = name;
		this.order = order;
	}

	@Override
	public String getName(I18n gui, String display) {
		return name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		int i = name.indexOf('#');
		if(i == -1)return name;
		if(i == 0)return "";
		return name.substring(0, i);
	}

	@Override
	public String toString() {
		return "Custom Pose: " + name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CustomPose other = (CustomPose) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}
}
