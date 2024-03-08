package com.tom.cpm.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import com.tom.cpl.command.StringCommandHandler;
import com.tom.cpl.text.IText;

public class Command extends StringCommandHandler<MinecraftServer, ICommandSender, CommandException> {

	public Command(Consumer<CommandBase> register, boolean client) {
		super(i -> register.accept(new Cmd(i)), client);
	}

	private static class Cmd extends CommandBase {
		private CommandImpl impl;

		public Cmd(CommandImpl i) {
			this.impl = i;
		}

		@Override
		public String getName() {
			return impl.getName();
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "commands." + impl.getName() + ".usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			impl.execute(server, sender, args);
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
				BlockPos targetPos) {
			return impl.getTabCompletions(server, sender, args);
		}

		@Override
		public int getRequiredPermissionLevel() {
			return impl.isOp() ? 2 : 0;
		}
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((EntityPlayer)pl).getName();
	}

	@Override
	public void sendSuccess(ICommandSender sender, IText text) {
		if(sender.sendCommandFeedback()) {
			sender.sendMessage(text.remap());
		}
	}

	@Override
	public CommandException generic(String text, Object... format) {
		return new CommandException(text, format);
	}

	@Override
	public CommandException wrongUsage(String text, Object... format) {
		return new WrongUsageException(text, format);
	}

	@Override
	public Object getPlayerObj(MinecraftServer server, ICommandSender sender, String name) throws CommandException {
		return CommandBase.getPlayer(server, sender, name);
	}

	@Override
	public CommandException checkExc(Exception exc) {
		if(exc instanceof CommandException)return (CommandException) exc;
		return new CommandException("commands.generic.exception");
	}

	@Override
	public List<String> getOnlinePlayers(MinecraftServer server) {
		return Arrays.asList(server.getOnlinePlayerNames());
	}
}
