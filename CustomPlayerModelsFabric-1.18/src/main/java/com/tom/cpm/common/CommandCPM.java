package com.tom.cpm.common;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandCPM {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> cpm = CommandManager.literal("cpm").
				requires(CommandManagerource -> CommandManagerource.hasPermissionLevel(2)).
				then(CommandManager.literal("setskin").
						then(CommandManager.literal("-f").
								then(CommandManager.argument("target", EntityArgumentType.player()).
										then(CommandManager.argument("skin", StringArgumentType.greedyString()).
												executes(c -> execute(c, StringArgumentType.getString(c, "skin"), true, true))
												)
										)
								).
						then(CommandManager.literal("-t").
								then(CommandManager.argument("target", EntityArgumentType.player()).
										then(CommandManager.argument("skin", StringArgumentType.greedyString()).
												executes(c -> execute(c, StringArgumentType.getString(c, "skin"), false, false))
												)
										)
								).
						then(CommandManager.literal("-r").
								then(CommandManager.argument("target", EntityArgumentType.player()).
										executes(c -> execute(c, null, false, true))
										)
								).
						then(CommandManager.argument("target", EntityArgumentType.player()).
								then(CommandManager.argument("skin", StringArgumentType.greedyString()).
										executes(c -> execute(c, StringArgumentType.getString(c, "skin"), false, true))
										)
								)
						);
		dispatcher.register(cpm);
	}

	private static int execute(CommandContext<ServerCommandSource> context, String skin, boolean force, boolean save) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
		ServerHandler.netHandler.onCommand(player, skin, force, save);
		if(force)context.getSource().sendFeedback(new TranslatableText("commands.cpm.setskin.success.force", player.getDisplayName()), true);
		else context.getSource().sendFeedback(new TranslatableText("commands.cpm.setskin.success", player.getDisplayName()), true);
		return 1;
	}
}
