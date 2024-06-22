package com.tom.cpm.bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.tom.cpl.command.BrigadierCommandHandler;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpm.bukkit.text.BukkitText;
import com.tom.cpm.bukkit.text.ComponentText;

import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIPlatform;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;

public class CommandAPICommands extends BrigadierCommandHandler<Object> implements CommandHandler {
	private CPMBukkitPlugin pl;

	@SuppressWarnings("unchecked")
	private CommandAPICommands(CPMBukkitPlugin pl) {
		super(Brigadier.getCommandDispatcher(), false);
		this.pl = pl;
	}

	public static CommandAPICommands init(CPMBukkitPlugin pl) {
		for (String commandName : pl.getDescription().getCommands().keySet()) {
			CommandAPIBukkit.unregister(commandName, true, true);
		}
		return new CommandAPICommands(pl);
	}

	@Override
	public String toStringPlayer(Object pl) {
		return ((Player)pl).getDisplayName();
	}

	@Override
	public void sendSuccess(Object sender, IText text) {
		text.<BukkitText>remap().sendTo(getCommandSender(sender).getSource());
	}

	@Override
	protected boolean hasOPPermission(Object source) {
		return getCommandSender(source).isOp();
	}

	@Override
	protected ArgumentType<?> player() {
		return Brigadier.fromArgument(new EntitySelectorArgument.OnePlayer("a")).getType();
	}

	@Override
	protected Object getPlayer(CommandContext<Object> ctx, String id) throws CommandSyntaxException {
		return Brigadier.parseArguments(ctx, Arrays.asList(new EntitySelectorArgument.OnePlayer(id)))[0];
	}

	@SuppressWarnings("unchecked")
	private static AbstractCommandSender<? extends CommandSender> getCommandSender(Object src) {
		CommandAPIPlatform<?, CommandSender, Object> platform = (CommandAPIPlatform<?, CommandSender, Object>) dev.jorel.commandapi.CommandAPIHandler.getInstance().getPlatform();
		return platform.getCommandSenderFromCommandSource(src);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Collections.emptyList();
	}

	public static class Text extends ComponentText implements Message {

		public Text(Simple fallback, BaseComponent comp) {
			super(fallback, comp);
		}

		@Override
		public String getString() {
			return toString();
		}
	}

	@Override
	public TextRemapper<ComponentText> remapper() {
		return ComponentText.make(BukkitText.simple(pl), Text::new);
	}
}
