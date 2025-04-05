package com.tom.cpm.web.client.render;

import java.io.File;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.util.CDNUtil;

import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;

public class ProjectViewerGui extends Frame implements IModelDisplayPanel {
	private Editor editor;
	private String error;
	private boolean loaded;

	public ProjectViewerGui(IGui gui) {
		super(gui);
		this.editor = new Editor();
		this.editor.setUI(gui);
		this.editor.loadDefaultPlayerModel();
		String id = Java.getQueryVariable("project");
		CDNUtil.fetchFromCDN(id).then(d -> {
			return new Promise<Void>((res, rej) -> {
				FS.mount(d, "download.cpmproject");
				editor.load(new File("/mnt/download.cpmproject")).handle((v, ex) -> {
					if(ex != null) {
						Log.warn("Error loading project file", ex);
						ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.load", ex);
						rej.onInvoke(ex);
					} else {
						loaded = true;
						res.onInvoke((Void) null);
					}
					return null;
				});
			});
		}).catch_(err -> {
			DomGlobal.console.log("Error loading model: " + id, err);
			error = String.valueOf(err);
			return null;
		});
	}

	@Override
	public ModelDefinition getSelectedDefinition() {
		return loaded ? editor.definition : null;
	}

	@Override
	public ViewportCamera getCamera() {
		return editor.camera;
	}

	@Override
	public void preRender() {
		editor.preRender();
	}

	@Override
	public boolean doRender() {
		return true;
	}

	@Override
	public void initFrame(int width, int height) {
		ModelDisplayPanel modelPanel = new ModelDisplayPanel(this, this) {

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				if (error != null) {
					setLoadingText(gui.i18nFormat("label.cpm.errorLoadingModel", error));
				}
				super.draw(event, partialTicks);
			}
		};
		modelPanel.setLoadingText(gui.i18nFormat("label.cpm.loading"));
		modelPanel.setBackgroundColor(gui.getColors().panel_background);
		modelPanel.setBounds(new Box(0, 0, width, height));
		addElement(modelPanel);
	}
}
