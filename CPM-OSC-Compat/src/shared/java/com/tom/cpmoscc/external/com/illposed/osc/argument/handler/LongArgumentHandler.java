/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument.handler;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.BytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Parses and serializes an OSC long type (64bit signed integer).
 */
public class LongArgumentHandler implements ArgumentHandler<Long>, Cloneable {

	// Public API
	/**
	 * The number of bytes used to represent this type in an OSC byte array (8).
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int BYTES = Long.SIZE / Byte.SIZE;
	public static final ArgumentHandler<Long> INSTANCE = new LongArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected LongArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'h';
	}

	@Override
	public Class<Long> getJavaClass() {
		return Long.class;
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
	public LongArgumentHandler clone() throws CloneNotSupportedException {
		return (LongArgumentHandler) super.clone();
	}

	@Override
	public Long parse(final ByteBuffer input) {

		final Long value = input.asLongBuffer().get();
		((Buffer)input).position(input.position() + BYTES);
		return value;
	}

	@Override
	public void serialize(final BytesReceiver output, final Long value) {

		long curValue = value;
		final byte[] longIntBytes = new byte[8];
		longIntBytes[7] = (byte)curValue; curValue >>>= 8;
		longIntBytes[6] = (byte)curValue; curValue >>>= 8;
		longIntBytes[5] = (byte)curValue; curValue >>>= 8;
		longIntBytes[4] = (byte)curValue; curValue >>>= 8;
		longIntBytes[3] = (byte)curValue; curValue >>>= 8;
		longIntBytes[2] = (byte)curValue; curValue >>>= 8;
		longIntBytes[1] = (byte)curValue; curValue >>>= 8;
		longIntBytes[0] = (byte)curValue;

		output.put(longIntBytes);
	}
}
