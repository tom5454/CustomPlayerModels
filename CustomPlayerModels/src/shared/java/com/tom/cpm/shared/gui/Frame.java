package com.tom.cpm.shared.gui;

import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Tooltip;
import com.tom.cpm.shared.math.Box;

public abstract class Frame extends Panel {
	protected PopupPanels popup;
	protected Tooltip tooltipBox;

	public Frame(IGui gui) {
		super(gui);
		popup = new PopupPanels();
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		super.keyPressed(event);
		if(!event.isConsumed() && event.matches(gui.getKeyCodes().KEY_ESCAPE)) {
			gui.close();
			event.consume();
		}
	}

	public final void init(int width, int height) {
		setBounds(new Box(0, 0, width, height));
		elements.clear();
		initFrame(width, height);
		addElement(popup);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		tooltipBox = null;
		super.draw(mouseX, mouseY, partialTicks);
		if(tooltipBox != null) {
			Box b = tooltipBox.getBounds();
			int tx = Math.min(mouseX + b.w + 5, bounds.w + b.w + 1);
			int ty = Math.min(mouseY + b.h + 5, bounds.h + b.h + 1);
			tooltipBox.setBounds(new Box(tx - b.w, ty - b.h, b.w, b.h));

			tooltipBox.draw(Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
		}
	}

	public abstract void initFrame(int width, int height);

	public void openPopup(PopupPanel popup) {
		this.popup.add(new PopupLayer(popup));
	}

	public Box getMinBounds() {
		return new Box(0, 0, 0, 0);
	}

	private class PopupPanels extends Panel {

		public PopupPanels() {
			super(Frame.this.gui);
		}

		public void add(PopupLayer popupLayer) {
			addElement(popupLayer);
		}

		public void remove(PopupLayer popupLayer) {
			elements.remove(popupLayer);
		}

		@Override
		public Box getBounds() {
			return Frame.this.bounds;
		}
	}

	public class PopupLayer extends Panel {
		private Button close;
		private PopupPanel popup;
		private Label title;

		public PopupLayer(PopupPanel panel) {
			super(Frame.this.gui);
			setBackgroundColor(gui.getColors().popup_background);
			this.popup = panel;

			if(panel.hasDecoration()) {
				close = new Button(gui, "X", this::close);
				addElement(close);
				title = new Label(gui, panel.getTitle());
				addElement(title);
				title.setBounds(new Box(2, 2, 0, 0));
			}

			addElement(panel);
			panel.setLayer(this);

			Box pb = popup.getBounds();
			if(!panel.hasDecoration()) {
				setBounds(new Box(pb.x, pb.y, pb.w, pb.h));
				popup.setBounds(new Box(0, 0, pb.w, pb.h));
			} else {
				Box bounds = Frame.this.getBounds();
				setBounds(new Box(bounds.w / 2 - pb.w / 2 - 1, bounds.h / 2 - pb.h / 2 - 6, pb.w + 2, pb.h + 12));
				popup.setBounds(new Box(1, 12, pb.w, pb.h));
				close.setBounds(new Box(pb.w - 10, 0, 12, 12));
			}
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			gui.pushMatrix();
			gui.setPosOffset(bounds);
			gui.drawBox(0, 0, bounds.w, bounds.h, gui.getColors().popup_border);
			gui.drawBox(1, 1, bounds.w - 2, bounds.h - 2, gui.getColors().popup_background);
			for (GuiElement guiElement : elements) {
				if(guiElement.isVisible())
					guiElement.draw(mouseX - bounds.x, mouseY - bounds.y, partialTicks);
			}
			gui.popMatrix();
		}

		@Override
		public void mouseClick(MouseEvent event) {
			super.mouseClick(event);
			event.consume();
		}

		@Override
		public void mouseDrag(MouseEvent event) {
			super.mouseDrag(event);
			event.consume();
		}

		@Override
		public void mouseRelease(MouseEvent event) {
			super.mouseRelease(event);
			event.consume();
		}
		@Override
		public void mouseWheel(MouseEvent event) {
			super.mouseWheel(event);
			event.consume();
		}

		@Override
		public void keyPressed(KeyboardEvent event) {
			super.keyPressed(event);
			if(!event.isConsumed() && event.matches(gui.getKeyCodes().KEY_ESCAPE)) {
				if(popup.onEscape()) {
					event.consume();
				}
			} else
				event.consume();
		}

		public void close() {
			Frame.this.popup.remove(this);
		}
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltipBox = tooltip;
	}

	public void logMessage(String msg) {

	}
}
