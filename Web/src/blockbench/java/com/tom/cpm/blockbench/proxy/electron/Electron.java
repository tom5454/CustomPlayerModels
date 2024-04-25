package com.tom.cpm.blockbench.proxy.electron;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_electron_$$")
public class Electron {
	public static ElectronApp app;
	public static ElectronDialog dialog;
	public IPCRenderer ipcRenderer;

	@JsOverlay
	public static Electron getElectron() {
		return G.electron;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m_electron_$$")
		public static Electron electron;
	}

	@JsOverlay
	public static boolean isElectron() {
		return Js.typeof(G.electron) != "undefined";
	}
}