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
 * Parses and serializes an OSC null type.
 */
public class NullArgumentHandler implements ArgumentHandler<Object>, Cloneable {

	public static final ArgumentHandler<Object> INSTANCE = new NullArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected NullArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'N';
	}

	@Override
	public Class<Object> getJavaClass() {
		return Object.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {
		// we make no use of any properties
	}

	@Override
	public boolean isMarkerOnly() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NullArgumentHandler clone() throws CloneNotSupportedException {
		return (NullArgumentHandler) super.clone();
	}

	@Override
	public Object parse(final ByteBuffer input) {
		return null;
	}

	@Override
	public void serialize(final BytesReceiver output, final Object value) {

//		if (value != null) {
//			throw new OSCSerializeException();
//		}
	}
}
