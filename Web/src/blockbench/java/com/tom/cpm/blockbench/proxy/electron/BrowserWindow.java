package com.tom.cpm.blockbench.proxy.electron;

import com.tom.cpm.blockbench.proxy.electron.IPCRenderer.IPC;
import com.tom.ugwt.client.JsRunnable;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_electron.BrowserWindow_$$")
public class BrowserWindow {

	public WebContents webContents;

	public BrowserWindow(BrowserWindowProperties pr) {
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class BrowserWindowProperties {
		public int width, height;
		public BrowserWindowWebPreferences webPreferences;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class BrowserWindowWebPreferences {
		public boolean nodeIntegration, contextIsolation, enableRemoteModule;
	}

	public native void loadURL(String url);

	public native void removeMenu();
	public native void openDevTools();
	public native void focus();

	public native void on(String ev, JsRunnable run);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class WebContents {
		public IPC ipc;

		public native void executeJavaScript(String script);
		public native void on(String ev, JsRunnable run);
		public native void send(String id, Object message);
	}

	public native void close();
	public native boolean isDestroyed();
}
