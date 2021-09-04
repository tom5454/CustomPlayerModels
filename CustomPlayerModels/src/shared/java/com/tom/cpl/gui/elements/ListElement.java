package com.tom.cpl.gui.elements;

import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;

public class ListElement<T> extends GuiElement {
	protected ListModel<T> model;
	protected List<T> elements;
	protected T selected;

	public ListElement(IGui gui, ListModel<T> model, List<T> elems) {
		super(gui);
		this.model = model;
		this.elements = elems;
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(evt.isConsumed())return;
		if(bounds.isInBounds(evt.x, evt.y)) {
			int yp = (evt.y - bounds.y) / 10;
			selected = yp >= 0 && yp < elements.size() ? elements.get(yp) : null;
			model.selected(selected);
			evt.consume();
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		for (int i = 0; i < elements.size(); i++) {
			int textColor = gui.getColors().button_text_color;
			T el = elements.get(i);
			if (event.isHovered(new Box(bounds.x, i * 10, bounds.w, 10))) {
				textColor = gui.getColors().button_text_hover;
				Tooltip tt = model.getTooltip(el);
				if(tt != null)tt.set();
			}
			if(el == selected)gui.drawBox(0, i * 10, bounds.w, 10, gui.getColors().select_background);
			gui.drawText(5, i * 10 + 1, model.getDisplay(el), textColor);
		}
	}

	public static abstract interface ListModel<T> {
		Tooltip getTooltip(T el);
		String getDisplay(T el);
		void selected(T selected);
	}

	public Vec2i getSize() {
		int w = 0;
		for (int i = 0; i < elements.size(); i++) {
			T el = elements.get(i);
			w = Math.max(w, gui.textWidth(model.getDisplay(el)));
		}
		return new Vec2i(w + 10, elements.size() * 10);
	}

	public void setSelected(T selected) {
		this.selected = selected;
	}
}
