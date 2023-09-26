package com.tom.cpl.gui;

import java.io.File;
import java.util.List;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;

public abstract class Frame extends Panel {
	protected PopupPanels popup;
	protected Tooltip tooltipBox;
	private KeybindHandler keybindHandler;

	public Frame(IGui gui) {
		super(gui);
		popup = new PopupPanels();
		keybindHandler = new KeybindHandler(gui);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		super.keyPressed(event);
		if(event.matches(gui.getKeyCodes().KEY_ESCAPE)) {
			onClosing();
			event.consume();
		}
		keybindHandler.keyEvent(event);
	}

	protected void onClosing() {
		gui.closeGui();
	}

	public final void init(int width, int height) {
		setBounds(new Box(0, 0, width, height));
		elements.clear();
		initFrame(width, height);
		elements.remove(popup);
		addElement(popup);
		popup.getElements().forEach(e -> {
			if(e instanceof PopupLayer) {
				((PopupLayer)e).onInit();
			}
		});
	}

	public void draw(int mouseX, int mouseY, float partialTicks) {
		tooltipBox = null;
		draw(new MouseEvent(mouseX, mouseY, 0), partialTicks);

		if(tooltipBox != null) {
			Box b = tooltipBox.getBounds();
			int tx = Math.min(mouseX + b.w + 5, bounds.w - 1);
			int ty = Math.min(mouseY + b.h + 5, bounds.h - 1);
			tooltipBox.setBounds(new Box(tx - b.w, ty - b.h, b.w, b.h));

			tooltipBox.draw(new MouseEvent(Integer.MAX_VALUE, Integer.MAX_VALUE, 0), partialTicks);
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.pushMatrix();
		gui.setPosOffset(getBounds());
		gui.setupCut();
		Box bounds = getBounds();
		if(backgroundColor != 0)
			gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		for (GuiElement guiElement : elements) {
			if(guiElement.isVisible()) {
				if(guiElement == popup || popup.getElements().isEmpty())
					guiElement.draw(event.offset(bounds), partialTicks);
				else
					guiElement.draw(event.offset(bounds).cancelled(), partialTicks);
			}
		}
		gui.popMatrix();
		gui.setupCut();
	}

	public abstract void initFrame(int width, int height);

	public void openPopup(PopupPanel popup) {
		for (GuiElement e : this.popup.getElements()) {
			if(e instanceof PopupLayer) {
				PopupLayer l = (PopupLayer) e;
				if(l.popup == popup) {
					this.popup.remove(l);
					this.popup.add(l);
					return;
				}
			}
		}
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

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			if(elements.isEmpty())return;
			gui.pushMatrix();
			gui.setPosOffset(getBounds());
			gui.setupCut();
			Box bounds = getBounds();
			GuiElement top = null;
			for (GuiElement guiElement : elements) {
				if(guiElement.isVisible()) {
					top = guiElement;
				}
			}
			for (GuiElement guiElement : elements) {
				if(guiElement.isVisible()) {
					if(guiElement == top)
						guiElement.draw(event.offset(bounds), partialTicks);
					else
						guiElement.draw(event.offset(bounds).cancelled(), partialTicks);
				}
			}
			elements.removeIf(e -> e instanceof PopupLayer && ((PopupLayer)e).handleClosing());
			gui.popMatrix();
			gui.setupCut();
		}

		public void filesDropped(List<File> files) {
			GuiElement top = null;
			for (GuiElement guiElement : elements) {
				if(guiElement.isVisible()) {
					top = guiElement;
				}
			}
			if(top != null && top instanceof PopupLayer)
				((PopupLayer)top).filesDropped(files);
		}
	}

	public class PopupLayer extends Panel {
		private Button close;
		private PopupPanel popup;
		private TitleBar title;
		private boolean closing, rendering;
		private int highlightPopup;

		public PopupLayer(PopupPanel panel) {
			super(Frame.this.gui);
			setBackgroundColor(gui.getColors().popup_background);
			this.popup = panel;

			if(panel.hasDecoration()) {
				close = new ButtonIcon(gui, "editor", 16, 0, true, popup::close);
				addElement(close);

				title = new TitleBar(gui, panel.getTitle());
				addElement(title);
				title.setBounds(new Box(1, 1, 0, 0));
			}

			addElement(panel);
			panel.setLayer(this);

			setPopupPosition();
		}

		public void onInit() {
			popup.onInit();
			setPopupPosition();
		}

		private void setPopupPosition() {
			Box pb = popup.getBounds();
			if(!popup.hasDecoration()) {
				setBounds(new Box(pb.x, pb.y, pb.w, pb.h));
				popup.setBounds(new Box(0, 0, pb.w, pb.h));
			} else {
				Box bounds = Frame.this.getBounds();
				setBounds(new Box(bounds.w / 2 - pb.w / 2 - 1, bounds.h / 2 - pb.h / 2 - 6, pb.w + 2, pb.h + 12));
				popup.setBounds(new Box(1, 12, pb.w, pb.h));
				close.setBounds(new Box(pb.w - 10, 0, 12, 12));
				title.setBounds(new Box(1, 1, pb.w - 12, 10));
			}
		}

		public void filesDropped(List<File> files) {
			popup.filesDropped(files);
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			rendering = true;
			gui.pushMatrix();
			gui.setPosOffset(bounds);
			gui.setupCut();
			gui.drawBox(0, 0, bounds.w, bounds.h, highlightPopup % 10 > 5 ? gui.getColors().popup_border_notify : gui.getColors().popup_border);
			gui.drawBox(1, 1, bounds.w - 2, bounds.h - 2, gui.getColors().popup_background);
			for (GuiElement guiElement : elements) {
				if(guiElement.isVisible())
					guiElement.draw(event.offset(bounds), partialTicks);
			}
			gui.popMatrix();
			gui.setupCut();
			rendering = false;
			if(highlightPopup > 0)highlightPopup--;
		}

		@Override
		public void mouseClick(MouseEvent event) {
			super.mouseClick(event);
			if (!event.isConsumed() && !event.isInBounds(bounds)) {
				highlightPopup = 20;
			}
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
			if(event.matches(gui.getKeyCodes().KEY_ESCAPE)) {
				if(popup.onEscape()) {
					event.consume();
				}
			} else
				event.consume();
		}

		public void close() {
			if(rendering)closing = true;
			else {
				Frame.this.popup.remove(this);
				popup.onClosed();
			}
		}

		public boolean handleClosing() {
			if(closing) {
				popup.onClosed();
				return true;
			}
			return false;
		}

		public void updateTitle() {
			title.setText(popup.getTitle());
		}

		private class TitleBar extends Panel {
			private Label titleLbl;
			private boolean dragging;
			private int dragX, dragY;

			public TitleBar(IGui gui, String title) {
				super(gui);
				titleLbl = new Label(gui, title);
				titleLbl.setBounds(new Box(1, 1, 0, 0));
				addElement(titleLbl);
			}

			public void setText(String title) {
				titleLbl.setText(title);
			}

			@Override
			public void mouseClick(MouseEvent event) {
				if (event.btn == 0 && event.isInBounds(bounds)) {
					dragging = true;
					dragX = event.x;
					dragY = event.y;
					event.consume();
				}
			}

			@Override
			public void mouseDrag(MouseEvent event) {
				if (dragging) {
					Box b = PopupLayer.this.getBounds();
					int x = b.x + event.x - dragX;
					int y = b.y + event.y - dragY;
					x = MathHelper.clamp(x, 0, Frame.this.bounds.w - b.w);
					y = MathHelper.clamp(y, 0, Frame.this.bounds.h - b.h);
					PopupLayer.this.setBounds(new Box(x, y, b.w, b.h));
					event.consume();
				}
			}

			@Override
			public void mouseRelease(MouseEvent event) {
				if (event.btn == 0 && dragging) {
					dragging = false;
					event.consume();
				}
			}
		}
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltipBox = tooltip;
	}

	public void tick() {
	}

	public void logMessage(String msg) {
	}

	public boolean enableChat() {
		return false;
	}

	public void filesDropped(List<File> files) {
		popup.filesDropped(files);
	}

	public KeybindHandler getKeybindHandler() {
		return keybindHandler;
	}

	public void onCrashed(String msg, Throwable e) {

	}

	public boolean hasPopupOpen() {
		return !popup.getElements().isEmpty();
	}
}
