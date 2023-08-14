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
import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCImpulse;

/**
 * Parses and serializes an OSC <i>Impulse</i> type.
 */
public class ImpulseArgumentHandler implements ArgumentHandler<OSCImpulse>, Cloneable {

	public static final ArgumentHandler<OSCImpulse> INSTANCE = new ImpulseArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected ImpulseArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'I';
	}

	@Override
	public Class<OSCImpulse> getJavaClass() {
		return OSCImpulse.class;
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
	public ImpulseArgumentHandler clone() throws CloneNotSupportedException {
		return (ImpulseArgumentHandler) super.clone();
	}

	@Override
	public OSCImpulse parse(final ByteBuffer input) {
		return OSCImpulse.INSTANCE;
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCImpulse value) {

//		if (value != OSCImpulse.INSTANCE) {
//			throw new OSCSerializeException();
//		}
	}
}
