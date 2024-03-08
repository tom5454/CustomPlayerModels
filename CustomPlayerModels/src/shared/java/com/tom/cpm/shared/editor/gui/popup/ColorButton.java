package com.tom.cpm.shared.editor.gui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.math.Box;

public class ColorButton extends Button {
	private static List<Integer> colorCache = new ArrayList<>();
	private IntConsumer actionColor;
	private int color;
	public ColorButton(IGui gui, Frame frame, IntConsumer action) {
		super(gui, "", null);
		this.action = () -> frame.openPopup(new ColorPopup(frame));
		this.actionColor = action;
	}

	public ColorButton(IGui gui, String text, Frame frame, IntConsumer action) {
		super(gui, text, null);
		this.action = () -> frame.openPopup(new ColorPopup(frame));
		this.actionColor = action;
	}

	private ColorButton(IGui gui, Runnable action) {
		super(gui, "", null);
		this.action = action;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		if(event.isHovered(bounds) && tooltip != null)
			tooltip.set();
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);

		if (name == null || name.isEmpty()) {
			gui.drawBox(bounds.x+3, bounds.y+3, bounds.w-6, bounds.h-6, 0xff000000 | this.color);
		} else {
			int w = gui.textWidth(name);
			gui.drawText(bounds.x + bounds.h-8 + (bounds.w - bounds.h-8) / 2 - w / 2, bounds.y + bounds.h / 2 - 4, name, color);

			gui.drawBox(bounds.x+3, bounds.y+3, bounds.h-6, bounds.h-6, gui.getColors().button_border);
			gui.drawBox(bounds.x+4, bounds.y+4, bounds.h-8, bounds.h-8, 0xff000000 | this.color);
		}
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

		public ColorPopup(Frame frm) {
			super(frm.getGui());

			boolean small = frm.getBounds().h < 320;

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

			sliderR.setBounds(new Box(5, small ? 30 : 120, 100, 20));
			sliderG.setBounds(new Box(5, small ? 60 : 150, 100, 20));
			sliderB.setBounds(new Box(5, small ? 90 : 180, 100, 20));

			sliderH.setBounds(new Box(5, 30, 100, 20));
			sliderS.setBounds(new Box(5, 60, 100, 20));
			sliderV.setBounds(new Box(5, 90, 100, 20));

			spinnerR.setBounds(new Box(110, small ? 30 : 120, 40, 20));
			spinnerG.setBounds(new Box(110, small ? 60 : 150, 40, 20));
			spinnerB.setBounds(new Box(110, small ? 90 : 180, 40, 20));

			spinnerH.setBounds(new Box(110, 30,  40, 20));
			spinnerS.setBounds(new Box(110, 60,  40, 20));
			spinnerV.setBounds(new Box(110, 90,  40, 20));

			hexField.setBounds(new Box(5, small ? 120 : 210, 100, 20));

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

			if(!small) {
				addElement(sliderH);
				addElement(sliderS);
				addElement(sliderV);

				addElement(spinnerH);
				addElement(spinnerS);
				addElement(spinnerV);
			}

			addElement(hexField);

			Button ok = new Button(gui, gui.i18nFormat("button.cpm.set"), () -> {
				int color = getColor();
				colorCache.remove(Integer.valueOf(color));
				if(colorCache.size() >= 6)colorCache.remove(0);
				colorCache.add(Integer.valueOf(color));
				actionColor.accept(color);
				close();
			});
			ok.setBounds(new Box(5, small ? 175 : 265, 100, 16));
			addElement(ok);

			for(int i = 0;i<6;i++) {
				int c = i < colorCache.size() ? colorCache.get(colorCache.size() - i - 1) : 0xffffffff;
				ColorButton btn = new ColorButton(gui, () -> loadColor(c));
				btn.setColor(c);
				btn.setBounds(new Box(5 + i * 25, small ? 150 : 240, 20, 16));
				addElement(btn);
			}

			setBounds(new Box(0, 0, 160, small ? 200 : 290));
			updateDisplayText();
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 35, bounds.y + 5, 30, 20, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 34, bounds.y + 6, 28, 18, getColor() | 0xff000000);
		}

		private void loadColor(int color) {
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
			hexField.setText(String.format("%1$06X", color));
		}

		private int getColor() {
			return
					((((int) (sliderR.getValue() * 255)) & 0xff) << 16) |
					((((int) (sliderG.getValue() * 255)) & 0xff) <<  8) |
					( ((int) (sliderB.getValue() * 255)) & 0xff)
					;
		}

		private float getV(Spinner s, float max) {
			float v = s.getValue();
			if (v < 0)s.setValue(0);
			if (v > max)s.setValue(max);
			return s.getValue();
		}

		private void updateColorRGB_spinner() {
			sliderR.setValue(getV(spinnerR, 255) / 255f);
			sliderG.setValue(getV(spinnerG, 255) / 255f);
			sliderB.setValue(getV(spinnerB, 255) / 255f);
			hexField.setText(String.format("%1$06X", getColor()));
			updateRGB();
			updateDisplayText();
		}

		private void updateColorHSV_spinner() {
			sliderH.setValue(getV(spinnerH, 360) / 360f);
			sliderS.setValue(getV(spinnerS, 100) / 100f);
			sliderV.setValue(getV(spinnerV, 100) / 100f);
			updateHSV();
			updateDisplayText();
		}

		private void updateRGB() {
			float[] hsv = RGBtoHSB((int) spinnerR.getValue(), (int) spinnerG.getValue(), (int) spinnerB.getValue());
			sliderH.setValue(hsv[0]);
			sliderS.setValue(hsv[1]);
			sliderV.setValue(hsv[2]);
			spinnerH.setValue(hsv[0] * 360);
			spinnerS.setValue(hsv[1] * 100);
			spinnerV.setValue(hsv[2] * 100);
		}

		private void updateHSV() {
			int color = HSBtoRGB(sliderH.getValue(), sliderS.getValue(), sliderV.getValue());
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
		public void draw(MouseEvent event, float partialTicks) {
			int textw = gui.textWidth(name);
			int color = gui.getColors().button_text_color;
			if(!enabled) {
				color = gui.getColors().button_text_disabled;
			} else if(event.isHovered(bounds)) {
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
		float[] hsv = RGBtoHSB(r, g, b);
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

		return HSBtoRGB(h / 255f, s / 255f, v / 255f);
	}

	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0f;
			float f = h - (float)Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}

	public static float[] RGBtoHSB(int r, int g, int b) {
		float hue, saturation, brightness;
		float[] hsbvals = new float[3];
		int cmax = (r > g) ? r : g;
		if (b > cmax) cmax = b;
		int cmin = (r < g) ? r : g;
		if (b < cmin) cmin = b;

		brightness = (cmax) / 255.0f;
		if (cmax != 0)
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0f + redc - bluec;
			else
				hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}
}
