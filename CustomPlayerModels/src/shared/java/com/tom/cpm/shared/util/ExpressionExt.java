package com.tom.cpm.shared.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.tom.cpl.function.FloatBiFunction;
import com.tom.cpl.function.FloatFunction;
import com.tom.cpl.function.FloatSupplier;
import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpm.externals.com.udojava.evalex.Expression;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.util.ExpressionExt.SerializedExpression.Op;
import com.tom.cpm.shared.util.ExpressionExt.SerializedExpression.Opcode;

public class ExpressionExt extends Expression {
	private Set<String> customFunc = new HashSet<>();

	public ExpressionExt(String expression) {
		super(expression);
		variables.put("E", (float) Math.E);
	}

	public ExpressionExt(SerializedExpression cex) {
		super(null);
		rpn = new ArrayList<>();
		cex.operations.forEach(op -> op.addToRPN(rpn));
	}

	public SerializedExpression serialize() {
		getRPN();//Eval RPN
		SerializedExpression cex = new SerializedExpression();
		for (String token : rpn) {

			boolean encoded = false;
			for(Opcode op : Opcode.VALUES) {
				if(op.value != null && token.equals(op.value)) {
					cex.operations.add(new Op(op, token));
					encoded = true;
					break;
				}
			}
			if(encoded) continue;

			if (variables.containsKey(token)) {
				cex.operations.add(new Op(Opcode.LOAD, token));
				continue;
			}

			if (functions.containsKey(token.toUpperCase())) {
				if(customFunc.contains(token))
					cex.operations.add(new Op(Opcode.INVOKE, token));
				else
					cex.operations.add(new Op(Opcode.INVOKE_BUILTIN_FUNC, token));
				continue;
			}

			if(token.equals("0")) {
				cex.operations.add(new Op(Opcode.ICONST_0, token));
				continue;
			}

			if(token.equals("1")) {
				cex.operations.add(new Op(Opcode.ICONST_1, token));
				continue;
			}

			try {
				int v = Integer.parseInt(token);
				if (Math.abs(v) < 0xffffff)cex.operations.add(new Op(Opcode.VICONST, token));
				else cex.operations.add(new Op(Opcode.ICONST, token));
				continue;
			} catch (NumberFormatException e) {
			}

			cex.operations.add(new Op(Opcode.FCONST, token));
		}
		return cex;
	}

	public FloatSupplier compile(Map<String, FloatSupplier> dynamicVars) {
		Stack<ExpResult> stack = new Stack<>();

		for (String token : getRPN()) {
			if (operators.containsKey(token)) {
				ExpResult v1 = stack.pop();
				ExpResult v2 = stack.pop();
				Operator op = operators.get(token);
				FloatSupplier f1 = v1.toResult();
				FloatSupplier f2 = v2.toResult();
				stack.push(new ExpResult(() -> op.eval(f2.getAsFloat(), f1.getAsFloat()), Arrays.asList(v1, v2), true));
			} else if (variables.containsKey(token)) {
				FloatSupplier fs = dynamicVars.get(token);
				if (fs != null) {
					stack.push(new ExpResult(fs, null, false));
				} else {
					float v = variables.get(token);
					stack.push(new ExpResult(v));
				}
			} else if (functions.containsKey(token.toUpperCase())) {
				Function f = functions.get(token.toUpperCase());
				List<ExpResult> p = new ArrayList<>(f.getNumParams());
				List<FloatSupplier> fs = new ArrayList<>(f.getNumParams());
				List<Float> vc = new ArrayList<>(f.getNumParams());
				for (int i = 0; i < f.getNumParams(); i++) {
					ExpResult r = stack.pop();
					p.add(0, r);
					fs.add(0, r.toResult());
					vc.add(0f);
				}
				stack.push(new ExpResult(() -> funcEval(f, fs, vc), p, f.isConstFunction()));
			} else {
				try {
					float v = Float.parseFloat(token);
					stack.push(new ExpResult(v));
				} catch (NumberFormatException e) {
					throw new ExpressionException(e.getMessage());
				}
			}
		}

		ExpResult fs = stack.pop();
		if (!stack.isEmpty())
			throw new ExpressionException("Stack not empty");

		return fs.toResult();
	}

	private static float funcEval(Function f, List<FloatSupplier> p, List<Float> fs) {
		for (int i = 0; i < f.getNumParams(); i++) {
			fs.set(i, p.get(i).getAsFloat());
		}
		return f.eval(fs);
	}

	@Override
	@Deprecated
	public Function addFunction(Function function) {
		return super.addFunction(function);
	}

	@Override
	@Deprecated
	public Operator addOperator(Operator operator) {
		return super.addOperator(operator);
	}

	public void addFunction(String name, FloatSupplier func, boolean isConst) {
		customFunc.add(name);
		addFunction(new Function(name, 0) {

			@Override
			public float eval(List<Float> parameters) {
				return func.getAsFloat();
			}

			@Override
			public boolean isConstFunction() {
				return isConst;
			}
		});
	}

	public void addFunction(String name, FloatFunction func, boolean isConst) {
		customFunc.add(name);
		addFunction(new Function(name, 1) {

			@Override
			public float eval(List<Float> parameters) {
				return func.apply(parameters.get(0));
			}

			@Override
			public boolean isConstFunction() {
				return isConst;
			}
		});
	}

	public void addFunction(String name, FloatBiFunction func, boolean isConst) {
		customFunc.add(name);
		addFunction(new Function(name, 2) {

			@Override
			public float eval(List<Float> parameters) {
				return func.apply(parameters.get(0), parameters.get(1));
			}

			@Override
			public boolean isConstFunction() {
				return isConst;
			}
		});
	}

	public void addFunction(String name, int args, ToFloatFunction<List<Float>> func, boolean isConst) {
		customFunc.add(name);
		addFunction(new Function(name, args) {

			@Override
			public float eval(List<Float> parameters) {
				return func.apply(parameters);
			}

			@Override
			public boolean isConstFunction() {
				return isConst;
			}
		});
	}

	public static class SerializedExpression {
		private List<Op> operations;

		public SerializedExpression(IOHelper in, ExpContext ctx) throws IOException {
			operations = new ArrayList<>();
			while(true) {
				int op = in.read();
				if(op >= Opcode.VALUES.length)throw new IOException("Illegal opcode in expression");
				Opcode opcode = Opcode.VALUES[op];
				if(opcode == Opcode.END)break;
				operations.add(new Op(opcode, in, ctx));
			}
		}

		private SerializedExpression() {
			operations = new ArrayList<>();
		}

		public void write(IOHelper out, ExpContext ctx) throws IOException {
			for (Op op : operations) {
				out.write(op.opcode.ordinal());
				op.opcode.store.store(out, ctx, op);
			}
			out.write(Opcode.END.ordinal());
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Serialized Expression:");
			for (Op op : operations) {
				sb.append("\n\t");
				sb.append(op.opcode.name());
				if (op.opcode.store == OpStore.NO_ARGS)continue;
				sb.append(' ');
				sb.append(op.value);
			}
			return sb.toString();
		}

		public static enum Opcode {
			LOAD((io, ctx, op) -> op.setValue(ctx.loadVar(io)), (io, ctx, op) -> ctx.storeVar(op.value, io)),
			OP_ADD("+"),
			OP_SUB("-"),
			OP_MUL("*"),
			OP_DIV("/"),
			OP_REM("%"),
			OP_POW("^"),
			OP_AND("&&"),
			OP_OR("||"),
			OP_CMP_L(">"),
			OP_CMP_LEQ(">="),
			OP_CMP_G("<"),
			OP_CMP_GEQ("<="),
			OP_EQ("="),
			OP_NEQ("!="),
			ICONST((io, ctx, op) -> Integer.toString(io.readInt()), (io, ctx, op) -> io.writeInt(Integer.parseInt(op.value))),
			VICONST((io, ctx, op) -> Integer.toString(io.readSignedVarInt()), (io, ctx, op) -> io.writeSignedVarInt(Integer.parseInt(op.value))),
			FCONST((io, ctx, op) -> Float.toString(io.readFloat()), (io, ctx, op) -> io.writeFloat(Float.parseFloat(op.value))),
			ICONST_0("FALSE"),
			ICONST_1("TRUE"),
			FCONST_PI("PI"),
			FCONST_E("E"),
			INVOKE_BUILTIN_FUNC(Functions::read, Functions::store),
			INVOKE((io, ctx, op) -> op.setValue(ctx.loadFunc(io)), (io, ctx, op) -> ctx.storeFunc(op.value, io)),
			END(),
			;
			public static final Opcode[] VALUES = values();
			private final OpInit init;
			private final OpStore store;
			private String value;
			private Opcode(OpInit init, OpStore store) {
				this.init = init;
				this.store = store;
			}

			private Opcode(String append) {
				this.init = (io, ctx, op) -> op.setValue(append);
				this.store = OpStore.NO_ARGS;
				this.value = append;
			}

			private Opcode() {
				this.init = null;
				this.store = null;
			}
		}

		public static enum Functions {
			NOT, IF, RANDOM, SIN, COS, TAN, SINH, COSH, TANH, RAD, DEG, MAX, MIN,
			ABS, LOG, LOG10, ROUND, FLOOR, CEILING, SQRT
			;
			private static final Functions[] VALUES = values();

			private static void read(IOHelper io, ExpContext ctx, Op op) throws IOException {
				int f = io.read();
				if(f >= VALUES.length)throw new IOException("Unknown function");
				op.setValue(Functions.VALUES[f].name());
			}

			private static void store(IOHelper io, ExpContext ctx, Op op) throws IOException {
				Functions func = null;
				for (Functions f : VALUES) {
					if(f.name().equals(op.value)) {
						func = f;
						break;
					}
				}
				if(func == null)throw new IOException("Unknown function");
				io.write(func.ordinal());
			}
		}

		public static class Op {
			private final Opcode opcode;
			private String value;

			public Op(Opcode opcode, String value) {
				this.opcode = opcode;
				this.value = value;
			}

			public Op(Opcode opcode, IOHelper io, ExpContext ctx) throws IOException {
				this(opcode, null);
				opcode.init.init(io, ctx, this);
			}

			public void addToRPN(List<String> rpn) {
				rpn.add(value);
			}

			public void setValue(String value) {
				this.value = value;
			}
		}

		@FunctionalInterface
		public interface OpInit {
			void init(IOHelper io, ExpContext ctx, Op op) throws IOException;
		}

		@FunctionalInterface
		public interface OpStore {
			public static final OpStore NO_ARGS = (io, ctx, op) -> {};

			void store(IOHelper io, ExpContext ctx, Op op) throws IOException;
		}
	}

	public static interface ExpContext {
		String loadVar(IOHelper h) throws IOException;
		void storeVar(String var, IOHelper h) throws IOException;

		String loadFunc(IOHelper h) throws IOException;
		void storeFunc(String var, IOHelper h) throws IOException;
	}

	public void setExpression(String expression) {
		this.expression = expression;
		rpn = null;
	}

	public ExpressionExt setVariables(Map<String, Float> variables) {
		this.variables.putAll(variables);
		return this;
	}

	private static class ExpResult {
		private FloatSupplier result;
		private List<ExpResult> in;
		private float constVal;
		private boolean constFun;
		private Boolean isConst;

		public ExpResult(FloatSupplier result, List<ExpResult> in, boolean constFun) {
			this.result = result;
			this.in = in;
			this.constFun = constFun;
		}

		public ExpResult(float constVal) {
			this.constVal = constVal;
		}

		public FloatSupplier toResult() {
			if (result == null) {
				Float v = constVal;
				return () -> v;
			}
			if (isConst()) {
				Float v = result.getAsFloat();
				return () -> v;
			}
			return result;
		}

		public boolean isConst() {
			if (isConst == null)
				isConst = result == null || (constFun && checkInputs());
			return isConst;
		}

		private boolean checkInputs() {
			if (in != null) {
				for (ExpResult expResult : in) {
					if (!expResult.isConst())
						return false;
				}
			}
			return true;
		}
	}
}
