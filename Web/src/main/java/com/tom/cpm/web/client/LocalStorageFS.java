package com.tom.cpm.web.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpm.web.client.FS.IFS;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.java.io.FileNotFoundException;
import com.tom.cpm.web.client.render.RenderSystem;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.URL;
import elemental2.dom.Window;
import elemental2.promise.Promise;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class LocalStorageFS implements IFS {
	private boolean enable;
	private Storage local, session;

	public LocalStorageFS(Window window) {
		try {
			local = WebStorageWindow.of(window).localStorage;
			session = WebStorageWindow.of(window).sessionStorage;
			local.setItem("m:/", Global.JSON.stringify(Meta.make(true)));
			enable = true;
		} catch (Throwable e) {
			DomGlobal.console.warn(e);
			enable = false;
		}
	}

	public static LocalStorageFS getInstance() {
		return (LocalStorageFS) FS.getImpl();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class Meta {
		public boolean folder;

		@JsOverlay
		public static LocalStorageFS.Meta make(boolean folder) {
			LocalStorageFS.Meta m = new Meta();
			m.folder = folder;
			return m;
		}

		@JsOverlay
		public static LocalStorageFS.Meta parse(String in) {
			return Js.cast(Global.JSON.parse(in));
		}
	}

	public String[] list(String path) {
		if("/mnt".equals(path))return JsObject.keys(session).map((v, __, ___) -> "/mnt/" + v).asArray(new String[0]);
		if(!this.enable) {
			if("/".equals(path))return new String[] {"/mnt"};
			return null;
		}
		String pf = "m:" + path;
		String i = local.getItem(pf);
		if(i != null) {
			if(Meta.parse(i).folder) {
				if(!path.endsWith("/"))pf += "/";
				final String fpf = pf;
				JsArray<String> l = new JsArray<>();
				JsObject.keys(local).forEach((name, __, ___) -> {
					if(name.startsWith(fpf) && name.indexOf("/", fpf.length()) == -1 && name != "m:/") {
						l.push(name.substring(2));
					}
					return null;
				});
				if(pf.equals("m:/")) {
					l.push("/mnt");
				}
				return l.asArray(new String[0]);
			} else
				return null;
		} else
			return null;
	}

	public boolean exists(String path) {
		if("/mnt".equals(path))return true;
		String[] sp = path.split("/");
		if("mnt".equals(sp[1]))return Js.isTruthy(session.getItem(sp[2]));
		if(!this.enable)return false;
		return Js.isTruthy(local.getItem("m:" + path));
	}

	public boolean isDir(String path) {
		if("/mnt".equals(path))return true;
		if(path.startsWith("/mnt"))return false;
		if(!this.enable)return false;
		String i = local.getItem("m:" + path);
		if(i != null)
			return Meta.parse(i).folder;
		return false;
	}

	public void mkdir(String path) {
		if(path.startsWith("/mnt"))return;
		if(!this.enable)return;
		String i = local.getItem("m:" + path);
		if(i == null) {
			local.setItem("m:" + path, Global.JSON.stringify(Meta.make(true)));
		}
	}

	@Override
	public String getContent(String path) throws FileNotFoundException {
		if(path.startsWith("/mnt/")) {
			String[] sp = path.split("/");
			if(sp[1] == "mnt") {
				String f = session.getItem(sp[2]);
				if(f == null)throw new FileNotFoundException();
				return f;
			}
			throw new FileNotFoundException();
		}
		if(!this.enable)throw new FileNotFoundException("File system not enabled");
		String c = local.getItem("f:" + path);
		if(c == null)throw new FileNotFoundException();
		return c;
	}

	@Override
	public boolean setContent(String path, String cont) {
		if(path.startsWith("/mnt")) {
			if(path.startsWith("/mnt/")) {
				String[] sp = path.split("/");
				DomGlobal.fetch("data:application/octet-binary;base64," + cont).
				then(v -> v.blob()).then(b -> {
					saveAs(b, sp[sp.length - 1]);
					return null;
				});
				return true;
			}
			return false;
		}
		if(!this.enable)return false;
		String i = local.getItem("m:" + path);
		if(i != null) {
			if(Meta.parse(i).folder)return false;
		} else
			local.setItem("m:" + path, Global.JSON.stringify(Meta.make(false)));
		local.setItem("f:" + path, cont);
		return true;
	}

	public void deleteFile(String path) {
		if(path.startsWith("/mnt"))return;
		if(!this.enable)return;
		String i = local.getItem("m:" + path);
		if(i != null) {
			if(Meta.parse(i).folder) {
				for(String c : this.list(path)) {
					this.deleteFile(c);
				}
				local.removeItem("m:" + path);
			} else {
				local.removeItem("m:" + path);
				local.removeItem("f:" + path);
			}
		}
	}

	@Override
	public Promise<File> mount(elemental2.dom.File file) {
		return new Promise<>((res, rej) -> {
			FileReader reader = new FileReader();
			reader.readAsDataURL(file);
			reader.onload = __ -> {
				String b64 = reader.result.asString().substring(reader.result.asString().indexOf(',') + 1);
				mount(b64, file.name);
				res.onInvoke(new File("/mnt/" + file.name));
				return null;
			};
			reader.onerror = err -> {
				rej.onInvoke(err);
				return null;
			};
		});
	}

	@Override
	public boolean needFileManager() {
		return true;
	}

	@Override
	public void mount(String b64, String name) {
		session.setItem(name, b64);
	}

	public static void saveAs(Blob blob, String name) {
		HTMLLinkElementEx a = Js.uncheckedCast(DomGlobal.document.createElement("a"));
		String url = URL.createObjectURL(blob);
		a.href = url;
		a.download = name;
		DomGlobal.document.body.appendChild(a);
		a.click();
		DomGlobal.setTimeout(__ -> {
			DomGlobal.document.body.removeChild(a);
			URL.revokeObjectURL(url);
		}, 1);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class HTMLLinkElementEx extends HTMLLinkElement {
		public String download;
		public native void click();
	}

	private class FileImpl implements IFile {
		private String[] path;

		public FileImpl(FileImpl file, String name) {
			List<String> l = new ArrayList<>();
			l.addAll(Arrays.asList(file.path));
			l.addAll(Arrays.asList(name.split("/")));
			l.removeIf(f -> f.isEmpty() || f.equals("."));
			path = l.toArray(new String[0]);
		}

		public FileImpl(String name) {
			if(name.startsWith("/"))name = name.substring(1);
			path = name.split("/");
		}

		private FileImpl(String[] path) {
			this.path = path;
		}

		@Override
		public String getName() {
			return path.length == 0 ? "" : path[path.length - 1];
		}

		@Override
		public boolean exists() {
			return LocalStorageFS.this.exists(getAbsolutePath());
		}

		@Override
		public FileImpl getParentFile() {
			return path.length > 1 ? new FileImpl(Arrays.copyOf(path, path.length - 1)) : new FileImpl("/");
		}

		@Override
		public void mkdirs() {
			if(path.length < 2)return;
			for (int i = 1; i <= path.length; i++) {
				LocalStorageFS.this.mkdir("/" + Arrays.stream(path, 0, i).collect(Collectors.joining("/")));
			}
		}

		@Override
		public boolean isDirectory() {
			return LocalStorageFS.this.isDir(getAbsolutePath());
		}

		@Override
		public String getAbsolutePath() {
			List<String> l = new ArrayList<>();
			for(String pe : path) {
				if(pe.equals("..")) {
					if(!l.isEmpty())
						l.remove(l.size() - 1);
				} else {
					l.add(pe);
				}
			}
			return "/" + l.stream().collect(Collectors.joining("/"));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(path);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FileImpl other = (FileImpl) obj;
			if (!Arrays.equals(path, other.path)) return false;
			return true;
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getCanonicalPath() throws IOException {
			return getAbsolutePath();
		}

		@Override
		public boolean isFile() {
			return exists() && !isDirectory();
		}

		@Override
		public String[] list() {
			return LocalStorageFS.this.list(getAbsolutePath());
		}

		@Override
		public boolean isRoot() {
			return getAbsolutePath().equals("/");
		}
	}

	@Override
	public IFile getImpl(IFile parent, String path) {
		if(parent != null)
			return new FileImpl((FileImpl) parent, path);
		else
			return new FileImpl(path);
	}

	@Override
	public String getWorkDir() {
		return "/";
	}

	@Override
	public CompletableFuture<File> openFileChooser(FileChooserPopup fc) {
		CompletableFuture<File> f = new CompletableFuture<>();
		if(fc.isSaveDialog()) {
			IGui gui = fc.getGui();
			fc.getFrame().openPopup(new InputPopup(fc.getFrame(), gui.i18nFormat("web-label.fileName"), gui.i18nFormat("web-label.enterFileName"), v -> f.complete(new File("/mnt/" + v)), null));
		} else {
			HTMLInputElement input = Js.uncheckedCast(RenderSystem.getDocument().createElement("input"));
			input.style.display = "none";
			input.type = "file";
			if(fc.getFilter() instanceof FileFilter) {
				FileFilter ff = (FileFilter) fc.getFilter();
				if(ff.getExt() != null) {
					input.accept = "." + ff.getExt();
				}
			}
			RenderSystem.getDocument().body.appendChild(input);
			input.onchange = e -> {
				RenderSystem.withContext(() -> {
					if(input.files.getLength() > 0) {
						Java.promiseToCf(FS.mount(input.files.getAt(0)), f);
					} else {
						f.complete(null);
					}
				});
				return null;
			};
			input.click();
		}
		return f;
	}
}