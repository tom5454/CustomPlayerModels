package com.tom.cpm.blockbench.proxy.electron;

import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.WebContents;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_electron.ipcRenderer_$$")
public class IPCRenderer {
	public native void on(String ev, IPCEventHandler run);
	public native void removeListener(String ev, IPCEventHandler run);
	public native void send(String id, Object message);

	@JsFunction
	public interface IPCEventHandler {
		void run(IPCEvent event, Object message);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class IPC {
		public native void on(String ev, IPCEventHandler run);
		public native void removeListener(String ev, IPCEventHandler run);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class IPCEvent {
		public WebContents sender;
	}
}
