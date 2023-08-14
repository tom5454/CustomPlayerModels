/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

public class OSCSerializeException extends Exception {

	public OSCSerializeException() {
		super();
	}

	public OSCSerializeException(final String message) {
		super(message);
	}

	public OSCSerializeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public OSCSerializeException(final Throwable cause) {
		super(cause);
	}
}
