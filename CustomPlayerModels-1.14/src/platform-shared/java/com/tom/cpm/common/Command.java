package com.tom.cpm.common;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;

public class Command extends BrigadierCommandHandler<CommandSource> {

	public Command(CommandDispatcher<CommandSource> dispatcher) {
		super(dispatcher);
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((PlayerEntity)pl).getScoreboardName();
	}

	@Override
	protected boolean hasOPPermission(CommandSource source) {
		return source.hasPermission(2);
	}

	@Override
	protected ArgumentType<?> player() {
		return EntityArgument.player();
	}

	@Override
	protected Object getPlayer(CommandContext<CommandSource> ctx, String id) throws CommandSyntaxException {
		return EntityArgument.getPlayer(ctx, id);
	}

	@Override
	public void sendSuccess(CommandSource sender, IText text) {
		sender.sendSuccess(text.remap(), true);
	}
}
