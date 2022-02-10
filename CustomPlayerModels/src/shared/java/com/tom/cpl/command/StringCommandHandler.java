package com.tom.cpl.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.tom.cpl.util.Pair;

public abstract class StringCommandHandler<S, CS, CE extends Exception> implements CommandHandler<CS> {
	private final Consumer<CommandImpl> register;

	public StringCommandHandler(Consumer<CommandImpl> register) {
		this.register = register;
		register();
	}

	@Override
	public void register(LiteralCommandBuilder builder) {
		register.accept(new CommandImpl(builder));
	}

	public abstract CE generic(String text, Object... format);
	public abstract CE wrongUsage(String text, Object... format);
	public abstract Object getPlayerObj(S server, CS sender, String name) throws CE;
	public abstract CE checkExc(Exception exc);
	public abstract List<String> getOnlinePlayers(S server);

	public class CommandImpl {
		private final LiteralCommandBuilder root;

		public CommandImpl(LiteralCommandBuilder root) {
			this.root = root;
		}

		public String getName() {
			return root.getName();
		}

		@SuppressWarnings("unchecked")
		public void execute(S server, CS sender, String[] args) throws CE {
			CommandCtx<CS> ctx = new CommandCtx<>(sender, StringCommandHandler.this);
			AbstractCommandBuilder<?> e = root;
			AbstractCommandBuilder<?> prev = null;
			CE lastExc = null;
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
								lastExc = checkExc(exc);
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
								lastExc = wrongUsage("commands.generic.boolean.invalid", arg);
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
								lastExc = wrongUsage("commands.generic.num.invalid", arg);
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
								lastExc = wrongUsage("commands.generic.num.invalid", arg);
							}
						}
						break;

						case STRING:
						{
							boolean greedyString = r.getSettings() == null || !((Boolean)r.getSettings());
							if(greedyString) {
								StringBuilder b = new StringBuilder(arg);
								for(i++;i<args.length;i++) {
									b.append(' ');
									b.append(args[i]);
								}
								e = next;
								ctx.arg(r.getId(), b.toString());
							} else {
								e = next;
								ctx.arg(r.getId(), arg);
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
			if(e.getFunc() != null)e.getFunc().accept(ctx);
			else if(lastExc != null)throw lastExc;
			else throw wrongUsage("commands." + getName() + ".usage");
		}

		public List<String> getTabCompletions(S server, CS sender, String[] args) {
			AbstractCommandBuilder<?> e = root;
			AbstractCommandBuilder<?> prev = null;
			for(int i = 0;i<args.length;i++) {
				String arg = args[i];
				prev = e;
				for (AbstractCommandBuilder<?> next : e.getNext()) {
					if(next instanceof LiteralCommandBuilder) {
						LiteralCommandBuilder l = (LiteralCommandBuilder) next;
						if(arg.equals(l.getName())) {
							e = next;
							break;
						}
					} else if(next instanceof RequiredCommandBuilder) {
						e = next;
						break;
					}
				}
			}
			List<String> compl = new ArrayList<>();
			for (AbstractCommandBuilder<?> next : prev.getNext()) {
				if(next instanceof LiteralCommandBuilder) {
					compl.add(((LiteralCommandBuilder)next).getName());
				} else if(next instanceof RequiredCommandBuilder) {
					RequiredCommandBuilder r = (RequiredCommandBuilder) next;
					if(r.getPossibleValues() != null) {
						compl.addAll(r.getPossibleValues().get());
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
}
