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
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.tom.cpl.command.StringCommandHandler;
import com.tom.cpl.text.IText;

public class Command extends StringCommandHandler<MinecraftServer, ICommandSender, CommandException> {

	public Command(Consumer<CommandBase> register) {
		super(i -> register.accept(new Cmd(i)));
	}

	private static class Cmd extends CommandBase {
		private CommandImpl impl;

		public Cmd(CommandImpl i) {
			this.impl = i;
		}

		@Override
		public String getCommandName() {
			return impl.getName();
		}

		@Override
		public String getCommandUsage(ICommandSender sender) {
			return "commands." + impl.getName() + ".usage";
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) throws CommandException {
			impl.execute(MinecraftServer.getServer(), sender, args);
		}

		@Override
		public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
			return impl.getTabCompletions(MinecraftServer.getServer(), sender, args);
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 2;
		}
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((EntityPlayer)pl).getDisplayName();
	}

	@Override
	public void sendSuccess(ICommandSender sender, IText text) {
		sender.addChatMessage(text.remap());
	}

	@Override
	public void sendFail(ICommandSender sender, IText text) {
		sender.addChatMessage(text.<IChatComponent>remap().setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
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
		return CommandBase.getPlayer(sender, name);
	}

	@Override
	public CommandException checkExc(Exception exc) {
		if(exc instanceof CommandException)return (CommandException) exc;
		return new CommandException("commands.generic.exception");
	}

	@Override
	public List<String> getOnlinePlayers(MinecraftServer server) {
		return Arrays.asList(server.getAllUsernames());
	}
}
