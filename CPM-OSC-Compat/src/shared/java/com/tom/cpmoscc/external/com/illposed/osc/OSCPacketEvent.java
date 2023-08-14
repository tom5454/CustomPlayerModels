/*
 * Copyright (C) 2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.util.EventObject;

/**
 * Event state of reception of an OSC packet.
 */
public class OSCPacketEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final OSCPacket packet;

	/**
	 * Creates a new packet received event.
	 * @param source the source of the packet
	 * @param packet the packet to process
	 */
	public OSCPacketEvent(final Object source, final OSCPacket packet) {
		super(source);
		this.packet = packet;
	}

	/**
	 * Returns the main content of this event.
	 * @return the received packet
	 */
	public OSCPacket getPacket() {
		return packet;
	}
}
