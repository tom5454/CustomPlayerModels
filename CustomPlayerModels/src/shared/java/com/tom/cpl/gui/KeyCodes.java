package com.tom.cpl.gui;

import java.util.HashMap;
import java.util.Map;

public abstract class KeyCodes {
	/** Printable keys. */
	public int
	KEY_SPACE         ,
	KEY_APOSTROPHE    ,
	KEY_COMMA         ,
	KEY_MINUS         ,
	KEY_PERIOD        ,
	KEY_SLASH         ,
	KEY_0             ,
	KEY_1             ,
	KEY_2             ,
	KEY_3             ,
	KEY_4             ,
	KEY_5             ,
	KEY_6             ,
	KEY_7             ,
	KEY_8             ,
	KEY_9             ,
	KEY_SEMICOLON     ,
	KEY_EQUAL         ,
	KEY_A             ,
	KEY_B             ,
	KEY_C             ,
	KEY_D             ,
	KEY_E             ,
	KEY_F             ,
	KEY_G             ,
	KEY_H             ,
	KEY_I             ,
	KEY_J             ,
	KEY_K             ,
	KEY_L             ,
	KEY_M             ,
	KEY_N             ,
	KEY_O             ,
	KEY_P             ,
	KEY_Q             ,
	KEY_R             ,
	KEY_S             ,
	KEY_T             ,
	KEY_U             ,
	KEY_V             ,
	KEY_W             ,
	KEY_X             ,
	KEY_Y             ,
	KEY_Z             ,
	KEY_LEFT_BRACKET  ,
	KEY_BACKSLASH     ,
	KEY_RIGHT_BRACKET ,
	KEY_GRAVE_ACCENT  ,
	KEY_WORLD_1       ,
	KEY_WORLD_2       ;

	/** Function keys. */
	public int
	KEY_ESCAPE        ,
	KEY_ENTER         ,
	KEY_TAB           ,
	KEY_BACKSPACE     ,
	KEY_INSERT        ,
	KEY_DELETE        ,
	KEY_RIGHT         ,
	KEY_LEFT          ,
	KEY_DOWN          ,
	KEY_UP            ,
	KEY_PAGE_UP       ,
	KEY_PAGE_DOWN     ,
	KEY_HOME          ,
	KEY_END           ,
	KEY_CAPS_LOCK     ,
	KEY_SCROLL_LOCK   ,
	KEY_NUM_LOCK      ,
	KEY_PRINT_SCREEN  ,
	KEY_PAUSE         ,
	KEY_F1            ,
	KEY_F2            ,
	KEY_F3            ,
	KEY_F4            ,
	KEY_F5            ,
	KEY_F6            ,
	KEY_F7            ,
	KEY_F8            ,
	KEY_F9            ,
	KEY_F10           ,
	KEY_F11           ,
	KEY_F12           ,
	KEY_F13           ,
	KEY_F14           ,
	KEY_F15           ,
	KEY_F16           ,
	KEY_F17           ,
	KEY_F18           ,
	KEY_F19           ,
	KEY_F20           ,
	KEY_F21           ,
	KEY_F22           ,
	KEY_F23           ,
	KEY_F24           ,
	KEY_F25           ,
	KEY_KP_0          ,
	KEY_KP_1          ,
	KEY_KP_2          ,
	KEY_KP_3          ,
	KEY_KP_4          ,
	KEY_KP_5          ,
	KEY_KP_6          ,
	KEY_KP_7          ,
	KEY_KP_8          ,
	KEY_KP_9          ,
	KEY_KP_DECIMAL    ,
	KEY_KP_DIVIDE     ,
	KEY_KP_MULTIPLY   ,
	KEY_KP_SUBTRACT   ,
	KEY_KP_ADD        ,
	KEY_KP_ENTER      ,
	KEY_KP_EQUAL      ,
	KEY_LEFT_SHIFT    ,
	KEY_LEFT_CONTROL  ,
	KEY_LEFT_ALT      ,
	KEY_LEFT_SUPER    ,
	KEY_RIGHT_SHIFT   ,
	KEY_RIGHT_CONTROL ,
	KEY_RIGHT_ALT     ,
	KEY_RIGHT_SUPER   ,
	KEY_MENU          ,
	KEY_LAST          ;

	private Map<Integer, String> keyNames;

	public String keyToString(IGui gui, int key) {
		if(keyNames == null)initKeyNames();
		String k = keyNames.get(key);
		return k == null ? "key: " + key : gui.i18nFormat(k);
	}

	private void initKeyNames() {
		keyNames = new HashMap<>();

		keyNames.put(KEY_F1, "label.cpm.key.f1");
		keyNames.put(KEY_F2, "label.cpm.key.f2");
		keyNames.put(KEY_F3, "label.cpm.key.f3");
		keyNames.put(KEY_F4, "label.cpm.key.f4");
		keyNames.put(KEY_F5, "label.cpm.key.f5");
		keyNames.put(KEY_F6, "label.cpm.key.f6");
		keyNames.put(KEY_F7, "label.cpm.key.f7");
		keyNames.put(KEY_F8, "label.cpm.key.f8");
		keyNames.put(KEY_F9, "label.cpm.key.f9");
		keyNames.put(KEY_F10, "label.cpm.key.f10");
		keyNames.put(KEY_F11, "label.cpm.key.f11");
		keyNames.put(KEY_F12, "label.cpm.key.f12");
		keyNames.put(KEY_F13, "label.cpm.key.f13");
		keyNames.put(KEY_F14, "label.cpm.key.f14");
		keyNames.put(KEY_F15, "label.cpm.key.f15");
		keyNames.put(KEY_F16, "label.cpm.key.f16");
		keyNames.put(KEY_F17, "label.cpm.key.f17");
		keyNames.put(KEY_F18, "label.cpm.key.f18");
		keyNames.put(KEY_F19, "label.cpm.key.f19");
		keyNames.put(KEY_F20, "label.cpm.key.f20");
		keyNames.put(KEY_F21, "label.cpm.key.f21");
		keyNames.put(KEY_F22, "label.cpm.key.f22");
		keyNames.put(KEY_F23, "label.cpm.key.f23");
		keyNames.put(KEY_F24, "label.cpm.key.f24");
		keyNames.put(KEY_F25, "label.cpm.key.f25");
		keyNames.put(KEY_ENTER, "label.cpm.key.enter");
		keyNames.put(KEY_KP_ENTER, "label.cpm.key.keypad.enter");
		keyNames.put(KEY_SPACE, "label.cpm.key.space");
		keyNames.put(KEY_TAB, "label.cpm.key.tab");
		keyNames.put(KEY_LEFT_ALT, "label.cpm.key.left.alt");
		keyNames.put(KEY_LEFT_CONTROL, "label.cpm.key.left.control");
		keyNames.put(KEY_LEFT_SHIFT, "label.cpm.key.left.shift");
		keyNames.put(KEY_RIGHT_ALT, "label.cpm.key.right.alt");
		keyNames.put(KEY_RIGHT_CONTROL, "label.cpm.key.right.control");
		keyNames.put(KEY_RIGHT_SHIFT, "label.cpm.key.right.shift");
		keyNames.put(KEY_DOWN, "label.cpm.key.down");
		keyNames.put(KEY_LEFT, "label.cpm.key.left");
		keyNames.put(KEY_RIGHT, "label.cpm.key.right");
		keyNames.put(KEY_UP, "label.cpm.key.up");
		keyNames.put(KEY_BACKSPACE, "label.cpm.key.backspace");
		keyNames.put(KEY_DELETE, "label.cpm.key.delete");
		keyNames.put(KEY_END, "label.cpm.key.end");
		keyNames.put(KEY_HOME, "label.cpm.key.home");
		keyNames.put(KEY_INSERT, "label.cpm.key.insert");
		keyNames.put(KEY_PAGE_DOWN, "label.cpm.key.page.down");
		keyNames.put(KEY_PAGE_UP, "label.cpm.key.page.up");
		keyNames.put(KEY_CAPS_LOCK, "label.cpm.key.caps.lock");
		keyNames.put(KEY_PAUSE, "label.cpm.key.pause");
		keyNames.put(KEY_SCROLL_LOCK, "label.cpm.key.scroll.lock");
		keyNames.put(KEY_MENU, "label.cpm.key.menu");
		keyNames.put(KEY_PRINT_SCREEN, "label.cpm.key.print.screen");
		keyNames.put(KEY_NUM_LOCK, "label.cpm.key.num.lock");
	}
}
