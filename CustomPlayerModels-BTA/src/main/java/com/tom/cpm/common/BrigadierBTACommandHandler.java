package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.ArgumentTypeBool;
import com.mojang.brigadier.arguments.ArgumentTypeFloat;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import com.tom.cpl.command.AbstractCommandBuilder;
import com.tom.cpl.command.ArgType;
import com.tom.cpl.command.CommandCtx;
import com.tom.cpl.command.CommandHandler;
import com.tom.cpl.command.LiteralCommandBuilder;
import com.tom.cpl.command.RequiredCommandBuilder;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.util.Log;

public abstract class BrigadierBTACommandHandler<S> implements CommandHandler<S> {
	private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(a -> new FormatText("commands.cpm.genericFail", a).remap());
	private final CommandDispatcher<S> dispatcher;

	public BrigadierBTACommandHandler(CommandDispatcher<S> dispatcher, boolean client) {
		this.dispatcher = dispatcher;
		if (client)registerClient();
		else registerCommon();
	}

	protected abstract boolean hasOPPermission(S source);

	@Override
	public void register(LiteralCommandBuilder builder, boolean isOp) {
		ArgumentBuilderLiteral<S> cmd = literal(builder.getName());
		if(isOp)cmd.requires(this::hasOPPermission);
		build(cmd, builder, Collections.emptyList(), cmd.getLiteral().toLowerCase(Locale.ROOT));
		dispatcher.register(cmd);
	}

	private void build(ArgumentBuilder<S, ?> a, AbstractCommandBuilder<?> b, List<RequiredCommandBuilder> args, String prefix) {
		for(AbstractCommandBuilder<?> v : b.getNext()) {
			if(v instanceof LiteralCommandBuilder) {
				ArgumentBuilderLiteral<S> n = literal(((LiteralCommandBuilder)v).getName());
				build(n, v, args, prefix + "_" + n.getLiteral().toLowerCase(Locale.ROOT));
				a.then(n);
			} else if(v instanceof RequiredCommandBuilder) {
				RequiredCommandBuilder rcb = (RequiredCommandBuilder) v;
				ArgumentBuilderRequired<S, ?> n = argument(rcb.getId(), getType(rcb.getType(), rcb.getSettings()));
				List<RequiredCommandBuilder> newArgs = new ArrayList<>(args);
				newArgs.add(rcb);
				String name = prefix + "_" + rcb.getId().toLowerCase(Locale.ROOT);
				build(n, v, newArgs, name);
				Function<CommandCtx<?>, List<String>> s = rcb.getPossibleValues();
				if(s != null) {
					n.suggests((c, bu) -> suggestMatching(s.apply(new WrappedCtx(c, args)), bu));
				}
				a.then(n);
			}
		}
		if(b.getFunc() != null) {
			Consumer<CommandCtx<?>> cmd = b.getFunc();
			a.executes(c -> {
				CommandCtx<S> ctx = new WrappedCtx(c, args);
				try {
					cmd.accept(ctx);
				} catch (Exception e) {
					Log.error("Command error, input: /" + c.getInput(), e);
					throw ERROR_FAILED.create("Unknown error");
				}
				if(ctx.getFail() != null)throw ERROR_FAILED.create(ctx.getFail());
				return ctx.getResult();
			});
		}
	}

	private class WrappedCtx extends CommandCtx<S> {

		public WrappedCtx(CommandContext<S> ctx, List<RequiredCommandBuilder> args) throws CommandSyntaxException {
			super(ctx.getSource(), BrigadierBTACommandHandler.this);
			for (RequiredCommandBuilder arg : args) {
				arg(arg.getId(), getValue(ctx, arg.getType(), arg.getId()));
			}
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
			return (ArgumentType<T>) ArgumentTypeBool.bool();
		case PLAYER:
			return (ArgumentType<T>) player();
		case INT:
			if(settings != null) {
				Pair<Integer, Integer> p = (Pair<Integer, Integer>) settings;
				return (ArgumentType<T>) ArgumentTypeInteger.integer(p.getKey(), p.getValue());
			}
			else return (ArgumentType<T>) ArgumentTypeInteger.integer();
		case STRING:
			if(settings == null || !((Boolean)settings))return (ArgumentType<T>) ArgumentTypeString.string();
			else return (ArgumentType<T>) ArgumentTypeString.greedyString();
		case FLOAT:
			if(settings != null) {
				Pair<Float, Float> p = (Pair<Float, Float>) settings;
				return (ArgumentType<T>) ArgumentTypeFloat.floatArg(p.getKey(), p.getValue());
			}
			else return (ArgumentType<T>) ArgumentTypeFloat.floatArg();
		default:
			return (ArgumentType<T>) ArgumentTypeString.word();
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getValue(CommandContext<S> ctx, ArgType type, String id) throws CommandSyntaxException {
		switch (type) {
		case BOOLEAN:
			return (T) Boolean.valueOf(ArgumentTypeBool.getBool(ctx, id));
		case PLAYER:
			return (T) getPlayer(ctx, id);
		case INT:
			return (T) Integer.valueOf(ArgumentTypeInteger.getInteger(ctx, id));
		case FLOAT:
			return (T) Float.valueOf(ArgumentTypeFloat.getFloat(ctx, id));
		case STRING:
		default:
			return (T) ctx.getArgument(id, String.class);
		}
	}

	public ArgumentBuilderLiteral<S> literal(String text) {
		return ArgumentBuilderLiteral.literal(text);
	}

	public <T> ArgumentBuilderRequired<S, T> argument(String id, ArgumentType<T> type) {
		return ArgumentBuilderRequired.argument(id, type);
	}

	protected abstract ArgumentType<?> player();
	protected abstract Object getPlayer(CommandContext<S> ctx, String id) throws CommandSyntaxException;
}
