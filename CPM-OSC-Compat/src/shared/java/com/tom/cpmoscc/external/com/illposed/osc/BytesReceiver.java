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
 * This has a very similar function to BytesBuffer.
 * It shares many of its methods, though it misses some,
 * and adds some more.
 * The general idea of it is,
 * to have a buffer that is just filled sequentially over time,
 * with the ability to replace specific parts of the internal buffer later on,
 * but only when the original put and the overwriting put
 * receive data of the same length.
 * This is a quite common scenario.
 * This interface alone has no advantage over {@link ByteBuffer},
 * but it allows for alternate implementations of it,
 * which have performance benefits in certain scenarios.
 * @see ByteArrayListBytesReceiver
 */
public interface BytesReceiver {

	/**
	 * Relative <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p> Writes the given byte into this buffer at the current
	 * position, and then increments the position. </p>
	 *
	 * @param  data
	 *         The byte to be written
	 *
	 * @return  This buffer
	 *
	 * @throws  java.nio.BufferOverflowException
	 *          If this buffer's current position is not smaller than its limit
	 *
	 * @throws  java.nio.ReadOnlyBufferException
	 *          If this buffer is read-only
	 */
	BytesReceiver put(byte data);

	/**
	 * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p> This method transfers the entire content of the given source
	 * byte array into this buffer.  An invocation of this method of the
	 * form <tt>dst.put(a)</tt> behaves in exactly the same way as the
	 * invocation
	 *
	 * <pre>
	 *     dst.put(a, 0, a.length) </pre>
	 *
	 * @param   src
	 *          The source array
	 *
	 * @return  This buffer
	 *
	 * @throws  java.nio.BufferOverflowException
	 *          If there is insufficient space in this buffer
	 *
	 * @throws  java.nio.ReadOnlyBufferException
	 *          If this buffer is read-only
	 */
	BytesReceiver put(byte[] src);

	/**
	 * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p> This method transfers the bytes remaining in the given source
	 * buffer into this buffer.  If there are more bytes remaining in the
	 * source buffer than in this buffer, that is, if
	 * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>,
	 * then no bytes are transferred and a {@link
	 * java.nio.BufferOverflowException} is thrown.
	 *
	 * <p> Otherwise, this method copies
	 * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> bytes from the given
	 * buffer into this buffer, starting at each buffer's current position.
	 * The positions of both buffers are then incremented by <i>n</i>.
	 *
	 * <p> In other words, an invocation of this method of the form
	 * <tt>dst.put(src)</tt> has exactly the same effect as the loop
	 *
	 * <pre>
	 *     while (src.hasRemaining())
	 *         dst.put(src.get()); </pre>
	 *
	 * except that it first checks that there is sufficient space in this
	 * buffer and it is potentially much more efficient.
	 *
	 * @param  src
	 *         The source buffer from which bytes are to be read;
	 *         must not be this buffer
	 *
	 * @return  This buffer
	 *
	 * @throws  java.nio.BufferOverflowException
	 *          If there is insufficient space in this buffer
	 *          for the remaining bytes in the source buffer
	 *
	 * @throws  IllegalArgumentException
	 *          If the source buffer is this buffer
	 *
	 * @throws  java.nio.ReadOnlyBufferException
	 *          If this buffer is read-only
	 */
	BytesReceiver put(ByteBuffer src);

	/**
	 * Clears this buffer.  The position is set to zero, the limit is set to
	 * the capacity, and the mark is discarded.
	 *
	 * <p> Invoke this method before using a sequence of channel-read or
	 * <i>put</i> operations to fill this buffer.  For example:
	 *
	 * <blockquote><pre>
	 * buf.clear();     // Prepare buffer for reading
	 * in.read(buf);    // Read data</pre></blockquote>
	 *
	 * <p> This method does not actually erase the data in the buffer, but it
	 * is named as if it did because it will most often be used in situations
	 * in which that might as well be the case. </p>
	 *
	 * @return  This buffer
	 */
	BytesReceiver clear();

	/**
	 * Returns this buffers position.
	 *
	 * @return  The position of this buffer
	 */
	int position();

	/**
	 * A piece of data, stored in the buffer at the current location,
	 * which can later be replaced with an other piece of data of the same length.
	 */
	interface PlaceHolder {
		void replace(byte[] src) throws OSCSerializeException;
	}

	/**
	 * But a piece of data, which we later might want to replace
	 * with an other one of equal length.
	 * @param src the preliminary piece of data
	 * @return an object holding an internal reference on the put piece of data,
	 *   which allows to later replace it in the internal buffer
	 */
	PlaceHolder putPlaceHolder(byte[] src);

	/**
	 * Returns a raw byte array containing all data this instance received
	 * since tha last {@link #clear()}.
	 * NOTE This is a costly method (performance wise -> memory usage),
	 * and should therefore only be used in unit tests.
	 *
	 * @return the current internal buffers content in a byte array
	 */
	byte[] toByteArray();
}
