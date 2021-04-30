package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandCPM extends CommandBase {

	@Override
	public String getName() {
		return "cpm";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.cpm.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1)throw new WrongUsageException("commands.cpm.usage");
		switch (args[0]) {
		case "setskin":
		{
			boolean force = false;
			boolean save = true;
			boolean reset = false;
			if(args.length < 2)throw new WrongUsageException("commands.cpm.usage");
			int i = 2;
			switch (args[1]) {
			case "-t":
				save = false;
				break;

			case "-r":
				reset = true;
				break;

			case "-f":
				force = true;
				break;

			default:
				i = 1;
				break;
			}
			if(args.length < i+1)throw new WrongUsageException("commands.cpm.usage");
			EntityPlayerMP player = getPlayer(server, sender, args[i]);
			if(reset) {
				execute(player, sender, null, force, save);
			} else {
				if(args.length < i+2)throw new WrongUsageException("commands.cpm.usage");
				String b64 = args[i+1];
				execute(player, sender, b64, force, save);
			}
		}
		break;

		default:
			throw new CommandException("commands.cpm.usage");
		}
	}

	private void execute(EntityPlayerMP player, ICommandSender sender, String skin, boolean force, boolean save) {
		ServerHandler.netHandler.onCommand(player, skin, force, save);
		if(sender.sendCommandFeedback()) {
			if(force)sender.sendMessage(new TextComponentTranslation("commands.cpm.setskin.success.force", player.getDisplayName()));
			else sender.sendMessage(new TextComponentTranslation("commands.cpm.setskin.success", player.getDisplayName()));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, new String[] {"setskin"});
		} else {
			switch (args[0]) {
			case "setskin":
			{
				List<String> result = new ArrayList<>();
				if(args.length == 2) {
					result.addAll(getListOfStringsMatchingLastWord(args, new String[] {"-f", "-r", "-t"}));
					result.addAll(getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()));
				} else if(args.length == 3 && args[2].startsWith("-")) {
					result.addAll(getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()));
				}
				return result;
			}

			default:
				break;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
}
