package com.tom.cpm.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;

import com.tom.cpl.command.ArgType;
import com.tom.cpl.command.CommandCtx;
import com.tom.cpl.command.CommandHandler;
import com.tom.cpl.command.LiteralCommandBuilder;
import com.tom.cpl.command.RequiredCommandBuilder;
import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.LiteralText;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetHandler.Scaler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.network.NetworkUtil.ScalingSettings;
import com.tom.cpm.shared.util.ScalingOptions;

public class CommandCPM {
	private static final String SCALING_DEFAULT = "default";

	public static void register(CommandHandler<?> dispatcher) {
		LiteralCommandBuilder cpm = new LiteralCommandBuilder("cpm").
				then(new LiteralCommandBuilder("setskin").
						then(new LiteralCommandBuilder("-f").
								then(new RequiredCommandBuilder("target", ArgType.PLAYER).
										then(new RequiredCommandBuilder("skin", ArgType.STRING, true).
												run(c -> executeSkinChange(c, c.getArgument("skin"), true, true))
												)
										)
								).
						then(new LiteralCommandBuilder("-t").
								then(new RequiredCommandBuilder("target", ArgType.PLAYER).
										then(new RequiredCommandBuilder("skin", ArgType.STRING, true).
												run(c -> executeSkinChange(c, c.getArgument("skin"), false, false))
												)
										)
								).
						then(new LiteralCommandBuilder("-r").
								then(new RequiredCommandBuilder("target", ArgType.PLAYER).
										run(c -> executeSkinChange(c, null, false, true))
										)
								).
						then(new RequiredCommandBuilder("target", ArgType.PLAYER).
								then(new RequiredCommandBuilder("skin", ArgType.STRING, true).
										run(c -> executeSkinChange(c, c.getArgument("skin"), false, true))
										)
								)
						).
				then(new LiteralCommandBuilder("safety").
						then(new LiteralCommandBuilder("recommend").
								then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
										run(c -> executeSafetyRec(c, c.getArgument("enable")))
										)
								).
						then(new LiteralCommandBuilder("set").
								thenAll(CommandCPM::buildSettings)
								)
						).
				then(new LiteralCommandBuilder("kick").
						then(new LiteralCommandBuilder("enable").
								then(new RequiredCommandBuilder("time", ArgType.INT, Pair.of(20, (int)Short.MAX_VALUE)).
										run(c -> executeSetKickTimer(c, c.getArgument("time")))
										)
								).
						then(new LiteralCommandBuilder("disable").
								run(c -> executeSetKickTimer(c, 0))
								).
						then(new LiteralCommandBuilder("message").
								then(new RequiredCommandBuilder("message", ArgType.STRING, true).
										run(CommandCPM::executeSetKickMessage)
										)
								)
						).
				then(new LiteralCommandBuilder("scaling").
						thenAll(CommandCPM::buildScaling)
						).
				then(new LiteralCommandBuilder("animate").
						then(new RequiredCommandBuilder("target", ArgType.PLAYER).
								then(new RequiredCommandBuilder("animation", ArgType.STRING, false).
										setPossibleValues(c -> getAnimationList(c, true)).
										run(c -> setAnimation(c, -1)).
										then(new RequiredCommandBuilder("value", ArgType.INT, Pair.of(0, 255)).
												run(c -> setAnimation(c, c.getArgument("value")))
												)
										)
								)
						).
				then(new LiteralCommandBuilder("detect").
						then(new RequiredCommandBuilder("target", ArgType.PLAYER).
								then(new RequiredCommandBuilder("animation", ArgType.STRING, false).
										setPossibleValues(c -> getAnimationList(c, false)).
										run(c -> getAnimation(c, -1)).
										then(new RequiredCommandBuilder("value", ArgType.INT, Pair.of(0, 255)).
												run(c -> getAnimation(c, c.getArgument("value")))
												)
										)
								)
						).
				then(new LiteralCommandBuilder("effects").
						then(new LiteralCommandBuilder("invisible_glow").
								then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
										run(c -> setInvisGlow(c, c.getArgument("enable")))
										)
								)
						)
				;
		dispatcher.register(cpm, true);
	}

	private static void setInvisGlow(CommandCtx<?> context, boolean en) {
		ModConfig.getWorldConfig().setBoolean(ConfigKeys.ENABLE_INVIS_GLOW, en);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("commands.cpm.effect.invisGlow"), en));
		context.sendSuccess(new FormatText("commands.cpm.requiresRelog"));
	}

	private static void executeSetKickTimer(CommandCtx<?> context, int time) {
		ModConfig.getWorldConfig().setInt(ConfigKeys.KICK_PLAYERS_WITHOUT_MOD, time);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("commands.cpm.kickWOMod"), time));
	}

	private static void executeSetKickMessage(CommandCtx<?> context) {
		String msg = context.getArgument("message");
		ModConfig.getWorldConfig().setString(ConfigKeys.KICK_MESSAGE, msg);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("commands.cpm.kickWOMod.msg"), msg));
	}

	private static List<LiteralCommandBuilder> buildSettings() {
		List<LiteralCommandBuilder> l = new ArrayList<>();
		LiteralCommandBuilder profile = new LiteralCommandBuilder("profile");
		for(BuiltInSafetyProfiles p : BuiltInSafetyProfiles.VALUES) {
			profile.then(new LiteralCommandBuilder(p.name().toLowerCase(Locale.ROOT)).
					run(c -> executeSafetyProfile(c, p))
					);
		}
		l.add(profile);
		for(PlayerSpecificConfigKey<?> e : ConfigKeys.SAFETY_KEYS) {
			LiteralCommandBuilder s = new LiteralCommandBuilder(e.getName());
			s.then(new RequiredCommandBuilder("value", e.getType(), e.getTypeArg()).
					run(c -> executeSetSettings(c, e))
					);
			l.add(s);
		}
		return l;
	}

	private static void executeSafetyRec(CommandCtx<?> context, boolean en) {
		ModConfig.getWorldConfig().setBoolean(ConfigKeys.RECOMMEND_SAFETY_SETTINGS, en);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("commands.cpm.safety.recommend"), en));
	}

	private static <V> void executeSetSettings(CommandCtx<?> context, PlayerSpecificConfigKey<V> key) {
		V value = context.getArgument("value");
		ConfigEntry main = ModConfig.getWorldConfig().getEntry(ConfigKeys.SAFETY_SETTINGS);
		key.setValue(main, value);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("label.cpm.safety." + key.getName()), key.formatValue(value)));
	}

	private static void executeSafetyProfile(CommandCtx<?> context, BuiltInSafetyProfiles profile) {
		ModConfig.getWorldConfig().setString(ConfigKeys.SAFETY_PROFILE, profile.name().toLowerCase(Locale.ROOT));
		ModConfig.getWorldConfig().save();
		FormatText t = profile == BuiltInSafetyProfiles.CUSTOM ? new FormatText("commands.cpm.safety.profile.custom") : new FormatText("label.cpm.safetyProfile." + profile.name().toLowerCase(Locale.ROOT));
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("label.cpm.safetyProfileName"), t));
	}

	@SuppressWarnings("unchecked")
	private static void executeSkinChange(CommandCtx<?> context, String skin, boolean force, boolean save) {
		Object player = context.getArgument("target");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		h.setSkin(player, skin, force, save);
		if(force)context.sendSuccess(new FormatText("commands.cpm.setskin.success.force", context.handler.toStringPlayer(player)));
		else context.sendSuccess(new FormatText("commands.cpm.setskin.success", context.handler.toStringPlayer(player)));
	}

	private static List<LiteralCommandBuilder> buildScaling() {
		List<LiteralCommandBuilder> l = new ArrayList<>();
		for(ScalingOptions o : ScalingOptions.VALUES) {
			String name = o.name().toLowerCase(Locale.ROOT);
			LiteralCommandBuilder m = new LiteralCommandBuilder("method");
			{
				RequiredCommandBuilder t = new RequiredCommandBuilder("target", ArgType.PLAYER);
				Supplier<List<String>> vals = () -> {
					List<String> scls = new ArrayList<>(MinecraftServerAccess.get().getNetHandler().getScaleSetters().getOrDefault(o, Collections.emptyMap()).keySet());
					scls.add(ConfigKeys.SCALING_METHOD_OFF);
					scls.add(SCALING_DEFAULT);
					return scls;
				};
				m.then(new RequiredCommandBuilder("method", ArgType.STRING, false).
						setPossibleValues(vals).
						run(c -> setScalingMethod(c, null, o, c.getArgument("method")))
						);
				t.then(new RequiredCommandBuilder("method", ArgType.STRING, false).
						setPossibleValues(vals).
						run(c -> setScalingMethod(c, c.getArgument("target"), o, c.getArgument("method")))
						);
				m.then(t);
			}
			LiteralCommandBuilder s = new LiteralCommandBuilder(name);
			s.then(new LiteralCommandBuilder("limit").
					then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
							run(c -> setScalingLimit(c, null, o, o.getMin(), c.getArgument("max")))
							).
					then(new RequiredCommandBuilder("min", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
							then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									run(c -> setScalingLimit(c, null, o, c.getArgument("min"), c.getArgument("max")))
									)
							).
					then(new RequiredCommandBuilder("target", ArgType.PLAYER).
							then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									run(c -> setScalingLimit(c, c.getArgument("target"), o, o.getMin(), c.getArgument("max")))
									).
							then(new RequiredCommandBuilder("min", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
											run(c -> setScalingLimit(c, c.getArgument("target"), o, c.getArgument("min"), c.getArgument("max")))
											)
									)
							)
					).
			then(new LiteralCommandBuilder("enabled").
					then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
							run(c -> setScalingEn(c, null, o, c.getArgument("enable")))
							).
					then(new RequiredCommandBuilder("target", ArgType.PLAYER).
							then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
									run(c -> setScalingEn(c, c.getArgument("target"), o, c.getArgument("enable")))
									)
							)
					).
			then(m);
			l.add(s);
		}
		LiteralCommandBuilder all = new LiteralCommandBuilder("all");
		all.then(new LiteralCommandBuilder("enabled").
				then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
						run(c -> setScalingEnAll(c, null, c.getArgument("enable")))
						).
				then(new RequiredCommandBuilder("target", ArgType.PLAYER).
						then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
								run(c -> setScalingEnAll(c, c.getArgument("target"), c.getArgument("enable")))
								)
						)
				);
		l.add(all);
		LiteralCommandBuilder reset = new LiteralCommandBuilder("reset");
		reset.then(new RequiredCommandBuilder("target", ArgType.PLAYER).
				run(c -> resetScaling(c))
				);
		l.add(reset);
		LiteralCommandBuilder debug = new LiteralCommandBuilder("debug");
		debug.then(new RequiredCommandBuilder("target", ArgType.PLAYER).
				run(c -> debugScaling(c))
				);
		l.add(debug);
		return l;
	}

	@SuppressWarnings("unchecked")
	private static void resetScaling(CommandCtx<?> c) {
		Object player = c.getArgument("target");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		PlayerData dt = h.getSNetH(player).cpm$getEncodedModelData();
		dt.rescaleToTarget(h, player, null);
		c.sendSuccess(new FormatText("commands.cpm.scalingReset", c.handler.toStringPlayer(player)));
	}

	@SuppressWarnings("unchecked")
	private static void debugScaling(CommandCtx<?> c) {
		Object player = c.getArgument("target");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		PlayerData dt = h.getSNetH(player).cpm$getEncodedModelData();
		c.sendSuccess(new LiteralText("---------------------"));
		c.sendSuccess(new FormatText("commands.cpm.scalingDebug.title", c.handler.toStringPlayer(player)));
		c.sendSuccess(new LiteralText(""));
		String id = h.getID(player);
		for (ScalingOptions o : ScalingOptions.VALUES) {
			ScalingSettings l = NetworkUtil.getScalingLimits(o, id);
			String scaler = l.scaler;
			if (scaler == null) {
				Map<String, Scaler<Object>> scl = h.getScaleSetters().get(o);
				scaler = scl != null && !scl.isEmpty() ? Iterables.getLast(scl.keySet()) : null;
			}
			float target = dt.targetScale.getOrDefault(o, 1f);
			FormatText mth;
			if (scaler == null) {
				mth = new FormatText("commands.cpm.scalingDebug.unavailable", target);
			} else if (ConfigKeys.SCALING_METHOD_OFF.equals(scaler)) {
				mth = new FormatText("commands.cpm.scalingDebug.scaler_disabled", target);
			} else if(l.min == l.max && l.min == 1) {
				mth = new FormatText("commands.cpm.scalingDebug.disabled", target);
			} else {
				mth = new FormatText("commands.cpm.scalingDebug.enabled", scaler, dt.scale.getOrDefault(o, 1f), target, l.min, l.max);
			}
			c.sendSuccess(new FormatText("commands.cpm.scalingDebug.entry", o.asText(), mth));
		}
		c.sendSuccess(new LiteralText(""));
		c.sendSuccess(new FormatText("commands.cpm.scalingDebug.info"));
		c.sendSuccess(new LiteralText("---------------------"));
	}

	@SuppressWarnings("unchecked")
	private static void editScaling(CommandCtx<?> context, Object player, BiConsumer<ConfigEntry, BiFunction<Object, Object, IText>> cc) {
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		ConfigEntry e = ModConfig.getWorldConfig();
		Map<ScalingOptions, Float> sclMap1 = null;
		Map<Object, Map<ScalingOptions, Float>> sclMap2 = null;
		BiFunction<Object, Object, IText> op;
		if(player != null) {
			e = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS).getEntry(h.getID(player));
			PlayerData dt = h.getSNetH(player).cpm$getEncodedModelData();
			sclMap1 = new EnumMap<>(dt.targetScale);
			dt.resetScale(h, player);
			op = (a, b) -> new FormatText("commands.cpm.setValueFor", a, b, context.handler.toStringPlayer(player));
		} else {
			e = e.getEntry(ConfigKeys.SCALING_SETTINGS);
			sclMap2 = new HashMap<>();
			for (Object o : h.getOnlinePlayers()) {
				PlayerData dt = h.getSNetH(o).cpm$getEncodedModelData();
				sclMap2.put(o, new EnumMap<>(dt.targetScale));
				dt.resetScale(h, o);
			}
			op = (a, b) -> new FormatText("commands.cpm.setValue", a, b);
		}
		cc.accept(e, op);
		if(player != null) {
			PlayerData dt = h.getSNetH(player).cpm$getEncodedModelData();
			dt.rescale(h, player, sclMap1, null);
		} else {
			sclMap2.forEach((o, sm) -> {
				PlayerData dt = h.getSNetH(o).cpm$getEncodedModelData();
				dt.rescale(h, o, sm, null);
			});
		}
		ModConfig.getWorldConfig().save();
	}

	private static void setScalingMethod(CommandCtx<?> c, Object player, ScalingOptions sc, String method) {
		editScaling(c, player, (e, v) -> {
			e = e.getEntry(sc.name().toLowerCase(Locale.ROOT));
			FormatText scl;
			if (method.equals(SCALING_DEFAULT)) {
				e.clearValue(ConfigKeys.SCALING_METHOD);
				scl = new FormatText("commands.cpm.setScalingMethod.default");
			} else {
				e.setString(ConfigKeys.SCALING_METHOD, method);
				scl = new FormatText("commands.cpm.setScalingMethod.method", method);
			}
			c.sendSuccess(v.apply(sc.asText(), scl));
		});
	}

	private static void setScalingEnAll(CommandCtx<?> context, Object player, boolean en) {
		editScaling(context, player, (e, v) -> {
			for(ScalingOptions o : ScalingOptions.VALUES) {
				ConfigEntry ce = e.getEntry(o.name().toLowerCase(Locale.ROOT));
				ce.setBoolean(ConfigKeys.ENABLED, en);
				context.sendSuccess(v.apply(o.asText(), new FormatText("label.cpm.enableX", en)));
			}
		});
	}

	private static void setScalingEn(CommandCtx<?> context, Object player, ScalingOptions sc, boolean en) {
		editScaling(context, player, (e, v) -> {
			e = e.getEntry(sc.name().toLowerCase(Locale.ROOT));
			e.setBoolean(ConfigKeys.ENABLED, en);
			context.sendSuccess(v.apply(sc.asText(), new FormatText("label.cpm.enableX", en)));
		});
	}

	private static void setScalingLimit(CommandCtx<?> context, Object player, ScalingOptions sc, float min, float max) {
		if(min > 1 || min > max || max < 1) {
			context.fail(new FormatText("commands.cpm.numberOutOfBounds"));
			return;
		}
		editScaling(context, player, (e, v) -> {
			e = e.getEntry(sc.name().toLowerCase(Locale.ROOT));
			e.setFloat(ConfigKeys.MIN, min);
			e.setFloat(ConfigKeys.MAX, max);
			context.sendSuccess(v.apply(sc.asText(), new FormatText("label.cpm.rangeOf", min, max)));
		});
	}

	@SuppressWarnings("unchecked")
	private static void setAnimation(CommandCtx<?> context, int value) {
		Object player = context.getArgument("target");
		String animation = context.getArgument("animation");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		h.playAnimation(player, animation, value);
		context.sendSuccess(new FormatText("commands.cpm.animate.success", animation, context.handler.toStringPlayer(player)));
	}

	@SuppressWarnings("unchecked")
	private static void getAnimation(CommandCtx<?> context, int value) {
		Object player = context.getArgument("target");
		String animation = context.getArgument("animation");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		int d = h.getAnimationPlaying(player, animation);
		if (d == -1)context.fail(new FormatText("commands.cpm.detect.notFound"));
		else if(value != -1) {
			if(d == value) {
				context.success(1);
				context.sendSuccess(new FormatText("commands.cpm.detect.success", context.handler.toStringPlayer(player), animation, d));
			} else {
				context.fail(new FormatText("commands.cpm.detect.success", context.handler.toStringPlayer(player), animation, 0));
			}
		} else if(d > 0) {
			context.success(d);
			context.sendSuccess(new FormatText("commands.cpm.detect.success", context.handler.toStringPlayer(player), animation, d));
		} else {
			context.fail(new FormatText("commands.cpm.detect.success", context.handler.toStringPlayer(player), animation, 0));
		}
	}

	@SuppressWarnings("unchecked")
	private static List<String> getAnimationList(CommandCtx<?> context, boolean cc) {
		Object player = context.getArgument("target");
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		PlayerData pd = h.getSNetH(player).cpm$getEncodedModelData();
		List<String> l = new ArrayList<>();
		if(cc)
			pd.animNames.forEach((k, v) -> {
				if(v.isCommandControlled())l.add(k);
			});
		else
			l.addAll(pd.animNames.keySet());
		return l;
	}
}
