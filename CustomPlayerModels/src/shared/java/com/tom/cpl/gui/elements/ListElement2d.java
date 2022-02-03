package com.tom.cpl.gui.elements;

import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;

public class ListElement2d<T> extends ListElement<T> {
	private int preferredWidth;

	public ListElement2d(IGui gui, ListModel<T> model, List<T> elems) {
		super(gui, model, elems);
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(evt.isConsumed())return;
		if(bounds.isInBounds(evt.x, evt.y)) {
			int colW = getMaxWidth();
			int cols = Math.max(preferredWidth / colW, 1);
			int colSel = evt.x / colW;
			int rowSel = (evt.y - bounds.y) / 10;
			int elem = colSel + rowSel * cols;
			selected = elem >= 0 && elem < elements.size() ? elements.get(elem) : null;
			model.selected(selected);
			evt.consume();
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		int colW = getMaxWidth();
		int cols = Math.max(preferredWidth / colW, 1);
		for (int i = 0; i < elements.size(); i++) {
			T el = elements.get(i);
			int x = i % cols;
			int y = i / cols;
			boolean hovered = false;
			if (event.isHovered(new Box(bounds.x + x * colW, bounds.y + y * 10, colW, 10))) {
				hovered = true;
				Tooltip tt = model.getTooltip(el);
				if(tt != null)tt.set();
			}
			model.draw(gui, el, bounds.x + x * colW, bounds.y + y * 10, colW, 10, hovered, el == selected);
		}
	}

	@Override
	public Vec2i getSize() {
		int colW = getMaxWidth();
		int cols = Math.max(preferredWidth / colW, 1);
		return new Vec2i(colW * cols, MathHelper.ceil(elements.size() / (float) cols) * 10);
	}

	private int getMaxWidth() {
		int w = 0;
		for (int i = 0; i < elements.size(); i++) {
			T el = elements.get(i);
			w = Math.max(w, model.getWidth(gui, el));
		}
		return w + 10;
	}

	public void setPreferredWidth(int preferredWidth) {
		this.preferredWidth = preferredWidth;
	}
}
