/*
 * Copyright (C) 2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpmoscc.external.com.illposed.osc.MessageSelector;
import com.tom.cpmoscc.external.com.illposed.osc.OSCMessageListener;
import com.tom.cpmoscc.external.com.illposed.osc.OSCPacketDispatcher;
import com.tom.cpmoscc.external.com.illposed.osc.OSCPacketListener;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializerAndParserBuilder;

public class OSCPortInBuilder {

	private OSCSerializerAndParserBuilder parserBuilder;
	private List<OSCPacketListener> packetListeners;
	private SocketAddress local;
	private SocketAddress remote;
	private NetworkProtocol networkProtocol = NetworkProtocol.UDP;

	private OSCPacketListener addDefaultPacketListener() {
		if (packetListeners == null) {
			packetListeners = new ArrayList<>();
		}

		final OSCPacketListener listener = OSCPortIn.defaultPacketListener();
		packetListeners.add(listener);

		return listener;
	}

	public OSCPortIn build() throws IOException {
		if (local == null) {
			throw new IllegalArgumentException(
				"Missing local socket address / port.");
		}

		if (remote == null) {
			remote = new InetSocketAddress(OSCPort.generateWildcard(local), 0);
		}

		if (parserBuilder == null) {
			parserBuilder = new OSCSerializerAndParserBuilder();
		}

		if (packetListeners == null) {
			addDefaultPacketListener();
		}

		return new OSCPortIn(
			parserBuilder, packetListeners, local, remote, networkProtocol
		);
	}

	public OSCPortInBuilder setPort(final int port) {
		final SocketAddress address = new InetSocketAddress(port);
		local = address;
		remote = address;
		return this;
	}

	public OSCPortInBuilder setLocalPort(final int port) {
		local = new InetSocketAddress(port);
		return this;
	}

	public OSCPortInBuilder setRemotePort(final int port) {
		remote = new InetSocketAddress(port);
		return this;
	}

	public OSCPortInBuilder setSocketAddress(final SocketAddress address) {
		local = address;
		remote = address;
		return this;
	}

	public OSCPortInBuilder setLocalSocketAddress(final SocketAddress address) {
		local = address;
		return this;
	}

	public OSCPortInBuilder setRemoteSocketAddress(final SocketAddress address) {
		remote = address;
		return this;
	}

	public OSCPortInBuilder setNetworkProtocol(final NetworkProtocol protocol) {
		networkProtocol = protocol;
		return this;
	}

	public OSCPortInBuilder setPacketListeners(
			final List<OSCPacketListener> listeners)
	{
		packetListeners = listeners;
		return this;
	}

	public OSCPortInBuilder setPacketListener(final OSCPacketListener listener) {
		packetListeners = new ArrayList<>();
		packetListeners.add(listener);
		return this;
	}

	public OSCPortInBuilder addPacketListener(final OSCPacketListener listener) {
		if (packetListeners == null) {
			packetListeners = new ArrayList<>();
		}

		packetListeners.add(listener);
		return this;
	}

	public OSCPortInBuilder addMessageListener(
		final MessageSelector selector, final OSCMessageListener listener)
	{
		OSCPacketDispatcher dispatcher = OSCPortIn.getDispatcher(packetListeners);

		if (dispatcher == null) {
			dispatcher = (OSCPacketDispatcher)addDefaultPacketListener();
		}

		dispatcher.addListener(selector, listener);

		return this;
	}
}
