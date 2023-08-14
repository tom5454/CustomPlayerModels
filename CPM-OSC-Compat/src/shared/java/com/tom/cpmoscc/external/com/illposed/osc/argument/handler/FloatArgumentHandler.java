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
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Parses and serializes an OSC float type (32bit floating point number).
 */
public class FloatArgumentHandler implements ArgumentHandler<Float>, Cloneable {

	public static final ArgumentHandler<Float> INSTANCE = new FloatArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected FloatArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'f';
	}

	@Override
	public Class<Float> getJavaClass() {
		return Float.class;
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
	public FloatArgumentHandler clone() throws CloneNotSupportedException {
		return (FloatArgumentHandler) super.clone();
	}

	@Override
	public Float parse(final ByteBuffer input) throws OSCParseException {
		return Float.intBitsToFloat(IntegerArgumentHandler.INSTANCE.parse(input));
	}

	@Override
	public void serialize(final BytesReceiver output, final Float value) throws OSCSerializeException {
		IntegerArgumentHandler.INSTANCE.serialize(output, Float.floatToRawIntBits(value));
	}
}
