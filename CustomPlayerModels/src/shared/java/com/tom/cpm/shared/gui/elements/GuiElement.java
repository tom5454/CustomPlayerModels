package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.Gui;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Box;

public class GuiElement extends Gui {
	protected IGui gui;
	protected Box bounds;
	protected boolean visible = true;
	protected boolean enabled = true;

	public GuiElement(IGui gui) {
		this.gui = gui;
	}

	public Box getBounds() {
		return bounds;
	}

	public GuiElement setBounds(Box bounds) {
		this.bounds = bounds;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(event.isConsumed())return;
		if(mouseClick(event.x, event.y, event.btn))event.consume();
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		if(event.isConsumed())return;
		if(mouseWheel(event.x, event.y, event.btn))event.consume();
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		if(event.isConsumed())return;
		if(mouseRelease(event.x, event.y, event.btn))event.consume();
	}

	@Override
	public void mouseDrag(MouseEvent event) {
		if(event.isConsumed())return;
		if(mouseDrag(event.x, event.y, event.btn))event.consume();
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.isConsumed())return;
		if(keyPressed(event.keyCode, event.charTyped))event.consume();
	}

	@Deprecated
	public boolean mouseClick(int x, int y, int btn) {
		return false;
	}

	@Deprecated
	public boolean mouseWheel(int x, int y, int dir) {
		return false;
	}

	@Deprecated
	public boolean mouseRelease(int x, int y, int btn) {
		return false;
	}

	@Deprecated
	public boolean mouseDrag(int x, int y, int btn) {
		return false;
	}

	@Deprecated
	public boolean keyPressed(int keyCode, char code) {
		return false;
	}
}
