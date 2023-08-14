/*
 * Copyright (C) 2004-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpmoscc.OSCLog;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;
import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCTimeTag64;
import com.tom.cpmoscc.external.com.illposed.osc.argument.handler.IntegerArgumentHandler;
import com.tom.cpmoscc.external.com.illposed.osc.argument.handler.TimeTag64ArgumentHandler;

/**
 * Converts a byte array conforming to the OSC byte stream format into Java objects.
 * This class is NOT thread-save, and will produce invalid results and errors
 * if used by multiple threads simultaneously.
 * Please use a separate instance per thread.
 */
public class OSCParser {

	// Public API
	/**
	 * Number of bytes the raw OSC data stream is aligned to.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int ALIGNMENT_BYTES = 4;
	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final String BUNDLE_START = "#bundle";
	private static final byte[] BUNDLE_START_BYTES
	= BUNDLE_START.getBytes(StandardCharsets.UTF_8);
	private static final String NO_ARGUMENT_TYPES = "";
	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final byte TYPES_VALUES_SEPARATOR = (byte) ',';
	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final char TYPE_ARRAY_BEGIN = (byte) '[';
	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final char TYPE_ARRAY_END = (byte) ']';

	private final OSCLog log = OSCLog.getLogger(OSCParser.class);

	private final Map<Character, ArgumentHandler> identifierToType;
	private final Map<String, Object> properties;
	private final byte[] bundleStartChecker;

	private static class UnknownArgumentTypeParseException extends OSCParseException {
		UnknownArgumentTypeParseException(
				final char argumentType,
				final ByteBuffer data)
		{
			super("No " + ArgumentHandler.class.getSimpleName() + " registered for type '"
					+ argumentType + '\'', data);
		}
	}

	// Public API
	/**
	 * Creates a new parser with all the required ingredients.
	 * @param identifierToType all of these, and only these arguments will be
	 *   parsable by this object, that are supported by these handlers
	 * @param properties see {@link ArgumentHandler#setProperties(Map)}
	 */
	@SuppressWarnings("WeakerAccess")
	public OSCParser(
			final Map<Character, ArgumentHandler> identifierToType,
			final Map<String, Object> properties)
	{
		// We create (shallow) copies of these collections,
		// so if "the creator" modifies them after creating us,
		// we do not get different behaviour during our lifetime,
		// which might be very confusing to users of this class.
		// As the copies are not deep though,
		// It does not protect us from change in behaviour
		// do to change of the objects themselves,
		// which are contained in these collections.
		// TODO instead of these shallow copies, maybe create deep ones?
		this.identifierToType = Collections.unmodifiableMap(
				new HashMap<>(identifierToType));
		this.properties = Collections.unmodifiableMap(
				new HashMap<>(properties));
		this.bundleStartChecker = new byte[BUNDLE_START.length()];
	}

	/**
	 * If not yet aligned, move the position to the next index dividable by
	 * {@link #ALIGNMENT_BYTES}.
	 * @param input to be aligned
	 * @see OSCSerializer#align
	 */
	public static void align(final ByteBuffer input) {
		final int mod = input.position() % ALIGNMENT_BYTES;
		final int padding = (ALIGNMENT_BYTES - mod) % ALIGNMENT_BYTES;
		((Buffer)input).position(input.position() + padding);
	}

	// Public API
	@SuppressWarnings("unused")
	public Map<Character, ArgumentHandler> getIdentifierToTypeMapping() {
		return identifierToType;
	}

	/**
	 * Returns the set of properties this parser was created with.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(Map)
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Converts a byte-buffer into an {@link OSCPacket}
	 * (either an {@link OSCMessage} or {@link OSCBundle}).
	 * @param rawInput the storage containing the raw OSC packet
	 * @return the successfully parsed OSC packet
	 * @throws OSCParseException if the input has an invalid format
	 */
	public OSCPacket convert(final ByteBuffer rawInput) throws OSCParseException {

		final ByteBuffer readOnlyInput = rawInput.asReadOnlyBuffer();
		final OSCPacket packet;
		if (isBundle(readOnlyInput)) {
			packet = convertBundle(readOnlyInput);
		} else {
			OSCPacket tmpPacket = null;
			try {
				tmpPacket = convertMessage(readOnlyInput);
			} catch (final UnknownArgumentTypeParseException ex) {
				// NOTE Some OSC applications communicate among instances of themselves
				//   with additional, nonstandard argument types beyond those specified
				//   in the OSC specification. OSC applications are not required to recognize
				//   these types; an OSC application should discard any message whose
				//   OSC Type Tag string contains any unrecognized OSC Type Tags.
				log.warn("Package ignored because: {}", ex.getMessage());
			}
			packet = tmpPacket;
		}

		return packet;
	}

	/**
	 * Checks whether my byte array is a bundle.
	 * From the OSC 1.0 specifications:
	 * <quote>
	 * The contents of an OSC packet must be either an OSC Message
	 * or an OSC Bundle. The first byte of the packet's contents unambiguously
	 * distinguishes between these two alternatives.
	 * </quote>
	 * Meaning, if the first byte is '#', the packet is a bundle,
	 * otherwise it is a message.
	 * Yet, the OSC standard body later itself broke with this standard/concept,
	 * when they released a document suggesting to use messages
	 * with an address of "#reply" for a stateful meta protocol
	 * in the following document:
	 *
	 * @see <a href="http://opensoundcontrol.org/files/osc-query-system.pdf">
	 *   A Query System for Open Sound Control (Draft Proposal)</a>
	 *
	 * @return true if it the byte array is a bundle, false o.w.
	 */
	private boolean isBundle(final ByteBuffer rawInput) {

		boolean bundle;
		final int positionStart = rawInput.position();
		try {
			rawInput.get(bundleStartChecker);
			bundle = Arrays.equals(bundleStartChecker, BUNDLE_START_BYTES);
		} catch (final BufferUnderflowException bue) {
			// the package is too short to even contain the bundle start indicator
			bundle = false;
		}
		((Buffer)rawInput).position(positionStart);
		return bundle;
	}

	/**
	 * Converts the byte array to a bundle.
	 * Assumes that the byte array is a bundle.
	 * @return a bundle containing the data specified in the byte stream
	 */
	private OSCBundle convertBundle(final ByteBuffer rawInput) throws OSCParseException {
		// skip the "#bundle " stuff
		((Buffer)rawInput).position(BUNDLE_START.length() + 1);
		final OSCTimeTag64 timestamp = TimeTag64ArgumentHandler.INSTANCE.parse(rawInput);
		final OSCBundle bundle = new OSCBundle(timestamp);
		while (rawInput.hasRemaining()) {
			// recursively read through the stream and convert packets you find
			final int packetLength = IntegerArgumentHandler.INSTANCE.parse(rawInput);
			if (packetLength == 0) {
				throw new IllegalArgumentException("Packet length may not be 0");
			} else if ((packetLength % ALIGNMENT_BYTES) != 0) {
				throw new IllegalArgumentException(
						"Packet length has to be a multiple of " + ALIGNMENT_BYTES
						+ ", is:" + packetLength);
			}
			final ByteBuffer packetBytes = rawInput.slice();
			((Buffer)packetBytes).limit(packetLength);
			((Buffer)rawInput).position(rawInput.position() + packetLength);
			final OSCPacket packet = convert(packetBytes);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	/**
	 * Converts the byte array to a simple message.
	 * Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage(final ByteBuffer rawInput) throws OSCParseException {

		final String address = readString(rawInput);
		final CharSequence typeIdentifiers = readTypes(rawInput);
		// typeIdentifiers.length() gives us an upper bound for the number of arguments
		// and a good approximation in general.
		// It is equal to the number of arguments if there are no arrays.
		final List<Object> arguments = new ArrayList<>(typeIdentifiers.length());
		for (int ti = 0; ti < typeIdentifiers.length(); ++ti) {
			if (TYPE_ARRAY_BEGIN == typeIdentifiers.charAt(ti)) {
				// we're looking at an array -- read it in
				arguments.add(readArray(rawInput, typeIdentifiers, ++ti));
				// then increment i to the end of the array
				while (typeIdentifiers.charAt(ti) != TYPE_ARRAY_END) {
					ti++;
				}
			} else {
				arguments.add(readArgument(rawInput, typeIdentifiers.charAt(ti)));
			}
		}

		try {
			return new OSCMessage(address, arguments, new OSCMessageInfo(typeIdentifiers));
		} catch (final IllegalArgumentException ex) {
			throw new OSCParseException(ex, rawInput);
		}
	}

	/**
	 * Reads a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	private String readString(final ByteBuffer rawInput) throws OSCParseException {
		return (String) identifierToType.get('s').parse(rawInput);
	}

	/**
	 * Reads the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments,
	 *   or <code>null</code>, in case of no arguments
	 */
	private CharSequence readTypes(final ByteBuffer rawInput) throws OSCParseException {

		final String typeTags;
		// The next byte should be a TYPES_VALUES_SEPARATOR, but some legacy code may omit it
		// in case of no arguments, according to "OSC Messages" in:
		// http://opensoundcontrol.org/spec-1_0
		if (rawInput.hasRemaining()) {
			if (rawInput.get(rawInput.position()) == TYPES_VALUES_SEPARATOR) {
				// position++ to skip the TYPES_VALUES_SEPARATOR
				rawInput.get();
				typeTags = readString(rawInput);
			} else {
				// data format is invalid
				throw new OSCParseException(
						"No '" + TYPES_VALUES_SEPARATOR + "' present after the address, "
								+ "but there is still more data left in the message",
								rawInput);
			}
		} else {
			// NOTE Strictly speaking, it is invalid for a message to omit the "OSC Type Tag String",
			//   even if that message has no arguments.
			//   See these two excerpts from the OSC 1.0 specification:
			//   1. "An OSC message consists of an OSC Address Pattern followed by
			//       an OSC Type Tag String followed by zero or more OSC Arguments."
			//   2. "An OSC Type Tag String is an OSC-string beginning with the character ','"
			//   But to be compatible with older OSC implementations,
			//   and because it should cause no troubles,
			//   we accept this as a valid message anyway.
			typeTags = NO_ARGUMENT_TYPES;
		}

		return typeTags;
	}

	/**
	 * Reads an object of the type specified by the type char.
	 * @param typeIdentifier type of the argument to read
	 * @return a Java representation of the argument
	 */
	private Object readArgument(final ByteBuffer rawInput, final char typeIdentifier)
			throws OSCParseException
	{
		final Object argumentValue;

		final ArgumentHandler type = identifierToType.get(typeIdentifier);
		if (type == null) {
			throw new UnknownArgumentTypeParseException(typeIdentifier, rawInput);
		} else {
			argumentValue = type.parse(rawInput);
		}

		return argumentValue;
	}

	/**
	 * Reads an array of arguments from the byte stream.
	 * @param typeIdentifiers type identifiers of the whole message
	 * @param pos at which position to start reading
	 * @return the array that was read
	 */
	private List<Object> readArray(
			final ByteBuffer rawInput, final CharSequence typeIdentifiers, final int pos)
					throws OSCParseException
	{
		int arrayLen = 0;
		while (typeIdentifiers.charAt(pos + arrayLen) != TYPE_ARRAY_END) {
			arrayLen++;
		}
		final List<Object> array = new ArrayList<>(arrayLen);
		for (int ai = 0; ai < arrayLen; ai++) {
			array.add(readArgument(rawInput, typeIdentifiers.charAt(pos + ai)));
		}
		return array;
	}
}
