package com.tom.cpm.shared.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.StringBuilderStream;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.SafetyException;

public class ErrorLog {
	private static List<LogEntry> errors = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public static List<LogEntry> collectErrors() {
		List<LogEntry> entries = new ArrayList<>();
		try {
			List<Player<?>> players = MinecraftClientAccess.get().getDefinitionLoader().getPlayers();
			players.stream().map(Player::getModelDefinition0).filter(d -> d != null && d.getError() != null).map(d -> {
				Throwable err = d.getError();
				if(err instanceof SafetyException)
					return new LogEntry(LogLevel.INFO, new FormatText("label.cpm.error.blockedBySafety", d.getPlayerObj().getName(), new FormatText("label.cpm.block_reason." + ((SafetyException)err).getBlockReason().name().toLowerCase(Locale.ROOT))), err, 0);
				else
					return new LogEntry(LogLevel.WARNING, new FormatText("label.cpm.error.errorWhileLoading", d.getPlayerObj().getName(), err.toString()), err, 0);
			}).forEach(entries::add);
		} catch (Exception e) {
			addLog(LogLevel.ERROR, "Error while fetching model loading errors", e);
		}
		entries.addAll(errors);
		return entries;
	}

	public static class LogEntry {
		private LogLevel level;
		private FormatText message;
		private Throwable error;
		private long time;

		public LogEntry(LogLevel level, FormatText name, Throwable error, long time) {
			this.level = level;
			this.message = name;
			this.error = error;
			this.time = time;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> m = new HashMap<>();
			m.put("level", level.name());
			m.put("msg", message.toMap());
			StringBuilder sb = new StringBuilder();
			StringBuilderStream.stacktraceToString(error, sb, "\n");
			m.put("error", sb.toString());
			m.put("time", Long.toString(time, 16));
			return m;
		}

		public LogLevel getLevel() {
			return level;
		}

		public FormatText getMessage() {
			return message;
		}

		public String toTooltipString(IGui gui, boolean st) {
			String msg;
			if(st) {
				StringBuilder sb = new StringBuilder();
				StringBuilderStream.stacktraceToString(error, sb, "\n\t");
				msg = sb.toString().replace("\t", "   ").replace("\n", "\\");
			} else {
				msg = error.toString();
			}
			String lvl = gui.i18nFormat("label.cpm.level." + level.name().toLowerCase(Locale.ROOT));
			return gui.i18nFormat(st ? "tooltip.cpm.errorLogST" : "tooltip.cpm.errorLog", message.toString(gui), lvl, msg);
		}
	}

	public static enum LogLevel {
		ERROR,
		WARNING,
		INFO
	}

	public static void addLog(LogLevel lvl, String text) {
		Log.warn(text);
		addLog0(lvl, text, new Throwable("Caller stacktrace:"));
	}

	public static void addLog(LogLevel lvl, String text, Throwable error) {
		Log.warn(text, error);
		addLog0(lvl, text, error);
	}

	private static void addLog0(LogLevel lvl, String text, Throwable error) {
		errors.add(new LogEntry(lvl, new FormatText("label.cpm.identity", text), error, System.currentTimeMillis()));
	}

	public static void addFormattedLog(LogLevel lvl, String text, Object... args) {
		errors.add(new LogEntry(lvl, new FormatText(text, args), new Throwable("Caller stacktrace:"), System.currentTimeMillis()));
	}

	public static void addFormattedLog(LogLevel lvl, String text, Throwable error, Object... args) {
		errors.add(new LogEntry(lvl, new FormatText(text, args), error, System.currentTimeMillis()));
	}

	public static void clear() {
		errors.clear();
	}
}
