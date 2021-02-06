package com.tom.cpm.shared.gui.elements;

import java.util.List;

import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.math.Vec2i;

public class DropDownBox<T> extends GuiElement {
	private List<T> values;
	private int selectedId = -1;
	private Frame frm;
	public DropDownBox(Frame frm, List<T> values) {
		super(frm.getGui());
		this.frm = frm;
		this.values = values;
		if(!values.isEmpty())selectedId = 0;
	}

	protected Runnable action;

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		String v = values.size() > selectedId && selectedId >= 0 ? String.valueOf(values.get(selectedId)) : gui.i18nFormat("label.cpm.no_elements");
		int w = gui.textWidth(v);
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(bounds.isInBounds(mouseX, mouseY)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 4, v, color);
		gui.drawTexture(bounds.x + bounds.w - 10, bounds.y + bounds.h / 2 - 4, 8, 8, 24, 8, "editor");
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(enabled && evt.isInBounds(bounds) && !evt.isConsumed()) {
			PopupMenu pmenu = new PopupMenu(gui);
			for (T t : values) {
				pmenu.addButton(String.valueOf(t), () -> {
					setSelected(t);
					if(action != null)
						action.run();
				});
			}
			if(values.isEmpty()) {
				pmenu.addButton(gui.i18nFormat("label.cpm.no_elements"), null).setEnabled(false);
			}
			Vec2i p = evt.getPos();
			pmenu.display(frm, p.x - evt.x + bounds.x, p.y - evt.y + bounds.h + bounds.y, bounds.w);
			evt.consume();
		}
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public void setSelected(T sel) {
		this.selectedId = values.indexOf(sel);
	}

	public T getSelected() {
		return values.size() > selectedId && selectedId >= 0 ? values.get(selectedId) : null;
	}
}
