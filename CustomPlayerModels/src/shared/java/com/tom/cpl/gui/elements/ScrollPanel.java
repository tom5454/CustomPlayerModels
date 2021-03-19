package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class ScrollPanel extends Panel {
	private Panel display;
	private int xScroll, yScroll;

	public ScrollPanel(IGui gui) {
		super(gui);
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		display.mouseWheel(event.offset(bounds).offset(-xScroll, -yScroll));
		if(!event.isConsumed() && event.isInBounds(bounds)) {
			int newScroll = yScroll - event.btn * 5;
			if(newScroll >= 0 && newScroll <= (display.getBounds().h - bounds.h)) {
				yScroll = newScroll;
			}
			event.consume();
		}
	}

	public void setDisplay(Panel display) {
		this.display = display;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.pushMatrix();
		Box bounds = getBounds();
		gui.setPosOffset(bounds);
		if(display.backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, display.backgroundColor);
		gui.setupCut();
		gui.setPosOffset(new Box(-xScroll, -yScroll, bounds.w, bounds.h));
		display.draw(mouseX - bounds.x + xScroll, mouseY - bounds.y + yScroll, partialTicks);
		gui.popMatrix();
		gui.setupCut();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		display.mouseClick(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		display.keyPressed(event);
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		display.mouseDrag(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		display.mouseRelease(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		display.setVisible(visible);
	}
}
