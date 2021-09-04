package com.tom.cpm.shared.gui;

import java.util.UUID;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.panel.SocialPanel;

public class SocialGui extends Frame {
	public ViewportCamera camera = new ViewportCamera();
	private SocialPanel panel;
	private UUID selected;

	public SocialGui(IGui gui) {
		super(gui);
		gui.setCloseListener(r -> {
			if(panel != null)panel.cleanup();
			r.run();
		});
	}

	public SocialGui(IGui gui, UUID uuid) {
		this(gui);
		this.selected = uuid;
	}

	@Override
	public void initFrame(int width, int height) {
		if(panel != null) {
			selected = panel.getSelectedUUID();
			panel.cleanup();
		}
		panel = new SocialPanel(this, width / 3 * 2, height / 3 * 2, camera, selected, true);
		panel.setBounds(new Box(width / 6, height / 6, width / 3 * 2, height / 3 * 2));
		addElement(panel);

		Button settings = new Button(gui, gui.i18nFormat("button.cpm.edit.settings"), () -> MinecraftClientAccess.get().openGui(SettingsGui::new));
		panel.topPanel.add(settings);

		Button btn = new Button(gui, "X", gui::close);
		btn.setBounds(new Box(width / 6 + width / 3 * 2 - 20, height / 6, 20, 20));
		addElement(btn);
	}
}
