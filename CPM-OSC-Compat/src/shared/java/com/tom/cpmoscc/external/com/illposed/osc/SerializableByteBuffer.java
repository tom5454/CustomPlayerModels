/*
 * Copyright (C) 2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Public API
/**
 * Allows to serialize and deserialize a {@link ByteBuffer}.
 */
@SuppressWarnings("WeakerAccess")
public class SerializableByteBuffer implements Serializable {

	private static final long serialVersionUID = 1L;

	// mark as transient so this is not serialized by default
	private transient ByteBuffer buffer;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public SerializableByteBuffer(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public ByteBuffer getBuffer() {
		return buffer;
	}

	private void writeObject(final ObjectOutputStream serializer) throws IOException {

		// write default properties
		serializer.defaultWriteObject();
		// write buffer meta info and data
		serializer.writeInt(buffer.capacity());
		serializer.writeInt(buffer.position());
		serializer.writeInt(buffer.limit());
		// sets position to mark
		buffer.reset();
		// store the mark
		serializer.writeInt(buffer.position());
		serializer.writeObject(buffer.order().toString());
		if (buffer.hasArray()) {
			serializer.write(buffer.array());
		} else {
			final byte[] bufferArray = new byte[buffer.capacity()];
			buffer.rewind();
			((Buffer)buffer).limit(buffer.capacity());
			buffer.get(bufferArray);
			serializer.write(buffer.array());
		}

	}

	private void readObject(final ObjectInputStream deserializer)
			throws IOException, ClassNotFoundException
	{
		//read default properties
		deserializer.defaultReadObject();

		//read buffer data and wrap with ByteBuffer
		final int capacity = deserializer.readInt();
		final int position = deserializer.readInt();
		final int limit = deserializer.readInt();
		final int mark = deserializer.readInt();
		final String orderName = (String) deserializer.readObject();
		final ByteOrder order
				= orderName.equals(ByteOrder.LITTLE_ENDIAN.toString())
						? ByteOrder.LITTLE_ENDIAN
						: ByteOrder.BIG_ENDIAN;
		final byte[] bufferArray = new byte[capacity];
		final int bufferContentSize = deserializer.read(bufferArray);
		if (bufferContentSize != capacity) {
			throw new IllegalStateException(
					"Actual content size does not match the indicated buffer size");
		}
		buffer = ByteBuffer.wrap(bufferArray, 0, capacity);
		buffer.order(order);
		((Buffer)buffer).position(mark);
		buffer.mark();
		((Buffer)buffer).limit(limit);
		((Buffer)buffer).position(position);
	}
}
