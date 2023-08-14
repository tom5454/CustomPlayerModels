/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument;

import java.io.Serializable;

/**
 * Represents an OSC compliant symbol.
 * Technically, this is not different from a {@code String}, but some systems may differentiate
 * between "strings" and "symbols".
 */
public class OSCSymbol implements Cloneable, Serializable, Comparable<OSCSymbol> {

	private static final long serialVersionUID = 1L;

	private final String value;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCSymbol(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object other) {

		return (other instanceof OSCSymbol)
				&& toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = (37 * hash) + ((this.value == null) ? 0 : this.value.hashCode());
		return hash;
	}

	@Override
	public int compareTo(final OSCSymbol other) {
		return toString().compareTo(other.toString());
	}

	@Override
	public OSCSymbol clone() throws CloneNotSupportedException {
		return (OSCSymbol) super.clone();
	}

	@Override
	public String toString() {
		return value;
	}

	public static OSCSymbol valueOf(final String str) {
		return new OSCSymbol(str);
	}
}
