package com.tom.cpmoscc;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.transport.OSCPortIn;

public class OSCReceiver implements MessageSelector, OSCMessageListener, Closeable {
	private final OSCPortIn oscIn;
	private Exception error;
	private OSCListener listener;

	public OSCReceiver(int port) {
		OSCPortIn oscIn;
		try {
			oscIn = new OSCPortIn(port);
			oscIn.setDaemonListener(true);
			oscIn.getDispatcher().addListener(this, this);
		} catch (IOException e) {
			oscIn = null;
			this.error = e;
		}
		this.oscIn = oscIn;
	}

	public void start() {
		if(oscIn != null)
			oscIn.startListening();
	}

	@Override
	public void close() throws IOException {
		if(oscIn != null)
			oscIn.close();
	}

	@Override
	public void acceptMessage(OSCMessageEvent event) {
		CPMOSC.manager.acceptOsc(event.getMessage().getAddress(), event.getMessage().getArguments());
		if(listener != null)
			listener.onReceive(event.getMessage().getAddress(), event.getMessage().getArguments());
	}

	@Override
	public boolean isInfoRequired() {
		return false;
	}

	@Override
	public boolean matches(OSCMessageEvent messageEvent) {
		return true;
	}

	public static void main(String[] args) throws IOException {
		OSCReceiver r = new OSCReceiver(9000);
		r.start();
		System.in.read();
		r.close();
	}

	public Exception getError() {
		return error;
	}

	public boolean canStart() {
		return oscIn != null;
	}

	public void setListener(OSCListener listener) {
		this.listener = listener;
	}

	public static interface OSCListener {
		void onReceive(String address, List<Object> args);
	}
}
