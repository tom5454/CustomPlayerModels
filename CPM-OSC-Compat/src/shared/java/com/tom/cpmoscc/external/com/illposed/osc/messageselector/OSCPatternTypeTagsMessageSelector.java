/*
 * Copyright (C) 2014-2017, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.messageselector;

import com.tom.cpmoscc.external.com.illposed.osc.MessageSelector;
import com.tom.cpmoscc.external.com.illposed.osc.OSCMessageEvent;

/**
 * Checks whether an OSC <i>Argument Type Tags</i> string matches a given wildcard expression,
 * as described in the OSC protocol specification for address matching.
 * See {@link OSCPatternAddressMessageSelector the corresponding address selector} for more details.
 */
public class OSCPatternTypeTagsMessageSelector implements MessageSelector {

	private final String selector;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCPatternTypeTagsMessageSelector(final String selector) {
		this.selector = selector;
	}

	// Public API
	@SuppressWarnings("unused")
	public static MessageSelector createAddressAndTypeTagsSelector(
			final String addressSelector,
			final String typeTagsSelector)
	{
		return new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector(addressSelector),
				new OSCPatternTypeTagsMessageSelector(typeTagsSelector));
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof OSCPatternTypeTagsMessageSelector) {
			final OSCPatternTypeTagsMessageSelector otherSelector
					= (OSCPatternTypeTagsMessageSelector) other;
			equal = this.selector.equals(otherSelector.selector);
		}

		return equal;
	}

	@Override
	public int hashCode() {
		return this.selector.hashCode();
	}

	@Override
	public boolean isInfoRequired() {
		return true;
	}

	@Override
	public boolean matches(final OSCMessageEvent messageEvent) {
		return OSCPatternAddressMessageSelector.matches(
				messageEvent.getMessage().getInfo().getArgumentTypeTags().toString(),
				selector);
	}
}
