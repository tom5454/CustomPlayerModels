package com.tom.cpl.gui.elements;

import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.gui.panel.ListPanel;

public class ListPicker<T> extends GuiElement {
	private Frame frame;
	private List<T> values;
	private int selectedId = -1;
	private Runnable action;
	private Consumer<ListPanel<T>> listLoader;

	public ListPicker(Frame frm, List<T> values) {
		super(frm.getGui());
		this.frame = frm;
		this.values = values;
		if(!values.isEmpty())selectedId = 0;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		String v = values.size() > selectedId && selectedId >= 0 ? String.valueOf(values.get(selectedId)) : gui.i18nFormat("label.cpm.no_elements");
		int w = gui.textWidth(v);
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 4, v, color);
		gui.drawTexture(bounds.x + bounds.w - 10, bounds.y + bounds.h / 2 - 4, 8, 8, 0, 8, "editor");
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(enabled && evt.isInBounds(bounds) && !evt.isConsumed()) {
			frame.openPopup(new Popup());
			evt.consume();
		}
	}

	@Override
	public GuiElement setBounds(Box b) {
		super.setBounds(b);
		return this;
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

	public void setListLoader(Consumer<ListPanel<T>> listLoader) {
		this.listLoader = listLoader;
	}

	private class Popup extends PopupPanel {

		protected Popup() {
			super(ListPicker.this.gui);

			int w = Math.min(frame.getBounds().w / 4 * 3, 500);
			int h = Math.min(frame.getBounds().h / 4 * 3, 350);

			ListPanel<T> list = new ListPanel<>(gui, values, w - 10, h - 35);
			if(listLoader != null)
				listLoader.accept(list);
			list.setSelected(getSelected());
			list.setBounds(new Box(5, 5, w - 10, h - 35));
			addElement(list);

			Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
				close();
				setSelected(list.getSelected());
				if(action != null)
					action.run();
			});
			btn.setBounds(new Box(5, h - 25, 60, 20));
			addElement(btn);

			setBounds(new Box(0, 0, w, h));
		}

		@Override
		public String getTitle() {
			return gui.i18nFormat("label.cpm.editElement");
		}
	}
}
