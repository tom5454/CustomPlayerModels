/*
 * Copyright (C) 2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.nio.ByteBuffer;

/**
 * Implementation of a {@link BytesReceiver} using a {@code ByteBuffer}
 * as the internal buffer.
 * This is useful if we know an upper limit for the size
 * of the expected data in total.
 * It is thus an ideal candidate for synchronous data receiving,
 * as is the case in TCP.
 */
public class BufferBytesReceiver implements BytesReceiver {

	private final ByteBuffer buffer;

	public BufferBytesReceiver(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public BytesReceiver put(final byte data) {

		buffer.put(data);
		return this;
	}

	@Override
	public BytesReceiver put(final byte[] src) {

		buffer.put(src);
		return this;
	}

	@Override
	public BytesReceiver put(final ByteBuffer src) {

		buffer.put(src);
		return this;
	}

	@Override
	public BytesReceiver clear() {

		buffer.clear();
		return this;
	}

	@Override
	public int position() {
		return buffer.position();
	}

	// HACK try to get rid of this method
	public ByteBuffer getBuffer() {
		return buffer;
	}

	private class PlaceHolderImpl implements PlaceHolder {

		private final int position;
		private final int size;

		PlaceHolderImpl(final int position, final int size) {

			this.position = position;
			this.size = size;
		}

		@Override
		public void replace(final byte[] src) throws OSCSerializeException {

			if (src.length != size) {
				throw new OSCSerializeException(String.format(
						"Trying to replace placeholder of size %d with data of size %d",
						size, src.length));
			}
			final int curPosition = buffer.position();
			buffer.position(position);
			put(src);
			buffer.position(curPosition);
		}
	}

	@Override
	public PlaceHolder putPlaceHolder(final byte[] src) {

		final PlaceHolderImpl placeHolder = new PlaceHolderImpl(position(), src.length);
		put(src);
		return placeHolder;
	}

	@Override
	public byte[] toByteArray() {

		// TODO check if this flip is always required
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}
}
