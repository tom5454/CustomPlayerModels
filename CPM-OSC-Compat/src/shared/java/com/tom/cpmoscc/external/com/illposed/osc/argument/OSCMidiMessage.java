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
 * This represents an OSC compliant MIDI message.
 * From the 1.0 specification: Bytes from MSB to LSB are: port id, status byte, data1, data2
 */
public class OSCMidiMessage implements Cloneable, Serializable, Comparable<OSCMidiMessage> {

	public static final int NUM_CONTENT_BYTES = 4;
	private static final long serialVersionUID = 1L;
	private final byte portId;
	private final byte status;
	private final byte data1;
	private final byte data2;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCMidiMessage(final byte portId, final byte status, final byte data1, final byte data2) {

		this.portId = portId;
		this.status = status;
		this.data1 = data1;
		this.data2 = data2;
	}

	public byte[] toContentArray() {
		return new byte[] {getPortId(), getStatus(), getData1(), getData2()};
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getPortId() {
		return portId;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getStatus() {
		return status;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getData1() {
		return data1;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getData2() {
		return data2;
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof OSCMidiMessage) {
			final OSCMidiMessage otherMidiMsg = (OSCMidiMessage) other;
			if (
					(this.portId == otherMidiMsg.portId)
					&& (this.status == otherMidiMsg.status)
					&& (this.data1 == otherMidiMsg.data1)
					&& (this.data2 == otherMidiMsg.data2))
			{
				equal = true;
			}
		}

		return equal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = (97 * hash) + this.portId;
		hash = (97 * hash) + this.status;
		hash = (97 * hash) + this.data1;
		hash = (97 * hash) + this.data2;
		return hash;
	}

	@Override
	public int compareTo(final OSCMidiMessage other) {
		return Integer.compare(hashCode(), other.hashCode());
	}

	@Override
	public OSCMidiMessage clone() throws CloneNotSupportedException {
		return (OSCMidiMessage) super.clone();
	}

	public static OSCMidiMessage valueOf(final byte[] content) {

		if (content.length != NUM_CONTENT_BYTES) {
			throw new IllegalArgumentException("The content has to be exactly " + NUM_CONTENT_BYTES
					+ " bytes");
		}
		int contentByteIndex = 0;
		return new OSCMidiMessage(
				content[contentByteIndex++],
				content[contentByteIndex++],
				content[contentByteIndex++],
				content[contentByteIndex]);
	}
}
