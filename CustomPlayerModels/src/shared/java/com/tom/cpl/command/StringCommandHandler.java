package com.tom.cpl.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.tom.cpl.util.Pair;

public abstract class StringCommandHandler<S, CS, CE extends Exception> implements CommandHandler<CS> {
	private final Consumer<CommandImpl> register;

	public StringCommandHandler(Consumer<CommandImpl> register, boolean client) {
		this.register = register;
		if (client)registerClient();
		else registerCommon();
	}

	@Override
	public void register(LiteralCommandBuilder builder, boolean isOp) {
		register.accept(new CommandImpl(builder, isOp));
	}

	public abstract CE generic(String text, Object... format);
	public abstract CE wrongUsage(String text, Object... format);
	public abstract Object getPlayerObj(S server, CS sender, String name) throws CE;
	public abstract CE checkExc(Exception exc);
	public abstract List<String> getOnlinePlayers(S server);

	public class CommandImpl {
		private final LiteralCommandBuilder root;
		private final boolean isOp;

		public CommandImpl(LiteralCommandBuilder root, boolean isOp) {
			this.root = root;
			this.isOp = isOp;
		}

		public String getName() {
			return root.getName();
		}

		@SuppressWarnings("unchecked")
		private StringCtx parseCommand(S server, CS sender, String[] args) throws CE {
			StringCtx ctx = new StringCtx(sender);
			AbstractCommandBuilder<?> e = root;
			AbstractCommandBuilder<?> prev = null;
			for(int i = 0;i<args.length;i++) {
				String arg = args[i];
				if(e == prev)throw wrongUsage("commands.generic.parameter.invalid", arg);
				prev = e;
				for (AbstractCommandBuilder<?> next : e.getNext()) {
					if(next instanceof LiteralCommandBuilder) {
						LiteralCommandBuilder l = (LiteralCommandBuilder) next;
						if(arg.equals(l.getName())) {
							e = next;
							break;
						}
					} else if(next instanceof RequiredCommandBuilder) {
						RequiredCommandBuilder r = (RequiredCommandBuilder) next;
						switch (r.getType()) {
						case PLAYER:
						{
							try {
								Object pl = getPlayerObj(server, sender, arg);
								e = next;
								ctx.arg(r.getId(), pl);
							} catch (Exception exc) {
								ctx.lastExc = checkExc(exc);
							}
						}
						break;

						case BOOLEAN:
						{
							if(arg.equalsIgnoreCase("true")) {
								e = next;
								ctx.arg(r.getId(), true);
							} else if(arg.equalsIgnoreCase("false")) {
								e = next;
								ctx.arg(r.getId(), false);
							} else {
								ctx.lastExc = wrongUsage("commands.generic.boolean.invalid", arg);
							}
						}
						break;

						case INT:
						{
							try {
								int v = Integer.parseInt(arg);
								if(r.getSettings() != null) {
									Pair<Integer, Integer> p = (Pair<Integer, Integer>) r.getSettings();
									if(v < p.getKey())throw wrongUsage("commands.generic.num.tooSmall", v);
									if(v > p.getValue())throw wrongUsage("commands.generic.num.tooBig", v);
								}
								ctx.arg(r.getId(), v);
								e = next;
							} catch (NumberFormatException num) {
								ctx.lastExc = wrongUsage("commands.generic.num.invalid", arg);
							}
						}
						break;

						case FLOAT:
						{
							try {
								float v = Float.parseFloat(arg);
								if(r.getSettings() != null) {
									Pair<Float, Float> p = (Pair<Float, Float>) r.getSettings();
									if(v < p.getKey())throw wrongUsage("commands.generic.num.tooSmall", v);
									if(v > p.getValue())throw wrongUsage("commands.generic.num.tooBig", v);
								}
								ctx.arg(r.getId(), v);
								e = next;
							} catch (NumberFormatException num) {
								ctx.lastExc = wrongUsage("commands.generic.num.invalid", arg);
							}
						}
						break;

						case STRING:
						{
							boolean normalString = r.getSettings() == null || !((Boolean)r.getSettings());
							if(!normalString) {
								StringBuilder b = new StringBuilder(arg);
								for(i++;i<args.length;i++) {
									b.append(' ');
									b.append(args[i]);
								}
								e = next;
								ctx.arg(r.getId(), b.toString());
							} else {
								if(arg.startsWith("\"")) {
									StringBuilder b = new StringBuilder(arg);
									for(i++;i<args.length;i++) {
										b.append(' ');
										b.append(args[i]);
										if(args[i].endsWith("\""))break;
									}
									e = next;
									ctx.arg(r.getId(), b.substring(1, b.length() - 1));
								} else {
									e = next;
									ctx.arg(r.getId(), arg);
								}
							}
						}
						break;

						default:
							e = next;
							ctx.arg(r.getId(), arg);
							break;
						}
						if(e == next)break;
					}
				}
			}
			ctx.node = e;
			return ctx;
		}

		public void execute(S server, CS sender, String[] args) throws CE {
			StringCtx ctx = parseCommand(server, sender, args);
			if(ctx.node.getFunc() != null) {
				ctx.node.getFunc().accept(ctx);
				if(ctx.getFail() != null)throw generic("commands." + getName() + ".genericFail", new Object[] {ctx.getFail().remap()});
			} else if(ctx.lastExc != null)throw ctx.lastExc;
			else throw wrongUsage("commands." + getName() + ".usage");
		}

		public List<String> getTabCompletions(S server, CS sender, String[] args) {
			StringCtx ctx;
			try {
				ctx = parseCommand(server, sender, Arrays.copyOf(args, Math.max(0, args.length - 1)));
			} catch (Exception e) {
				return Collections.emptyList();
			}
			List<String> compl = new ArrayList<>();
			for (AbstractCommandBuilder<?> next : ctx.node.getNext()) {
				if(next instanceof LiteralCommandBuilder) {
					compl.add(((LiteralCommandBuilder)next).getName());
				} else if(next instanceof RequiredCommandBuilder) {
					RequiredCommandBuilder r = (RequiredCommandBuilder) next;
					if(r.getPossibleValues() != null) {
						List<String> s = r.getPossibleValues().apply(ctx);
						for (String string : s) {
							if(string.contains(" "))
								compl.add("\"" + string + "\"");
							else
								compl.add(string);
						}
					} else if(r.getType() == ArgType.PLAYER) {
						compl.addAll(getOnlinePlayers(server));
					} else if(r.getType() == ArgType.BOOLEAN) {
						compl.add("true");
						compl.add("false");
					}
					break;
				}
			}
			return getListOfStringsMatchingLastWord(args, compl);
		}

		public boolean isOp() {
			return isOp;
		}
	}

	public static List<String> getListOfStringsMatchingLastWord(String[] inputArgs, Collection<?> possibleCompletions) {
		String s = inputArgs[inputArgs.length - 1];
		List<String> list = Lists.<String>newArrayList();

		if (!possibleCompletions.isEmpty()) {
			for (String s1 : Iterables.transform(possibleCompletions, Functions.toStringFunction()))  {
				if (doesStringStartWith(s, s1)) {
					list.add(s1);
				}
			}
		}

		return list;
	}

	public static boolean doesStringStartWith(String original, String region) {
		return region.regionMatches(true, 0, original, 0, original.length());
	}

	private class StringCtx extends CommandCtx<CS> {
		private AbstractCommandBuilder<?> node;
		private CE lastExc;

		public StringCtx(CS sender) {
			super(sender, StringCommandHandler.this);
		}

	}
}
