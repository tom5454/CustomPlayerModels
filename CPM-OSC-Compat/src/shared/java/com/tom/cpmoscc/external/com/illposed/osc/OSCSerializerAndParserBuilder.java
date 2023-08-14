/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;
import com.tom.cpmoscc.external.com.illposed.osc.argument.handler.Activator;

/**
 * Builder to create serializers and parsers.
 */
public class OSCSerializerAndParserBuilder {

	private final Map<String, Object> properties;
	private final Map<Character, ArgumentHandler> identifierToType;
	private boolean usingDefaultHandlers;

	public OSCSerializerAndParserBuilder() {

		this.properties = new HashMap<>();
		this.identifierToType = new HashMap<>();
		this.usingDefaultHandlers = true;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public Map<Character, ArgumentHandler> getIdentifierToTypeMapping() {
		return Collections.unmodifiableMap(identifierToType);
	}

	public OSCSerializer buildSerializer(final BytesReceiver output) {

		final Map<String, Object> currentProperties = getProperties();
		final List<ArgumentHandler> typeCopies
				= new ArrayList<>(identifierToType.size());
		for (final ArgumentHandler<?> type : identifierToType.values()) {
			final ArgumentHandler<?> typeClone;
			try {
				typeClone = type.clone();
			} catch (final CloneNotSupportedException ex) {
				throw new IllegalStateException("Does not support cloning: " + type.getClass(), ex);
			}
			typeClone.setProperties(currentProperties);

			typeCopies.add(typeClone);
		}

		if (usingDefaultHandlers) {
			final List<ArgumentHandler> defaultParserTypes = Activator.createSerializerTypes();
			typeCopies.addAll(defaultParserTypes);
		}

		return new OSCSerializer(typeCopies, currentProperties, output);
	}

	public OSCParser buildParser() {

		// first create a shallow copy
		final Map<Character, ArgumentHandler> identifierToTypeCopy
				= new HashMap<>(identifierToType.size());
		// now make it deep & set the properties
		final Map<String, Object> currentProperties = getProperties();
		for (final Map.Entry<Character, ArgumentHandler> typeEntry : identifierToType.entrySet()) {
			final ArgumentHandler<?> typeClone;
			try {
				typeClone = typeEntry.getValue().clone();
			} catch (final CloneNotSupportedException ex) {
				throw new IllegalStateException("Does not support cloning: "
						+ typeEntry.getValue().getClass(), ex);
			}
			identifierToTypeCopy.put(typeEntry.getKey(), typeClone);

			typeClone.setProperties(currentProperties);
		}

		if (usingDefaultHandlers) {
			final Map<Character, ArgumentHandler> defaultParserTypes = Activator.createParserTypes();
			identifierToTypeCopy.putAll(defaultParserTypes);
		}

		return new OSCParser(identifierToTypeCopy, currentProperties);
	}

	public OSCSerializerAndParserBuilder setUsingDefaultHandlers(final boolean newUsingDefaultHandlers) {

		this.usingDefaultHandlers = newUsingDefaultHandlers;
		return this;
	}

	/**
	 * Returns the current set of properties.
	 * These will be propagated to created serializers and parsers
	 * and to the argument-handlers.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(Map)
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Sets a new set of properties after clearing the current ones.
	 * Properties will be propagated to created serializers and parsers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers, parsers and argument-handlers
	 * being created in the future.
	 * @param newProperties the new set of properties to adhere to
	 */
	public OSCSerializerAndParserBuilder setProperties(final Map<String, Object> newProperties) {

		clearProperties();
		addProperties(newProperties);
		return this;
	}

	// Public API
	/**
	 * Adds a new set of properties, extending,
	 * possibly overriding the current ones.
	 * Properties will be propagated to created serializers and parsers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers, parsers and argument-handlers
	 * being created in the future.
	 * @param additionalProperties the new set of properties to adhere to
	 */
	@SuppressWarnings("WeakerAccess")
	public OSCSerializerAndParserBuilder addProperties(final Map<String, Object> additionalProperties) {

		properties.putAll(additionalProperties);
		return this;
	}

	// Public API
	/**
	 * Clears all currently stored properties.
	 * Properties will be propagated to created serializers and parsers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers, parsers and argument-handlers
	 * being created in the future.
	 */
	@SuppressWarnings("WeakerAccess")
	public OSCSerializerAndParserBuilder clearProperties() {

		properties.clear();
		return this;
	}

	// Public API
	public OSCSerializerAndParserBuilder registerArgumentHandler(final ArgumentHandler argumentHandler) {

		registerArgumentHandler(argumentHandler, argumentHandler.getDefaultIdentifier());
		return this;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCSerializerAndParserBuilder registerArgumentHandler(
			final ArgumentHandler argumentHandler,
			final char typeIdentifier)
	{
		final ArgumentHandler previousArgumentHandler = identifierToType.get(typeIdentifier);
		if (previousArgumentHandler != null) {
			throw new IllegalStateException("Type identifier '" + typeIdentifier
					+ "' is already used for type "
					+ previousArgumentHandler.getClass().getCanonicalName());
		}
		identifierToType.put(typeIdentifier, argumentHandler);
		return this;
	}

	// Public API
	@SuppressWarnings("unused")
	public OSCSerializerAndParserBuilder unregisterArgumentHandler(final ArgumentHandler argumentHandler) {

		unregisterArgumentHandler(argumentHandler.getDefaultIdentifier());
		return this;
	}

	// Public API
	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public OSCSerializerAndParserBuilder unregisterArgumentHandler(final char typeIdentifier) {

		identifierToType.remove(typeIdentifier);
		return this;
	}
}
