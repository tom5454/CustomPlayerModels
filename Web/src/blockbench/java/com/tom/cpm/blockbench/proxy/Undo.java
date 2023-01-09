package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Undo_$$")
public class Undo {
	public static native void initEdit(UndoData dt);
	public static native void finishEdit(String action, UndoData dt);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class UndoData {
		public OutlinerElement[] elements;
		public boolean selection, outliner, uv_mode, uv_only;
		public Group group;
		public Texture[] textures;
		public Object[] animations;

		@JsOverlay
		public static UndoData make(OutlinerElement... elem) {
			UndoData d = new UndoData();
			d.elements = elem;
			return d;
		}

		@JsOverlay
		public static UndoData make(Group elem) {
			UndoData d = new UndoData();
			d.group = elem;
			return d;
		}
	}
}
