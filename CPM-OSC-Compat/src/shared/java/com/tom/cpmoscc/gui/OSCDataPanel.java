package com.tom.cpmoscc.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpmoscc.CPMOSC;
import com.tom.cpmoscc.OSCReceiver.OSCListener;

public class OSCDataPanel extends Panel implements OSCListener {
	private static final int SIZE = 128;
	private Map<String, List<OSCValue>> values = new ConcurrentHashMap<>();
	private Box hoveredBox;
	private OSCChannel hoveredChannel;
	private Consumer<OSCChannel> select;
	private int w;
	private boolean oscEn;

	public OSCDataPanel(IGui gui, Consumer<OSCChannel> select, int w) {
		super(gui);
		this.select = select;
		this.w = w;
		if(CPMOSC.isEnabled()) {
			CPMOSC.getOsc().setListener(this);
			oscEn = true;
		}

		setBounds(new Box(0, 0, w, SIZE));
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		Map<String, List<OSCValue>> values = new HashMap<>(this.values);
		List<String> keys = new ArrayList<>(values.keySet());
		int cols = w / (SIZE + 5);
		int rows = MathHelper.ceil(keys.size() / (float) cols);
		setBounds(new Box(0, 0, cols * (SIZE + 5), Math.max(1, rows) * (SIZE + 15)));
		gui.pushMatrix();
		gui.setPosOffset(getBounds());
		gui.setupCut();
		if(!oscEn) {
			String[] sp = gui.i18nFormat("osc-label.cpmosc.oscDisabled").split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				gui.drawText(2, 2 + i * 10, sp[i], gui.getColors().label_text_color);
			}
		} else if(values.isEmpty()) {
			String[] sp = gui.i18nFormat("osc-label.cpmosc.noOscData").split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				gui.drawText(2, 2 + i * 10, sp[i], gui.getColors().label_text_color);
			}
		} else {
			long time = System.currentTimeMillis();
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				List<OSCValue> args = values.get(key);
				int x = (i % cols) * (SIZE + 5);
				int y = (i / cols) * (SIZE + 15);
				gui.drawBox(x + 1, y + 1, SIZE, SIZE, gui.getColors().button_disabled);
				if(args.size() == 1 && args.get(0).binary) {
					gui.drawBox(x + 32, y + 32, 64, 64, gui.getColors().button_hover);
					if(args.get(0).value > 0)gui.drawBox(x + 40, y + 40, 48, 48, gui.getColors().button_text_color);
					int update = 128 - ((int) (MathHelper.clamp(time - args.get(0).updateTime, 0, 500) / 500f * 128));
					Box b = new Box(x + 1, y + 1, SIZE, SIZE);
					if(select != null && event.isHovered(b)) {
						update = 64;
						hoveredBox = b;
						hoveredChannel = new OSCChannel(key, i, 0, 1);
					}
					gui.drawBox(x + 1, y + 1, SIZE, SIZE, update << 24 | 0xffffff);
				} else {
					if(!args.isEmpty()) {
						int h = (SIZE - 4) / args.size();
						for (int j = 0; j < args.size(); j++) {
							OSCValue oscValue = args.get(j);
							gui.drawBox(x + 2, y + 2 + j * h, 126, h, gui.getColors().slider_bar);
							int len = 0;
							int start = 0;
							if(oscValue.min == oscValue.max) {
								len = 126;
							} else {
								float zero = (-oscValue.min) / (oscValue.max - oscValue.min);
								if(zero < 0 || zero > 1)
									len = (int) ((oscValue.value - oscValue.min) / (oscValue.max - oscValue.min) * 126);
								else
									len = MathHelper.clamp((int) (Math.abs(oscValue.value / (oscValue.max - oscValue.min)) * 126), 0, 126);
								start = (int) (MathHelper.clamp(zero, 0, 1) * 126);
								if(oscValue.value < 0) {
									start -= len;
								}
							}
							gui.drawBox(x + 2 + start, y + 2 + j * h, len, h, gui.getColors().button_hover);
							int update = 128 - ((int) (MathHelper.clamp(time - oscValue.updateTime, 0, 500) / 500f * 128));
							Box b = new Box(x + 2, y + 2 + j * h, 126, h);
							if(select != null && event.isHovered(b)) {
								update = 64;
								hoveredBox = b;
								hoveredChannel = new OSCChannel(key, i, oscValue.min, oscValue.max);
							}
							gui.drawBox(x + 2, y + 2 + j * h, 126, h, update << 24 | 0xffffff);

							gui.drawText(x + 2, y + 2 + j * h + h / 2, String.format("%.1f", oscValue.min), gui.getColors().label_text_color);
							String v = String.format("%.1f", oscValue.value);
							gui.drawText(x + SIZE / 2 - gui.textWidth(v) / 2, y + 2 + j * h + h / 2, v, gui.getColors().label_text_color);
							v = String.format("%.1f", oscValue.max);
							gui.drawText(x + SIZE - gui.textWidth(v), y + 2 + j * h + h / 2, v, gui.getColors().label_text_color);
						}
					}
				}
				if(gui.textWidth(key) > 120 && event.isHovered(new Box(x + 3, y + SIZE + 2, SIZE, 10))) {
					new Tooltip(gui.getFrame(), key).set();
				}
				gui.drawText(x + 3, y + SIZE + 2, key, gui.getColors().label_text_color);
			}
		}
		gui.popMatrix();
		gui.setupCut();
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(select != null && hoveredBox != null && event.isHovered(hoveredBox)) {
			event.consume();
			select.accept(hoveredChannel);
		}
	}

	public void close() {
		if(oscEn)
			CPMOSC.getOsc().setListener(null);
	}

	@Override
	public void onReceive(String address, List<Object> args) {
		args = new ArrayList<>(args);
		if(args.size() > 0 && args.get(0) instanceof String) {
			address = address + " " + args.get(0);
			args.remove(0);
		}
		final List<Float> as = args.stream().map(a -> {
			if(a instanceof Boolean)return (boolean) a ? 1f : 0f;
			else if(a instanceof Number)return ((Number)a).floatValue();
			return (Float) null;
		}).filter(a -> a != null).collect(Collectors.toList());

		values.compute(address, (__, old) -> {
			if(old == null || old.size() != as.size()) {
				return as.stream().map(OSCValue::new).collect(Collectors.toList());
			}
			for (int i = 0; i < as.size(); i++) {
				old.get(i).setValue(as.get(i));
			}
			return old;
		});
	}

	public static class OSCValue {
		private float value;
		private float min, max;
		private boolean binary;
		private long updateTime;

		public OSCValue(float v) {
			value = v;
			if(v == 0 || v == 1)binary = true;
			min = v;
			max = v;
			updateTime = System.currentTimeMillis();
		}

		public void setValue(float value) {
			this.value = value;
			if(binary && value != 0 && value != 1)
				binary = false;
			min = Math.min(min, value);
			max = Math.max(max, value);
			updateTime = System.currentTimeMillis();
		}
	}

	public static class OSCChannel {
		public final String address;
		public final String arg1;
		public final int argId;
		public final float min, max;

		public OSCChannel(String address, int argId, float min, float max) {
			String[] sp = address.split(" ", 2);
			this.address = sp[0];
			this.arg1 = sp.length > 1 ? sp[1] : null;
			this.argId = argId;
			this.min = min;
			this.max = max;
		}

		public OSCChannel(String address, String arg1, int argId, float min, float max) {
			this.address = address;
			this.arg1 = arg1;
			this.argId = argId;
			this.min = min;
			this.max = max;
		}
	}

	public void reset() {
		values.clear();
		if(CPMOSC.isEnabled()) {
			CPMOSC.getOsc().setListener(this);
			oscEn = true;
		} else oscEn = false;
	}
}
