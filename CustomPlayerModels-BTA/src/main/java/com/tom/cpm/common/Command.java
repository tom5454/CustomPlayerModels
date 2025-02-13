package com.tom.cpm.common;

import java.util.List;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.helpers.EntitySelector;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.text.IText;

public class Command extends BrigadierBTACommandHandler<CommandSource> {

	public Command(CommandDispatcher<CommandSource> dispatcher, boolean client) {
		super(dispatcher, client);
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((Player) pl).getDisplayName();
	}

	@Override
	public void sendSuccess(CommandSource sender, IText text) {
		sender.sendMessage(text.remap());
	}

	@Override
	protected boolean hasOPPermission(CommandSource source) {
		return source.hasAdmin();
	}

	@Override
	protected ArgumentType<?> player() {
		return ArgumentTypeEntity.username();
	}

	@Override
	protected Object getPlayer(CommandContext<CommandSource> ctx, String id) throws CommandSyntaxException {
		final EntitySelector entitySelector = ctx.getArgument(id, EntitySelector.class);
		final List<? extends Entity> entities = entitySelector.get(ctx.getSource());
		return entities.size() == 1 ? entities.get(0) : null;
	}
}
