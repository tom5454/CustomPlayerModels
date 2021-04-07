package com.tom.cpm.common;

import java.util.Base64;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

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
		ServerNetH handler = (ServerNetH) player.networkHandler;
		handler.cpm$setEncodedModelData(skin != null ? new PlayerData(Base64.getDecoder().decode(skin), force, save) : null);
		NetworkHandler.sendToAllTrackingAndSelf(player, new CustomPayloadS2CPacket(NetworkHandler.setSkin, ServerHandler.writeSkinData(handler.cpm$getEncodedModelData(), player)), ServerHandler::hasMod, null);
		if(save && context.getSource().getMinecraftServer().isDedicated()) {
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
			if(skin == null)
				e.clearValue(player.getUuid().toString());
			else {
				e = e.getEntry(player.getUuid().toString());
				e.setString(ConfigKeys.MODEL, skin);
				e.setBoolean(ConfigKeys.FORCED, force);
			}
			ModConfig.getConfig().save();
		}
		if(force)context.getSource().sendFeedback(new TranslatableText("commands.cpm.setskin.success.force", player.getDisplayName()), true);
		else context.getSource().sendFeedback(new TranslatableText("commands.cpm.setskin.success", player.getDisplayName()), true);
		return 1;
	}
}
