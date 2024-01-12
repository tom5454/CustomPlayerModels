package com.tom.cpm.common;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;

public class Command extends BrigadierCommandHandler<CommandSourceStack> {

	public Command(CommandDispatcher<CommandSourceStack> dispatcher) {
		super(dispatcher);
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((Player)pl).getScoreboardName();
	}

	@Override
	protected boolean hasOPPermission(CommandSourceStack source) {
		return source.hasPermission(2);
	}

	@Override
	protected ArgumentType<?> player() {
		return EntityArgument.player();
	}

	@Override
	protected Object getPlayer(CommandContext<CommandSourceStack> ctx, String id) throws CommandSyntaxException {
		return EntityArgument.getPlayer(ctx, id);
	}

	@Override
	public void sendSuccess(CommandSourceStack sender, IText text) {
		sender.sendSuccess(() -> text.remap(), true);
	}
}
