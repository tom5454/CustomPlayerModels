package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Undo_$$")
public class Undo {
	public static native void initEdit(UndoData dt);
	public static native void finishEdit(String action);
	public static native void finishEdit(String action, UndoData dt);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class UndoData {
		public OutlinerElement[] elements;
		public boolean selection, outliner, uv_mode, uv_only;
		public Group group;
		public Group[] groups;
		public Texture[] textures;
		public Animation[] animations;

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

		@JsOverlay
		public static UndoData make(Animation... an) {
			UndoData d = new UndoData();
			d.animations = an;
			return d;
		}

		@JsOverlay
		public static UndoData make(Group... elem) {
			UndoData d = new UndoData();
			d.groups = elem;
			return d;
		}
	}
}
