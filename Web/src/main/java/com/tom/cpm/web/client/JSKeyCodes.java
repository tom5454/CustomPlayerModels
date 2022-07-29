package com.tom.cpm.web.client;

import com.tom.cpl.gui.KeyCodes;

public class JSKeyCodes extends KeyCodes {
	private static final int UNKNOWN = codeHack("NULL");

	public JSKeyCodes() {
		/** Printable keys. */
		KEY_SPACE         = codeHack("Space");
		KEY_APOSTROPHE    = codeHack("Quote");
		KEY_COMMA         = codeHack("Comma");
		KEY_MINUS         = codeHack("Minus");
		KEY_PERIOD        = codeHack("Period");
		KEY_SLASH         = codeHack("Slash");
		KEY_0             = codeHack("Digit0");
		KEY_1             = codeHack("Digit1");
		KEY_2             = codeHack("Digit2");
		KEY_3             = codeHack("Digit3");
		KEY_4             = codeHack("Digit4");
		KEY_5             = codeHack("Digit5");
		KEY_6             = codeHack("Digit6");
		KEY_7             = codeHack("Digit7");
		KEY_8             = codeHack("Digit8");
		KEY_9             = codeHack("Digit9");
		KEY_SEMICOLON     = codeHack("Semicolon");
		KEY_EQUAL         = codeHack("Equal");
		KEY_A             = codeHack("KeyA");
		KEY_B             = codeHack("KeyB");
		KEY_C             = codeHack("KeyC");
		KEY_D             = codeHack("KeyD");
		KEY_E             = codeHack("KeyE");
		KEY_F             = codeHack("KeyF");
		KEY_G             = codeHack("KeyG");
		KEY_H             = codeHack("KeyH");
		KEY_I             = codeHack("KeyI");
		KEY_J             = codeHack("KeyJ");
		KEY_K             = codeHack("KeyK");
		KEY_L             = codeHack("KeyL");
		KEY_M             = codeHack("KeyM");
		KEY_N             = codeHack("KeyN");
		KEY_O             = codeHack("KeyO");
		KEY_P             = codeHack("KeyP");
		KEY_Q             = codeHack("KeyQ");
		KEY_R             = codeHack("KeyR");
		KEY_S             = codeHack("KeyS");
		KEY_T             = codeHack("KeyT");
		KEY_U             = codeHack("KeyU");
		KEY_V             = codeHack("KeyV");
		KEY_W             = codeHack("KeyW");
		KEY_X             = codeHack("KeyX");
		KEY_Y             = codeHack("KeyY");
		KEY_Z             = codeHack("KeyZ");
		KEY_LEFT_BRACKET  = codeHack("BracketLeft");
		KEY_BACKSLASH     = codeHack("Backslash");
		KEY_RIGHT_BRACKET = codeHack("BracketRight");
		KEY_GRAVE_ACCENT  = codeHack("Backquote");
		KEY_WORLD_1       = UNKNOWN;
		KEY_WORLD_2       = UNKNOWN;

		/** Function keys. */
		KEY_ESCAPE        = codeHack("Escape");
		KEY_ENTER         = codeHack("Enter");
		KEY_TAB           = codeHack("Tab");
		KEY_BACKSPACE     = codeHack("Backspace");
		KEY_INSERT        = codeHack("Insert");
		KEY_DELETE        = codeHack("Delete");
		KEY_RIGHT         = codeHack("ArrowRight");
		KEY_LEFT          = codeHack("ArrowLeft");
		KEY_DOWN          = codeHack("ArrowDown");
		KEY_UP            = codeHack("ArrowUp");
		KEY_PAGE_UP       = codeHack("PageUp");
		KEY_PAGE_DOWN     = codeHack("PageDown");
		KEY_HOME          = codeHack("Home");
		KEY_END           = codeHack("End");
		KEY_CAPS_LOCK     = codeHack("CapsLock");
		KEY_SCROLL_LOCK   = codeHack("ScrollLock");
		KEY_NUM_LOCK      = codeHack("NumLock");
		KEY_PRINT_SCREEN  = UNKNOWN;
		KEY_PAUSE         = codeHack("Pause");
		KEY_F1            = codeHack("F1");
		KEY_F2            = codeHack("F2");
		KEY_F3            = codeHack("F3");
		KEY_F4            = codeHack("F4");
		KEY_F5            = codeHack("F5");
		KEY_F6            = codeHack("F6");
		KEY_F7            = codeHack("F7");
		KEY_F8            = codeHack("F8");
		KEY_F9            = codeHack("F9");
		KEY_F10           = codeHack("F10");
		KEY_F11           = codeHack("F11");
		KEY_F12           = codeHack("F12");
		KEY_F13           = UNKNOWN;
		KEY_F14           = UNKNOWN;
		KEY_F15           = UNKNOWN;
		KEY_F16           = UNKNOWN;
		KEY_F17           = UNKNOWN;
		KEY_F18           = UNKNOWN;
		KEY_F19           = UNKNOWN;
		KEY_F20           = UNKNOWN;
		KEY_F21           = UNKNOWN;
		KEY_F22           = UNKNOWN;
		KEY_F23           = UNKNOWN;
		KEY_F24           = UNKNOWN;
		KEY_F25           = UNKNOWN;
		KEY_KP_0          = codeHack("Numpad0");
		KEY_KP_1          = codeHack("Numpad1");
		KEY_KP_2          = codeHack("Numpad2");
		KEY_KP_3          = codeHack("Numpad3");
		KEY_KP_4          = codeHack("Numpad4");
		KEY_KP_5          = codeHack("Numpad5");
		KEY_KP_6          = codeHack("Numpad6");
		KEY_KP_7          = codeHack("Numpad7");
		KEY_KP_8          = codeHack("Numpad8");
		KEY_KP_9          = codeHack("Numpad9");
		KEY_KP_DECIMAL    = codeHack("NumpadDecimal");
		KEY_KP_DIVIDE     = codeHack("NumpadDivide");
		KEY_KP_MULTIPLY   = codeHack("NumpadMultiply");
		KEY_KP_SUBTRACT   = codeHack("NumpadSubtract");
		KEY_KP_ADD        = codeHack("NumpadAdd");
		KEY_KP_ENTER      = codeHack("NumpadEnter");
		KEY_KP_EQUAL      = UNKNOWN;
		KEY_LEFT_SHIFT    = codeHack("ShiftLeft");
		KEY_LEFT_CONTROL  = codeHack("ControlLeft");
		KEY_LEFT_ALT      = codeHack("AltLeft");
		KEY_LEFT_SUPER    = UNKNOWN;
		KEY_RIGHT_SHIFT   = codeHack("ShiftRight");
		KEY_RIGHT_CONTROL = codeHack("ControlRight");
		KEY_RIGHT_ALT     = codeHack("AltRight");
		KEY_RIGHT_SUPER   = UNKNOWN;
		KEY_MENU          = codeHack("ContextMenu");
		KEY_LAST          = UNKNOWN;
	}

	public static native int codeHack(Object obj) /*-{
    	return obj;
  	}-*/;

	public static native String codeHack(int obj) /*-{
		return obj;
	}-*/;

	/**
document.addEventListener("keydown", e => {
  document.getElementById("root").innerText = e.code;
  navigator.clipboard.writeText(e.code);
  e.preventDefault();
})
	 */
}
