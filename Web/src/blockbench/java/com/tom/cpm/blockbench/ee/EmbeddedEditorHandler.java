package com.tom.cpm.blockbench.ee;

import com.tom.cpm.blockbench.BBInstance;
import com.tom.cpm.blockbench.BlockBenchFS;
import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.DialogProperties;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.web.client.CPMWebInterface;
import com.tom.cpm.web.client.CPMWebInterface.WebEntry;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.RenderSystem;

import elemental2.core.ArrayBuffer;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class EmbeddedEditorHandler implements WebEntry {
	private static EmbeddedEditor editor;
	private static final boolean isApp = com.tom.cpm.blockbench.proxy.Global.isApp();

	public static Promise<EmbeddedEditor> open() {
		if (editor == null || editor.isClosed()) {
			if (Electron.isElectron() && Electron.app != null) {
				editor = new ElectronEmbeddedEditor();
				return editor.open();
			} else {
				editor = new WebEmbeddedEditor();
				return editor.open();
			}
		} else {
			focus();
			return Promise.resolve(editor);
		}
	}

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {
		@JsProperty(name = "$$ugwt_m__cpmEmbeddedEditorMarker_$$")
		public static JsObject embeddedEditorMarker;

		@JsProperty(name = "$$ugwt_m__embeddedPlatform_$$")
		public static String embeddedPlatform;

		@JsProperty(name = "$$ugwt_m__embeddedHandler_$$")
		public static String embeddedHandler;
	}

	public static boolean isRunningInEmbeddedEditor() {
		return Js.typeof(G.embeddedEditorMarker) != "undefined";
	}

	public static void focus() {
		if(editor != null && !editor.isClosed())editor.focus();
	}

	public static void close() {
		if(editor != null && !editor.isClosed())editor.close();
		editor = null;
	}

	@Override
	public void doLaunch(GuiImpl gui) {
		gui.setGui(new EditorGui(gui));
		editor.onReady();
	}

	@Override
	public WebMC createInstance() {
		return new BBInstance() {

			@Override
			protected String buildPlatformString() {
				return G.embeddedPlatform;
			}
		};
	}

	public static void run() {
		String p = G.embeddedHandler;
		switch (p) {
		case "electron":
			editor = new ElectronEmbeddedEditor();
			break;

		case "web":
			editor = new WebEmbeddedEditor();
			break;

		default:
		{
			HTMLElement el = Js.uncheckedCast(RenderSystem.getDocument().getElementById("loadingBar"));
			el.style.display = "";
			el.innerHTML = "Unknown platform: " + p;
			return;
		}
		}
		FS.setImpl(isApp ? new BlockBenchFS() : new LocalStorageFS(DomGlobal.window));
		CPMWebInterface.init(new EmbeddedEditorHandler());
	}

	public static void openInHost(ArrayBuffer a) {
		editor.openInHost(a);
	}

	public static Promise<String> openFileDialog(boolean save, DialogProperties dialog) {
		return editor.openFileDialog(save, dialog);
	}
}
