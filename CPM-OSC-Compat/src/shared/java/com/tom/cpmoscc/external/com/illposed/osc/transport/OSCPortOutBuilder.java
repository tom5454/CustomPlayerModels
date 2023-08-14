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

import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializerAndParserBuilder;

public class OSCPortOutBuilder {

	private OSCSerializerAndParserBuilder serializerBuilder;
	private SocketAddress remote;
	private SocketAddress local;
	private NetworkProtocol networkProtocol = NetworkProtocol.UDP;

	public OSCPortOut build() throws IOException {
		if (remote == null) {
			throw new IllegalArgumentException(
				"Missing remote socket address / port.");
		}

		if (local == null) {
			local = new InetSocketAddress(OSCPort.generateWildcard(remote), 0);
		}

		if (serializerBuilder == null) {
			serializerBuilder = new OSCSerializerAndParserBuilder();
		}

		return new OSCPortOut(
			serializerBuilder, remote, local, networkProtocol
		);
	}

	public OSCPortOutBuilder setPort(final int port) {
		final SocketAddress address = new InetSocketAddress(port);
		local = address;
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setRemotePort(final int port) {
		remote = new InetSocketAddress(port);
		return this;
	}

	public OSCPortOutBuilder setLocalPort(final int port) {
		local = new InetSocketAddress(port);
		return this;
	}

	public OSCPortOutBuilder setSocketAddress(final SocketAddress address) {
		local = address;
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setLocalSocketAddress(final SocketAddress address) {
		local = address;
		return this;
	}

	public OSCPortOutBuilder setRemoteSocketAddress(final SocketAddress address) {
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setNetworkProtocol(final NetworkProtocol protocol) {
		networkProtocol = protocol;
		return this;
	}
}
