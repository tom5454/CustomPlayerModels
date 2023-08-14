/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.nio.ByteBuffer;

public class OSCParseException extends Exception {
	private final ByteBuffer data;

	public ByteBuffer getData() {
		return data;
	}

	public OSCParseException(final String message, final ByteBuffer data) {
		super(message);
		this.data = data;
	}

	public OSCParseException(final Throwable cause, final ByteBuffer data) {
		super(cause);
		this.data = data;
	}

	public OSCParseException(
		final String message,
		final Throwable cause,
		final ByteBuffer data)
	{
		super(message, cause);
		this.data = data;
	}
}
