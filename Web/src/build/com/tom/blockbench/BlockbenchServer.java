package com.tom.blockbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.tom.cpl.util.Pair;

@SuppressWarnings("restriction")
public class BlockbenchServer {
	public static class MyHandler implements HttpHandler {
		private boolean log;
		private File map;

		public MyHandler(boolean log) {
			this.log = log;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				URI uri = t.getRequestURI();
				String path = uri.getPath();
				path = path.substring(1);
				if(log)System.out.println(t.getRemoteAddress() + " " + path);
				if(path.equals("cpm_plugin.js")) {
					Pair<File, File> r = BuildBlockbench.main(false, false);
					File in = r.getKey();
					map = r.getValue();
					try(FileInputStream f = new FileInputStream(in)) {
						t.sendResponseHeaders(200, in.length());
						BuildBlockbench.copy(f, t.getResponseBody());
					}
				} else if(map != null && path.equals("src/cpmblockbench.map")) {
					try(FileInputStream f = new FileInputStream(map)) {
						t.sendResponseHeaders(200, map.length());
						BuildBlockbench.copy(f, t.getResponseBody());
					}
				} else if(path.startsWith("srcmap")) {
					File m = new File("war/" + path);
					try(FileInputStream f = new FileInputStream(m)) {
						t.sendResponseHeaders(200, m.length());
						BuildBlockbench.copy(f, t.getResponseBody());
					}
				} else if(path.startsWith("src")) {
					try(InputStream f = BlockbenchServer.class.getResourceAsStream(path.substring(3))) {
						if(f != null) {
							t.sendResponseHeaders(200, f.available());
							BuildBlockbench.copy(f, t.getResponseBody());
						} else {
							t.sendResponseHeaders(404, 0);
						}
					}
				} else {
					t.sendResponseHeaders(404, 0);
				}
				if(t.getRequestBody().available() > 0){
					byte[] trash = new byte[1024];
					while(t.getRequestBody().read(trash) >= 0);
				}
				t.close();
			} catch(RuntimeException | IOException e){
				if(e instanceof RuntimeException)e.printStackTrace();
				throw e;
			}
		}
	}

	private static HttpServer server;

	public static void main(String[] args) {
		System.out.println("Load the plugin from: http://localhost:8000/cpm_plugin.js");
		try {
			server = HttpServer.create(new InetSocketAddress(8000), 0);
			server.createContext("/", new MyHandler(true));
			server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Press enter to exit");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping server...");
		server.stop(1);
		System.exit(0);
	}

}
