package com.tom.cpl.gui.elements;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class Panel extends GuiElement {
	protected List<GuiElement> elements = new ArrayList<>();
	protected int backgroundColor;
	public Panel(IGui gui) {
		super(gui);
	}

	public <T extends GuiElement> T addElement(T elem) {
		elements.add(elem);
		return elem;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.pushMatrix();
		gui.setPosOffset(getBounds());
		gui.setupCut();
		Box bounds = getBounds();
		if(backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		for (GuiElement guiElement : elements) {
			if(guiElement.isVisible())
				guiElement.draw(mouseX - bounds.x, mouseY - bounds.y, partialTicks);
		}
		gui.popMatrix();
		gui.setupCut();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
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
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.keyPressed(event);
				}
			}
		}
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.mouseDrag(event.offset(getBounds()));
				}
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.mouseRelease(event.offset(getBounds()));
				}
			}
		}
		if(event.isInBounds(getBounds()) && backgroundColor > 0)event.consume();
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		if(!elements.isEmpty()) {
			List<GuiElement> elements = new ArrayList<>(this.elements);
			for (int i = elements.size()-1; i >= 0; i--) {
				GuiElement guiElement = elements.get(i);
				if(guiElement.isVisible()) {
					guiElement.mouseWheel(event.offset(getBounds()));
				}
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

	public boolean remove(GuiElement o) {
		return elements.remove(o);
	}
}
