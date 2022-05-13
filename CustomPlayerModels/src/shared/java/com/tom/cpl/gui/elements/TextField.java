package com.tom.cpl.gui.elements;

import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.util.TabFocusHandler.Focusable;
import com.tom.cpl.math.Box;

public class TextField extends GuiElement implements Supplier<IGui>, Focusable {
	private ITextField field;
	private int bgColor;

	public TextField(IGui gui) {
		super(gui);
		field = gui.getNative().getNative(TextField.class, this);
		this.bgColor = gui.getColors().popup_border;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_fill);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, enabled ? bgColor : gui.getColors().button_disabled);
		field.draw(event.x, event.y, partialTicks, bounds);
	}
	@Override
	public void keyPressed(KeyboardEvent evt) {
		if(evt.matches(gui.getKeyCodes().KEY_ENTER) || evt.matches(gui.getKeyCodes().KEY_KP_ENTER)) {
			setFocused(false);
			evt.consume();
		}
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
		boolean isFocused();
		void setFocused(boolean focused);
		int getCursorPos();
		void setCursorPos(int pos);
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

	@Override
	public boolean isFocused() {
		return field.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		field.setFocused(focused);
	}

	public void setBackgroundColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getCursorPos() {
		return field.getCursorPos();
	}

	public void setCursorPos(int pos) {
		field.setCursorPos(pos);
	}
}
