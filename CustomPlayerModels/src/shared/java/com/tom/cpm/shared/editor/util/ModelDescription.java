package com.tom.cpm.shared.editor.util;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;

public class ModelDescription {
	public String name = "";
	public String desc = "";
	public Image icon;
	public ViewportCamera camera = new ViewportCamera();
	public CopyProtection copyProtection = CopyProtection.NORMAL;

	public static enum CopyProtection {
		NORMAL,
		UUID_LOCK,
		CLONEABLE
		;
		public static final CopyProtection[] VALUES = values();

		public static CopyProtection lookup(String name) {
			for (CopyProtection t : VALUES) {
				if(t.name().equalsIgnoreCase(name))return t;
			}
			return null;
		}
	}
}
