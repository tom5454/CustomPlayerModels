package com.tom.cpmoscc;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

import com.tom.cpmoscc.external.com.illposed.osc.OSCMessage;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;
import com.tom.cpmoscc.external.com.illposed.osc.transport.OSCPortOut;

public class OSCTransmitter implements Closeable {
	private final OSCPortOut oscOut;
	private Exception error;

	public OSCTransmitter(String outputAddr) {
		String[] sp = outputAddr.split(":");
		OSCPortOut oscOut;
		try {
			InetAddress ip = sp[0].equals("localhost") || sp[0].isEmpty() ? InetAddress.getLocalHost() : InetAddress.getByName(outputAddr);
			int port = sp.length > 1 ? Integer.parseInt(sp[1]) : 9001;
			oscOut = new OSCPortOut(new InetSocketAddress(ip, port));
		} catch (IOException e) {
			oscOut = null;
			this.error = e;
		}
		this.oscOut = oscOut;
	}

	@Override
	public void close() throws IOException {
		if(oscOut != null)
			oscOut.close();
	}

	public Exception getError() {
		return error;
	}

	public boolean canSend() {
		return oscOut != null;
	}

	public void send(String path, Object... args) throws IOException {
		if(oscOut != null) {
			try {
				oscOut.send(new OSCMessage(path, Arrays.asList(args)));
			} catch (OSCSerializeException e) {
				throw new IOException(e);
			}
		}
	}
}
