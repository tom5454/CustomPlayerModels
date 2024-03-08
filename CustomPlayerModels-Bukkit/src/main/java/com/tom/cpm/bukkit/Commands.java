package com.tom.cpm.bukkit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.tom.cpl.command.StringCommandHandler;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.IText;

public class Commands {

	private static class CommandException extends Exception {
		private static final long serialVersionUID = 3508944502637337553L;
		private IText msg;

		public CommandException(IText msg) {
			this.msg = msg;
		}
	}

	public static class CommandHandler extends StringCommandHandler<Void, CommandSender, CommandException> {
		private Map<String, CommandImpl> commands;

		private CommandHandler(JavaPlugin pl, Map<String, CommandImpl> commands) {
			super(i -> {
				commands.put(i.getName(), i);
				pl.getCommand(i.getName()).setExecutor(pl);
			}, false);
			this.commands = commands;
		}

		public CommandHandler(JavaPlugin pl) {
			this(pl, new HashMap<>());
		}

		public boolean onCommand(CommandSender s, Command cmdIn, String name, String[] args) {
			try {
				CommandImpl cmd = commands.get(cmdIn.getName());
				if(cmd != null) {
					cmd.execute(null, s, args);
				}
			} catch (CommandException e) {
				s.sendMessage(ChatColor.RED + e.msg.<String>remap());
				return false;
			}
			return true;
		}

		public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
			CommandImpl cmd = commands.get(command.getName());
			if(cmd != null) {
				return cmd.getTabCompletions(null, null, args);
			}
			return Collections.emptyList();
		}

		@Override
		public String toStringPlayer(Object pl) {
			return ((Player)pl).getDisplayName();
		}

		@Override
		public void sendSuccess(CommandSender sender, IText text) {
			sender.sendMessage(text.<String>remap());
		}

		@Override
		public List<String> getOnlinePlayers(Void server) {
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
		}

		@Override
		public CommandException generic(String text, Object... format) {
			return new CommandException(new FormatText(text, format));
		}

		@Override
		public CommandException wrongUsage(String text, Object... format) {
			return new CommandException(new FormatText(text, format));
		}

		@Override
		public Object getPlayerObj(Void server, CommandSender sender, String name) throws CommandException {
			return Bukkit.getPlayer(name);
		}

		@Override
		public CommandException checkExc(Exception exc) {
			if(exc instanceof CommandException)return (CommandException) exc;
			return new CommandException(new FormatText("commands.generic.exception"));
		}
	}
}
