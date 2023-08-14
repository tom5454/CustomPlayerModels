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
 * Checks whether a certain logical combination of two message selectors matches.
 *
 * An example:<br>
 * <blockquote><pre>{@code
 * // matches only if both the address and the argument tags match
 * MessageSelector matcher = new CombinedMessageSelector(
 *         new OSCPatternAddressMessageSelector("/hello/*"),
 *         new OSCPatternTypeTagsMessageSelector("tiff"),
 *         CombinedMessageSelector.LogicOperator.AND);
 * }</pre></blockquote>
 */
public class CombinedMessageSelector implements MessageSelector {

	public enum LogicOperator {
		AND {
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return (matches1 && matches2);
			}
		},
		// Public API
		@SuppressWarnings("unused")
		OR
		{
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return (matches1 || matches2);
			}
		},
		// Public API
		@SuppressWarnings("unused")
		XOR
		{
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return ((matches1 && !matches2) || (!matches1 && matches2));
			}
		};

		public abstract boolean matches(boolean matches1, boolean matches2);
	}

	private final MessageSelector selector1;
	private final MessageSelector selector2;
	private final LogicOperator logicOperator;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public CombinedMessageSelector(
			final MessageSelector selector1,
			final MessageSelector selector2,
			final LogicOperator logicOperator)
	{
		this.selector1 = selector1;
		this.selector2 = selector2;
		this.logicOperator = logicOperator;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public CombinedMessageSelector(
			final MessageSelector selector1,
			final MessageSelector selector2)
	{
		this(selector1, selector2, LogicOperator.AND);
	}

	@Override
	public boolean isInfoRequired() {
		return (selector1.isInfoRequired() || selector2.isInfoRequired());
	}

	@Override
	public boolean matches(final OSCMessageEvent messageEvent) {
		return logicOperator.matches(selector1.matches(messageEvent), selector2.matches(messageEvent));
	}
}
