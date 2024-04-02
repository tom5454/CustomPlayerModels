package com.tom.cpm.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.src.EntityPlayer;

import com.tom.cpl.command.StringCommandHandler;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.IText;
import com.tom.cpm.CustomPlayerModels;

public class Command {

	private static class CommandException extends Exception {
		private static final long serialVersionUID = 3508944502637337553L;
		private IText msg;

		public CommandException(IText msg) {
			this.msg = msg;
		}
	}

	public static abstract class CommandHandlerBase<L> extends StringCommandHandler<Void, L, CommandException> {
		private Map<String, CommandImpl> commands;

		public CommandHandlerBase(Map<String, CommandImpl> commands) {
			super(i -> commands.put(i.getName(), i), false);
			this.commands = commands;
		}

		public CommandHandlerBase() {
			this(new HashMap<>());
		}

		@Override
		public String toStringPlayer(Object pl) {
			return ((EntityPlayer) pl).username;
		}

		public boolean onCommand(L sender, String fullText) {
			try {
				String[] sp = fullText.split(" ");
				CommandImpl cmd = commands.get(sp[0]);
				if(cmd != null) {
					cmd.execute(null, sender, Arrays.copyOfRange(sp, 1, sp.length));
					return true;
				}
			} catch (CommandException e) {
				sendMessage(sender, "§c" + e.msg.<String>remap());
				return true;
			}
			return false;
		}

		public List<String> onTabComplete(String command) {
			String[] sp = command.split(" ");
			try {
				CommandImpl cmd = commands.get(sp[0]);
				if(cmd != null) {
					return cmd.getTabCompletions(null, null, Arrays.copyOfRange(sp, 1, sp.length));
				}
			} catch (Exception e) {
			}
			return Collections.emptyList();
		}

		@Override
		public void sendSuccess(L sender, IText text) {
			sendMessage(sender, text.remap());
		}

		protected abstract void sendMessage(L sender, String string);

		@Override
		public CommandException generic(String text, Object... format) {
			return new CommandException(new FormatText(text, format));
		}

		@Override
		public CommandException wrongUsage(String text, Object... format) {
			return new CommandException(new FormatText(text, format));
		}

		@Override
		public Object getPlayerObj(Void server, L sender, String name) throws CommandException {
			return CustomPlayerModels.proxy.getPlayersOnline().stream().filter(e -> e.username.equals(name)).findFirst().orElse(null);
		}

		@Override
		public CommandException checkExc(Exception exc) {
			if(exc instanceof CommandException)return (CommandException) exc;
			return new CommandException(new FormatText("commands.generic.exception"));
		}

		@Override
		public List<String> getOnlinePlayers(Void server) {
			return CustomPlayerModels.proxy.getPlayersOnline().stream().map(e -> e.username).collect(Collectors.toList());
		}
	}
}
