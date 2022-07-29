/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tom.cpm.externals.org.apache.commons.math3;

import java.util.Arrays;

/**
 * Immutable representation of a real polynomial function with real
 * coefficients.
 * <p>
 * <a href="http://mathworld.wolfram.com/HornersMethod.html">Horner's Method</a>
 * is used to evaluate the function.
 * </p>
 *
 */
public class PolynomialFunction {
	/**
	 * The coefficients of the polynomial, ordered by degree -- i.e.,
	 * coefficients[0] is the constant term and coefficients[n] is the
	 * coefficient of x^n where n is the degree of the polynomial.
	 */
	private final double coefficients[];

	/**
	 * Construct a polynomial with the given coefficients. The first element of
	 * the coefficients array is the constant term. Higher degree coefficients
	 * follow in sequence. The degree of the resulting polynomial is the index
	 * of the last non-null element of the array, or 0 if all elements are null.
	 * <p>
	 * The constructor makes a copy of the input array and assigns the copy to
	 * the coefficients property.
	 * </p>
	 *
	 * @param c
	 *            Polynomial coefficients.
	 * @throws NullArgumentException
	 *             if {@code c} is {@code null}.
	 * @throws NoDataException
	 *             if {@code c} is empty.
	 */
	public PolynomialFunction(double c[]) {
		super();
		if (c == null)
			throw new RuntimeException();
		int n = c.length;
		if (n == 0) {
			throw new RuntimeException("No data: " + n);
		}
		while ((n > 1) && (c[n - 1] == 0)) {
			--n;
		}
		this.coefficients = new double[n];
		System.arraycopy(c, 0, this.coefficients, 0, n);
	}

	/**
	 * Compute the value of the function for the given argument.
	 * <p>
	 * The value returned is <br/>
	 * <code>coefficients[n] * x^n + ... + coefficients[1] * x  + coefficients[0]</code>
	 * </p>
	 *
	 * @param x
	 *            Argument for which the function value should be computed.
	 * @return the value of the polynomial at the given point.
	 * @see UnivariateFunction#value(double)
	 */
	double value(double x) {
		return evaluate(coefficients, x);
	}

	/**
	 * Returns a copy of the coefficients array.
	 * <p>
	 * Changes made to the returned copy will not affect the coefficients of the
	 * polynomial.
	 * </p>
	 *
	 * @return a fresh copy of the coefficients array.
	 */
	public double[] getCoefficients() {
		return Arrays.copyOf(coefficients, coefficients.length);
	}

	/**
	 * Uses Horner's Method to evaluate the polynomial with the given
	 * coefficients at the argument.
	 *
	 * @param coefficients
	 *            Coefficients of the polynomial to evaluate.
	 * @param argument
	 *            Input value.
	 * @return the value of the polynomial.
	 * @throws NoDataException
	 *             if {@code coefficients} is empty.
	 * @throws NullArgumentException
	 *             if {@code coefficients} is {@code null}.
	 */
	private static double evaluate(double[] coefficients, double argument) {
		if (coefficients == null)
			throw new RuntimeException();
		int n = coefficients.length;
		if (n == 0) {
			throw new RuntimeException("no data: " + n);
		}
		double result = coefficients[n - 1];
		for (int j = n - 2; j >= 0; j--) {
			result = argument * result + coefficients[j];
		}
		return result;
	}

	/**
	 * Returns the coefficients of the derivative of the polynomial with the
	 * given coefficients.
	 *
	 * @param coefficients
	 *            Coefficients of the polynomial to differentiate.
	 * @return the coefficients of the derivative or {@code null} if
	 *         coefficients has length 1.
	 * @throws NoDataException
	 *             if {@code coefficients} is empty.
	 * @throws NullArgumentException
	 *             if {@code coefficients} is {@code null}.
	 */
	private static double[] differentiate(double[] coefficients) {
		if (coefficients == null)
			throw new RuntimeException();
		int n = coefficients.length;
		if (n == 0) {
			throw new RuntimeException("no data: " + n);
		}
		if (n == 1) {
			return new double[] { 0 };
		}
		double[] result = new double[n - 1];
		for (int i = n - 1; i > 0; i--) {
			result[i - 1] = i * coefficients[i];
		}
		return result;
	}

	/**
	 * Returns the derivative as a {@link PolynomialFunction}.
	 *
	 * @return the derivative polynomial.
	 */
	PolynomialFunction polynomialDerivative() {
		return new PolynomialFunction(differentiate(coefficients));
	}

	/**
	 * Creates a string representing a coefficient, removing ".0" endings.
	 *
	 * @param coeff
	 *            Coefficient.
	 * @return a string representation of {@code coeff}.
	 */
	private static String toString(double coeff) {
		final String c = Double.toString(coeff);
		if (c.endsWith(".0")) {
			return c.substring(0, c.length() - 2);
		} else {
			return c;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coefficients);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PolynomialFunction)) {
			return false;
		}
		PolynomialFunction other = (PolynomialFunction) obj;
		if (!Arrays.equals(coefficients, other.coefficients)) {
			return false;
		}
		return true;
	}
}
