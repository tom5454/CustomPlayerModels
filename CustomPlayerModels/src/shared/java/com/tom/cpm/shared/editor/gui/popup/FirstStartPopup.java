package com.tom.cpm.shared.editor.gui.popup;

import java.util.Arrays;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.MarkdownRenderer;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.panel.MouseControlsPanel;
import com.tom.cpm.shared.util.MdResourceLoader;

public class FirstStartPopup extends PopupPanel {
	private MarkdownRenderer mdr;
	private boolean loaded;

	public FirstStartPopup(IGui gui) {
		super(gui);

		mdr = new MarkdownRenderer(gui, new MdResourceLoader(gui::openURL, null, false), "Loading") {

			@Override
			public void browse(String url) {
				if(loaded) {
					if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki"))
						gui.getFrame().openPopup(new WikiBrowserPopup(gui, url));
					else
						gui.openURL(url);
				} else super.browse(url);
			}
		};
		addElement(mdr.setBounds(new Box(5, 5, 100, 100)));
		mdr.registerCustomElement("mouseControls", (r, c, a) -> {
			MouseControlsPanel mcp = new MouseControlsPanel(gui, ModConfig.getCommonConfig());
			mcp.setBounds(c.bounds(5, 0, 250, 95));
			c.y += 100;
			return Arrays.asList(mcp);
		});
		mdr.registerCustomElement("guiScale", (r, c, a) -> {
			Button guiScale = new Button(gui, gui.i18nFormat("button.cpm.config.scale", getScale()), null);
			guiScale.setAction(() -> {
				int scale = ModConfig.getCommonConfig().getInt(ConfigKeys.EDITOR_SCALE, -1) + 1;
				if(scale >= gui.getMaxScale()) {
					scale = gui.canScaleVanilla() ? -1 : 0;
				}
				ModConfig.getCommonConfig().setInt(ConfigKeys.EDITOR_SCALE, scale);
				try {
					EditorGui.rescaleGui = false;
					gui.setScale(scale);
					guiScale.setText(gui.i18nFormat("button.cpm.config.scale", getScale()));
				} finally {
					EditorGui.rescaleGui = true;
				}
			});
			guiScale.setBounds(c.bounds(5, 0, 250, 20));
			c.y += 25;
			return Arrays.asList(guiScale);
		});
		mdr.browse("https://github.com/tom5454/CustomPlayerModels/wiki/FirstStartGuide.md");
		loaded = true;

		onInit();
	}

	@Override
	public void onInit() {
		Box b = gui.getFrame().getBounds();
		int w = b.w * 2 / 3;
		int h = b.h * 3 / 4;
		setBounds(new Box(0, 0, w, h));
		mdr.setBounds(new Box(5, 5, w - 10, h - 10));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.firstStart.title");
	}

	@Override
	public void onClosed() {
		mdr.cleanup();
		ModConfig.getCommonConfig().save();
	}

	private String getScale() {
		int scale = ModConfig.getCommonConfig().getInt(ConfigKeys.EDITOR_SCALE, gui.canScaleVanilla() ? -1 : 0);
		return scale == -1 ? gui.i18nFormat("button.cpm.config.scale.vanilla") : scale == 0 ? gui.i18nFormat("button.cpm.config.scale.auto") : Integer.toString(scale);
	}
}
