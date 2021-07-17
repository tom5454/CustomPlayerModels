package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;

public class Label extends GuiElement {
	private String text;
	private int color;
	private Tooltip tooltip;
	public Label(IGui gui, String text) {
		super(gui);
		this.text = text;
		this.color = gui.getColors().label_text_color;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(text == null)
			gui.drawText(bounds.x, bounds.y, "~~NULL~~", color);
		else
			gui.drawText(bounds.x, bounds.y, text, color);

		if(event.isHovered(bounds)) {
			if(tooltip != null)tooltip.set();
		}
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public String getText() {
		return text;
	}
}
