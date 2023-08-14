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
import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCUnsigned;

/**
 * Parses and serializes an OSC unsigned type (32bit unsigned integer).
 */
public class UnsignedIntegerArgumentHandler implements ArgumentHandler<OSCUnsigned>, Cloneable {

	public static final ArgumentHandler<OSCUnsigned> INSTANCE = new UnsignedIntegerArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected UnsignedIntegerArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'u';
	}

	@Override
	public Class<OSCUnsigned> getJavaClass() {
		return OSCUnsigned.class;
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
	public UnsignedIntegerArgumentHandler clone() throws CloneNotSupportedException {
		return (UnsignedIntegerArgumentHandler) super.clone();
	}

	/**
	 * Reads an unsigned integer (32 bit) from the byte stream.
	 * This code is copied from <a href="http://darksleep.com/player/JavaAndUnsignedTypes.html">
	 * here</a>, and it is licensed under the Public Domain.
	 * @return single precision, unsigned integer (32 bit) wrapped in a 64 bit integer (long)
	 */
	@Override
	public OSCUnsigned parse(final ByteBuffer input) {
		return OSCUnsigned.valueOf(new byte[] {
				input.get(),
				input.get(),
				input.get(),
				input.get()});
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCUnsigned value) {

		final long asLong = value.toLong();
		output.put((byte) (asLong >> 24 & 0xFFL));
		output.put((byte) (asLong >> 16 & 0xFFL));
		output.put((byte) (asLong >>  8 & 0xFFL));
		output.put((byte) (asLong       & 0xFFL));
	}
}
