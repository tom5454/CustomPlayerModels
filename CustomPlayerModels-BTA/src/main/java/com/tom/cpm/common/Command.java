package com.tom.cpm.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandError;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;

import com.tom.cpl.command.StringCommandHandler;
import com.tom.cpl.text.IText;
import com.tom.cpm.CustomPlayerModels;

public class Command extends StringCommandHandler<CommandHandler, CommandSender, CommandError> {

	public Command(Consumer<net.minecraft.core.net.command.Command> register, boolean client) {
		super(i -> register.accept(new Cmd(i)), client);
	}

	private static class Cmd extends net.minecraft.core.net.command.Command {
		private CommandImpl impl;

		public Cmd(CommandImpl i) {
			super(i.getName(), new String[0]);
			this.impl = i;
		}

		@Override
		public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] strings) {
			impl.execute(commandHandler, commandSender, strings);
			return true;
		}

		@Override
		public boolean opRequired(String[] strings) {
			return impl.isOp();
		}

		@Override
		public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {
		}
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((EntityPlayer) pl).username;
	}

	@Override
	public void sendSuccess(CommandSender sender, IText text) {
		sender.sendMessage(text.remap());
	}

	@Override
	public CommandError generic(String text, Object... format) {
		return new CommandError(I18n.getInstance().translateKeyAndFormat(text, format));
	}

	@Override
	public CommandError wrongUsage(String text, Object... format) {
		return new CommandError(I18n.getInstance().translateKeyAndFormat(text, format));
	}

	@Override
	public Object getPlayerObj(CommandHandler server, CommandSender sender, String name) throws CommandError {
		return CustomPlayerModels.proxy.getPlayersOnline().stream().filter(e -> e.nickname.equals(name)).findFirst().orElse(null);
	}

	@Override
	public CommandError checkExc(Exception exc) {
		if(exc instanceof CommandError)return (CommandError) exc;
		return new CommandError(I18n.getInstance().translateKey("commands.generic.exception"));
	}

	@Override
	public List<String> getOnlinePlayers(CommandHandler server) {
		return CustomPlayerModels.proxy.getPlayersOnline().stream().map(e -> e.username).collect(Collectors.toList());
	}
}
