package com.tom.cpmoscc.gui;

import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.math.Box;
import com.tom.cpmoscc.gui.OSCDataPanel.OSCChannel;

public class OSCSelectPopup extends PopupPanel {
	private OSCDataPanel data;

	public OSCSelectPopup(Frame frame, Consumer<OSCChannel> select) {
		super(frame.getGui());

		Box b = frame.getBounds();
		setBounds(new Box(0, 0, b.w - 50, b.h - 50));

		data = new OSCDataPanel(gui, c -> {
			close();
			select.accept(c);
		}, b.w - 60);

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setDisplay(data);
		scp.setBounds(new Box(5, 5, b.w - 60, b.h - 60));
		addElement(scp);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("osc-label.cpmosc.oscDebugger");
	}

	@Override
	public void onClosed() {
		data.close();
	}
}
