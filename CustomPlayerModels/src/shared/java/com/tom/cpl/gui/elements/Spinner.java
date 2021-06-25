package com.tom.cpl.gui.elements;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.util.TabFocusHandler.Focusable;
import com.tom.cpl.math.Box;
import com.tom.cpm.externals.com.udojava.evalex.Expression.ExpressionException;
import com.tom.cpm.shared.util.ExpressionExt;

public class Spinner extends GuiElement implements Focusable {
	private float value;
	private int dp = 3;
	private List<Runnable> changeListeners = new ArrayList<>();
	private TextField txtf;
	private boolean txtfNeedsUpdate;
	private String error;
	public Spinner(IGui gui) {
		super(gui);
		txtf = new TextField(gui);
		txtf.setEventListener(this::updateTxtf);
		txtf.setText(String.format("%." + dp + "f", value));
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x + 1, bounds.y + 1, bounds.w - 2, bounds.h - 2, enabled ? gui.getColors().button_fill : gui.getColors().button_disabled);
		txtf.draw(mouseX, mouseY, partialTicks);
		Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, bounds.w, bounds.h / 2);
		Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, bounds.w, bounds.h / 2);
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + 1, 8, 8, enabled ? bounds.isInBounds(mouseX, mouseY) && bUp.isInBounds(mouseX, mouseY) ? 16 : 8 : 0, 0, "editor");
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, 8, 8, enabled ? bounds.isInBounds(mouseX, mouseY) && bDown.isInBounds(mouseX, mouseY) ? 16 : 8 : 0, 8, "editor");
		if(txtfNeedsUpdate && !txtf.isFocused()) {
			txtfNeedsUpdate = false;
			txtf.setText(String.format("%." + dp + "f", value));
		}
		if(bounds.isInBounds(mouseX, mouseY) && txtf.isFocused() && error != null) {
			new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.exp_error", error)).set();
		}
		if(txtf.isFocused() && error != null) {
			gui.drawRectangle(bounds.x, bounds.y, bounds.w, bounds.h, 0xffff0000);
		}
	}

	@Override
	public void mouseClick(MouseEvent e) {
		if(bounds.isInBounds(e.x, e.y) && enabled) {
			Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, bounds.w, bounds.h / 2);
			Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, bounds.w, bounds.h / 2);
			float v = gui.isAltDown() && dp > 1 ? (gui.isShiftDown() && dp > 2 ? 0.001f : 0.01f) : (gui.isShiftDown() && dp > 0 ? 0.1f : (gui.isCtrlDown() ? (gui.isShiftDown() ? 100f : 10f) : 1f));
			if(!e.isConsumed()) {
				if(bUp.isInBounds(e.x, e.y)) {
					value += v;
					changeListeners.forEach(Runnable::run);
					txtf.setText(String.format("%." + dp + "f", value));
					e.consume();
				} else if(bDown.isInBounds(e.x, e.y)) {
					value -= v;
					changeListeners.forEach(Runnable::run);
					txtf.setText(String.format("%." + dp + "f", value));
					e.consume();
				}
			}
		}
		txtf.mouseClick(e);
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		txtf.setBounds(new Box(bounds.x, bounds.y, bounds.w - 10, bounds.h));
		return super.setBounds(bounds);
	}

	@Override
	public void keyPressed(KeyboardEvent evt) {
		txtf.keyPressed(evt);
	}

	public void addChangeListener(Runnable r) {
		changeListeners.add(r);
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		double d = Math.pow(10, dp);
		this.value = (float) (((int) (value * d)) / d);
		if(!txtf.isFocused())
			txtf.setText(String.format("%." + dp + "f", value));
		else txtfNeedsUpdate = true;
	}

	public void setDp(int dp) {
		this.dp = dp;
		txtf.setText(String.format("%." + dp + "f", value));
	}

	@Override
	public void setEnabled(boolean enabled) {
		txtf.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	private void updateTxtf() {
		try {
			float value = new ExpressionExt(txtf.getText().replace(',', '.')).eval();
			double d = Math.pow(10, dp);
			this.value = (float) (((int) (value * d)) / d);
			changeListeners.forEach(Runnable::run);
			error = null;
			txtfNeedsUpdate = true;
		} catch (ExpressionException e) {
			error = e.getMessage();
		}
	}

	@Override
	public boolean isFocused() {
		return txtf.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		txtf.setFocused(focused);
	}
}
