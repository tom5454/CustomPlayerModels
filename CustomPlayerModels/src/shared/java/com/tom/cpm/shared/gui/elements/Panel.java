package com.tom.cpm.shared.gui.elements;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Box;

public class Panel extends GuiElement {
	protected List<GuiElement> elements = new ArrayList<>();
	private int backgroundColor;
	public Panel(IGui gui) {
		super(gui);
	}

	public void addElement(GuiElement elem) {
		elements.add(elem);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.pushMatrix();
		gui.setPosOffset(getBounds());
		Box bounds = getBounds();
		if(backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		for (GuiElement guiElement : elements) {
			if(guiElement.isVisible())
				guiElement.draw(mouseX - bounds.x, mouseY - bounds.y, partialTicks);
		}
		gui.popMatrix();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(!elements.isEmpty()) {
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.mouseClick(event.offset(getBounds()));
				}
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		for (int i = elements.size()-1; i >= 0; i--) {
			GuiElement guiElement = elements.get(i);
			if(guiElement.isVisible()) {
				guiElement.keyPressed(event);
			}
		}
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		for (int i = elements.size()-1; i >= 0; i--) {
			GuiElement guiElement = elements.get(i);
			if(guiElement.isVisible()) {
				guiElement.mouseDrag(event.offset(getBounds()));
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		for (int i = elements.size()-1; i >= 0; i--) {
			GuiElement guiElement = elements.get(i);
			if(guiElement.isVisible()) {
				guiElement.mouseRelease(event.offset(getBounds()));
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		for (int i = elements.size()-1; i >= 0; i--) {
			GuiElement guiElement = elements.get(i);
			if(guiElement.isVisible()) {
				guiElement.mouseWheel(event.offset(getBounds()));
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	public IGui getGui() {
		return gui;
	}

	public List<GuiElement> getElements() {
		return elements;
	}
}
