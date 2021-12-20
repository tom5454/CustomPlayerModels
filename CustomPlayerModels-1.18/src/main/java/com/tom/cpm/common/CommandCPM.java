package com.tom.cpm.common;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandCPM {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> cpm = Commands.literal("cpm").
				requires(CommandSource -> CommandSource.hasPermission(2)).
				then(Commands.literal("setskin").
						then(Commands.literal("-f").
								then(Commands.argument("target", EntityArgument.player()).
										then(Commands.argument("skin", StringArgumentType.greedyString()).
												executes(c -> execute(c, StringArgumentType.getString(c, "skin"), true, true))
												)
										)
								).
						then(Commands.literal("-t").
								then(Commands.argument("target", EntityArgument.player()).
										then(Commands.argument("skin", StringArgumentType.greedyString()).
												executes(c -> execute(c, StringArgumentType.getString(c, "skin"), false, false))
												)
										)
								).
						then(Commands.literal("-r").
								then(Commands.argument("target", EntityArgument.player()).
										executes(c -> execute(c, null, false, true))
										)
								).
						then(Commands.argument("target", EntityArgument.player()).
								then(Commands.argument("skin", StringArgumentType.greedyString()).
										executes(c -> execute(c, StringArgumentType.getString(c, "skin"), false, true))
										)
								)
						);
		dispatcher.register(cpm);
	}

	private static int execute(CommandContext<CommandSourceStack> context, String skin, boolean force, boolean save) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "target");
		ServerHandler.netHandler.onCommand(player, skin, force, save);
		if(force)context.getSource().sendSuccess(new TranslatableComponent("commands.cpm.setskin.success.force", player.getDisplayName()), true);
		else context.getSource().sendSuccess(new TranslatableComponent("commands.cpm.setskin.success", player.getDisplayName()), true);
		return 1;
	}
}
