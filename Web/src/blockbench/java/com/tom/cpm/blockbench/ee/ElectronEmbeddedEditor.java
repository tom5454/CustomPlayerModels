package com.tom.cpm.blockbench.ee;

import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.BlockBenchFS;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.BrowserWindowProperties;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.BrowserWindowWebPreferences;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.WebContents;
import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.Http.NodeURL;
import com.tom.cpm.blockbench.proxy.electron.Http.NodeURLProperties;
import com.tom.cpm.blockbench.proxy.electron.IPCRenderer.IPCEventHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.project.ProjectIO;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.util.JSZip;
import com.tom.ugwt.client.UGWTContext;

import elemental2.core.ArrayBuffer;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class ElectronEmbeddedEditor implements EmbeddedEditor {
	private static final String EMBEDDED_PAGE = ".embedded_page.html";
	private static final String PAGE_SCRIPT = "(function() {document.title = \"CPM Embedded Editor (Beta) $ver\"; _embeddedPlatform = atob(\"$platform\"); _cpmEmbeddedEditorMarker = true; isApp = true; _embeddedHandler = 'electron'; _editorWorkDir = atob('$workDir'); electron = require('electron'); var scr = document.createElement('script'); scr.innerText = atob(\"$$$\"); document.head.appendChild(scr);})()";
	private BrowserWindow electronWindow;
	private boolean loadingFinished;

	public ElectronEmbeddedEditor() {
	}

	@Override
	public Promise<EmbeddedEditor> open() {
		return new Promise<>((res, rej) -> {
			String path = BlockBenchFS.PATH.join(FS.getWorkDir(), EMBEDDED_PAGE);
			FS.setContent(path, DomGlobal.btoa(PAGE_HTML)).then(__ -> {
				BrowserWindowProperties pr = new BrowserWindowProperties();
				pr.width = 1280;
				pr.height = 720;
				pr.webPreferences = new BrowserWindowWebPreferences();
				pr.webPreferences.nodeIntegration = true;
				pr.webPreferences.contextIsolation = false;
				pr.webPreferences.enableRemoteModule = true;
				electronWindow = new BrowserWindow(pr);
				electronWindow.removeMenu();
				if(MinecraftObjectHolder.DEBUGGING)electronWindow.openDevTools();
				ElectronRemoteMain e = Js.uncheckedCast(Global.require("@electron/remote/main"));
				e.enable(electronWindow.webContents);
				NodeURL url = Js.uncheckedCast(Global.require("url"));
				NodeURLProperties up = new NodeURLProperties();
				up.pathname = path;
				up.protocol = "file:";
				up.slashes = true;
				electronWindow.loadURL(url.format(up));
				electronWindow.webContents.on("dom-ready", () -> {
					electronWindow.webContents.executeJavaScript(BlockBenchFS.getLibrary("jszip.min.js"));
					electronWindow.webContents.executeJavaScript(PAGE_SCRIPT.
							replace("$workDir", DomGlobal.btoa(FS.getWorkDir())).
							replace("$ver", System.getProperty("cpm.version")).
							replace("$platform", DomGlobal.btoa(WebMC.platform)).
							replace("$$$", DomGlobal.btoa("(" + UGWTContext.getAppScript() + ")()"))
							);
				});
				electronWindow.webContents.ipc.on("cpm_on_ready", (ev, msg) -> {
					if (ev.sender == electronWindow.webContents) {
						DomGlobal.console.log("Embedded Editor Ready");
						loadingFinished = true;
						res.onInvoke(this);
					}
				});
				electronWindow.webContents.ipc.on("cpm_open_bb", (ev, msg) -> {
					if (ev.sender == electronWindow.webContents) {
						DomGlobal.console.log("Opening CPM project from Embedded editor");
						ProjectConvert.parse((ArrayBuffer) msg).then(___ -> {
							electronWindow.webContents.send("cpm_open_finish_bb", "");
							Blockbench.focus();
							return null;
						}).catch_(ex -> {
							electronWindow.webContents.send("cpm_open_finish_bb", "Error: " + ex);
							return null;
						});
					}
				});
				electronWindow.on("closed", () -> {
					if (!loadingFinished)rej.onInvoke("Window closed");
					electronWindow = null;
				});
				return null;
			}).catch_(e -> {
				rej.onInvoke(e);
				return null;
			});
		});
	}

	@Override
	public void focus() {
		electronWindow.focus();
	}

	@Override
	public void close() {
		electronWindow.close();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class ElectronRemoteMain {
		public native void enable(WebContents web);
	}

	@Override
	public boolean isClosed() {
		return electronWindow == null || electronWindow.isDestroyed();
	}

	@Override
	public void onReady() {
		Electron.getElectron().ipcRenderer.on("cpm_open_project", (ev, msg) -> {
			DomGlobal.console.log("Opening project", msg);
			ArrayBuffer blob = (ArrayBuffer) msg;
			openProject0(blob).then(__ -> {
				Electron.getElectron().ipcRenderer.send("cpm_open_finish", "");
				return null;
			}).catch_(e -> {
				Electron.getElectron().ipcRenderer.send("cpm_open_finish", "Error: " + e);
				return null;
			});
		});

		Electron.getElectron().ipcRenderer.send("cpm_on_ready", "");
	}

	private Promise<Object> openProject0(ArrayBuffer ab) {
		EditorGui eg = (EditorGui) WebMC.getInstance().getGui().getFrame();
		Editor editor = eg.getEditor();
		editor.setInfoMsg.accept(Pair.of(200000, editor.ui.i18nFormat("tooltip.cpm.loading", "BlockBench Transfer")));
		return new JSZip().loadAsync(ab).then(z -> {
			Promise<Object> load;
			if (editor.dirty) {
				load = Promise.reject("Project has unsaved changes");
			} else {
				load = Promise.resolve((Object) null);
			}
			return load.then(__ -> {
				editor.loadDefaultPlayerModel();
				return editor.project.load(z);
			}).then(__ -> {
				try {
					ProjectIO.loadProject(editor, editor.project);
				} catch (Exception e) {
					return Promise.reject(e);
				}
				return Promise.resolve((Void) null);
			}).then(__ -> {
				editor.restitchTextures();
				editor.updateGui();
				editor.setInfoMsg.accept(Pair.of(2000, editor.ui.i18nFormat("tooltip.cpm.loadSuccess", "BlockBench Transfer")));
				return null;
			});
		});
	}

	@Override
	public Promise<Void> openProject(ArrayBuffer dt) {
		return new Promise<>((res, rej) -> {
			DomGlobal.console.log("Opening project in embedded editor");
			if (isClosed()) {
				rej.onInvoke("Window closed");
				return;
			}
			IPCEventHandler[] h = new IPCEventHandler[1];
			h[0] = (ev, msg) -> {
				if (ev.sender == electronWindow.webContents) {
					DomGlobal.console.log(msg);
					res.onInvoke((Void) null);
					electronWindow.webContents.ipc.removeListener("cpm_open_finish", h[0]);
					electronWindow.focus();
				}
			};
			electronWindow.webContents.ipc.on("cpm_open_finish", h[0]);
			electronWindow.webContents.send("cpm_open_project", dt);
		});
	}

	@Override
	public Promise<Void> openInHost(ArrayBuffer dt) {
		return new Promise<>((res, rej) -> {
			DomGlobal.console.log("Opening project in blockbench");
			IPCEventHandler[] h = new IPCEventHandler[1];
			h[0] = (ev, msg) -> {
				DomGlobal.console.log(msg);
				res.onInvoke((Void) null);
				Electron.getElectron().ipcRenderer.removeListener("cpm_open_finish_bb", h[0]);
			};
			Electron.getElectron().ipcRenderer.on("cpm_open_finish_bb", h[0]);
			Electron.getElectron().ipcRenderer.send("cpm_open_bb", dt);
		});
	}
}
