/*
 * Copyright 2012 Udo Klimaschewski
 *
 * http://UdoJava.com/
 * http://about.me/udo.klimaschewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tom.cpm.externals.com.udojava.evalex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * <h1>EvalEx - Java Expression Evaluator</h1>
 *
 * <h2>Introduction</h2>
 * EvalEx is a handy expression evaluator for Java, that allows to evaluate simple mathematical and boolean expressions.
 * <br>
 * Key Features:
 * <ul>
 * <li>Uses float for calculation and result</li>
 * <li>Single class implementation, very compact</li>
 * <li>No dependencies to external libraries</li>
 * <li>Precision and rounding mode can be set</li>
 * <li>Supports variables</li>
 * <li>Standard boolean and mathematical operators</li>
 * <li>Standard basic mathematical and boolean functions</li>
 * <li>Custom functions and operators can be added at runtime</li>
 * </ul>
 * <br>
 * <h2>Examples</h2>
 * <pre>
 *  float result = null;
 *
 *  Expression expression = new Expression("1+1/3");
 *  result = expression.eval():
 *  expression.setPrecision(2);
 *  result = expression.eval():
 *
 *  result = new Expression("(3.4 + -4.1)/2").eval();
 *
 *  result = new Expression("SQRT(a^2 + b^2").with("a","2.4").and("b","9.253").eval();
 *
 *  float a = new float("2.4");
 *  float b = new float("9.235");
 *  result = new Expression("SQRT(a^2 + b^2").with("a",a).and("b",b).eval();
 *
 *  result = new Expression("2.4/PI").setPrecision(128).setRoundingMode(RoundingMode.UP).eval();
 *
 *  result = new Expression("random() > 0.5").eval();
 *
 *  result = new Expression("not(x<7 || sqrt(max(x,9)) <= 3))").with("x","22.9").eval();
 * </pre>
 * <br>
 * <h2>Supported Operators</h2>
 * <table>
 *   <tr><th>Mathematical Operators</th></tr>
 *   <tr><th>Operator</th><th>Description</th></tr>
 *   <tr><td>+</td><td>Additive operator</td></tr>
 *   <tr><td>-</td><td>Subtraction operator</td></tr>
 *   <tr><td>*</td><td>Multiplication operator</td></tr>
 *   <tr><td>/</td><td>Division operator</td></tr>
 *   <tr><td>%</td><td>Remainder operator (Modulo)</td></tr>
 *   <tr><td>^</td><td>Power operator</td></tr>
 * </table>
 * <br>
 * <table>
 *   <tr><th>Boolean Operators<sup>*</sup></th></tr>
 *   <tr><th>Operator</th><th>Description</th></tr>
 *   <tr><td>=</td><td>Equals</td></tr>
 *   <tr><td>==</td><td>Equals</td></tr>
 *   <tr><td>!=</td><td>Not equals</td></tr>
 *   <tr><td>&lt;&gt;</td><td>Not equals</td></tr>
 *   <tr><td>&lt;</td><td>Less than</td></tr>
 *   <tr><td>&lt;=</td><td>Less than or equal to</td></tr>
 *   <tr><td>&gt;</td><td>Greater than</td></tr>
 *   <tr><td>&gt;=</td><td>Greater than or equal to</td></tr>
 *   <tr><td>&amp;&amp;</td><td>Boolean and</td></tr>
 *   <tr><td>||</td><td>Boolean or</td></tr>
 * </table>
 * *Boolean operators result always in a float value of 1 or 0 (zero). Any non-zero value is treated as a _true_ value. Boolean _not_ is implemented by a function.
 * <br>
 * <h2>Supported Functions</h2>
 * <table>
 *   <tr><th>Function<sup>*</sup></th><th>Description</th></tr>
 *   <tr><td>NOT(<i>expression</i>)</td><td>Boolean negation, 1 (means true) if the expression is not zero</td></tr>
 *   <tr><td>IF(<i>condition</i>,<i>value_if_true</i>,<i>value_if_false</i>)</td><td>Returns one value if the condition evaluates to true or the other if it evaluates to false</td></tr>
 *   <tr><td>RANDOM()</td><td>Produces a random number between 0 and 1</td></tr>
 *   <tr><td>MIN(<i>e1</i>,<i>e2</i>)</td><td>Returns the smaller of both expressions</td></tr>
 *   <tr><td>MAX(<i>e1</i>,<i>e2</i>)</td><td>Returns the bigger of both expressions</td></tr>
 *   <tr><td>ABS(<i>expression</i>)</td><td>Returns the absolute (non-negative) value of the expression</td></tr>
 *   <tr><td>ROUND(<i>expression</i>,precision)</td><td>Rounds a value to a certain number of digits, uses the current rounding mode</td></tr>
 *   <tr><td>FLOOR(<i>expression</i>)</td><td>Rounds the value down to the nearest integer</td></tr>
 *   <tr><td>CEILING(<i>expression</i>)</td><td>Rounds the value up to the nearest integer</td></tr>
 *   <tr><td>LOG(<i>expression</i>)</td><td>Returns the natural logarithm (base e) of an expression</td></tr>
 *   <tr><td>LOG10(<i>expression</i>)</td><td>Returns the common logarithm (base 10) of an expression</td></tr>
 *   <tr><td>SQRT(<i>expression</i>)</td><td>Returns the square root of an expression</td></tr>
 *   <tr><td>SIN(<i>expression</i>)</td><td>Returns the trigonometric sine of an angle (in degrees)</td></tr>
 *   <tr><td>COS(<i>expression</i>)</td><td>Returns the trigonometric cosine of an angle (in degrees)</td></tr>
 *   <tr><td>TAN(<i>expression</i>)</td><td>Returns the trigonometric tangens of an angle (in degrees)</td></tr>
 *   <tr><td>SINH(<i>expression</i>)</td><td>Returns the hyperbolic sine of a value</td></tr>
 *   <tr><td>COSH(<i>expression</i>)</td><td>Returns the hyperbolic cosine of a value</td></tr>
 *   <tr><td>TANH(<i>expression</i>)</td><td>Returns the hyperbolic tangens of a value</td></tr>
 *   <tr><td>RAD(<i>expression</i>)</td><td>Converts an angle measured in degrees to an approximately equivalent angle measured in radians</td></tr>
 *   <tr><td>DEG(<i>expression</i>)</td><td>Converts an angle measured in radians to an approximately equivalent angle measured in degrees</td></tr>
 * </table>
 * *Functions names are case insensitive.
 * <br>
 * <h2>Supported Constants</h2>
 * <table>
 *   <tr><th>Constant</th><th>Description</th></tr>
 *   <tr><td>PI</td><td>The value of <i>PI</i>, exact to 100 digits</td></tr>
 *   <tr><td>TRUE</td><td>The value one</td></tr>
 *   <tr><td>FALSE</td><td>The value zero</td></tr>
 * </table>
 *
 * <h2>Add Custom Operators</h2>
 *
 * Custom operators can be added easily, simply create an instance of `Expression.Operator` and add it to the expression.
 * Parameters are the operator string, its precedence and if it is left associative. The operators `eval()` method will be called with the float values of the operands.
 * All existing operators can also be overridden.
 * <br>
 * For example, add an operator `x >> n`, that moves the decimal point of _x_ _n_ digits to the right:
 *
 * <pre>
 * Expression e = new Expression("2.1234 >> 2");
 *
 * e.addOperator(e.new Operator(">>", 30, true) {
 *     {@literal @}Override
 *     public float eval(float v1, float v2) {
 *         return v1.movePointRight(v2.toBigInteger().intValue());
 *     }
 * });
 *
 * e.eval(); // returns 212.34
 * </pre>
 * <br>
 * <h2>Add Custom Functions</h2>
 *
 * Adding custom functions is as easy as adding custom operators. Create an instance of `Expression.Function`and add it to the expression.
 * Parameters are the function name and the count of required parameters. The functions `eval()` method will be called with a list of the float parameters.
 * All existing functions can also be overridden.
 * <br>
 * For example, add a function `average(a,b,c)`, that will calculate the average value of a, b and c:
 * <br>
 * <pre>
 * Expression e = new Expression("2 * average(12,4,8)");
 *
 * e.addFunction(e.new Function("average", 3) {
 *     {@literal @}Override
 *     public float eval(List<Float> parameters) {
 *         float sum = parameters.get(0).add(parameters.get(1)).add(parameters.get(2));
 *         return sum.divide(new float(3));
 *     }
 * });
 *
 * e.eval(); // returns 16
 * </pre>
 * The software is licensed under the MIT Open Source license (see LICENSE file).
 * <br>
 * <ul>
 * <li>The *power of* operator (^) implementation was copied from [Stack Overflow](http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-float-in-java) Thanks to Gene Marin</li>
 * <li>The SQRT() function implementation was taken from the book [The Java Programmers Guide To numerical Computing](http://www.amazon.de/Java-Number-Cruncher-Programmers-Numerical/dp/0130460419) (Ronald Mak, 2002)</li>
 * </ul>
 *
 * <h1>tom5454 changes:</h1>
 * <ul>
 * <li>use float instead of BigDecimal
 * <li>made fields protected
 * <li>accepts numbers in '.##' and '-.##' format
 * </ul>
 * </p>
 *
 *@author Udo Klimaschewski (http://about.me/udo.klimaschewski)
 *
 */
public class Expression {

	/**
	 * The original infix expression.
	 */
	protected String expression = null;

	/**
	 * The cached RPN (Reverse Polish Notation) of the expression.
	 */
	protected List<String> rpn = null;

	/**
	 * All defined operators with name and implementation.
	 */
	protected Map<String, Operator> operators = new HashMap<>();

	/**
	 * All defined functions with name and implementation.
	 */
	protected Map<String, Function> functions = new HashMap<>();

	/**
	 * All defined variables with name and value.
	 */
	protected Map<String, Float> variables = new HashMap<>();

	/**
	 * What character to use for decimal separators.
	 */
	private final char decimalSeparator = '.';

	/**
	 * What character to use for minus sign (negative values).
	 */
	private final char minusSign = '-';

	/**
	 * The expression evaluators exception class.
	 */
	public class ExpressionException extends RuntimeException {
		private static final long serialVersionUID = 1118142866870779047L;

		public ExpressionException(String message) {
			super(message);
		}
	}

	/**
	 * Abstract definition of a supported expression function. A function is
	 * defined by a name, the number of parameters and the actual processing
	 * implementation.
	 */
	public abstract class Function {
		/**
		 * Name of this function.
		 */
		private String name;
		/**
		 * Number of parameters expected for this function.
		 */
		private int numParams;

		/**
		 * Creates a new function with given name and parameter count.
		 *
		 * @param name
		 *            The name of the function.
		 * @param numParams
		 *            The number of parameters for this function.
		 */
		public Function(String name, int numParams) {
			this.name = name.toUpperCase();
			this.numParams = numParams;
		}

		public String getName() {
			return name;
		}

		public int getNumParams() {
			return numParams;
		}

		/**
		 * Implementation for this function.
		 *
		 * @param parameters
		 *            Parameters will be passed by the expression evaluator as a
		 *            {@link List} of {@link float} values.
		 * @return The function must return a new {@link float} value as a
		 *         computing result.
		 */
		public abstract float eval(List<Float> parameters);
	}

	/**
	 * Abstract definition of a supported operator. An operator is defined by
	 * its name (pattern), precedence and if it is left- or right associative.
	 */
	public abstract class Operator {
		/**
		 * This operators name (pattern).
		 */
		private String oper;
		/**
		 * Operators precedence.
		 */
		private int precedence;
		/**
		 * Operator is left associative.
		 */
		private boolean leftAssoc;

		/**
		 * Creates a new operator.
		 *
		 * @param oper
		 *            The operator name (pattern).
		 * @param precedence
		 *            The operators precedence.
		 * @param leftAssoc
		 *            <code>true</code> if the operator is left associative,
		 *            else <code>false</code>.
		 */
		public Operator(String oper, int precedence, boolean leftAssoc) {
			this.oper = oper;
			this.precedence = precedence;
			this.leftAssoc = leftAssoc;
		}

		public String getOper() {
			return oper;
		}

		public int getPrecedence() {
			return precedence;
		}

		public boolean isLeftAssoc() {
			return leftAssoc;
		}

		/**
		 * Implementation for this operator.
		 *
		 * @param v1
		 *            Operand 1.
		 * @param v2
		 *            Operand 2.
		 * @return The result of the operation.
		 */
		public abstract float eval(float v1, float v2);
	}

	/**
	 * Expression tokenizer that allows to iterate over a {@link String}
	 * expression token by token. Blank characters will be skipped.
	 */
	private class Tokenizer implements Iterator<String> {

		/**
		 * Actual position in expression string.
		 */
		private int pos = 0;

		/**
		 * The original input expression.
		 */
		private String input;
		/**
		 * The previous token or <code>null</code> if none.
		 */
		private String previousToken;

		/**
		 * Creates a new tokenizer for an expression.
		 *
		 * @param input
		 *            The expression string.
		 */
		public Tokenizer(String input) {
			this.input = input.trim();
		}

		@Override
		public boolean hasNext() {
			return pos < input.length();
		}

		/**
		 * Peek at the next character, without advancing the iterator.
		 *
		 * @return The next character or character 0, if at end of string.
		 */
		private char peekNextChar() {
			if (pos < input.length() - 1) {
				return input.charAt(pos + 1);
			} else {
				return 0;
			}
		}

		@Override
		public String next() {
			StringBuilder token = new StringBuilder();
			if (pos >= input.length()) { return previousToken = null; }
			char ch = input.charAt(pos);
			while (Character.isWhitespace(ch) && pos < input.length()) {
				ch = input.charAt(++pos);
			}
			//tom5454: accept numbers starting with .###
			if (Character.isDigit(ch) || (ch == decimalSeparator && Character.isDigit(peekNextChar()))) {
				while ((Character.isDigit(ch) || ch == decimalSeparator) && pos < input.length()) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
				//tom5454: accept numbers starting with -.###
			} else if (ch == minusSign && (Character.isDigit(peekNextChar()) || (peekNextChar() == decimalSeparator && pos < input.length() - 2 && Character.isDigit(input.charAt(pos + 2)))) && ("(".equals(previousToken) || ",".equals(previousToken) || previousToken == null || operators.containsKey(previousToken))) {
				token.append(minusSign);
				pos++;
				token.append(next());
			} else if (Character.isLetter(ch) || ch == '_') {
				while ((Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') && pos < input.length()) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
			} else if (ch == '(' || ch == ')' || ch == ',') {
				token.append(ch);
				pos++;
			} else {
				while (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_' && !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != ',' && pos < input.length()) {
					token.append(input.charAt(pos));
					pos++;
					ch = pos == input.length() ? 0 : input.charAt(pos);
					if (ch == minusSign) {
						break;
					}
				}
				if (!operators.containsKey(token.toString())) { throw new ExpressionException("Unknown operator '" + token + "' at position " + (pos - token.length() + 1)); }
			}
			return previousToken = token.toString();
		}

		@Override
		public void remove() {
			throw new ExpressionException("remove() not supported");
		}

		/**
		 * Get the actual character position in the string.
		 *
		 * @return The actual character position.
		 */
		public int getPos() {
			return pos;
		}

	}

	/**
	 * Creates a new expression instance from an expression string.
	 *
	 * @param expression
	 *            The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
	 *            <code>"sin(y)>0 & max(z, 3)>3"</code>
	 */
	public Expression(String expression) {
		this.expression = expression;
		addOperator(new Operator("+", 20, true) {
			@Override
			public float eval(float v1, float v2) {
				return v1 + v2;
			}
		});
		addOperator(new Operator("-", 20, true) {
			@Override
			public float eval(float v1, float v2) {
				return v1 - v2;
			}
		});
		addOperator(new Operator("*", 30, true) {
			@Override
			public float eval(float v1, float v2) {
				return v1 * v2;
			}
		});
		addOperator(new Operator("/", 30, true) {
			@Override
			public float eval(float v1, float v2) {
				return v1 / v2;
			}
		});
		addOperator(new Operator("%", 30, true) {
			@Override
			public float eval(float v1, float v2) {
				return v1 % v2;
			}
		});
		addOperator(new Operator("^", 40, false) {
			@Override
			public float eval(float v1, float v2) {
				return (float) Math.pow(v1, v2);
			}
		});
		addOperator(new Operator("&&", 4, false) {
			@Override
			public float eval(float v1, float v2) {
				boolean b1 = v1 >= 0.5f;
				boolean b2 = v2 >= 0.5f;
				return b1 && b2 ? 1f : 0f;
			}
		});

		addOperator(new Operator("||", 2, false) {
			@Override
			public float eval(float v1, float v2) {
				boolean b1 = v1 >= 0.5f;
				boolean b2 = v2 >= 0.5f;
				return b1 || b2 ? 1f : 0f;
			}
		});

		addOperator(new Operator(">", 10, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) == 1 ? 1f : 0f;
			}
		});

		addOperator(new Operator(">=", 10, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) >= 0 ? 1f : 0f;
			}
		});

		addOperator(new Operator("<", 10, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) == -1 ? 1f : 0f;
			}
		});

		addOperator(new Operator("<=", 10, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) <= 0 ? 1f : 0f;
			}
		});

		addOperator(new Operator("=", 7, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) == 0 ? 1f : 0f;
			}
		});
		addOperator(new Operator("==", 7, false) {
			@Override
			public float eval(float v1, float v2) {
				return operators.get("=").eval(v1, v2);
			}
		});

		addOperator(new Operator("!=", 7, false) {
			@Override
			public float eval(float v1, float v2) {
				return Float.compare(v1, v2) != 0 ? 1f : 0f;
			}
		});
		addOperator(new Operator("<>", 7, false) {
			@Override
			public float eval(float v1, float v2) {
				return operators.get("!=").eval(v1, v2);
			}
		});

		addFunction(new Function("NOT", 1) {
			@Override
			public float eval(List<Float> parameters) {
				boolean zero = parameters.get(0).compareTo(0f) == 0;
				return zero ? 1f : 0f;
			}
		});

		addFunction(new Function("IF", 3) {
			@Override
			public float eval(List<Float> parameters) {
				boolean isTrue = !parameters.get(0).equals(0f);
				return isTrue ? parameters.get(1) : parameters.get(2);
			}
		});

		addFunction(new Function("RANDOM", 0) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.random();
			}
		});
		addFunction(new Function("SIN", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.sin(Math.toRadians(parameters.get(0)));
			}
		});
		addFunction(new Function("COS", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.cos(Math.toRadians(parameters.get(0)));
			}
		});
		addFunction(new Function("TAN", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.tan(Math.toRadians(parameters.get(0)));
			}
		});
		addFunction(new Function("SINH", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.sinh(parameters.get(0));
			}
		});
		addFunction(new Function("COSH", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.cosh(parameters.get(0));
			}
		});
		addFunction(new Function("TANH", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.tanh(parameters.get(0));
			}
		});
		addFunction(new Function("RAD", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.toRadians(parameters.get(0));
			}
		});
		addFunction(new Function("DEG", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.toDegrees(parameters.get(0));
			}
		});
		addFunction(new Function("MAX", 2) {
			@Override
			public float eval(List<Float> parameters) {
				float v1 = parameters.get(0);
				float v2 = parameters.get(1);
				return Float.compare(v1, v2) > 0 ? v1 : v2;
			}
		});
		addFunction(new Function("MIN", 2) {
			@Override
			public float eval(List<Float> parameters) {
				float v1 = parameters.get(0);
				float v2 = parameters.get(1);
				return Float.compare(v1, v2) < 0 ? v1 : v2;
			}
		});
		addFunction(new Function("ABS", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return Math.abs(parameters.get(0));
			}
		});
		addFunction(new Function("LOG", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.log(parameters.get(0));
			}
		});
		addFunction(new Function("LOG10", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.log10(parameters.get(0));
			}
		});
		addFunction(new Function("ROUND", 2) {
			@Override
			public float eval(List<Float> parameters) {
				float toRound = parameters.get(0);
				int precision = parameters.get(1).intValue();
				double d = Math.pow(10, precision);
				return (float) (((int) (toRound * d)) / d);
			}
		});
		addFunction(new Function("FLOOR", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.floor(parameters.get(0));
			}
		});
		addFunction(new Function("CEILING", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.ceil(parameters.get(0));
			}
		});
		addFunction(new Function("SQRT", 1) {
			@Override
			public float eval(List<Float> parameters) {
				return (float) Math.sqrt(parameters.get(0));
			}
		});

		variables.put("PI", (float) Math.PI);
		variables.put("TRUE", 1f);
		variables.put("FALSE", 0f);

	}

	/**
	 * Is the string a number?
	 *
	 * @param st
	 *            The string.
	 * @return <code>true</code>, if the input string is a number.
	 */
	private boolean isNumber(String st) {
		if (st.charAt(0) == minusSign && st.length() == 1) return false;
		for (char ch : st.toCharArray()) {
			if (!Character.isDigit(ch) && ch != minusSign && ch != decimalSeparator) return false;
		}
		return true;
	}

	/**
	 * Implementation of the <i>Shunting Yard</i> algorithm to transform an
	 * infix expression to a RPN expression.
	 *
	 * @param expression
	 *            The input expression in infx.
	 * @return A RPN representation of the expression, with each token as a list
	 *         member.
	 */
	private List<String> shuntingYard(String expression) throws ExpressionException {
		List<String> outputQueue = new ArrayList<>();
		Stack<String> stack = new Stack<>();

		Tokenizer tokenizer = new Tokenizer(expression);

		String lastFunction = null;
		String previousToken = null;
		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			if (isNumber(token)) {
				outputQueue.add(token);
			} else if (variables.containsKey(token)) {
				outputQueue.add(token);
			} else if (functions.containsKey(token.toUpperCase())) {
				stack.push(token);
				lastFunction = token;
			} else if (Character.isLetter(token.charAt(0))) {
				stack.push(token);
			} else if (",".equals(token)) {
				while (!stack.isEmpty() && !"(".equals(stack.peek())) {
					outputQueue.add(stack.pop());
				}
				if (stack.isEmpty()) { throw new ExpressionException("Parse error for function '" + lastFunction + "'"); }
			} else if (operators.containsKey(token)) {
				Operator o1 = operators.get(token);
				String token2 = stack.isEmpty() ? null : stack.peek();
				while (operators.containsKey(token2) && (o1.isLeftAssoc() && o1.getPrecedence() <= operators.get(token2).getPrecedence() || o1.getPrecedence() < operators.get(token2).getPrecedence())) {
					outputQueue.add(stack.pop());
					token2 = stack.isEmpty() ? null : stack.peek();
				}
				stack.push(token);
			} else if ("(".equals(token)) {
				if (previousToken != null) {
					if (isNumber(previousToken)) { throw new ExpressionException("Missing operator at character position " + tokenizer.getPos()); }
				}
				stack.push(token);
			} else if (")".equals(token)) {
				while (!stack.isEmpty() && !"(".equals(stack.peek())) {
					outputQueue.add(stack.pop());
				}
				if (stack.isEmpty()) { throw new ExpressionException("Mismatched parentheses"); }
				stack.pop();
				if (!stack.isEmpty() && functions.containsKey(stack.peek().toUpperCase())) {
					outputQueue.add(stack.pop());
				}
			}
			previousToken = token;
		}
		while (!stack.isEmpty()) {
			String element = stack.pop();
			if ("(".equals(element) || ")".equals(element)) { throw new ExpressionException("Mismatched parentheses"); }
			if (!operators.containsKey(element)) { throw new ExpressionException("Unknown operator or function: " + element); }
			outputQueue.add(element);
		}
		return outputQueue;
	}

	/**
	 * Evaluates the expression.
	 *
	 * @return The result of the expression.
	 */
	public float eval() throws ExpressionException {

		Stack<Float> stack = new Stack<>();

		for (String token : getRPN()) {
			if (operators.containsKey(token)) {
				float v1 = stack.pop();
				float v2 = stack.pop();
				stack.push(operators.get(token).eval(v2, v1));
			} else if (variables.containsKey(token)) {
				stack.push(variables.get(token));
			} else if (functions.containsKey(token.toUpperCase())) {
				Function f = functions.get(token.toUpperCase());
				ArrayList<Float> p = new ArrayList<>();
				for (int i = 0; i < f.numParams; i++) {
					p.add(0, stack.pop());
				}
				float fResult = f.eval(p);
				stack.push(fResult);
			} else {
				try {
					stack.push(Float.parseFloat(token));
				} catch (NumberFormatException e) {
					throw new ExpressionException(e.getMessage());
				}
			}
		}
		return stack.pop();
	}

	/**
	 * Adds an operator to the list of supported operators.
	 *
	 * @param operator
	 *            The operator to add.
	 * @return The previous operator with that name, or <code>null</code> if
	 *         there was none.
	 */
	public Operator addOperator(Operator operator) {
		return operators.put(operator.getOper(), operator);
	}

	/**
	 * Adds a function to the list of supported functions
	 *
	 * @param function
	 *            The function to add.
	 * @return The previous operator with that name, or <code>null</code> if
	 *         there was none.
	 */
	public Function addFunction(Function function) {
		return functions.put(function.getName(), function);
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable name.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression setVariable(String variable, float value) {
		variables.put(variable, value);
		return this;
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable to set.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression setVariable(String variable, String value) {
		if (isNumber(value)) variables.put(variable, Float.parseFloat(value));
		else {
			expression = expression.replaceAll("\\b" + variable + "\\b", "(" + value + ")");
			rpn = null;
		}
		return this;
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable to set.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression with(String variable, float value) {
		return setVariable(variable, value);
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable to set.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression and(String variable, String value) {
		return setVariable(variable, value);
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable to set.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression and(String variable, float value) {
		return setVariable(variable, value);
	}

	/**
	 * Sets a variable value.
	 *
	 * @param variable
	 *            The variable to set.
	 * @param value
	 *            The variable value.
	 * @return The expression, allows to chain methods.
	 */
	public Expression with(String variable, String value) {
		return setVariable(variable, value);
	}

	/**
	 * Get an iterator for this expression, allows iterating over an expression
	 * token by token.
	 *
	 * @return A new iterator instance for this expression.
	 */
	public Iterator<String> getExpressionTokenizer() {
		return new Tokenizer(expression);
	}

	/**
	 * Cached access to the RPN notation of this expression, ensures only one
	 * calculation of the RPN per expression instance. If no cached instance
	 * exists, a new one will be created and put to the cache.
	 *
	 * @return The cached RPN instance.
	 */
	protected List<String> getRPN() throws ExpressionException {
		if (rpn == null) {
			rpn = shuntingYard(expression);
		}
		return rpn;
	}

	/**
	 * Get a string representation of the RPN (Reverse Polish Notation) for this
	 * expression.
	 *
	 * @return A string with the RPN representation for this expression.
	 */
	public String toRPN() {
		String result = new String();
		for (String st : getRPN()) {
			result = result.isEmpty() ? result : result + " ";
			result += st;
		}
		return result;
	}

}
