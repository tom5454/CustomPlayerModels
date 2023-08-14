/*
 * Copyright (C) 2004-2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.transport.tcp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.tom.cpmoscc.external.com.illposed.osc.ByteArrayListBytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.OSCPacket;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParser;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializer;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializerAndParserBuilder;
import com.tom.cpmoscc.external.com.illposed.osc.transport.Transport;

/**
 * A {@link Transport} implementation for sending and receiving OSC packets over
 * a network via TCP.
 */
public class TCPTransport implements Transport {

	private final InetSocketAddress local;
	private final InetSocketAddress remote;
	private final OSCParser parser;
	private final ByteArrayListBytesReceiver serializationBuffer;
	private final OSCSerializer serializer;
	private Socket clientSocket;
	private ServerSocket serverSocket;

	public TCPTransport(
			final InetSocketAddress local,
			final InetSocketAddress remote)
			throws IOException
	{
		this(local, remote, new OSCSerializerAndParserBuilder());
	}

	public TCPTransport(
			final InetSocketAddress local,
			final InetSocketAddress remote,
			final OSCSerializerAndParserBuilder builder)
			throws IOException
	{
		this.local = local;
		this.remote = remote;
		this.parser = builder.buildParser();
		this.serializationBuffer = new ByteArrayListBytesReceiver();
		this.serializer = builder.buildSerializer(serializationBuffer);
		this.clientSocket = null;
		this.serverSocket = null;
	}

	private Socket getClientSocket() throws IOException {

		if ((clientSocket == null) || clientSocket.isClosed()) {
			clientSocket = new Socket();
		}

		return clientSocket;
	}

	private ServerSocket getServerSocket() throws IOException {
		if ((serverSocket == null) || serverSocket.isClosed()) {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(local);
		}

		return serverSocket;
	}

	@Override
	public void connect() throws IOException {
		// Not relevant for TCP. TCP does involve connections, but we create them as
		// we need them, when either `send` or `receive` is invoked.
	}

	@Override
	public void disconnect() throws IOException {
		// Not possible/relevant for TCP.
	}

	@Override
	public boolean isConnected() {
		// Not relevant for TCP.
		return false;
	}

	public boolean isListening() throws IOException {

		boolean listening;
		try {
			new Socket(local.getAddress(), local.getPort()).close();
			listening = true;
		} catch (ConnectException cex) {
			listening = false;
		}

		return listening;
	}

	/**
	 * Close the socket and free-up resources.
	 * It is recommended that clients call this when they are done with the port.
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		if (clientSocket != null) {
			clientSocket.close();
		}

		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	@Override
	public void send(final OSCPacket packet)
			throws IOException, OSCSerializeException
	{
		serializer.write(packet);

		final Socket clientSock = getClientSocket();
		if (!clientSock.isConnected()) {
			clientSock.connect(remote);
		}

		// Closing the output stream is necessary in order for the receiving side to
		// know that it has received everything. When reading bytes off of the
		// stream, an EOF (-1) byte signals the end of the stream, and this doesn't
		// get sent until the output stream is closed.
		//
		// NB: Closing the output stream effectively closes the client socket as
		// well. The next time getClientSocket() is called, it will recognize that
		// the socket is closed and create a new one. So, every message sent uses a
		// new client socket.
		try (OutputStream out = clientSock.getOutputStream()) {
			serializationBuffer.writeTo(out);
		}
	}

	// InputStream.readAllBytes is available as of Java 9. Implementing it here in
	// order to support Java 8.
	//
	// Source: https://stackoverflow.com/a/53347936/2338327
	//
	// I modified the source in that we don't want to close the input stream, as
	// it belongs to a ServerSocket and we don't want to close the stream because
	// that would also close the socket.
	private byte[] readAllBytes(final InputStream inputStream)
			throws IOException
	{
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			// 4 * 0x400 = 4 KB
			final int bufLen = 4 * 0x400;
			int readLen;
			final byte[] buffer = new byte[bufLen];
			while ((readLen = inputStream.read(buffer, 0, bufLen)) != -1) {
				outputStream.write(buffer, 0, readLen);
			}

			return outputStream.toByteArray();
		}
	}

	@Override
	public OSCPacket receive() throws IOException, OSCParseException {

		final ServerSocket serverSock = getServerSocket();

		// Unlike UDP, TCP involves connections, and a valid use case is for a
		// client socket to connect to a server socket simply to test whether the
		// server socket is listening.
		//
		// When this happens, an empty byte array is received. We recognize this
		// scenario and move onto the next connection, and keep doing this until we
		// receive a connection that sends > 0 bytes.
		while (true) {
			final Socket dataSocket = serverSock.accept();
			final byte[] bytes = readAllBytes(dataSocket.getInputStream());

			if (bytes.length == 0) {
				continue;
			}

			return parser.convert(ByteBuffer.wrap(bytes));
		}
	}

	@Override
	public boolean isBlocking() {
		// Not relevant for TCP.
		return false;
	}

	@Override
	public String toString() {
		return String.format(
			"%s: local=%s, remote=%s", getClass().getSimpleName(), local, remote
		);
	}
}
