package com.tom.cpm.common;

import java.util.Base64;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.text.TranslationTextComponent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;

public class CommandCPM {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> cpm = Commands.literal("cpm").
				requires(CommandSource -> CommandSource.hasPermissionLevel(2)).
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

	private static int execute(CommandContext<CommandSource> context, String skin, boolean force, boolean save) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
		ServerNetH handler = (ServerNetH) player.connection;
		handler.cpm$setEncodedModelData(skin != null ? new PlayerData(Base64.getDecoder().decode(skin), force, save) : null);
		NetworkHandler.sendToAllTrackingAndSelf(player, new SCustomPayloadPlayPacket(NetworkHandler.setSkin, ServerHandler.writeSkinData(handler.cpm$getEncodedModelData(), player)), ServerHandler::hasMod, null);
		if(save && context.getSource().getServer().isDedicatedServer()) {
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
			if(skin == null)
				e.clearValue(player.getUniqueID().toString());
			else {
				e = e.getEntry(player.getUniqueID().toString());
				e.setString(ConfigKeys.MODEL, skin);
				e.setBoolean(ConfigKeys.FORCED, force);
			}
			ModConfig.getConfig().save();
		}
		if(force)context.getSource().sendFeedback(new TranslationTextComponent("commands.cpm.setskin.success.force", player.getDisplayName()), true);
		else context.getSource().sendFeedback(new TranslationTextComponent("commands.cpm.setskin.success", player.getDisplayName()), true);
		return 1;
	}
}
