package com.tom.cpm.client;

import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;

public class ClientCommand extends BrigadierCommandHandler<QuiltClientCommandSource> {

	public ClientCommand(CommandDispatcher<QuiltClientCommandSource> dispatcher) {
		super(dispatcher, true);
	}

	@Override
	public String toStringPlayer(Object pl) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean hasOPPermission(QuiltClientCommandSource source) {
		return source.hasPermission(2);
	}

	@Override
	protected ArgumentType<?> player() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object getPlayer(CommandContext<QuiltClientCommandSource> ctx, String id) throws CommandSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendSuccess(QuiltClientCommandSource sender, IText text) {
		sender.sendFeedback(text.remap());
	}
}