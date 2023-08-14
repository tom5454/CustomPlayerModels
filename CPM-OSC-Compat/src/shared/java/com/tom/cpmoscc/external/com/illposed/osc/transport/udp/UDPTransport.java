/*
 * Copyright (C) 2004-2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.transport.udp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.tom.cpmoscc.external.com.illposed.osc.LibraryInfo;
import com.tom.cpmoscc.external.com.illposed.osc.OSCPacket;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializerAndParserBuilder;
import com.tom.cpmoscc.external.com.illposed.osc.transport.Transport;
import com.tom.cpmoscc.external.com.illposed.osc.transport.channel.OSCDatagramChannel;

/**
 * A {@link Transport} implementation for sending and receiving OSC packets over
 * a network via UDP.
 */
public class UDPTransport implements Transport {

	/**
	 * Buffers were 1500 bytes in size, but were increased to 1536, as this
	 * is a common MTU, and then increased to 65507, as this is the maximum
	 * incoming datagram data size.
	 */
	public static final int BUFFER_SIZE = 65507;
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

	private final SocketAddress local;
	private final SocketAddress remote;
	private final DatagramChannel channel;
	private final OSCDatagramChannel oscChannel;

	public UDPTransport(
		final SocketAddress local,
		final SocketAddress remote)
		throws IOException
	{
		this(local, remote, new OSCSerializerAndParserBuilder());
	}

	public UDPTransport(
			final SocketAddress local,
			final SocketAddress remote,
			final OSCSerializerAndParserBuilder serializerAndParserBuilder)
			throws IOException
	{
		this.local = local;
		this.remote = remote;
		final DatagramChannel tmpChannel;
		if ((local instanceof InetSocketAddress) && LibraryInfo.hasStandardProtocolFamily()) {
			final InetSocketAddress localIsa = (InetSocketAddress) local;
			final InetSocketAddress remoteIsa = (InetSocketAddress) remote;
			final Class<?> localClass = localIsa.getAddress().getClass();
			final Class<?> remoteClass = remoteIsa.getAddress().getClass();

			if (!localClass.equals(remoteClass)) {
				throw new IllegalArgumentException(
					"local and remote addresses are not of the same family"
					+ " (IP v4 vs v6)");
			}
			if (localIsa.getAddress() instanceof Inet4Address) {
				tmpChannel = DatagramChannel.open(StandardProtocolFamily.INET);
			} else if (localIsa.getAddress() instanceof Inet6Address) {
				tmpChannel = DatagramChannel.open(StandardProtocolFamily.INET6);
			} else {
				throw new IllegalArgumentException(
					"Unknown address type: "
					+ localIsa.getAddress().getClass().getCanonicalName());
			}
		} else {
			tmpChannel = DatagramChannel.open();
		}
		this.channel = tmpChannel;
		if (LibraryInfo.hasStandardProtocolFamily()) {
			this.channel.setOption(StandardSocketOptions.SO_SNDBUF, BUFFER_SIZE);
			// NOTE So far, we never saw an issue with the receive-buffer size,
			//      thus we leave it at its default.
			this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			this.channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
		} else {
			this.channel.socket().setSendBufferSize(BUFFER_SIZE);
			// NOTE So far, we never saw an issue with the receive-buffer size,
			//      thus we leave it at its default.
			this.channel.socket().setReuseAddress(true);
			this.channel.socket().setBroadcast(true);
		}
		this.channel.socket().bind(local);
		this.oscChannel = new OSCDatagramChannel(channel, serializerAndParserBuilder);
	}

	@Override
	public void connect() throws IOException {
		if (remote == null) {
			throw new IllegalStateException(
				"Can not connect a socket without a remote address specified"
			);
		}
		channel.connect(remote);
	}

	@Override
	public void disconnect() throws IOException {
		channel.disconnect();
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	/**
	 * Close the socket and free-up resources.
	 * It is recommended that clients call this when they are done with the port.
	 * @throws IOException If an I/O error occurs on the channel
	 */
	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public void send(final OSCPacket packet) throws IOException, OSCSerializeException {
		oscChannel.send(buffer, packet, remote);
	}

	@Override
	public OSCPacket receive() throws IOException, OSCParseException {
		return oscChannel.read(buffer);
	}

	@Override
	public boolean isBlocking() {
		return channel.isBlocking();
	}

	@Override
	public String toString() {
		return String.format(
			"%s: local=%s, remote=%s", getClass().getSimpleName(), local, remote
		);
	}
}
