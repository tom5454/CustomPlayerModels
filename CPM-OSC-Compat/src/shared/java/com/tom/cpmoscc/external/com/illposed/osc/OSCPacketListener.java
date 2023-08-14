/*
 * Copyright (C) 2018-2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

/**
 * A listener that handles <b>packets</b>. A packet can be either a bundle or a
 * message.
 *
 * This is useful if you need to handle the messages in bundles differently than
 * individual messages.
 *
 * If you don't have this constraint, you might prefer to use OSCMessageListener
 * instead.
 */
public interface OSCPacketListener {

	/**
	 * Process an incoming packet.
	 * @param event contains the packet content and meta-data
	 */
	void handlePacket(final OSCPacketEvent event);

	/**
	 * Process a bad data event.
	 * @param event the bad data event to handle
	 */
	void handleBadData(final OSCBadDataEvent event);
}
