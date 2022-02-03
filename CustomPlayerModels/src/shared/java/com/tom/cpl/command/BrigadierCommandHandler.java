package com.tom.cpl.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import com.tom.cpl.util.Pair;

public abstract class BrigadierCommandHandler<S> implements CommandHandler<S> {
	private final CommandDispatcher<S> dispatcher;

	public BrigadierCommandHandler(CommandDispatcher<S> dispatcher) {
		this.dispatcher = dispatcher;
		register();
	}

	protected abstract boolean hasOPPermission(S source);

	@Override
	public void register(LiteralCommandBuilder builder) {
		LiteralArgumentBuilder<S> cmd = literal(builder.getName()).
				requires(this::hasOPPermission);
		build(cmd, builder, Collections.emptyList(), Collections.emptySet());
		dispatcher.register(cmd);
	}

	private void build(ArgumentBuilder<S, ?> a, AbstractCommandBuilder<?> b, List<RequiredCommandBuilder> args, Set<String> setFlags) {
		for(AbstractCommandBuilder<?> v : b.getNext()) {
			if(v instanceof LiteralCommandBuilder) {
				ArgumentBuilder<S, ?> n = literal(((LiteralCommandBuilder)v).getName());
				build(n, v, args, setFlags);
				a.then(n);
			} else if(v instanceof RequiredCommandBuilder) {
				RequiredCommandBuilder rcb = (RequiredCommandBuilder) v;
				RequiredArgumentBuilder<S, ?> n = argument(rcb.getId(), getType(rcb.getType(), rcb.getSettings()));
				List<RequiredCommandBuilder> newArgs = new ArrayList<>(args);
				newArgs.add(rcb);
				build(n, v, newArgs, setFlags);
				a.then(n);
				Supplier<List<String>> s = rcb.getPossibleValues();
				if(s != null) {
					n.suggests((c, bu) -> suggestMatching(s.get(), bu));
				}
			}
		}
		if(b.getFunc() != null) {
			Consumer<CommandCtx<?>> cmd = b.getFunc();
			a.executes(c -> {
				CommandCtx<S> ctx = new CommandCtx<>(c.getSource(), this);
				for (RequiredCommandBuilder arg : args) {
					ctx.arg(arg.getId(), getValue(c, arg.getType(), arg.getId()));
				}
				cmd.accept(ctx);
				return ctx.getResult();
			});
		}
	}

	private static CompletableFuture<Suggestions> suggestMatching(List<String> list, SuggestionsBuilder builder) {
		String txt = builder.getRemaining().toLowerCase(Locale.ROOT);
		for (String elem : list) {
			if (elem.toLowerCase(Locale.ROOT).startsWith(txt)) {
				builder.suggest(elem);
			}
		}
		return builder.buildFuture();
	}

	@SuppressWarnings("unchecked")
	protected <T> ArgumentType<T> getType(ArgType type, Object settings) {
		switch (type) {
		case BOOLEAN:
			return (ArgumentType<T>) BoolArgumentType.bool();
		case PLAYER:
			return (ArgumentType<T>) player();
		case INT:
			if(settings != null) {
				Pair<Integer, Integer> p = (Pair<Integer, Integer>) settings;
				return (ArgumentType<T>) IntegerArgumentType.integer(p.getKey(), p.getValue());
			}
			else return (ArgumentType<T>) IntegerArgumentType.integer();
		case STRING:
			if(settings == null || !((Boolean)settings))return (ArgumentType<T>) StringArgumentType.word();
			else return (ArgumentType<T>) StringArgumentType.greedyString();
		case ENUM:
		default:
			return (ArgumentType<T>) StringArgumentType.word();
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getValue(CommandContext<S> ctx, ArgType type, String id) throws CommandSyntaxException {
		switch (type) {
		case BOOLEAN:
			return (T) Boolean.valueOf(BoolArgumentType.getBool(ctx, id));
		case PLAYER:
			return (T) getPlayer(ctx, id);
		case INT:
			return (T) Integer.valueOf(IntegerArgumentType.getInteger(ctx, id));
		case ENUM:
		case STRING:
		default:
			return (T) ctx.getArgument(id, String.class);
		}
	}

	public LiteralArgumentBuilder<S> literal(String text) {
		return LiteralArgumentBuilder.literal(text);
	}

	public <T> RequiredArgumentBuilder<S, T> argument(String id, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(id, type);
	}

	protected abstract ArgumentType<?> player();
	protected abstract Object getPlayer(CommandContext<S> ctx, String id) throws CommandSyntaxException;
}
