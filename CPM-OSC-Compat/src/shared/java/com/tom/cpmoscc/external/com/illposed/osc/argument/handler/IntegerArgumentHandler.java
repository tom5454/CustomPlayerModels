/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument.handler;

import java.nio.ByteBuffer;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.BytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Parses and serializes an OSC integer type (32bit signed integer).
 */
public class IntegerArgumentHandler implements ArgumentHandler<Integer>, Cloneable {

	// Public API
	/**
	 * The number of bytes used to represent this type in an OSC byte array (4).
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int BYTES = Integer.SIZE / Byte.SIZE;
	public static final ArgumentHandler<Integer> INSTANCE = new IntegerArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected IntegerArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'i';
	}

	@Override
	public Class<Integer> getJavaClass() {
		return Integer.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {
		// we make no use of any properties
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public IntegerArgumentHandler clone() throws CloneNotSupportedException {
		return (IntegerArgumentHandler) super.clone();
	}

	@Override
	public Integer parse(final ByteBuffer input) {

		final Integer value = input.asIntBuffer().get();
		input.position(input.position() + BYTES);
		return value;
	}

	@Override
	public void serialize(final BytesReceiver output, final Integer value) {

		int curValue = value;
		final byte[] intBytes = new byte[4];
		intBytes[3] = (byte)curValue; curValue >>>= 8;
		intBytes[2] = (byte)curValue; curValue >>>= 8;
		intBytes[1] = (byte)curValue; curValue >>>= 8;
		intBytes[0] = (byte)curValue;

		output.put(intBytes);
	}
}
