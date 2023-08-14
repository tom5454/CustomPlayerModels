/*
 * Copyright (C) 2014-2017, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

/**
 * Checks whether an OSC <i>Message</i> fulfills certain criteria.
 */
public interface MessageSelector {

	/**
	 * Returns whether this selector requires meta-info to be present for messages
	 * that are checked for matching.
	 * @return {@code true} if this matcher requires meta-info
	 */
	boolean isInfoRequired();

	/**
	 * Checks whether the OSC <i>Message</i> in question matches this selector.
	 * @param messageEvent the message and meta-data to be checked if it matches
	 * @return {@code true} if this matcher selects the message in question
	 */
	boolean matches(OSCMessageEvent messageEvent);
}
