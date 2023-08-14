/*
 * Copyright (C) 2003-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCTimeTag64;

/**
 * A bundle represents a collection of OSC packets
 * (either messages or other bundles)
 * and has a time-tag which can be used by a scheduler to execute
 * a bundle in the future, instead of immediately.
 * {@link OSCMessage}s are executed immediately.
 *
 * Bundles should be used if you want to send multiple messages to be executed
 * as soon as possible and in immediate succession to each other,
 * or you want to schedule one or more messages to be executed in the future.
 */
public class OSCBundle implements OSCPacket {

	private static final long serialVersionUID = 1L;

	private OSCTimeTag64 timestamp;
	private List<OSCPacket> packets;

	/**
	 * Create a new empty OSCBundle with a timestamp of immediately.
	 * You can add packets to the bundle with addPacket()
	 */
	public OSCBundle() {
		this(OSCTimeTag64.IMMEDIATE);
	}

	/**
	 * Create an OSCBundle with the specified timestamp.
	 * @param timestamp the time to execute the bundle
	 */
	public OSCBundle(final OSCTimeTag64 timestamp) {
		this(null, timestamp);
	}

	/**
	 * Creates an OSCBundle made up of the given packets
	 * with a timestamp of now.
	 * @param packets array of OSCPackets to initialize this object with
	 */
	public OSCBundle(final List<OSCPacket> packets) {
		this(packets, OSCTimeTag64.IMMEDIATE);
	}

	/**
	 * Create an OSCBundle, specifying the packets and timestamp.
	 * @param packets the packets that make up the bundle
	 * @param timestamp the time to execute the bundle
	 */
	public OSCBundle(final List<OSCPacket> packets, final OSCTimeTag64 timestamp) {

		if (null == packets) {
			this.packets = new LinkedList<>();
		} else {
			this.packets = new ArrayList<>(packets);
		}
		checkNonNullTimestamp(timestamp);
		this.timestamp = timestamp;
	}

	private static void checkNonNullTimestamp(final OSCTimeTag64 timestamp) {

		if (timestamp == null) {
			throw new IllegalArgumentException("Bundle time-stamp may not be null; you may want to "
					+ "use OSCTimeStamp.IMMEDIATE.");
		}
	}

	/**
	 * Returns the time the bundle will execute.
	 * @return will never be {@code null}
	 */
	public OSCTimeTag64 getTimestamp() {
		return timestamp;
	}

	// Public API
	/**
	 * Sets the time the bundle will execute.
	 * @param timestamp when the bundle should execute, can not be {@code null},
	 *   but {@code OSCTimeTag64.IMMEDIATE}
	 */
	@SuppressWarnings("WeakerAccess")
	public void setTimestamp(final OSCTimeTag64 timestamp) {

		checkNonNullTimestamp(timestamp);
		this.timestamp = timestamp;
	}

	/**
	 * Add a packet to the list of packets in this bundle.
	 * @param packet OSCMessage or OSCBundle
	 */
	public void addPacket(final OSCPacket packet) {
		packets.add(packet);
	}

	/**
	 * Get the packets contained in this bundle.
	 * @return the packets contained in this bundle.
	 */
	public List<OSCPacket> getPackets() {
		return Collections.unmodifiableList(packets);
	}
}
