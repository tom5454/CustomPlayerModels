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
 * Parses and serializes an OSC binary-blob type.
 */
public class ByteArrayBlobArgumentHandler implements ArgumentHandler<byte[]>, Cloneable {

	public static final ArgumentHandler<byte[]> INSTANCE = new ByteArrayBlobArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected ByteArrayBlobArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'b';
	}

	@Override
	public Class<byte[]> getJavaClass() {
		return byte[].class;
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
	public ByteArrayBlobArgumentHandler clone() throws CloneNotSupportedException {
		return (ByteArrayBlobArgumentHandler) super.clone();
	}

	@Override
	public byte[] parse(final ByteBuffer input) throws OSCParseException {

		final ByteBuffer bufferValue = BlobArgumentHandler.INSTANCE.parse(input);
		final byte[] value = new byte[bufferValue.remaining()];
		bufferValue.get(value);
		return value;
	}

	@Override
	public void serialize(final BytesReceiver output, final byte[] value) throws OSCSerializeException {

		final ByteBuffer bufferValue = ByteBuffer.wrap(value).asReadOnlyBuffer();
		BlobArgumentHandler.INSTANCE.serialize(output, bufferValue);
	}
}
