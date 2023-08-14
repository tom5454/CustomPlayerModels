/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument.handler;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.BytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParser;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializer;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Parses and serializes an OSC string type.
 */
public class StringArgumentHandler implements ArgumentHandler<String>, Cloneable {

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final char DEFAULT_IDENTIFIER = 's';
	public static final String PROP_NAME_CHARSET = "charset";

	private Charset charset;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public StringArgumentHandler(final Charset charset) {
		this.charset = charset;
	}
	// Public API
	@SuppressWarnings("WeakerAccess")
	public StringArgumentHandler() {
		this(Charset.defaultCharset());
	}

	// Public API
	/**
	 * Returns the character-set used to encode and decode string arguments.
	 * @return the currently used character-encoding-set
	 */
	@SuppressWarnings("unused")
	public Charset getCharset() {
		return charset;
	}

	// Public API
	/**
	 * Sets the character-set used to encode and decode string arguments.
	 * @param charset the new character-encoding-set
	 */
	@SuppressWarnings("WeakerAccess")
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	@Override
	public char getDefaultIdentifier() {
		return DEFAULT_IDENTIFIER;
	}

	@Override
	public Class<String> getJavaClass() {
		return String.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {

		final Charset newCharset = (Charset) properties.get(PROP_NAME_CHARSET);
		if (newCharset != null) {
			setCharset(newCharset);
		}
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringArgumentHandler clone() throws CloneNotSupportedException {
		return (StringArgumentHandler) super.clone();
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 */
	private int lengthOfCurrentString(final ByteBuffer rawInput) {
		int len = 0;
		while (rawInput.get(rawInput.position() + len) != 0) {
			len++;
		}
		return len;
	}

	@Override
	public String parse(final ByteBuffer input) throws OSCParseException {

		final int strLen = lengthOfCurrentString(input);
		final ByteBuffer strBuffer = input.slice();
		((Buffer)strBuffer).limit(strLen);
		final String res;
		try {
			res = charset.newDecoder().decode(strBuffer).toString();
		} catch (final CharacterCodingException ex) {
			throw new OSCParseException(
				"Failed decoding a string argument", ex, input
			);
		}
		((Buffer)input).position(input.position() + strLen);
		// because strings are always padded with at least one zero,
		// as their length is not given in advance, as is the case with blobs,
		// we skip over the terminating zero byte (position++)
		input.get();
		OSCParser.align(input);
		return res;
	}

	@Override
	public void serialize(final BytesReceiver output, final String value) {

		final byte[] stringBytes = value.getBytes(charset);
		output.put(stringBytes);
		OSCSerializer.terminateAndAlign(output);
	}
}
