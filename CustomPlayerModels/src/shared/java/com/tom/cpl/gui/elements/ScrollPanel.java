package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class ScrollPanel extends Panel {
	private Panel display;
	private int xScroll, yScroll;
	private int enableDrag;
	private int dragX, dragY, xScOld, yScOld;
	private boolean scrollBarSide;

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
		Box b = display.getBounds();
		gui.setPosOffset(new Box(-xScroll, -yScroll, b.w, b.h));
		display.draw(mouseX - bounds.x + xScroll, mouseY - bounds.y + yScroll, partialTicks);
		gui.popMatrix();
		gui.pushMatrix();
		gui.setPosOffset(bounds);
		gui.setupCut();
		float overflowY = bounds.h / (float) display.getBounds().h;
		int scx = scrollBarSide ? 0 : bounds.w - 3;
		if(overflowY < 1) {
			float h = overflowY * bounds.h;
			float scroll = yScroll / (float) (display.getBounds().h - bounds.h);
			int y = (int) (scroll * (bounds.h - h));
			Box bar = new Box(bounds.x + scx, bounds.y + y, 3, (int) h);
			gui.drawBox(scx, y, 3, h, bar.isInBounds(mouseX, mouseY) ? gui.getColors().button_hover : gui.getColors().button_disabled);
		}
		gui.popMatrix();
		gui.setupCut();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(event.offset(bounds).isInBounds(new Box(scrollBarSide ? 0 : bounds.w - 3, 0, 3, bounds.h))) {
			dragX = event.x;
			dragY = event.y;
			xScOld = xScroll;
			yScOld = yScroll;
			enableDrag = 1;
			event.consume();
		}
		display.mouseClick(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		display.keyPressed(event);
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		if(enableDrag != 0) {
			switch (enableDrag) {
			case 1:
			{
				int drag = event.y - dragY;
				int newScroll = yScOld + drag*2;
				if(newScroll >= 0 && newScroll <= (display.getBounds().h - bounds.h)) {
					yScroll = newScroll;
				}
			}
			break;

			default:
				break;
			}
			event.consume();
		} else
			display.mouseDrag(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		if(enableDrag != 0) {
			enableDrag = 0;
			event.consume();
		} else
			display.mouseRelease(event.offset(bounds).offset(-xScroll, -yScroll));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		display.setVisible(visible);
	}

	public void setScrollBarSide(boolean scrollBarSide) {
		this.scrollBarSide = scrollBarSide;
	}
}
