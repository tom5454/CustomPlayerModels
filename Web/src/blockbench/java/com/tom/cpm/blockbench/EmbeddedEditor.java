package com.tom.cpm.blockbench;

import java.util.function.Consumer;

import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.ElectronApp.ElectronEventHandler;
import com.tom.cpm.blockbench.proxy.electron.ElectronApp.ElectronWindow;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.web.client.EventHandler;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.render.RenderSystem.WindowEx;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLIFrameElement;
import jsinterop.base.Js;

public class EmbeddedEditor {
	private static WindowEx window;
	private static ElectronWindow electronWindow;
	private static ElectronEventHandler noMenuBar = (e, win) -> {
		electronWindow = win;
		win.removeMenu();
		if(MinecraftObjectHolder.DEBUGGING)win.openDevTools();
	};
	private static Consumer<Editor> openListener;

	public static void setOpenListener(Consumer<Editor> openListener) {
		EmbeddedEditor.openListener = openListener;
	}

	public static void open() {
		if(window == null) {
			if(Electron.isElectron() && Electron.app != null) {
				Electron.app.on("browser-window-created", noMenuBar);
				window = Js.uncheckedCast(DomGlobal.window.open(Js.undefined(), "", "toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=1280,height=720"));
				Electron.app.removeListener("browser-window-created", noMenuBar);
			} else {
				window = Js.uncheckedCast(DomGlobal.window.open(Js.undefined(), "", "toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=1280,height=720"));
			}
			runIn(window);
		} else focus();
	}

	public static void focus() {
		if(electronWindow != null)electronWindow.focus();
		else window.focus();
	}

	public static void close() {
		if(window != null)window.close();
		window = null;
	}

	public static HTMLIFrameElement openIframe() {
		if(window == null) {
			HTMLIFrameElement ifrm = Js.cast(DomGlobal.document.createElement("iframe"));
			ifrm.width = "" + (DomGlobal.window.innerWidth * 2 / 3);
			ifrm.height = "" + (DomGlobal.window.innerHeight * 2 / 3);
			ifrm.onload = ___ -> runIn(Js.uncheckedCast(ifrm.contentWindow));
			return ifrm;
		} else {
			return null;
		}
	}

	public static void runIn(WindowEx window) {
		window.onunload = e -> {
			EmbeddedEditor.window = null;
			electronWindow = null;
			return null;
		};
		window.document.body.innerHTML = "<h1 id=\"loadingBar\" style=\"font-size: 2em; font-weight: bold; color: white; margin: 40px 0px 70px; text-align: center;\">Loading...</h1>";
		window.document.body.style.background = "#333333";
		window.document.title = "CPM Embedded Editor (Beta) " + WebMC.platform;
		RenderSystem.init(window, EmbeddedEditor::init);
	}

	/*public static void openDialog() {
		HTMLIFrameElement i = openIframe();
		if(i != null) {
			DialogProperties p = new DialogProperties();
			p.id = "cpm_editor_dialog";
			p.title = "CPM Embedded Editor (Beta) " + WebMC.platform;
			p.component = new VueProperties();
			p.width = DomGlobal.window.innerWidth * 3 / 4;
			String id = "cpm_" + UUID.randomUUID();
			p.component.template = "<div id=\"" + id + "\";\"></div>";
			Dialog d = new Dialog(p);
			d.show();
			HTMLDivElement div = Js.cast(DomGlobal.document.getElementById(id));
			div.childNodes.delete(0);
			div.append(i);
		}
	}

	public static void openTab() {
		if(window == null) {
			HTMLIFrameElement ifrm = openIframe();
			if(ifrm != null) {//Fix selection
				HTMLDivElement start = Js.cast(DomGlobal.document.getElementById("start_screen"));
				HTMLDivElement div = Js.cast(DomGlobal.document.createElement("div"));
				div.id = "cpm_embedded_editor";
				div.style.display = "none";
				start.after(div);
				div.append(ifrm);
				DummyProject dp = new DummyProject();
				dp.__cpm_marker__ = "dummy";
				dp.textures = new JsArray<>();
				dp.getDisplayName = () -> "CPM Embedded Editor (Beta) " + WebMC.platform;
				dp.close = () -> {
					//TODO close event
					int i = ModelProject.all.indexOf(dp);
					if(i > -1)ModelProject.all.splice(i, 1);
				};
				dp.openSettings = () -> {};
				dp.select = () -> {
					Interface.work_screen.style.display = "none";
					start.style.display = "none";
					div.style.display = "block";
				};
				ModelProject.all.push(dp);
			}
		}
	}*/

	static {
		/*HTMLDivElement start = Js.cast(DomGlobal.document.getElementById("start_screen"));
		MutationObserver o = new MutationObserver((mut, __) -> {
			mut.forEach((e, ___, ____) -> {
				HTMLDivElement ed = Js.cast(DomGlobal.document.getElementById("cpm_embedded_editor"));
				if(start.style.display.equals("none")) {
					ed.style.display = "";
				} else {
					ed.style.display = "none";
				}
				return null;
			});
			return null;
		});
		MutationObserverInit c = MutationObserverInit.create();
		c.setAttributes(true);
		c.setAttributeFilter(new String[] {"style"});
		o.observe(start, c);*/
		PluginStart.cleanup.add(() -> {
			if(window != null)window.close();
			//o.disconnect();
			window = null;
		});
	}

	private static EventHandler init() {
		HTMLElement el = Js.uncheckedCast(RenderSystem.getDocument().getElementById("loadingBar"));
		el.style.display = "none";
		GuiImpl gui = new GuiImpl();
		EditorGui g = null;
		try {
			gui.setGui(g = new EditorGui(gui));
		} catch (Throwable e) {
			gui.onGuiException("Error creating gui", e, true);
		}
		WebMC.getInstance().setGui(gui);
		if(openListener != null && g != null)openListener.accept(g.getEditor());
		openListener = null;
		return gui;
	}

	/*@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DummyProject {
		public String  __cpm_marker__;
		public JsArray<Object> textures;
		public NameSupplier getDisplayName;
		public ProjectAction openSettings, select, close;
	}

	@JsFunction
	public static interface NameSupplier {
		String get();
	}

	@JsFunction
	public static interface ProjectAction {
		void run();
	}*/
}
