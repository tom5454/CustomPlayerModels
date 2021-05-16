package com.tom.cpm.shared.gui;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;

public class ModelsGui extends Frame {
	public ViewportCamera camera = new ViewportCamera();
	private ModelsPanel panel;

	public ModelsGui(IGui gui) {
		super(gui);
		panel = new ModelsPanel(this, camera);
		gui.setCloseListener(c -> {
			panel.onClosed();
			c.run();
		});
	}

	@Override
	public void initFrame(int width, int height) {
		panel.setSize(width, height);
		addElement(panel);

		Button openEditor = new Button(gui, gui.i18nFormat("button.cpm.open_editor"), () -> MinecraftClientAccess.get().openGui(EditorGui::new));
		openEditor.setBounds(new Box(0, 0, 100, 20));
		addElement(openEditor);
	}
}
