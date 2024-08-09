package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ComboSlider;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.Keybinds;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;

public class GestureValueSlider extends Panel implements IGestureButton {
	private ValueParameterButtonData data;
	private ComboSlider slider;
	private Button btn;
	private boolean isHovered;

	public GestureValueSlider(IGestureButtonContainer c, ValueParameterButtonData data) {
		super(c.gui());
		this.data = data;
		if(!MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			btn = new Button(gui, data.getName(), null);
			btn.setEnabled(false);
			btn.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("label.cpm.feature_unavailable")));
			addElement(btn);
		} else {
			slider = new ComboSlider(gui, a -> data.getName(), a -> a * data.maxValue, a -> a / data.maxValue);
			slider.getSpinner().setDp(0);
			slider.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.gesture.valueDefaultValue", (int) (data.getDefaultValue() * data.maxValue), Keybinds.RESET_VALUE_LAYER.getSetKey(gui))));
			slider.setAction(() -> {
				data.setValue(slider.getValue());
				c.valueChanged();
			});
			slider.setSteps(1f / data.maxValue);
			slider.setValue(data.getValue());
			addElement(slider);
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		isHovered = event.isHovered(bounds);
		super.draw(event, partialTicks);
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		if(slider != null)slider.setBounds(new Box(0, 0, bounds.w, bounds.h));
		if(btn != null)btn.setBounds(new Box(0, 0, bounds.w, bounds.h));
		return super.setBounds(bounds);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		super.keyPressed(event);
		if(Keybinds.RESET_VALUE_LAYER.isPressed(gui, event)) {
			if (gui.isCtrlDown() || isHovered) {
				if (isHovered)event.consume();
				slider.setValue(data.getDefaultValue());
				data.setValue(data.getDefaultValue());
			}
		}
	}

	@Override
	public void updateKeybinds() {
	}
}
