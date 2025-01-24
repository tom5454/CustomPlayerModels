package com.tom.cpm.blockbench.ee;

import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.DialogProperties;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.project.ProjectIO;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.util.JSZip;

import elemental2.core.ArrayBuffer;
import elemental2.promise.Promise;

public interface EmbeddedEditor {
	Promise<EmbeddedEditor> open();
	void focus();
	void close();
	boolean isClosed();
	void onReady();
	Promise<Void> openProject(ArrayBuffer dt);
	Promise<Void> openInHost(ArrayBuffer a);
	Promise<String> openFileDialog(boolean isSave, DialogProperties dialog);

	public static Promise<Object> openProjectFromBuffer(ArrayBuffer ab) {
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
}
