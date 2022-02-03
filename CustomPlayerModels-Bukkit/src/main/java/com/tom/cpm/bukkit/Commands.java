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

	private static class Sender {
		private CommandSender sender;
		private boolean success;

		public Sender(CommandSender sender) {
			this.sender = sender;
		}
	}

	public static class CommandHandler extends StringCommandHandler<Void, Sender, CommandException> {
		private Map<String, CommandImpl> commands;

		private CommandHandler(JavaPlugin pl, Map<String, CommandImpl> commands) {
			super(i -> {
				commands.put(i.getName(), i);
				pl.getCommand(i.getName()).setExecutor(pl);
			});
			this.commands = commands;
		}

		public CommandHandler(JavaPlugin pl) {
			this(pl, new HashMap<>());
		}

		public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
			Sender s = new Sender(var1);
			try {
				CommandImpl cmd = commands.get(var2.getName());
				if(cmd != null) {
					cmd.execute(null, s, var4);
				}
			} catch (CommandException e) {
				sendFail(s, e.msg);
			}
			return s.success;
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
		public void sendSuccess(Sender sender, IText text) {
			sender.sender.sendMessage(text.<String>remap());
			sender.success = true;
		}

		@Override
		public void sendFail(Sender sender, IText text) {
			String c = text.remap();
			sender.sender.sendMessage(ChatColor.RED + c);
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
		public Object getPlayerObj(Void server, Sender sender, String name) throws CommandException {
			return Bukkit.getPlayer(name);
		}

		@Override
		public CommandException checkExc(Exception exc) {
			if(exc instanceof CommandException)return (CommandException) exc;
			return new CommandException(new FormatText("commands.generic.exception"));
		}
	}
}
