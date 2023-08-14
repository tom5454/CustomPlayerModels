/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument;

import java.io.Serializable;

/**
 * This class represents an OSC 32bit unsigned integer.
 * We use this class, because Java has no matching primitive data-type,
 * and <tt>long</tt> is already used for signed 64bit integers.
 */
public final class OSCUnsigned implements Cloneable, Serializable, Comparable<OSCUnsigned> {

	// Public API
	/**
	 * The number of bytes used to represent an unsigned integer value in binary form.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int BYTES = 4;
	// Public API
	/**
	 * A constant holding the minimum value a 32bit unsigned integer can have, 0.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final OSCUnsigned MIN_VALUE = new OSCUnsigned(0x0L);
	// Public API
	/**
	 * A constant holding the maximum value a 32bit unsigned integer can have, 2^{32}.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final OSCUnsigned MAX_VALUE = new OSCUnsigned(0xFFFFFFFFL);

	private static final long serialVersionUID = 1L;

	private final long value;

	private OSCUnsigned(final long value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object other) {

		return (other instanceof OSCUnsigned)
				&& (toLong() == ((OSCUnsigned) other).toLong());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = (89 * hash) + (int) (this.value ^ (this.value >>> 32));
		return hash;
	}

	@Override
	public int compareTo(final OSCUnsigned other) {
		return (int) (toLong() - other.toLong());
	}

	@Override
	public OSCUnsigned clone() throws CloneNotSupportedException {
		return (OSCUnsigned) super.clone();
	}

	/**
	 * Returns the 32bit unsigned value in form of a long.
	 * @return contains the value in the lower/least significant 32 bits; always positive
	 */
	public long toLong() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public static OSCUnsigned valueOf(final byte[] bytes) {

		if (bytes.length != BYTES) {
			throw new IllegalArgumentException("We need exactly 4 bytes");
		}
		final long value
				= (((long) bytes[0] & 0xFF) << (3 * Byte.SIZE))
				| (((long) bytes[1] & 0xFF) << (2 * Byte.SIZE))
				| (((long) bytes[2] & 0xFF) << (Byte.SIZE))
				|  ((long) bytes[3] & 0xFF);
		return valueOf(value);
	}

	public static OSCUnsigned valueOf(final long value) {

		if ((value < MIN_VALUE.value) || (value > MAX_VALUE.value)) {
			throw new IllegalArgumentException(
					"Value " + value + " lies not within 32bit unsigned integer range ("
					+ MIN_VALUE.value + " - " + MAX_VALUE.value + ").");
		}
		return new OSCUnsigned(value);
	}
}
