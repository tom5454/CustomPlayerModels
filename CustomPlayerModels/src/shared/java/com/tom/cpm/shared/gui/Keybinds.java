package com.tom.cpm.shared.gui;

import java.util.EnumMap;
import java.util.Map;

import com.tom.cpm.shared.config.Keybind;
import com.tom.cpm.shared.editor.tree.VecType;

public class Keybinds {
	public static final Keybind ZOOM_IN_CAMERA = new Keybind("zoomInCamera", "+");
	public static final Keybind ZOOM_OUT_CAMERA = new Keybind("zoomOutCamera", "-");
	public static final Keybind RESET_CAMERA = new Keybind("resetCamera", "c");
	public static final Keybind FOCUS_CAMERA = new Keybind("focusCamera", "f");

	public static final Keybind OFFSET = new Keybind("offset", "o");
	public static final Keybind ROTATION = new Keybind("rotation", "r");
	public static final Keybind POSITION = new Keybind("position", "p");
	public static final Keybind SIZE = new Keybind("size", "s");
	public static final Keybind QUICK_MENU = new Keybind("quickMenu", "q");
	public static final Keybind SAVE = new Keybind("save", "s", Keybind.CTRL);
	public static final Keybind UNDO = new Keybind("undo", "z", Keybind.CTRL);
	public static final Keybind REDO = new Keybind("redo", "y", Keybind.CTRL);
	public static final Keybind DELETE = new Keybind("delete_part", c -> c.KEY_DELETE);
	public static final Keybind TREE_PREV = new Keybind("tree_prev", c -> c.KEY_UP);
	public static final Keybind TREE_NEXT = new Keybind("tree_next", c -> c.KEY_DOWN);
	public static final Keybind TREE_UP = new Keybind("tree_up", c -> c.KEY_LEFT);
	public static final Keybind TREE_DOWN = new Keybind("tree_down", c -> c.KEY_RIGHT);
	public static final Keybind NEW_PART = new Keybind("new_part", "n", Keybind.CTRL);
	public static final Keybind TOGGLE_GIZMO = new Keybind("toggleGizmo", "g");
	public static final Keybind RUN_QUICK_ACTION = new Keybind("quickAction", "w");
	public static final Keybind TOGGLE_VIS_ACTION = new Keybind("toggleVis", "v");
	public static final Keybind TOGGLE_HIDDEN_ACTION = new Keybind("toggleHidden", "h");
	public static final Keybind TOGGLE_LOCK_ACTION = new Keybind("toggleLock", "l");
	public static final Keybind FIND_ELEMENT = new Keybind("findElement", "f", Keybind.CTRL);
	public static final Keybind RELOAD_TEXTURE = new Keybind("reloadTexture");

	public static final Keybind COPY_ANIM_FRAME = new Keybind("copyAnimFrame", "c", Keybind.ALT);
	public static final Keybind PASTE_ANIM_FRAME = new Keybind("pasteAnimFrame", "v", Keybind.ALT);
	public static final Keybind COPY_ANIM_PART = new Keybind("copyAnimPart", "c", Keybind.CTRL);
	public static final Keybind PASTE_ANIM_PART = new Keybind("pasteAnimPart", "v", Keybind.CTRL);
	public static final Keybind ANIM_PREV_FRAME = new Keybind("animPrevFrame");
	public static final Keybind ANIM_NEXT_FRAME = new Keybind("animNextFrame");

	public static final Keybind RESET_VALUE_LAYER = new Keybind("resetValueLayer", "r");

	public static final Keybind OPEN_WIKI = new Keybind("openWiki", c -> c.KEY_F1);

	public static final Object MOUSE_MARKER = new Object();

	public static final Object[] KEYBINDS = new Object[] {
			"general",
			OPEN_WIKI,
			"gestureGui",
			RESET_VALUE_LAYER,
			"3dView",
			ZOOM_IN_CAMERA, ZOOM_OUT_CAMERA, RESET_CAMERA, FOCUS_CAMERA,
			"editor",
			MOUSE_MARKER, OFFSET, ROTATION, POSITION, SIZE, SAVE, UNDO, REDO, NEW_PART, DELETE, TOGGLE_GIZMO, RUN_QUICK_ACTION,
			TOGGLE_VIS_ACTION, TOGGLE_HIDDEN_ACTION, TOGGLE_LOCK_ACTION, FIND_ELEMENT, RELOAD_TEXTURE,
			"treeNav",
			TREE_PREV, TREE_NEXT, TREE_UP, TREE_DOWN,
			"animAct",
			COPY_ANIM_FRAME, PASTE_ANIM_FRAME, COPY_ANIM_PART, PASTE_ANIM_PART, ANIM_PREV_FRAME, ANIM_NEXT_FRAME,
	};

	public static final Map<VecType, Keybind> EDITOR_TOOL_KBS = new EnumMap<>(VecType.class);
	static {
		EDITOR_TOOL_KBS.put(VecType.OFFSET, OFFSET);
		EDITOR_TOOL_KBS.put(VecType.POSITION, POSITION);
		EDITOR_TOOL_KBS.put(VecType.ROTATION, ROTATION);
		EDITOR_TOOL_KBS.put(VecType.SIZE, SIZE);
	}
}
