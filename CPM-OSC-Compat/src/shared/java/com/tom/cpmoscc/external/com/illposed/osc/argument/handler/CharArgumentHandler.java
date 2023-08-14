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
 * Parses and serializes an OSC character type.
 */
public class CharArgumentHandler implements ArgumentHandler<Character>, Cloneable {

	public static final ArgumentHandler<Character> INSTANCE = new CharArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected CharArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'c';
	}

	@Override
	public Class<Character> getJavaClass() {
		return Character.class;
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
	public CharArgumentHandler clone() throws CloneNotSupportedException {
		return (CharArgumentHandler) super.clone();
	}

	@Override
	public Character parse(final ByteBuffer input) {

		// Read the char as 1 byte from the last 8 of 32bits
		// to be compatible with liblo.
		// This might later be expanded to support multi-byte encoded chars.
		input.get();
		input.get();
		input.get();
		return (char) input.get();
	}

	@Override
	public void serialize(final BytesReceiver output, final Character value) {

		// Put the char as 1 byte in the last 8 of 32bits
		// to be compatible with liblo.
		// This might later be expanded to support multi-byte encoded chars.
		output.put((byte) 0);
		output.put((byte) 0);
		output.put((byte) 0);
		output.put((byte) (char) value);
	}
}
