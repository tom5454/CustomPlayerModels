/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;

/**
 * Allows to easily register and unregister all types in this package.
 */
public final class Activator {

	private static final List<ArgumentHandler> TYPES_STATIC_COMMON;
	static {
		final ArrayList<ArgumentHandler> types = new ArrayList<>();
		types.add(BlobArgumentHandler.INSTANCE);
		types.add(BooleanFalseArgumentHandler.INSTANCE);
		types.add(BooleanTrueArgumentHandler.INSTANCE);
		types.add(CharArgumentHandler.INSTANCE);
		types.add(ColorArgumentHandler.INSTANCE);
		types.add(DoubleArgumentHandler.INSTANCE);
		types.add(FloatArgumentHandler.INSTANCE);
		types.add(ImpulseArgumentHandler.INSTANCE);
		types.add(IntegerArgumentHandler.INSTANCE);
		types.add(LongArgumentHandler.INSTANCE);
		types.add(MidiMessageArgumentHandler.INSTANCE);
		types.add(NullArgumentHandler.INSTANCE);
		types.add(TimeTag64ArgumentHandler.INSTANCE);
		types.add(UnsignedIntegerArgumentHandler.INSTANCE);
		types.trimToSize();
		TYPES_STATIC_COMMON = Collections.unmodifiableList(types);
	}

	private Activator() {}

	public static Map<Character, ArgumentHandler> createParserTypes() {

		final Map<Character, ArgumentHandler> parserTypes
		= new HashMap<>(TYPES_STATIC_COMMON.size() + 1);
		for (final ArgumentHandler type : TYPES_STATIC_COMMON) {
			parserTypes.put(type.getDefaultIdentifier(), type);
		}

		final StringArgumentHandler stringArgumentHandler = new StringArgumentHandler();
		parserTypes.put(stringArgumentHandler.getDefaultIdentifier(), stringArgumentHandler);

		final SymbolArgumentHandler symbolArgumentHandler = new SymbolArgumentHandler();
		parserTypes.put(symbolArgumentHandler.getDefaultIdentifier(), symbolArgumentHandler);

		// NOTE We do not register ByteArrayBlobArgumentHandler (byte[]) here,
		//   because type 'b' already converts to byteArray.

		// NOTE We do not register DateTimeStampArgumentHandler (Date) here,
		//   because type 't' already converts to OSCTimeTag64.

		return parserTypes;
	}

	public static List<ArgumentHandler> createSerializerTypes() {

		final List<ArgumentHandler> serializerTypes
		= new ArrayList<>(TYPES_STATIC_COMMON.size() + 2);
		serializerTypes.addAll(TYPES_STATIC_COMMON);

		final StringArgumentHandler stringArgumentHandler = new StringArgumentHandler();
		serializerTypes.add(stringArgumentHandler);

		final SymbolArgumentHandler symbolArgumentHandler = new SymbolArgumentHandler();
		serializerTypes.add(symbolArgumentHandler);

		// NOTE We add this for legacy support, though it is recommended
		//   to use ByteBuffer over byte[], as it may be handled more efficiently by some code.
		final ArgumentHandler byteArrayBlobArgumentHandler = ByteArrayBlobArgumentHandler.INSTANCE;
		serializerTypes.add(byteArrayBlobArgumentHandler);

		// NOTE We add this for legacy support, though it is recommended
		//   to use OSCTimeTag64 over Date, to not loose precision and range during conversions.
		final ArgumentHandler dateArgumentHandler = DateTimeStampArgumentHandler.INSTANCE;
		serializerTypes.add(dateArgumentHandler);

		return serializerTypes;
	}
}
