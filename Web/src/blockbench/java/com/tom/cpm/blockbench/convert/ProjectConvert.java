package com.tom.cpm.blockbench.convert;

import java.util.List;
import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpm.blockbench.BBGui;
import com.tom.cpm.blockbench.format.CPMCodec;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.ExportProperties;
import com.tom.cpm.blockbench.proxy.Blockbench.ImportProperties;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.util.PopupDialogs;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.project.ProjectIO;
import com.tom.cpm.shared.editor.util.StoreIDGen;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.I18n;
import com.tom.cpm.web.client.util.JSZip;

import elemental2.core.ArrayBuffer;
import elemental2.core.JsObject;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class ProjectConvert {

	public static Promise<Blob> compile(JsObject options) {
		DomGlobal.console.log("Export");
		Editor editor = new Editor();
		editor.setGui(BBGui.makeFrame());
		return exportWithWarnings(editor).then(__ -> {
			try {
				StoreIDGen storeIDgen = new StoreIDGen();
				Editor.walkElements(editor.elements, storeIDgen::setID);
				ProjectIO.saveProject(editor, editor.project);
				return editor.project.save().then(model -> {
					JsPropertyMap<?> map = JsPropertyMap.of("model", model, "options", options);
					CPMCodec.codec.dispatchEvent("compile", Js.cast(map));
					Blob m = Js.cast(map.get("model"));
					return Promise.resolve(m);
				});
			} catch (Throwable e) {
				return Promise.reject(e);
			}
		});
	}

	public static Promise<Void> exportWithWarnings(Editor editor) {
		return PopupDialogs.runTaskWithWarning(w -> prepExport(editor, w));
	}

	public static Promise<Void> prepExport(Editor editor, Function<List<WarnEntry>, Promise<Void>> openWarn) {
		return new Promise<>((res, rej) -> {
			RenderSystem.withContext(() -> {
				editor.loadDefaultPlayerModel();
				try {
					res.onInvoke(new BlockbenchExport(editor, openWarn).doExport().finally_(() -> {
						editor.dirty = false;
						editor.autoSaveDirty = false;
					}));
				} catch (Throwable e) {
					rej.onInvoke(e);
				}
			});
		});
	}

	public static Promise<Void> parse(ArrayBuffer ab) {
		try {
			DomGlobal.console.log("Parse");
			CPMCodec.codec.dispatchEvent("parse", Js.cast(JsPropertyMap.of("arraybuffer", ab)));
			return RenderSystem.withContext(() -> {
				Editor editor = new Editor();
				editor.setGui(BBGui.makeFrame());
				editor.loadDefaultPlayerModel();
				return new JSZip().loadAsync(ab).then(editor.project::load).then(__ -> {
					return RenderSystem.withContext(() -> {
						try {
							ProjectIO.loadProject(editor, editor.project);
						} catch (Exception e) {
							return Promise.reject(e);
						}
						return Promise.resolve((Void) null);
					});
				}).then(v -> {
					return PopupDialogs.runTaskWithWarning(w -> RenderSystem.withContext(() -> {
						return new BlockbenchImport(editor, w).doImport();
					}));
				});
			});
		} catch (Throwable e) {
			return Promise.reject(e);
		}
	}

	public static float ceil(float val) {
		return (float) Math.ceil(val - 0.01);
	}

	public static float floor(float val) {
		return (float) Math.floor(val + 0.01);
	}

	public static void open() {
		ImportProperties pr = new ImportProperties();
		pr.extensions = new String[] {"cpmproject"};
		pr.type = "Customizable Player Models Project";
		pr.readtype = "binary";
		pr.resource_id = "cpmproject_files";
		Blockbench.import_(pr, files -> {
			String csname = files[0].name.replace(".cpmproject", "").replaceAll("\\s+", "_").toLowerCase();
			parse(files[0].binaryContent).then(__ -> {
				Project.name = csname;
				Project.geometry_name = csname;
				return null;
			}).catch_(e -> {
				PopupDialogs.displayError(I18n.get("bb-label.error.import"), e);
				return null;
			});
		});
	}

	public static void export() {
		try {
			compile(null).then(content -> {
				ExportProperties pr = new ExportProperties();
				pr.resource_id = "model";
				pr.type = CPMCodec.codec.name;
				pr.extensions = new String[] {CPMCodec.codec.extension};
				pr.name = CPMCodec.codec.fileName();
				pr.startpath = CPMCodec.codec.startPath();
				pr.binaryContent = content;
				pr.custom_writer = Global.isApp() ? CPMCodec.codec::write : null;
				Blockbench.export(pr, CPMCodec.codec::afterDownload);
				return null;
			}).catch_(err -> {
				PopupDialogs.displayError(I18n.get("bb-label.error.export"), err);
				return null;
			});
		} catch (Exception e) {
			PopupDialogs.displayError(I18n.get("bb-label.error.export"), e);
		}
	}

	public static void viewInBB() {
		Frame frm = WebMC.getInstance().getGui().getFrame();
		if(frm instanceof EditorGui) {
			EditorGui eg = (EditorGui) frm;
			Blockbench.focus();
			PopupDialogs.runTaskWithWarning(w -> RenderSystem.withContext(() -> {
				return new BlockbenchImport(eg.getEditor(), w).doImport();
			}));
		}
	}
}
