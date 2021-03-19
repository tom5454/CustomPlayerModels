package com.tom.cpl.gui.elements;

import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class TextField extends GuiElement implements Supplier<IGui> {
	private ITextField field;
	public TextField(IGui gui) {
		super(gui);
		field = gui.getNative().getNative(TextField.class, this);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, 0xff888888);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, enabled ? 0xffaaaaaa : 0xff444444);
		field.draw(mouseX, mouseY, partialTicks, bounds);
	}
	@Override
	public void keyPressed(KeyboardEvent evt) {
		field.keyPressed(evt);
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		field.mouseClick(evt);
	}

	public static interface ITextField {
		void draw(int mouseX, int mouseY, float partialTicks, Box bounds);
		void keyPressed(KeyboardEvent evt);
		void mouseClick(MouseEvent evt);
		String getText();
		void setText(String txt);
		void setEventListener(Runnable eventListener);
		void setEnabled(boolean enabled);
	}

	public String getText() {
		return field.getText();
	}

	public void setText(String txt) {
		field.setText(txt);
	}

	public void setEventListener(Runnable eventListener) {
		field.setEventListener(eventListener);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		field.setEnabled(enabled);
	}

	@Override
	public IGui get() {
		return gui;
	}
}
