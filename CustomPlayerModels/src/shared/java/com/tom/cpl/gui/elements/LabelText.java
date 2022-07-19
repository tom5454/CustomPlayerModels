package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.LiteralText;

public class LabelText extends GuiElement {
	private static final LiteralText NULL = new LiteralText("~~NULL~~");
	private IText text;
	private float scale;
	private int color;
	private Tooltip tooltip;
	public LabelText(IGui gui, IText text) {
		super(gui);
		this.text = text;
		this.color = gui.getColors().label_text_color;
		this.scale = 1;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(text == null)
			gui.drawFormattedText(bounds.x, bounds.y, NULL, color, scale);
		else
			gui.drawFormattedText(bounds.x, bounds.y, text, color, scale);

		if(event.isHovered(bounds)) {
			if(tooltip != null)tooltip.set();
		}
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setText(IText text) {
		this.text = text;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public IText getText() {
		return text;
	}

	public LabelText setScale(float scale) {
		this.scale = scale;
		return this;
	}

	public float getScale() {
		return scale;
	}
}
