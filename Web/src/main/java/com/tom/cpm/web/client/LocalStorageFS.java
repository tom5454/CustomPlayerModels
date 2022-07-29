package com.tom.cpm.web.client;

import com.tom.cpm.web.client.FS.IFS;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileReader;
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

	@Override
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

	@Override
	public boolean exists(String path) {
		if("/mnt".equals(path))return true;
		String[] sp = path.split("/");
		if("mnt".equals(sp[1]))return Js.isTruthy(session.getItem(sp[2]));
		if(!this.enable)return false;
		return Js.isTruthy(local.getItem("m:" + path));
	}

	@Override
	public boolean isDir(String path) {
		if("/mnt".equals(path))return true;
		if(path.startsWith("/mnt"))return false;
		if(!this.enable)return false;
		String i = local.getItem("m:" + path);
		if(i != null)
			return Meta.parse(i).folder;
		return false;
	}

	@Override
	public void mkdir(String path) {
		if(path.startsWith("/mnt"))return;
		if(!this.enable)return;
		String i = local.getItem("m:" + path);
		if(i == null) {
			local.setItem("m:" + path, Global.JSON.stringify(Meta.make(true)));
		}
	}

	@Override
	public Promise<String> getContent(String path) {
		if(path.startsWith("/mnt/")) {
			String[] sp = path.split("/");
			if(sp[1] == "mnt") {
				String f = session.getItem(sp[2]);
				if(f == null)return Promise.reject("File not found");
				return Promise.resolve(f);
			}
			return Promise.reject("File not found");
		}
		if(!this.enable)return Promise.reject("File system not enabled");
		String c = local.getItem("f:" + path);
		if(c == null)return Promise.reject("File not found");
		return Promise.resolve(c);
	}

	@Override
	public boolean setContent(String path, String cont) {
		if(path.startsWith("/mnt")) {
			if(path.startsWith("/mnt/")) {
				String[] sp = path.split("/");
				DomGlobal.fetch("data:application/octet-binary;base64," + cont).
				then(v -> v.blob()).then(b -> {
					FS.saveAs(b, sp[sp.length - 1]);
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

	@Override
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
	public Promise<Object> mount(File file) {
		return new Promise<>((res, rej) -> {
			FileReader reader = new FileReader();
			reader.readAsDataURL(file);
			reader.onload = __ -> {
				String b64 = reader.result.asString().substring(reader.result.asString().indexOf(',') + 1);
				session.setItem(file.name, b64);
				res.onInvoke((Object) null);
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
}