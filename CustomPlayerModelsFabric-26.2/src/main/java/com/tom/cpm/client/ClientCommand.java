package com.tom.cpm.client;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;

public class ClientCommand extends BrigadierCommandHandler<FabricClientCommandSource> {

	public ClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		super(dispatcher, true);
	}

	@Override
	public String toStringPlayer(Object pl) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean hasOPPermission(FabricClientCommandSource source) {
		return false;
	}

	@Override
	protected ArgumentType<?> player() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object getPlayer(CommandContext<FabricClientCommandSource> ctx, String id) throws CommandSyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendSuccess(FabricClientCommandSource sender, IText text) {
		sender.sendFeedback(text.remap());
	}
}