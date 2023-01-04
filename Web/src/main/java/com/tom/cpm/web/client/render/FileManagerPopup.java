package com.tom.cpm.web.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.Stylesheet;
import com.tom.cpm.web.client.java.io.FileNotFoundException;
import com.tom.cpm.web.client.util.JSZip;
import com.tom.cpm.web.client.util.JSZip.ZipFileProperties;
import com.tom.cpm.web.client.util.JSZip.ZipWriteProperties;

import elemental2.core.JsArray;
import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CSSProperties.MarginLeftUnionType;
import elemental2.dom.CSSProperties.MarginRightUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.DataTransferItem;
import elemental2.dom.Element;
import elemental2.dom.File;
import elemental2.dom.FileReader;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.HTMLStyleElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class FileManagerPopup extends PopupPanel {
	private IFramePanel ifrm;

	@SuppressWarnings("unchecked")
	public FileManagerPopup(IGui gui) {
		super(gui);
		Box w = gui.getFrame().getBounds();
		setBounds(new Box(0, 0, w.w / 3 * 2, w.h / 3 * 2));

		ifrm = new IFramePanel(gui);
		ifrm.setBounds(new Box(0, 0, w.w / 3 * 2, w.h / 3 * 2));
		addElement(ifrm);

		ifrm.iframe().onload = ___ -> {
			HTMLDocument doc = Js.uncheckedCast(ifrm.iframe().contentDocument);
			HTMLStyleElement style = Js.uncheckedCast(doc.createElement("style"));
			style.innerHTML = "a, a:visited {"
					+ "color: #e8eaed;"
					+ "}"
					+ " html {"
					+ "margin: 0px;"
					+ "height: 95%;"
					+ "}"
					+ " body {"
					+ "margin: 0px;"
					+ "min-height: 100%;"
					+ "font-family: Minecraftia;"
					+ "}" + Stylesheet.FONT
					+ " button {"
					+ "padding: 4 10;"
					+ "font-family: Minecraftia;"
					+ "height: 36px;"
					+ "vertical-align: middle;"
					+ "}"
					+ " input {"
					+ "font-family: Minecraftia;"
					+ "}";
			doc.head.appendChild(style);

			HTMLLinkElement materialLink = Js.uncheckedCast(doc.createElement("link"));
			materialLink.href = "https://fonts.googleapis.com/icon?family=Material+Icons";
			materialLink.rel = "stylesheet";
			doc.head.appendChild(materialLink);

			doc.body.style.background = "#202124";
			doc.body.style.color = "#e8eaed";
			doc.body.innerHTML = "<h1>" + gui.i18nFormat("web-label.filemanager.title") + "</h1>"
					+ "<label for=\"path\">" + gui.i18nFormat("web-label.filemanager.path") + "</label>"
					+ "<input id=\"path\" value=\"/\" name=\"path\"> "
					+ "<label for=\"upload\"><button onclick=\"document.getElementById('upload').click()\">" + gui.i18nFormat("web-button.filemanager.add") + "</button></label> "
					+ "<input type=\"file\" id=\"upload\" name=\"upload\" multiple=\"multiple\" style=\"display: none;\">"
					+ "<button id=\"newFolder\" title=\"" + gui.i18nFormat("web-button.filemanager.newFolder") + "\"><span class=\"material-icons\">create_new_folder</span></button> "
					+ "<button id=\"exportZip\" title=\"" + gui.i18nFormat("web-button.filemanager.makeZip") + "\"><span class=\"material-icons\">folder_zip</span><span class=\"material-icons\">download</span></button>"
					+ "<p id=\"info\"></p>"
					+ "<div id=\"fileList\" style=\"background: #303134; margin-left: 8px; margin-right: 8px; padding: 1px 10px;\"></div>";
			doc.body.addEventListener("drop", ev -> {
				ev.preventDefault();
				Element info = doc.getElementById("info");
				List<Promise<Void>> prs = new ArrayList<>();
				MouseEvent e = Js.uncheckedCast(ev);

				if (e.dataTransfer.items != null) {
					for(int i = 0;i<e.dataTransfer.items.length;i++) {
						DataTransferItem dti = e.dataTransfer.items.getAt(i);
						if("file".equals(dti.kind))
							prs.add(loadFile(dti.getAsFile()));
					}
				} else {
					for(int i = 0;i<e.dataTransfer.files.length;i++) {
						prs.add(loadFile(e.dataTransfer.files.getAt(i)));
					}
				}

				Promise.all(prs.toArray(new Promise[0])).then(v -> {
					info.innerHTML = gui.i18nFormat("web-label.filemanager.added");
					initFS();
					return null;
				});
			});
			doc.body.addEventListener("dragover", ev -> ev.preventDefault());

			HTMLInputElement path = Js.uncheckedCast(doc.getElementById("path"));
			path.onkeyup = ev -> {
				initFS();
				return null;
			};
			HTMLInputElement upload = Js.uncheckedCast(doc.getElementById("upload"));
			upload.onchange = ev -> {
				Element info = doc.getElementById("info");
				List<Promise<Void>> prs = new ArrayList<>();
				upload.files.asList().forEach(f -> {
					prs.add(loadFile(f));
				});
				Promise.all(prs.toArray(new Promise[0])).then(v -> {
					info.innerHTML = gui.i18nFormat("web-label.filemanager.added");
					initFS();
					return null;
				}).finally_(() -> {
					upload.value = "";
				});
				return null;
			};
			HTMLButtonElement btn = Js.uncheckedCast(doc.getElementById("newFolder"));
			btn.onclick = ev -> {
				openPopup("<div style=\"padding: 10;\"><h1>" + gui.i18nFormat("web-label.filemanager.folderName") + "</h1>"
						+ "<input id=\"folderNameIn\" value=\"\" name=\"folderNameIn\"><br><br>"
						+ "<button id=\"folderNameOk\">" + gui.i18nFormat("button.cpm.ok") + "</button> <button id=\"folderNameCancel\">" + gui.i18nFormat("button.cpm.cancel") + "</button>"
						+ "</div>");

				HTMLDivElement div = Js.uncheckedCast(doc.getElementById("popupBg"));
				HTMLDivElement divP = Js.uncheckedCast(doc.getElementById("popupDiv"));

				HTMLButtonElement btnOk = Js.uncheckedCast(doc.getElementById("folderNameOk"));
				HTMLButtonElement btnCancel = Js.uncheckedCast(doc.getElementById("folderNameCancel"));
				btnOk.onclick = __ -> {
					HTMLInputElement folderName = Js.uncheckedCast(doc.getElementById("folderNameIn"));
					String name = folderName.value;
					if(!name.isEmpty()) {
						String np = path.value + "/" + name;
						if(np.startsWith("//"))np = np.substring(1);
						if(FS.exists(np)) {
							divP.innerHTML = "<div style=\"padding: 10;\"><h1>" + gui.i18nFormat("web-label.filemanager.fileExists") + "</h1>"
									+ "<button id=\"fileExistOk\">" + gui.i18nFormat("button.cpm.ok") + "</button>"
									+ "</div>";
							HTMLButtonElement btnOk2 = Js.uncheckedCast(doc.getElementById("fileExistOk"));
							btnOk2.onclick = ____ -> {
								div.remove();
								return null;
							};
							return null;
						}
						FS.mkdir(np);
						initFS();
					}
					div.remove();
					return null;
				};
				btnCancel.onclick = __ -> {
					div.remove();
					return null;
				};
				return null;
			};
			btn = Js.uncheckedCast(doc.getElementById("exportZip"));
			btn.onclick = ev -> {
				JSZip zip = new JSZip();
				try {
					exportZip(zip, path.value);
					zip.generateAsync(ZipWriteProperties.make()).
					then(blob -> {
						FS.saveAs(blob, "cpm_fs.zip");
						return null;
					});
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			};
			initFS();
		};
	}

	private void exportZip(JSZip zip, String path) throws FileNotFoundException {
		for(String f : FS.list(path)) {
			String[] sp = f.split("/");
			if(FS.isDir(f)) {
				exportZip(zip.folder(sp[sp.length - 1]), f);
			} else {
				zip.file(sp[sp.length - 1], FS.getContent(f), ZipFileProperties.make());
			}
		}
	}

	private void openPopup(String innerHTML) {
		HTMLDocument doc = Js.uncheckedCast(ifrm.iframe().contentDocument);
		HTMLDivElement div = Js.uncheckedCast(doc.createElement("div"));
		div.style.position = "absolute";
		div.style.backgroundColor = "rgba(64, 64, 64, 0.5)";
		div.id = "popupBg";
		div.style.top = "0";
		div.style.height = HeightUnionType.of("100%");
		div.style.left = "0";
		div.style.width = WidthUnionType.of("100%");

		HTMLDivElement divP = Js.uncheckedCast(doc.createElement("div"));
		divP.id = "popupDiv";
		divP.style.position = "absolute";
		divP.style.top = "20px";
		divP.style.height = HeightUnionType.of("200px");
		divP.style.left = "0";
		divP.style.right = "0";
		divP.style.width = WidthUnionType.of("400px");
		divP.style.background = "#303134";
		divP.style.marginLeft = MarginLeftUnionType.of("auto");
		divP.style.marginRight = MarginRightUnionType.of("auto");
		divP.innerHTML = innerHTML;
		div.append(divP);
		doc.body.append(div);
	}

	private Promise<Void> loadFile(File file) {
		HTMLDocument doc = Js.uncheckedCast(ifrm.iframe().contentDocument);
		HTMLInputElement path = Js.uncheckedCast(doc.getElementById("path"));
		String filePath = path.value + "/" + file.name;
		if(filePath.startsWith("//"))filePath = filePath.substring(1);
		Promise<Void> process;
		if(FS.exists(filePath)) {
			process = new Promise<>((res, rej) -> {
				openPopup("<div style=\"padding: 10;\"><h1>" + gui.i18nFormat("label.cpm.overwrite") + "</h1>"
						+ "<p>" + file.name + "</p>"
						+ "<button id=\"overwriteOk\">" + gui.i18nFormat("button.cpm.ok") + "</button> <button id=\"overwriteCancel\">" + gui.i18nFormat("button.cpm.cancel") + "</button>"
						+ "</div>");
				HTMLButtonElement btnOk = Js.uncheckedCast(doc.getElementById("overwriteOk"));
				HTMLButtonElement btnCancel = Js.uncheckedCast(doc.getElementById("overwriteCancel"));
				HTMLDivElement div = Js.uncheckedCast(doc.getElementById("popupBg"));
				btnOk.onclick = __ -> {
					div.remove();
					res.onInvoke((Void) null);
					return null;
				};
				btnCancel.onclick = __ -> {
					div.remove();
					rej.onInvoke(null);
					return null;
				};
			});
		} else {
			process = Promise.resolve((Void) null);
		}
		final String fp = filePath;
		return process.then(__ -> {
			return new Promise<>((res, rej) -> {
				FileReader reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onload = ___ -> {
					String b64 = reader.result.asString().substring(reader.result.asString().indexOf(',') + 1);
					FS.setContent(fp, b64);
					res.onInvoke((Void) null);
					return null;
				};
				reader.onerror = err -> {
					rej.onInvoke(err);
					return null;
				};
			});
		});
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("web-button.fileManager");
	}

	private void openDir(String pathIn) {
		HTMLDocument doc = Js.uncheckedCast(ifrm.iframe().contentDocument);
		HTMLInputElement path = Js.uncheckedCast(doc.getElementById("path"));
		path.value = pathIn;
		initFS();
	}

	private void initFS() {
		HTMLDocument doc = Js.uncheckedCast(ifrm.iframe().contentDocument);
		HTMLDivElement fl = Js.uncheckedCast(doc.getElementById("fileList"));
		HTMLInputElement path = Js.uncheckedCast(doc.getElementById("path"));
		fl.innerHTML = "";
		String[] files = FS.list(path.value);
		Arrays.sort(files, (a, b) -> {
			boolean dirA = FS.isDir(a);
			boolean dirB = FS.isDir(b);

			if(dirA && !dirB)return -1;
			else if(!dirA && dirB)return 1;

			return a.compareToIgnoreCase(b);
		});
		if(!"/".equals(path.value)) {
			JsArray<String> sp = Js.uncheckedCast(path.value.split("/"));
			sp.pop();
			String name = sp.join("/");
			if(name.length() == 0)name = "/";
			Element ent = doc.createElement("p");
			UUID uuid = UUID.randomUUID();
			ent.innerHTML = "<a href=\"javascript:void(0)\" id=\"" + uuid + "\"><span class=\"material-icons\" style='vertical-align:middle;'>arrow_upward</span> ..</a>";
			fl.appendChild(ent);
			String fname = name;
			doc.getElementById(uuid.toString()).onclick = __ -> {
				openDir(fname);
				return null;
			};
		}
		for (String name : files) {
			String[] sp = name.split("/");
			Element ent = doc.createElement("p");
			UUID uuid = UUID.randomUUID();
			if(FS.isDir(name)) {
				ent.innerHTML = "<a href=\"javascript:void(0)\" id=\"o-" + uuid + "\"><span class=\"material-icons\" style='vertical-align:middle;'>folder</span> " + sp[sp.length - 1] + "</a> <button id=\"d-" + uuid + "\"><span class=\"material-icons\" style='vertical-align:middle;'>delete</span></button>";
			} else {
				String type = "insert_drive_file";
				if(name.endsWith(".png"))type = "image";
				else if(name.endsWith(".cpmproject"))type = "view_in_ar";
				ent.innerHTML = "<a href=\"javascript:void(0)\" id=\"o-" + uuid + "\"><span class=\"material-icons\" style='vertical-align:middle;'>" + type + "</span> " + sp[sp.length - 1] + "</a> <button id=\"d-" + uuid + "\"><span class=\"material-icons\" style='vertical-align:middle;'>delete</span></button>";
			}
			fl.appendChild(ent);
			doc.getElementById("d-" + uuid).onclick = __ -> {
				openPopup("<div style=\"padding: 10;\"><h1>" + gui.i18nFormat("web-label.filemanager.delete") + "</h1>"
						+ "<p>" + name + "</p>"
						+ "<button id=\"deleteOk\">" + gui.i18nFormat("button.cpm.ok") + "</button> <button id=\"deleteCancel\">" + gui.i18nFormat("button.cpm.cancel") + "</button>"
						+ "</div>");
				HTMLButtonElement btnOk = Js.uncheckedCast(doc.getElementById("deleteOk"));
				HTMLButtonElement btnCancel = Js.uncheckedCast(doc.getElementById("deleteCancel"));
				HTMLDivElement div = Js.uncheckedCast(doc.getElementById("popupBg"));
				btnOk.onclick = ___ -> {
					div.remove();
					FS.deleteFile(name);
					initFS();
					return null;
				};
				btnCancel.onclick = ___ -> {
					div.remove();
					return null;
				};
				return null;
			};
			doc.getElementById("o-" + uuid).onclick = __ -> {
				if(FS.isDir(name)) {
					openDir(name);
				} else {
					String[] sp2 = name.split("/");
					FS.getContentFuture(name).
					then(v -> v.blob()).then(b -> {
						FS.saveAs(b, sp2[sp2.length - 1]);
						return null;
					});
				}
				return null;
			};
		}
	}
}
