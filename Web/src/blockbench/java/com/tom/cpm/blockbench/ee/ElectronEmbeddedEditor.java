package com.tom.cpm.blockbench.ee;

import com.tom.cpm.blockbench.BlockBenchFS;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.BrowserWindowProperties;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.BrowserWindowWebPreferences;
import com.tom.cpm.blockbench.proxy.electron.BrowserWindow.WebContents;
import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.DialogProperties;
import com.tom.cpm.blockbench.proxy.electron.Http.NodeURL;
import com.tom.cpm.blockbench.proxy.electron.Http.NodeURLProperties;
import com.tom.cpm.blockbench.proxy.electron.IPCRenderer.IPCEventHandler;
import com.tom.cpm.blockbench.util.SystemFileChooser;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.resources.Resources;
import com.tom.ugwt.client.UGWTContext;

import elemental2.core.ArrayBuffer;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class ElectronEmbeddedEditor implements EmbeddedEditor {
	private static final String EMBEDDED_PAGE = ".embedded_page.html";
	private BrowserWindow electronWindow;
	private boolean loadingFinished;

	private static final String IPC_CPM_READY = "cpm_on_ready";
	private static final String IPC_OPEN_IN_BB = "cpm_open_bb";
	private static final String IPC_OPEN_IN_BB_FIN = "cpm_open_finish_bb";
	private static final String IPC_OPEN_PROJECT = "cpm_on_ready";
	private static final String IPC_OPEN_PROJECT_FIN = "cpm_on_ready";
	private static final String IPC_SHOW_FC = "cpm_show_file_dialog";
	private static final String IPC_FC_FIN = "cpm_file_dialog_finish";

	public ElectronEmbeddedEditor() {
	}

	@Override
	public Promise<EmbeddedEditor> open() {
		return new Promise<>((res, rej) -> {
			String path = BlockBenchFS.PATH.join(FS.getWorkDir(), EMBEDDED_PAGE);
			FS.setContent(path, Resources.getResource("assets/cpmblockbench/ee/embedded_page.html")).then(__ -> {
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
					String bootstrap = DomGlobal.atob(Resources.getResource("assets/cpmblockbench/ee/electron_bootstrap.js"));
					electronWindow.webContents.executeJavaScript(bootstrap.
							replace("$workDir", DomGlobal.btoa(FS.getWorkDir())).
							replace("$ver", System.getProperty("cpm.version")).
							replace("$platform", DomGlobal.btoa(WebMC.platform)).
							replace("$$$", DomGlobal.btoa("(" + UGWTContext.getAppScript() + ")()"))
							);
				});
				electronWindow.webContents.ipc.on(IPC_CPM_READY, (ev, msg) -> {
					if (ev.sender == electronWindow.webContents) {
						DomGlobal.console.log("Embedded Editor Ready");
						loadingFinished = true;
						res.onInvoke(this);
					}
				});
				electronWindow.webContents.ipc.on(IPC_OPEN_IN_BB, (ev, msg) -> {
					if (ev.sender == electronWindow.webContents) {
						DomGlobal.console.log("Opening CPM project from Embedded editor");
						Blockbench.focus();
						ProjectConvert.parse((ArrayBuffer) msg).then(___ -> {
							electronWindow.webContents.send(IPC_OPEN_IN_BB_FIN, "");
							return null;
						}).catch_(ex -> {
							electronWindow.webContents.send(IPC_OPEN_IN_BB_FIN, "Error: " + ex);
							return null;
						});
					}
				});
				electronWindow.webContents.ipc.on(IPC_SHOW_FC, (ev, msg) -> {
					if (ev.sender == electronWindow.webContents) {
						DomGlobal.console.log("Opening File chooser");
						DialogMessage m = Js.uncheckedCast(elemental2.core.Global.JSON.parse((String) msg));
						if (m.dialog != null) {
							Promise<String> fc = m.isSave ? SystemFileChooser.showSaveDialog(electronWindow, m.dialog) : SystemFileChooser.showOpenDialog(electronWindow, m.dialog);
							fc.then(pth -> {
								DialogResponseMessage r = new DialogResponseMessage();
								r.path = pth;
								r.cancel = pth == null;
								electronWindow.webContents.send(IPC_FC_FIN, elemental2.core.Global.JSON.stringify(r));
								return null;
							}).catch_(ex -> {
								DialogResponseMessage r = new DialogResponseMessage();
								r.error = "Error: " + ex;
								electronWindow.webContents.send(IPC_FC_FIN, elemental2.core.Global.JSON.stringify(r));
								return null;
							});
						}
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
		Electron.getElectron().ipcRenderer.on(IPC_OPEN_PROJECT, (ev, msg) -> {
			DomGlobal.console.log("Opening project", msg);
			ArrayBuffer blob = (ArrayBuffer) msg;
			EmbeddedEditor.openProjectFromBuffer(blob).then(__ -> {
				Electron.getElectron().ipcRenderer.send(IPC_OPEN_PROJECT_FIN, "");
				return null;
			}).catch_(e -> {
				Electron.getElectron().ipcRenderer.send(IPC_OPEN_PROJECT_FIN, "Error: " + e);
				return null;
			});
		});

		Electron.getElectron().ipcRenderer.send(IPC_CPM_READY, "");
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
					electronWindow.webContents.ipc.removeListener(IPC_OPEN_PROJECT_FIN, h[0]);
					electronWindow.focus();
				}
			};
			electronWindow.webContents.ipc.on(IPC_OPEN_PROJECT_FIN, h[0]);
			electronWindow.webContents.send(IPC_OPEN_PROJECT, dt);
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
				Electron.getElectron().ipcRenderer.removeListener(IPC_OPEN_IN_BB_FIN, h[0]);
			};
			Electron.getElectron().ipcRenderer.on(IPC_OPEN_IN_BB_FIN, h[0]);
			Electron.getElectron().ipcRenderer.send(IPC_OPEN_IN_BB, dt);
		});
	}

	@Override
	public Promise<String> openFileDialog(boolean isSave, DialogProperties dialog) {
		return new Promise<>((res, rej) -> {
			DomGlobal.console.log("Opening file chooser");
			IPCEventHandler[] h = new IPCEventHandler[1];
			h[0] = (ev, msg) -> {
				DomGlobal.console.log(msg);
				DialogResponseMessage m = Js.uncheckedCast(elemental2.core.Global.JSON.parse((String) msg));
				Electron.getElectron().ipcRenderer.removeListener(IPC_FC_FIN, h[0]);
				if (m.path != null) {
					res.onInvoke(m.path);
				} else if (m.cancel) {
					res.onInvoke((String) null);
				} else {
					rej.onInvoke(m.error);
				}
			};
			Electron.getElectron().ipcRenderer.on(IPC_FC_FIN, h[0]);
			Electron.getElectron().ipcRenderer.send(IPC_SHOW_FC, elemental2.core.Global.JSON.stringify(DialogMessage.make(dialog, isSave)));
		});
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogMessage {
		public DialogProperties dialog;
		public boolean isSave;

		@JsOverlay
		public static final DialogMessage make(DialogProperties dialog, boolean isSave) {
			DialogMessage m = new DialogMessage();
			m.dialog = dialog;
			m.isSave = isSave;
			return m;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogResponseMessage {
		public String path;
		public boolean cancel;
		public String error;
	}
}
