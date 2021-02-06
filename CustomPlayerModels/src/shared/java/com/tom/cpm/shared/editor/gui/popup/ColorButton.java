package com.tom.cpm.shared.editor.gui.popup;

import java.awt.Color;
import java.util.function.IntConsumer;

import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Slider;
import com.tom.cpm.shared.gui.elements.Spinner;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.math.Box;

public class ColorButton extends Button {
	private IntConsumer actionColor;
	private int color;
	public ColorButton(IGui gui, Frame frame, IntConsumer action) {
		super(gui, "", null);
		this.action = () -> frame.openPopup(new ColorPopup(gui));
		this.actionColor = action;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		int bgColor = gui.getColors().button_fill;
		if(!enabled) {
			bgColor = gui.getColors().button_disabled;
		} else if(bounds.isInBounds(mouseX, mouseY)) {
			bgColor = gui.getColors().button_hover;
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawBox(bounds.x+3, bounds.y+3, bounds.w-6, bounds.h-6, 0xff000000 | color);
	}

	public void setColor(int color) {
		this.color = color;
	}

	public class ColorPopup extends PopupPanel {
		private SliderGradient sliderR;
		private SliderGradient sliderG;
		private SliderGradient sliderB;

		private SliderGradient sliderH;
		private SliderGradient sliderS;
		private SliderGradient sliderV;

		private Spinner spinnerR;
		private Spinner spinnerG;
		private Spinner spinnerB;

		private Spinner spinnerH;
		private Spinner spinnerS;
		private Spinner spinnerV;

		private TextField hexField;

		public ColorPopup(IGui gui) {
			super(gui);

			sliderR = new SliderGradient(gui);
			sliderG = new SliderGradient(gui);
			sliderB = new SliderGradient(gui);

			sliderH = new SliderGradient(gui);
			sliderS = new SliderGradient(gui);
			sliderV = new SliderGradient(gui);

			spinnerR = new Spinner(gui);
			spinnerG = new Spinner(gui);
			spinnerB = new Spinner(gui);

			spinnerH = new Spinner(gui);
			spinnerS = new Spinner(gui);
			spinnerV = new Spinner(gui);

			hexField = new TextField(gui);

			int r = (color & 0xff0000) >> 16;
			int g = (color & 0x00ff00) >> 8;
			int b =  color & 0x0000ff;

			spinnerR.setValue(r);
			spinnerG.setValue(g);
			spinnerB.setValue(b);
			updateColorRGB_spinner();

			sliderR.setBounds(new Box(5, 120, 100, 20));
			sliderG.setBounds(new Box(5, 150, 100, 20));
			sliderB.setBounds(new Box(5, 180, 100, 20));

			sliderH.setBounds(new Box(5, 30, 100, 20));
			sliderS.setBounds(new Box(5, 60, 100, 20));
			sliderV.setBounds(new Box(5, 90, 100, 20));

			spinnerR.setBounds(new Box(110, 120, 40, 20));
			spinnerG.setBounds(new Box(110, 150, 40, 20));
			spinnerB.setBounds(new Box(110, 180, 40, 20));

			spinnerH.setBounds(new Box(110, 30,  40, 20));
			spinnerS.setBounds(new Box(110, 60,  40, 20));
			spinnerV.setBounds(new Box(110, 90,  40, 20));

			hexField.setBounds(new Box(5, 210, 100, 20));

			spinnerR.setDp(0);
			spinnerG.setDp(0);
			spinnerB.setDp(0);

			spinnerH.setDp(0);
			spinnerS.setDp(0);
			spinnerV.setDp(0);

			sliderR.setAction(this::updateColorRGB_slider);
			sliderG.setAction(this::updateColorRGB_slider);
			sliderB.setAction(this::updateColorRGB_slider);

			sliderH.setAction(this::updateColorHSV_slider);
			sliderS.setAction(this::updateColorHSV_slider);
			sliderV.setAction(this::updateColorHSV_slider);

			spinnerR.addChangeListener(this::updateColorRGB_spinner);
			spinnerG.addChangeListener(this::updateColorRGB_spinner);
			spinnerB.addChangeListener(this::updateColorRGB_spinner);

			spinnerH.addChangeListener(this::updateColorHSV_spinner);
			spinnerS.addChangeListener(this::updateColorHSV_spinner);
			spinnerV.addChangeListener(this::updateColorHSV_spinner);

			hexField.setEventListener(this::updateHexField);

			addElement(sliderR);
			addElement(sliderG);
			addElement(sliderB);

			addElement(spinnerR);
			addElement(spinnerG);
			addElement(spinnerB);

			addElement(sliderH);
			addElement(sliderS);
			addElement(sliderV);

			addElement(spinnerH);
			addElement(spinnerS);
			addElement(spinnerV);

			addElement(hexField);

			Button ok = new Button(gui, gui.i18nFormat("button.cpm.set"), () -> {
				actionColor.accept(getColor());
				close();
			});
			ok.setBounds(new Box(5, 240, 100, 16));
			addElement(ok);

			setBounds(new Box(0, 0, 160, 270));
			updateDisplayText();
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			super.draw(mouseX, mouseY, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 35, bounds.y + 5, 30, 20, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 34, bounds.y + 6, 28, 18, getColor() | 0xff000000);
		}

		private int getColor() {
			return
					((((int) (sliderR.getValue() * 255)) & 0xff) << 16) |
					((((int) (sliderG.getValue() * 255)) & 0xff) <<  8) |
					( ((int) (sliderB.getValue() * 255)) & 0xff)
					;
		}

		private void updateColorRGB_spinner() {
			sliderR.setValue(spinnerR.getValue() / 255f);
			sliderG.setValue(spinnerG.getValue() / 255f);
			sliderB.setValue(spinnerB.getValue() / 255f);
			hexField.setText(String.format("%1$06X", getColor()));
			updateRGB();
			updateDisplayText();
		}

		private void updateColorHSV_spinner() {
			sliderH.setValue(spinnerH.getValue() / 360f);
			sliderS.setValue(spinnerS.getValue() / 100f);
			sliderV.setValue(spinnerV.getValue() / 100f);
			updateHSV();
			updateDisplayText();
		}

		private void updateRGB() {
			float[] hsv = Color.RGBtoHSB((int) spinnerR.getValue(), (int) spinnerG.getValue(), (int) spinnerB.getValue(), null);
			sliderH.setValue(hsv[0]);
			sliderS.setValue(hsv[1]);
			sliderV.setValue(hsv[2]);
			spinnerH.setValue(hsv[0] * 360);
			spinnerS.setValue(hsv[1] * 100);
			spinnerV.setValue(hsv[2] * 100);
		}

		private void updateHSV() {
			int color = Color.HSBtoRGB(sliderH.getValue(), sliderS.getValue(), sliderV.getValue());
			int r = ((color & 0xff0000) >> 16);
			int g = ((color & 0x00ff00) >> 8);
			int b =  color & 0x0000ff;

			spinnerR.setValue(r);
			spinnerG.setValue(g);
			spinnerB.setValue(b);

			sliderR.setValue(r / 255f);
			sliderG.setValue(g / 255f);
			sliderB.setValue(b / 255f);

			hexField.setText(String.format("%1$06X", getColor()));
		}

		private void updateColorRGB_slider() {
			spinnerR.setValue(sliderR.getValue() * 255);
			spinnerG.setValue(sliderG.getValue() * 255);
			spinnerB.setValue(sliderB.getValue() * 255);
			hexField.setText(String.format("%1$06X", getColor()));
			updateRGB();
			updateDisplayText();
		}

		private void updateColorHSV_slider() {
			spinnerH.setValue(sliderH.getValue() * 360);
			spinnerS.setValue(sliderS.getValue() * 100);
			spinnerV.setValue(sliderV.getValue() * 100);
			updateHSV();
			updateDisplayText();
		}

		private void updateDisplayText() {
			sliderR.setText("R: " + ((int) (sliderR.getValue() * 255)));
			sliderG.setText("G: " + ((int) (sliderG.getValue() * 255)));
			sliderB.setText("B: " + ((int) (sliderB.getValue() * 255)));

			sliderH.setText("H: " + ((int) (sliderH.getValue() * 360)));
			sliderS.setText("S: " + ((int) (sliderS.getValue() * 100)));
			sliderV.setText("V: " + ((int) (sliderV.getValue() * 100)));

			int color = getColor();

			sliderR.setColor(color & 0x00ffff | 0xff000000, color | 0xff0000);
			sliderG.setColor(color & 0xff00ff | 0xff000000, color | 0x00ff00);
			sliderB.setColor(color & 0xffff00 | 0xff000000, color | 0x0000ff);

			int hsv = rgb2hsv(color);

			sliderH.setColor(0, hsv);
			sliderS.setColor(hsv2rgb(hsv & 0xff00ff) | 0xff000000, hsv2rgb(hsv | 0x00ff00));
			sliderV.setColor(hsv2rgb(hsv & 0xffff00) | 0xff000000, hsv2rgb(hsv | 0x0000ff));
		}

		private void updateHexField() {
			try {
				int color = Integer.parseInt(hexField.getText(), 16);
				int r = ((color & 0xff0000) >> 16);
				int g = ((color & 0x00ff00) >> 8);
				int b =  color & 0x0000ff;

				spinnerR.setValue(r);
				spinnerG.setValue(g);
				spinnerB.setValue(b);

				sliderR.setValue(r / 255f);
				sliderG.setValue(g / 255f);
				sliderB.setValue(b / 255f);
				updateRGB();
				updateDisplayText();
			} catch (NumberFormatException e) {
			}
		}

		@Override
		public String getTitle() {
			return gui.i18nFormat("label.cpm.changeColor");
		}
	}

	private static class SliderGradient extends Slider {
		private int left, right;

		public SliderGradient(IGui gui) {
			super(gui, "");
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			int textw = gui.textWidth(name);
			int color = gui.getColors().button_text_color;
			if(!enabled) {
				color = gui.getColors().button_text_disabled;
			} else if(bounds.isInBounds(mouseX, mouseY)) {
				color = gui.getColors().button_text_hover;
			}
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
			if(left == 0) {
				float w = (bounds.w-2) / 6f;
				int x = bounds.x+1;
				int y = bounds.y+1;
				int h = bounds.h-2;
				int sv = right & 0x00ffff;
				for(int i = 0;i<6;i++) {
					int left = 0xff000000 | hsv2rgb(sv | ((42 * i) << 16));
					int right = 0xff000000 | hsv2rgb(sv | ((42 * (i+1)) << 16));
					gui.drawGradientBox((int) (x + i * w), y, (int) Math.ceil(w), h, left, right, left, right);
				}
			} else
				gui.drawGradientBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, left, right, left, right);
			gui.drawBox((int) (bounds.x+1 + v * (bounds.w - 5)), bounds.y+1, 3, bounds.h-2, gui.getColors().slider_bar);
			gui.drawText(bounds.x + bounds.w / 2 - textw / 2, bounds.y + bounds.h / 2 - 4, name, color);
		}

		private void setColor(int left, int right) {
			this.left = left;
			this.right = right | 0xff000000;
		}
	}

	private static int rgb2hsv(int color) {
		int r = ((color & 0xff0000) >> 16);
		int g = ((color & 0x00ff00) >> 8);
		int b =  color & 0x0000ff;
		float[] hsv = Color.RGBtoHSB(r, g, b, null);
		return
				((((int) (hsv[0] * 255)) & 0xff) << 16) |
				((((int) (hsv[1] * 255)) & 0xff) <<  8) |
				( ((int) (hsv[2] * 255)) & 0xff)
				;
	}

	private static int hsv2rgb(int hsv) {
		int h = ((hsv & 0xff0000) >> 16);
		int s = ((hsv & 0x00ff00) >> 8);
		int v =  hsv & 0x0000ff;

		return Color.HSBtoRGB(h / 255f, s / 255f, v / 255f);
	}
}
