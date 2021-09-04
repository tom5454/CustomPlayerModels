package com.tom.cpm.shared.config;

import java.util.EnumMap;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.SafetyException;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.gui.panel.SafetyPanel;

public abstract class PlayerSpecificConfigKey<V> {
	protected String name;
	private EnumMap<KeyGroup, V> defValue;

	private PlayerSpecificConfigKey(String name, EnumMap<KeyGroup, V> defValue) {
		this.name = name;
		this.defValue = defValue;
	}

	public static <T extends Enum<T>> PlayerSpecificConfigKey<T> createEnum(String name, T[] values, Object... defVal) {
		return new PlayerSpecificConfigKey<T>(name, parseMap(defVal)) {

			@Override
			public GuiElement createConfigElement(SafetyPanel panel) {
				IGui gui = panel.getGui();
				Panel p = new Panel(gui);

				T val = getValueFor(null, panel.uuid, panel.mainConfig);
				Button btn = new Button(gui, formatName(gui, val), null);

				Button clr = new Button(gui, gui.i18nFormat("button.cpm.safetyClear"), null);
				clr.setAction(() -> {
					T v = getValueFor(null, panel.uuid, panel.mainConfig);
					resetValue(panel.getConfig());
					btn.setText(formatName(gui, v));
					clr.setEnabled(false);
				});
				clr.setEnabled(hasValue(panel.getConfig()));

				btn.setAction(() -> {
					T v = getValue(panel.getConfig(), panel.getKeyGroup());
					v = values[(v.ordinal() + 1) % values.length];
					btn.setText(formatName(gui, v));
					setValue(panel.getConfig(), v);
					clr.setEnabled(true);
				});
				btn.setBounds(new Box(5, 0, panel.getBounds().w - 35, 20));
				clr.setBounds(new Box(panel.getBounds().w - 25, 0, 20, 20));

				p.addElement(btn);
				p.addElement(clr);
				p.setBounds(new Box(0, 0, panel.getBounds().w, 20));
				return p;
			}

			private String formatName(IGui gui, T v) {
				return gui.i18nFormat("label.cpm.safety." + name, gui.i18nFormat("label.cpm.safety." + name + "." + v.name().toLowerCase()));
			}

			@Override
			public void setValue(ConfigEntry c, T v) {
				c.setInt(name, v.ordinal());
			}

			@Override
			public T getValue(ConfigEntry c, T d) {
				return values[Math.abs(c.getInt(name, d.ordinal()) % values.length)];
			}
		};
	}

	public static PlayerSpecificConfigKey<Boolean> createBool(String name, Object... defVal) {
		return new PlayerSpecificConfigKey<Boolean>(name, parseMap(defVal)) {

			@Override
			public GuiElement createConfigElement(SafetyPanel panel) {
				IGui gui = panel.getGui();
				Panel p = new Panel(gui);

				Checkbox chbx = new Checkbox(gui, gui.i18nFormat("label.cpm.safety." + name));

				Button clr = new Button(gui, gui.i18nFormat("button.cpm.safetyClear"), null);
				clr.setAction(() -> {
					chbx.setSelected(getValueFor(null, panel.uuid, panel.mainConfig));
					resetValue(panel.getConfig());
					clr.setEnabled(false);
				});
				clr.setEnabled(hasValue(panel.getConfig()));

				chbx.setSelected(getValue(panel.getConfig(), getValueFor(null, panel.uuid, panel.mainConfig)));
				chbx.setAction(() -> {
					boolean v = !chbx.isSelected();
					chbx.setSelected(v);
					setValue(panel.getConfig(), v);
					clr.setEnabled(true);
				});
				chbx.setBounds(new Box(5, 0, panel.getBounds().w - 35, 20));
				clr.setBounds(new Box(panel.getBounds().w - 25, 0, 20, 20));

				p.addElement(chbx);
				p.addElement(clr);
				p.setBounds(new Box(0, 0, panel.getBounds().w, 20));
				return p;
			}

			@Override
			public void setValue(ConfigEntry c, Boolean v) {
				c.setBoolean(name, v);
			}

			@Override
			public Boolean getValue(ConfigEntry c, Boolean d) {
				return c.getBoolean(name, d);
			}

			@Override
			public void checkFor(Player<?, ?> player, BlockReason err) throws SafetyException {
				checkFor(player, v -> v, err);
			}
		};
	}

	public static PlayerSpecificConfigKey<Integer> createInt(String name, int minVal, int maxVal, Object... defVal) {
		return createIntF(name, minVal, maxVal, DoubleUnaryOperator.identity(), DoubleUnaryOperator.identity(), Object::toString, 0, defVal);
	}

	public static PlayerSpecificConfigKey<Integer> createIntF(String name, int minVal, int maxVal, DoubleUnaryOperator sliderFunc, DoubleUnaryOperator revFunc, Function<Integer, String> toStringFunc, float sliderStep, Object... defVal) {
		return new PlayerSpecificConfigKey<Integer>(name, parseMap(defVal)) {

			@Override
			public GuiElement createConfigElement(SafetyPanel panel) {
				IGui gui = panel.getGui();
				Panel p = new Panel(gui);
				int val = getValueFor(null, panel.uuid, panel.mainConfig);
				double min = sliderFunc.applyAsDouble(minVal);
				double div = sliderFunc.applyAsDouble(maxVal) - min;
				Slider slider = new Slider(gui, gui.i18nFormat("label.cpm.safety." + name, toStringFunc.apply(val)));

				Button clr = new Button(gui, gui.i18nFormat("button.cpm.safetyClear"), null);
				clr.setAction(() -> {
					int v = getValueFor(null, panel.uuid, panel.mainConfig);
					resetValue(panel.getConfig());
					slider.setValue((float) ((sliderFunc.applyAsDouble(v) - min) / div));
					slider.setText(gui.i18nFormat("label.cpm.safety." + name, toStringFunc.apply(v)));
					clr.setEnabled(false);
				});
				clr.setEnabled(hasValue(panel.getConfig()));

				slider.setSteps(sliderStep);
				slider.setValue((float) ((sliderFunc.applyAsDouble(val) - min) / div));
				slider.setAction(() -> {
					int v = (int) revFunc.applyAsDouble(slider.getValue() * div + min);
					setValue(panel.getConfig(), v);
					slider.setText(gui.i18nFormat("label.cpm.safety." + name, toStringFunc.apply(v)));
					clr.setEnabled(true);
				});

				slider.setBounds(new Box(5, 0, panel.getBounds().w - 35, 20));
				clr.setBounds(new Box(panel.getBounds().w - 25, 0, 20, 20));

				p.addElement(slider);
				p.addElement(clr);
				p.setBounds(new Box(0, 0, panel.getBounds().w, 20));
				return p;
			}

			@Override
			public void setValue(ConfigEntry c, Integer v) {
				c.setInt(name, v);
			}

			@Override
			public Integer getValue(ConfigEntry c, Integer d) {
				return MathHelper.clamp(c.getInt(name, d), minVal, maxVal);
			}

			@Override
			public void checkFor(Player<?, ?> player, Integer w, BlockReason err) throws SafetyException {
				checkFor(player, o -> w <= o, err);
			}
		};
	}

	public static PlayerSpecificConfigKey<Integer> createIntLog2(String name, int minVal, int maxVal, Function<Integer, String> toStringFunc, Object... defVal) {
		return createIntF(name, minVal, maxVal, MathHelper::log2, v -> Math.pow(2, v), toStringFunc, (float) (1 / (MathHelper.log2(maxVal) - MathHelper.log2(minVal))), defVal);
	}

	@SuppressWarnings("unchecked")
	private static <T> EnumMap<KeyGroup, T> parseMap(Object[] array) {
		EnumMap<KeyGroup, T> map = new EnumMap<>(KeyGroup.class);
		int i = 0;
		if(array.length == 1 || !(array[i] instanceof KeyGroup)) {
			i++;
			putAll(map, KeyGroup.GLOBAL, (T) array[0]);
		}
		for(;i<array.length;i+=2) {
			KeyGroup kg = (KeyGroup) array[i];
			T val = (T) array[i+1];
			putAll(map, kg, val);
		}
		return map;
	}

	private static <T> void putAll(EnumMap<KeyGroup, T> map, KeyGroup key, T value) {
		for (int i = key.ordinal(); i < KeyGroup.VALUES.length; i++) {
			KeyGroup kg = KeyGroup.VALUES[i];
			map.put(kg, value);
		}
	}

	public void checkFor(Player<?, ?> player, Predicate<V> check, BlockReason err) throws SafetyException {
		if(player.isClientPlayer())return;
		if(!check.test(getValueFor(player)))throw new SafetyException(err);
	}

	public void checkFor(Player<?, ?> player, V v, BlockReason err) throws SafetyException {
		throw new SafetyException(err);
	}

	public void checkFor(Player<?, ?> player, BlockReason err) throws SafetyException {
		throw new SafetyException(err);
	}

	public V getValueFor(String server, String uuid, ConfigEntry ce) {
		ConfigEntry gs = ce.getEntry(ConfigKeys.GLOBAL_SETTINGS);
		String[] spf = gs.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase()).split(":", 2);
		BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);
		ConfigEntry fr = ce.getEntry(ConfigKeys.FRIEND_SETTINGS);
		ConfigEntry ss = ce.getEntry(ConfigKeys.SERVER_SETTINGS);

		if(server != null && ss.hasEntry(server)) {
			ConfigEntry e = ss.getEntry(server);
			if(e.hasEntry(ConfigKeys.SAFETY_PROFILE)) {
				spf = e.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase()).split(":", 2);
				profile = SocialConfig.getProfile(spf);
			}
		}

		if(uuid != null && SocialConfig.isFriend(uuid) && fr.hasEntry(name)) {
			return getValue(fr, defValue.get(KeyGroup.FRIEND));
		}

		if(server != null && ss.hasEntry(server)) {
			ConfigEntry e = ss.getEntry(server);
			if(e.hasEntry(name))
				return getValue(e, defValue.get(KeyGroup.GLOBAL));
		}

		ConfigEntry spfs = ce.getEntry(ConfigKeys.SAFETY_PROFILES);
		if(profile == BuiltInSafetyProfiles.CUSTOM) {
			if(spfs.hasEntry(spf[1])) {
				ConfigEntry e = spfs.getEntry(spf[1]);
				if(e.hasEntry(name))
					return getValue(e, defValue.get(KeyGroup.GLOBAL));
			}
		} else {
			V v = profile.getValue(name);
			if(v != null)return v;
		}

		return defValue.get(KeyGroup.GLOBAL);
	}

	public V getValueFor(Player<?, ?> player) {
		String uuid = null;
		if(player != null) {
			uuid = player.getUUID().toString();
			ConfigEntry pl = ModConfig.getCommonConfig().getEntry(ConfigKeys.PLAYER_SETTINGS);
			if(pl.hasEntry(uuid)) {
				ConfigEntry e = pl.getEntry(player.getUUID().toString());
				if(e.hasEntry(name))
					return getValue(e, defValue.get(KeyGroup.GLOBAL));
			}
		}
		String server = MinecraftClientAccess.get().getConnectedServer();

		return getValueFor(server, uuid, ModConfig.getCommonConfig());
	}

	public V getValue(ConfigEntry ce, KeyGroup group) {
		return getValue(ce, defValue.get(group));
	}

	public void resetValue(ConfigEntry ce) {
		ce.clearValue(name);
	}

	public boolean hasValue(ConfigEntry ce) {
		return ce.hasEntry(name);
	}

	public static enum KeyGroup {
		GLOBAL,
		FRIEND,
		;
		public static final KeyGroup[] VALUES = values();
	}

	public abstract GuiElement createConfigElement(SafetyPanel panel);
	public abstract void setValue(ConfigEntry c, V v);
	public abstract V getValue(ConfigEntry c, V d);

	public String getName() {
		return name;
	}

	public void copyValue(ConfigEntry from, ConfigEntry to) {
		if(hasValue(from)) {
			setValue(to, getValue(from, KeyGroup.GLOBAL));
		}
	}
}
