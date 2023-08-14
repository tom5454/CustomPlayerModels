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
 * Parses and serializes an OSC double type (64bit floating point number).
 */
public class DoubleArgumentHandler implements ArgumentHandler<Double>, Cloneable {

	public static final ArgumentHandler<Double> INSTANCE = new DoubleArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected DoubleArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'd';
	}

	@Override
	public Class<Double> getJavaClass() {
		return Double.class;
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
	public DoubleArgumentHandler clone() throws CloneNotSupportedException {
		return (DoubleArgumentHandler) super.clone();
	}

	@Override
	public Double parse(final ByteBuffer input) throws OSCParseException {
		return Double.longBitsToDouble(LongArgumentHandler.INSTANCE.parse(input));
	}

	@Override
	public void serialize(final BytesReceiver output, final Double value) throws OSCSerializeException {
		LongArgumentHandler.INSTANCE.serialize(output, Double.doubleToRawLongBits(value));
	}
}
