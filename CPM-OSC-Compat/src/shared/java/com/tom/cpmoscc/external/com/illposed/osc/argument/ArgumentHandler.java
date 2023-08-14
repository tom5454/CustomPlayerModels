/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument;

import java.nio.ByteBuffer;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.BytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;

/**
 * Handles all aspects regarding a single OSC <i>Argument</i> type.
 * This is used to {@link #parse} (convert from OSC byte representation to Java objects),
 * and to {@link #serialize} (convert from Java objects to OSC byte representation).
 * @param <T> Java type of the OSC argument this handler deals with
 */
public interface ArgumentHandler<T> extends Cloneable {

	/**
	 * Returns the default identifier character of this type of OSC Argument,
	 * which is the character used in the types string of an OSC Message.
	 * @return default type identifier, as specified in the OSC specification,
	 *   if this type is part of it
	 */
	char getDefaultIdentifier();

	/**
	 * Returns the Java class this OSC type converts to/from.
	 * Objects of this class are serialized to this OSC Argument type,
	 * and this type is parsed into Java Objects of this class.
	 * @return the Java class this OSC type converts to/from.
	 */
	Class<T> getJavaClass();

	/**
	 * Used to configure special properties that might further specify
	 * parsing and/or serializing objects of this type.
	 * Most default OSC Argument are "static" in this regard,
	 * as they have a single, not configurable way of operating.
	 * But the <code>String</code> type for example, allows to specify the character-set.
	 * This method is usually called once for all types before the parsing/serialization starts.
	 * Note that this set of properties is parser/serializer global,
	 * meaning that there is a single set per parser/serializer
	 * and all the associated handlers,
	 * which means one has to make sure to not use a single property key
	 * in multiple places (i.e. two different handlers)
	 * with different meaning or syntax.
	 * @param properties a set of properties that usually gets passed over to all types,
	 *   for example: <code>properties.put(StringArgumentHandler.PROP_NAME_CHARSET, Charset.defaultCharset());</code>
	 */
	void setProperties(Map<String, Object> properties);

	/**
	 * Indicates whether this type is only a marker type,
	 * meaning all of its information is incorporated in the type identifier,
	 * and that it comes with no additional data.
	 * This means that the {@link #parse} and {@link #serialize} methods never throw exceptions,
	 * and that {@link #parse} always returns the same value, even <code>parse(null)</code>,
	 * and that {@link #serialize} is basically a no-op.
	 * @return <code>true</code> if this is only a marker type,
	 *   <code>false</code> if it comes with additional data
	 */
	boolean isMarkerOnly();

	ArgumentHandler<T> clone() throws CloneNotSupportedException;

	/**
	 * Converts from the OSC byte representation to a Java object.
	 * @param input contains the OSC byte representation of one instance of this type.
	 *   The data for this types object starts at the current position.
	 *   Where the end is, depends on the specific type.
	 *   After the invocation of this method, the current position should point at
	 *   the last byte of this types object + 1.
	 * @return the parsed Java object
	 * @throws OSCParseException if anything went wrong while parsing,
	 *   for example invalid or incomplete data in the input
	 */
	T parse(ByteBuffer input) throws OSCParseException;

	/**
	 * Converts from a Java objects to the OSC byte representation.
	 * @param output where the OSC byte representation of value has to be written to
	 * @param value the Java value to be serialized into its OSC byte representation
	 * @throws OSCSerializeException if anything went wrong while serializing,
	 *   for example an invalid value was given
	 */
	void serialize(BytesReceiver output, T value) throws OSCSerializeException;
}
