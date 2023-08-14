/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

public final class LibraryInfo {
	/**
	 * Checks for StandardProtocolFamily Jdk8 compatibility of the runtime.
	 * E.g. Android API 23 and lower has only a
	 * java 8 subset without java.net.StandardProtocolFamily
	 * @return true when the runtime supports java.net.StandardProtocolFamily
	 * (e.g. Android API 23 and lower)
	 */
	public static boolean hasStandardProtocolFamily() {
		try {
			Class.forName("java.net.StandardProtocolFamily");
			return true;
		} catch (ClassNotFoundException ignore) {
			return false;
		}
	}
}
