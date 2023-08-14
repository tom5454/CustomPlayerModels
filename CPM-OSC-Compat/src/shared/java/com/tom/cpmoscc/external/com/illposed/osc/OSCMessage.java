/*
 * Copyright (C) 2003-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An simple (non-bundle) OSC message.
 *
 * An OSC <i>Message</i> is made up of
 * an <i>Address Pattern</i> (the receiver of the message)
 * and <i>Arguments</i> (the content of the message).
 */
public class OSCMessage implements OSCPacket {

	private static final long serialVersionUID = 1L;

	/**
	 * Java regular expression pattern matching a single invalid character.
	 * The invalid characters are:
	 * ' ', '#', '*', ',', '?', '[', ']', '{', '}'
	 */
	private static final Pattern ILLEGAL_ADDRESS_CHAR
			= Pattern.compile("[ #*,?\\[\\]{}]");

	private final String address;
	private final List<Object> arguments;
	private OSCMessageInfo info;

	/**
	 * Creates an OSCMessage with an address already initialized.
	 * @param address  the recipient of this OSC message
	 */
	public OSCMessage(final String address) {
		this(address, Collections.emptyList());
	}

	/**
	 * Creates an OSCMessage with an address
	 * and arguments already initialized.
	 * @param address  the recipient of this OSC message
	 * @param arguments  the data sent to the receiver
	 */
	public OSCMessage(final String address, final List<?> arguments) {
		this(address, arguments, null);
	}

	/**
	 * Creates an OSCMessage with an address
	 * and arguments already initialized.
	 * @param address  the recipient of this OSC message
	 * @param arguments  the data sent to the receiver
	 * @param info  meta-info about the message, or {@code null}, if not yet available
	 */
	public OSCMessage(final String address, final List<?> arguments, final OSCMessageInfo info) {
		this(address, arguments, info, true);
	}

	/**
	 * Creates an OSCMessage with an address
	 * and arguments already initialized,
	 * optionally skipping address verification.
	 * NOTE This only makes sense for testing purposes!
	 * Under normal circumstances one should never disable address checking.
	 * @param address  the recipient of this OSC message
	 * @param arguments  the data sent to the receiver
	 * @param info  meta-info about the message, or {@code null}, if not yet available
	 * @param checkAddress  whether to check the address for validity; CAUTION!
	 */
	OSCMessage(final String address, final List<?> arguments, final OSCMessageInfo info, final boolean checkAddress) {

		if (checkAddress) {
			checkAddress(address);
		}
		this.address = address;
		this.arguments = Collections.unmodifiableList(arguments);
		this.info = info;
	}

	/**
	 * The receiver of this message.
	 * @return the receiver of this OSC Message
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * The arguments of this message.
	 * @return the arguments to this message
	 */
	public List<Object> getArguments() {
		return arguments;
	}

	/**
	 * Returns meta-info about this message.
	 * @return the meta-info, or {@code null}, if none are set yet.
	 */
	public OSCMessageInfo getInfo() {
		return info;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public boolean isInfoSet() {
		return (info != null);
	}

	// Public API
	/**
	 * Sets meta-info about this message.
	 * This method may only be called if the meta-info has not yet been set.
	 * @param info the meta-info for this message, may not be {@code null}
	 */
	@SuppressWarnings("WeakerAccess")
	public void setInfo(final OSCMessageInfo info) {

		if (this.info != null) {
			throw new IllegalStateException("The meta-info of a message may only be set once");
		}
		this.info = info;
	}

	/**
	 * Throws an exception if the given address is invalid.
	 * @param address to be checked for validity
	 */
	private static void checkAddress(final String address) {
		if (!isValidAddress(address)) {
			throw new IllegalArgumentException("Not a valid OSC address: " + address);
		}
	}

	// Public API
	/**
	 * Checks whether a given string is a valid OSC <i>Address Pattern</i>.
	 * @param address to be checked for validity
	 * @return true if the supplied string constitutes a valid OSC address
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean isValidAddress(final String address) {
		return (address != null)
				&& !address.isEmpty()
				&& (
					address.equals("#reply")
					|| (
						(address.charAt(0) == '/')
						&& !address.contains("//")
						&& !ILLEGAL_ADDRESS_CHAR.matcher(address).find()
					));
	}
}
