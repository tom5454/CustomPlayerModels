package com.tom.cpm.common;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;

public class Command extends BrigadierCommandHandler<ServerCommandSource> {

	public Command(CommandDispatcher<ServerCommandSource> dispatcher) {
		super(dispatcher);
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((PlayerEntity)pl).getEntityName();
	}

	@Override
	protected boolean hasOPPermission(ServerCommandSource source) {
		return source.hasPermissionLevel(2);
	}

	@Override
	protected ArgumentType<?> player() {
		return EntityArgumentType.player();
	}

	@Override
	protected Object getPlayer(CommandContext<ServerCommandSource> ctx, String id) throws CommandSyntaxException {
		return EntityArgumentType.getPlayer(ctx, id);
	}

	@Override
	public void sendSuccess(ServerCommandSource sender, IText text) {
		sender.sendFeedback(text.remap(), true);
	}

	@Override
	public void sendFail(ServerCommandSource sender, IText text) {
		sender.sendError(text.remap());
	}
}
