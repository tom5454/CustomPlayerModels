package com.tom.cpm.web.client.render;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpm.web.client.render.GuiImpl.HTMLNativeElement;

import elemental2.dom.HTMLIFrameElement;

public class IFramePanel extends Panel {
	private IFrame iframe;

	public IFramePanel(IGui gui) {
		super(gui);
		iframe = new IFrame();
	}

	private class IFrame extends HTMLNativeElement<HTMLIFrameElement> {

		public IFrame() {
			super("iframe", gui);
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		iframe.setBounds(bounds, visible);
		super.draw(event, partialTicks);
	}

	public HTMLIFrameElement iframe() {
		return iframe.element;
	}
}
