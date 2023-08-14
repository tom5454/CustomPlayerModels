/*
 * Copyright (C) 2003-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tom.cpmoscc.OSCLog;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Converts OSC packet Java objects to their byte stream representations,
 * conforming to the OSC specification.
 * This class is NOT thread-save, and will produce invalid results and errors
 * if used by multiple threads simultaneously.
 * Please use a separate instance per thread.
 *
 * The implementation was originally based on
 * <a href="http://www.emergent.de">Markus Gaelli</a> and
 * Iannis Zannos's OSC implementation in Squeak (a Smalltalk dialect).
 */
public class OSCSerializer {

	/**
	 * If a supplied arguments class implements more then one supported argument types,
	 * there is no solid way to figure out as which one we should interpret and serialize it,
	 * so we will fail fast.
	 */
	private static final int MAX_IMPLEMENTED_ARGUMENT_TYPES = 1;

	/**
	 * Intermediate/Mock/Placeholder value indicating the size of a packet.
	 * It will be used internally, as long as we do not yet know
	 * the size of the packet in bytes.
	 * The actual value is arbitrary.
	 */
	private static final Integer PACKET_SIZE_PLACEHOLDER = -1;

	private final OSCLog log = OSCLog.getLogger(OSCSerializer.class);

	private final BytesReceiver output;
	/**
	 * Cache for Java classes of which we know, that we have no argument handler
	 * that supports serializing them.
	 */
	private final Set<Class> unsupportedTypes;
	/**
	 * Cache for Java classes that are sub-classes of a supported argument handlers Java class,
	 * mapped to that base class.
	 * This does not include base classes
	 * (Java classes directly supported by one of our argument handlers).
	 */
	private final Map<Class, Class> subToSuperTypes;
	/**
	 * For each base-class, indicates whether it is a marker-only type.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Class, Boolean> classToMarker;
	/**
	 * Maps supported Java class to argument-handlers for all our non-marker-only base-classes.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Class, ArgumentHandler> classToType;
	/**
	 * Maps values to argument-handlers for all our marker-only base-classes.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Object, ArgumentHandler> markerValueToType;
	private final Map<String, Object> properties;

	/**
	 * Creates a new serializer with all the required ingredients.
	 * @param types all of these, and only these arguments will be serializable
	 *   by this object, that are supported by these handlers
	 * @param properties see {@link ArgumentHandler#setProperties(Map)}
	 * @param output the output buffer, where raw OSC data is written to
	 */
	public OSCSerializer(
			final List<ArgumentHandler> types,
			final Map<String, Object> properties,
			final BytesReceiver output)
	{
		final Map<Class, Boolean> classToMarkerTmp = new HashMap<>(types.size());
		final Map<Class, ArgumentHandler> classToTypeTmp = new HashMap<>();
		final Map<Object, ArgumentHandler> markerValueToTypeTmp = new HashMap<>();
		for (final ArgumentHandler type : types) {
			final Class typeJava = type.getJavaClass();
			final Boolean registeredIsMarker = classToMarkerTmp.get(typeJava);
			if ((registeredIsMarker != null) && (registeredIsMarker != type.isMarkerOnly())) {
				throw new IllegalStateException(ArgumentHandler.class.getSimpleName()
						+ " implementations disagree on the marker nature of their class: "
						+ typeJava);
			}
			classToMarkerTmp.put(typeJava, type.isMarkerOnly());

			if (type.isMarkerOnly()) {
				try {
					final Object markerValue = type.parse(null);
					final ArgumentHandler previousType = markerValueToTypeTmp.get(markerValue);
					if (previousType != null) {
						throw new IllegalStateException("Marker value \"" + markerValue
								+ "\" is already used for type "
								+ previousType.getClass().getCanonicalName());
					}
					markerValueToTypeTmp.put(markerValue, type);
				} catch (final OSCParseException ex) {
					throw new IllegalStateException("Developer error; this should never happen",
							ex);
				}
			} else {
				final ArgumentHandler previousType = classToTypeTmp.get(typeJava);
				if (previousType != null) {
					throw new IllegalStateException("Java argument type "
							+ typeJava.getCanonicalName() + " is already used for type "
							+ previousType.getClass().getCanonicalName());
				}
				classToTypeTmp.put(typeJava, type);
			}
		}

		this.output = output;
		// usually, we should not need a stack size of 16, which is the default initial size
		this.unsupportedTypes = new HashSet<>(4);
		this.subToSuperTypes = new HashMap<>(4);
		// We create (shallow) copies of these collections,
		// so if "the creator" modifies them after creating us,
		// we do not get different behaviour during our lifetime,
		// which might be very confusing to users of this class.
		// As the copies are not deep though,
		// It does not protect us from change in behaviour
		// do to change of the objects themselves,
		// which are contained in these collections.
		// TODO instead of these shallow copies, maybe create deep ones?
		this.classToMarker = Collections.unmodifiableMap(
				new HashMap<>(classToMarkerTmp));
		this.classToType = Collections.unmodifiableMap(
				new HashMap<>(classToTypeTmp));
		this.markerValueToType = Collections.unmodifiableMap(
				new HashMap<>(markerValueToTypeTmp));
		this.properties = Collections.unmodifiableMap(
				new HashMap<>(properties));
	}

	// Public API
	@SuppressWarnings({"WeakerAccess", "unused"})
	public Map<Class, ArgumentHandler> getClassToTypeMapping() {
		return classToType;
	}

	/**
	 * Returns the set of properties this parser was created with.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(Map)
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static byte[] toByteArray(final ByteBuffer buffer) {

		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	// Public API
	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'}.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param output to receive the data-piece termination
	 */
	@SuppressWarnings("WeakerAccess")
	public static void terminate(final BytesReceiver output) {
		output.put((byte) 0);
	}

	/**
	 * Align a buffer by padding it with {@code (byte) '0'}s so it has a size
	 * divisible by {@link OSCParser#ALIGNMENT_BYTES}.
	 * @param output to be aligned
	 * @see OSCParser#align
	 */
	public static void align(final BytesReceiver output) {
		final int alignmentOverlap = output.position() % OSCParser.ALIGNMENT_BYTES;
		final int padLen = (OSCParser.ALIGNMENT_BYTES - alignmentOverlap) % OSCParser.ALIGNMENT_BYTES;
		for (int pci = 0; pci < padLen; pci++) {
			output.put((byte) 0);
		}
	}

	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'},
	 * and then aligns the stream by padding it with {@code (byte) '0'}s so it has a size
	 * divisible by {@link OSCParser#ALIGNMENT_BYTES}.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param output to receive the data-piece termination and alignment
	 */
	public static void terminateAndAlign(final BytesReceiver output) {
		terminate(output);
		align(output);
	}

	private void write(final OSCBundle bundle) throws OSCSerializeException {
		write(OSCParser.BUNDLE_START);
		write(bundle.getTimestamp());
		for (final OSCPacket pkg : bundle.getPackets()) {
			writeSizeAndData(pkg);
		}
	}

	/**
	 * Serializes a messages address.
	 * @param message the address of this message will be serialized
	 * @throws OSCSerializeException if the message failed to serialize
	 */
	private void writeAddress(final OSCMessage message) throws OSCSerializeException {
		write(message.getAddress());
	}

	/**
	 * Serializes the arguments of a message.
	 * @param message the arguments of this message will be serialized
	 * @throws OSCSerializeException if the message arguments failed to serialize
	 */
	private void writeArguments(final OSCMessage message) throws OSCSerializeException {

		output.put(OSCParser.TYPES_VALUES_SEPARATOR);
		writeTypeTags(message.getArguments());
		for (final Object argument : message.getArguments()) {
			write(argument);
		}
	}

	private void write(final OSCMessage message) throws OSCSerializeException {
		writeAddress(message);
		writeArguments(message);
	}

	/**
	 * Converts the packet into its OSC compliant byte array representation,
	 * then writes the number of bytes to the stream, followed by the actual data bytes.
	 * @param packet to be converted and written to the stream
	 * @throws OSCSerializeException if the packet failed to serialize
	 */
	private void writeSizeAndData(final OSCPacket packet) throws OSCSerializeException {

		final ByteBuffer serializedPacketBuffer = ByteBuffer.allocate(4);
		final BufferBytesReceiver serializedPacketSize = new BufferBytesReceiver(serializedPacketBuffer);
		final ArgumentHandler<Integer> type = findType(PACKET_SIZE_PLACEHOLDER);

		final int sizePosition = output.position();
		// write place-holder size (will be overwritten later)
		type.serialize(serializedPacketSize, PACKET_SIZE_PLACEHOLDER);
		final BytesReceiver.PlaceHolder packetSizePlaceholder = output.putPlaceHolder(serializedPacketBuffer.array());
		writePacket(packet);
		final int afterPacketPosition = output.position();
		final int packetSize = afterPacketPosition - sizePosition - OSCParser.ALIGNMENT_BYTES;

		serializedPacketSize.clear();
		type.serialize(serializedPacketSize, packetSize);
		packetSizePlaceholder.replace(serializedPacketBuffer.array());
	}

	private void writePacket(final OSCPacket packet) throws OSCSerializeException {

		if (packet instanceof OSCBundle) {
			write((OSCBundle) packet);
		} else if (packet instanceof OSCMessage) {
			write((OSCMessage) packet);
		} else {
			throw new UnsupportedOperationException("We do not support writing packets of type: "
					+ packet.getClass());
		}
	}

	public void write(final OSCPacket packet) throws OSCSerializeException {

		// reset position, limit and mark
		output.clear();
		try {
			writePacket(packet);
		} catch (final BufferOverflowException ex) {
			throw new OSCSerializeException("Packet is too large for the buffer in use", ex);
		}
	}

	/**
	 * Write only the type tags for a given list of arguments.
	 * This is primarily used by the packet dispatcher.
	 * @param arguments to write the type tags from
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	public void writeOnlyTypeTags(final List<?> arguments) throws OSCSerializeException {

		// reset position, limit and mark
		output.clear();
		try {
			writeTypeTagsRaw(arguments);
		} catch (final BufferOverflowException ex) {
			throw new OSCSerializeException("Type tags are too large for the buffer in use", ex);
		}
	}

	private Set<ArgumentHandler> findSuperTypes(final Class argumentClass) {

		final Set<ArgumentHandler> matchingSuperTypes = new HashSet<>();

		// check all base-classes, for whether our argument-class is a sub-class
		// of any of them
		for (final Map.Entry<Class, ArgumentHandler> baseClassAndType
				: classToType.entrySet())
		{
			final Class<?> baseClass = baseClassAndType.getKey();
			if ((baseClass != Object.class)
					&& baseClass.isAssignableFrom(argumentClass))
			{
				matchingSuperTypes.add(baseClassAndType.getValue());
			}
		}

		return matchingSuperTypes;
	}

	private Class findSuperType(final Class argumentClass) throws OSCSerializeException {

		// check if we already found the base-class for this argument-class before
		Class superType = subToSuperTypes.get(argumentClass);
		// ... if we did not, ...
		if ((superType == null)
				// check if we already know this argument-class to not be supported
				&& !unsupportedTypes.contains(argumentClass))
		{
			final Set<ArgumentHandler> matchingSuperTypes = findSuperTypes(argumentClass);
			if (matchingSuperTypes.isEmpty()) {
				unsupportedTypes.add(argumentClass);
			} else {
				if (matchingSuperTypes.size() > MAX_IMPLEMENTED_ARGUMENT_TYPES) {
					log.warn("Java class {} is a sub-class of multiple supported argument types:",
							argumentClass.getCanonicalName());
					for (final ArgumentHandler matchingSuperType : matchingSuperTypes) {
						log.warn("\t{} (supported by {})",
								matchingSuperType.getJavaClass().getCanonicalName(),
								matchingSuperType.getClass().getCanonicalName());
					}
				}
				final ArgumentHandler matchingSuperType = matchingSuperTypes.iterator().next();
				log.info("Java class {} will be mapped to {} (supported by {})",
						argumentClass.getCanonicalName(),
						matchingSuperType.getJavaClass().getCanonicalName(),
						matchingSuperType.getClass().getCanonicalName());
				final Class matchingSuperClass = matchingSuperType.getJavaClass();
				subToSuperTypes.put(argumentClass, matchingSuperClass);
				superType = matchingSuperClass;
			}
		}

		if (superType == null) {
			throw new OSCSerializeException("No type handler registered for serializing class "
					+ argumentClass.getCanonicalName());
		}

		return superType;
	}

	private ArgumentHandler findType(final Object argumentValue, final Class argumentClass)
			throws OSCSerializeException
	{
		final ArgumentHandler type;

		final Boolean markerType = classToMarker.get(argumentClass);
		if (markerType == null) {
			type = findType(argumentValue, findSuperType(argumentClass));
		} else if (markerType) {
			type = markerValueToType.get(argumentValue);
		} else {
			type = classToType.get(argumentClass);
		}

		return type;
	}

	private ArgumentHandler findType(final Object argumentValue) throws OSCSerializeException {
		return findType(argumentValue, extractTypeClass(argumentValue));
	}

	/**
	 * Write an object into the byte stream.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer, Long,
	 *   or a Collection of these.
	 *   See {@link #getClassToTypeMapping()} for a complete list of which classes may be used here.
	 * @throws OSCSerializeException if the argument object failed to serialize
	 */
	private void write(final Object anObject) throws OSCSerializeException {

		if (anObject instanceof Collection) {
			// We can safely suppress the warning, as we already made sure the cast will not fail.
			@SuppressWarnings("unchecked") final Collection<?> theArray = (Collection<?>) anObject;
			for (final Object entry : theArray) {
				write(entry);
			}
		} else {
			@SuppressWarnings("unchecked") final ArgumentHandler<Object> type = findType(anObject);
			type.serialize(output, anObject);
		}
	}

	private static Class extractTypeClass(final Object value) {
		return (value == null) ? Object.class : value.getClass();
	}

	/**
	 * Write the OSC specification type tag for the type a certain Java type
	 * converts to.
	 * @param value of this argument, we need to write the type identifier
	 * @throws OSCSerializeException if the value failed to serialize
	 */
	private void writeType(final Object value) throws OSCSerializeException {

		final ArgumentHandler type = findType(value);
		output.put((byte) type.getDefaultIdentifier());
	}

	/**
	 * Write the type tags for a given list of arguments.
	 * @param arguments array of base Objects
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTagsRaw(final List<?> arguments) throws OSCSerializeException {

		for (final Object argument : arguments) {
			if (argument instanceof List) {
				@SuppressWarnings("unchecked") final List<?> argumentsArray = (List<?>) argument;
				// This is used for nested arguments.
				// open the array
				output.put((byte) OSCParser.TYPE_ARRAY_BEGIN);
				// fill the [] with the nested argument types
				writeTypeTagsRaw(argumentsArray);
				// close the array
				output.put((byte) OSCParser.TYPE_ARRAY_END);
			} else {
				// write a single, simple arguments type
				writeType(argument);
			}
		}
	}

	/**
	 * Write the type tags for a given list of arguments, and cleanup the stream.
	 * @param arguments  the arguments to an OSCMessage
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTags(final List<?> arguments) throws OSCSerializeException {

		writeTypeTagsRaw(arguments);
		terminateAndAlign(output);
	}
}
