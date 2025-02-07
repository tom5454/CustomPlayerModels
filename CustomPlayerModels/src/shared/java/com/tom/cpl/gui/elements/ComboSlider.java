package com.tom.cpl.gui.elements;

import java.util.function.Function;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class ComboSlider extends Slider {
	private Spinner spinner;
	private Function<Float, String> valueToString;
	private FloatUnaryOperator value, normalize;

	public ComboSlider(IGui gui, Function<Float, String> valueToString, FloatUnaryOperator value, FloatUnaryOperator normalize) {
		super(gui, valueToString.apply(0f));
		this.valueToString = valueToString;
		this.value = value;
		this.normalize = normalize;
		spinner = new Spinner(gui);
		spinner.addChangeListener(this::updateSpinner);
		spinner.visible = false;
	}

	private void updateSpinner() {
		float max = value.apply(1f);
		if(spinner.getValue() > max) {
			spinner.setValue(max);
		}
		float min = value.apply(0f);
		if(spinner.getValue() < min) {
			spinner.setValue(min);
		}
		setValue0(this.normalize.apply(spinner.getValue()));
		onAction();
	}

	public void setActualValue(float v) {
		setValue0(normalize.apply(v));
		spinner.setValue(v);
	}

	@Override
	public void setValue(float v) {
		setValue0(v);
		spinner.setValue(value.apply(v));
	}

	private void setValue0(float v) {
		super.setValue(v);
		setText(valueToString.apply(getActualValue()));
	}

	public float getActualValue() {
		return value.apply(getValue());
	}

	@Override
	protected void onAction() {
		setText(valueToString.apply(getActualValue()));
		super.onAction();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(spinner.visible)spinner.mouseClick(event);
		if(event.isHovered(bounds)) {
			if(event.btn == 1) {
				spinner.setValue(getActualValue());
				if (bounds.y < 30) {
					spinner.setBounds(new Box(bounds.x + bounds.w / 2, bounds.y + bounds.h - 20, bounds.w / 2, 20));
				} else {
					spinner.setBounds(new Box(bounds.x, bounds.y + bounds.h - 20, bounds.w, 20));
				}
				spinner.visible = true;
				event.consume();
			}
		} else if(!event.isConsumed() && spinner.visible) {
			hideSpinner();
			event.consume();
		}
		super.mouseClick(event);
	}

	@Override
	public void mouseRelease(MouseEvent event) {
		if(spinner.visible)spinner.mouseRelease(event);
		super.mouseRelease(event);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(spinner.visible) {
			if(event.keyCode == gui.getKeyCodes().KEY_ESCAPE) {
				hideSpinner();
				event.consume();
			} else
				spinner.keyPressed(event);
		}
	}

	public void hideSpinner() {
		if (spinner.visible) {
			spinner.visible = false;
			setActualValue(spinner.getValue());
			onAction();
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(spinner.visible) {
			if (bounds.y < 30) {
				gui.drawText(bounds.x, bounds.y + bounds.h / 2 - 4, name, gui.getColors().button_text_color);
			} else {
				gui.drawText(bounds.x, bounds.y, name, gui.getColors().button_text_color);
			}
			spinner.draw(event, partialTicks);
		}
		else super.draw(event, partialTicks);
	}

	public Spinner getSpinner() {
		return spinner;
	}
}
