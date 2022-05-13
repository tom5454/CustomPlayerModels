package com.tom.cpm.shared;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.command.ArgType;
import com.tom.cpl.command.CommandCtx;
import com.tom.cpl.command.CommandHandler;
import com.tom.cpl.command.LiteralCommandBuilder;
import com.tom.cpl.command.RequiredCommandBuilder;
import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.ScalingOptions;

public class CommandCPM {

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
								then(new RequiredCommandBuilder("time", ArgType.INT, Pair.of(1, (int)Short.MAX_VALUE)).
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
						)
				;
		dispatcher.register(cpm);
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
			profile.then(new LiteralCommandBuilder(p.name().toLowerCase()).
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
		ModConfig.getWorldConfig().setString(ConfigKeys.SAFETY_PROFILE, profile.name().toLowerCase());
		ModConfig.getWorldConfig().save();
		FormatText t = profile == BuiltInSafetyProfiles.CUSTOM ? new FormatText("commands.cpm.safety.profile.custom") : new FormatText("label.cpm.safetyProfile." + profile.name().toLowerCase());
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
			String name = o.name().toLowerCase();
			LiteralCommandBuilder s = new LiteralCommandBuilder(name);
			s.then(new LiteralCommandBuilder("limit").
					then(new RequiredCommandBuilder("target", ArgType.PLAYER).
							then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									run(c -> setScalingLimit(c, c.getArgument("target"), o, o.getMin(), c.getArgument("max")))
									).
							then(new RequiredCommandBuilder("min", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
											run(c -> setScalingLimit(c, c.getArgument("target"), o, c.getArgument("min"), c.getArgument("max")))
											)
									)
							).
					then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
							run(c -> setScalingLimit(c, null, o, o.getMin(), c.getArgument("max")))
							).
					then(new RequiredCommandBuilder("min", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
							then(new RequiredCommandBuilder("max", ArgType.FLOAT, Pair.of(o.getMin(), o.getMax())).
									run(c -> setScalingLimit(c, null, o, c.getArgument("min"), c.getArgument("max")))
									)
							)
					).
			then(new LiteralCommandBuilder("enabled").
					then(new RequiredCommandBuilder("target", ArgType.PLAYER).
							then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
									run(c -> setScalingEn(c, c.getArgument("target"), o, c.getArgument("enable")))
									)
							).
					then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
							run(c -> setScalingEn(c, null, o, c.getArgument("enable")))
							)
					);
			l.add(s);
		}
		LiteralCommandBuilder all = new LiteralCommandBuilder("all");
		all.then(new LiteralCommandBuilder("enabled").
				then(new RequiredCommandBuilder("target", ArgType.PLAYER).
						then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
								run(c -> setScalingEnAll(c, c.getArgument("target"), c.getArgument("enable")))
								)
						).
				then(new RequiredCommandBuilder("enable", ArgType.BOOLEAN).
						run(c -> setScalingEnAll(c, null, c.getArgument("enable")))
						)
				);
		l.add(all);
		return l;
	}

	@SuppressWarnings("unchecked")
	private static void setScalingEnAll(CommandCtx<?> context, Object player, boolean en) {
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		ConfigEntry e = ModConfig.getWorldConfig();
		if(player != null)e = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS).getEntry(h.getID(player));
		else e = e.getEntry(ConfigKeys.SCALING_SETTINGS);
		for(ScalingOptions o : ScalingOptions.VALUES) {
			ConfigEntry ce = e.getEntry(o.name().toLowerCase());
			ce.setBoolean(ConfigKeys.ENABLED, en);
			context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("label.cpm.tree.scaling." + o.name().toLowerCase()), new FormatText("label.cpm.enableX", en)));
		}
		ModConfig.getWorldConfig().save();
	}

	@SuppressWarnings("unchecked")
	private static void setScalingEn(CommandCtx<?> context, Object player, ScalingOptions sc, boolean en) {
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		ConfigEntry e = ModConfig.getWorldConfig();
		if(player != null)e = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS).getEntry(h.getID(player));
		else e = e.getEntry(ConfigKeys.SCALING_SETTINGS);
		e = e.getEntry(sc.name().toLowerCase());
		e.setBoolean(ConfigKeys.ENABLED, en);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("label.cpm.tree.scaling." + sc.name().toLowerCase()), new FormatText("label.cpm.enableX", en)));
	}

	@SuppressWarnings("unchecked")
	private static void setScalingLimit(CommandCtx<?> context, Object player, ScalingOptions sc, float min, float max) {
		if(min > 1 || min > max || max < 1) {
			context.fail(new FormatText("commands.cpm.numberOutOfBounds"));
			return;
		}
		NetHandler<?, Object, ?> h = (NetHandler<?, Object, ?>) MinecraftServerAccess.get().getNetHandler();
		ConfigEntry e = ModConfig.getWorldConfig();
		if(player != null)e = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS).getEntry(h.getID(player));
		else e = e.getEntry(ConfigKeys.SCALING_SETTINGS);
		e = e.getEntry(sc.name().toLowerCase());
		e.setFloat(ConfigKeys.MIN, min);
		e.setFloat(ConfigKeys.MAX, max);
		ModConfig.getWorldConfig().save();
		context.sendSuccess(new FormatText("commands.cpm.setValue", new FormatText("label.cpm.tree.scaling." + sc.name().toLowerCase()), new FormatText("label.cpm.rangeOf", min, max)));
	}
}
