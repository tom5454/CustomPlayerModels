package com.tom.cpm.blockbench.proxy.electron;

import elemental2.core.JsObject;
import elemental2.dom.URL;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Http {
	public native HttpRequest request(RequestProperties url, RequestCallback cb);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class RequestProperties {
		public String hostname, method, path, port;
		public HttpAgent agent;

		@JsOverlay
		public static RequestProperties make(String url) {
			URL u = new URL(url);
			RequestProperties p = new RequestProperties();
			p.hostname = u.hostname;
			p.port = u.port;
			p.path = u.pathname + u.search;
			return p;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class HttpRequest {
		public native void on(String event, EventCallback cb);
		public native void end();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class HttpResponse {
		public native void setEncoding(String enc);
		public native void on(String event, EventCallback cb);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_http.Agent_$$")
	public static class HttpAgent {
	}

	@JsFunction
	public interface RequestCallback {
		void run(HttpResponse error);
	}

	@JsFunction
	public interface EventCallback {
		void run(JsObject error);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class NodeURL {
		public native String format(NodeURLProperties prop);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class NodeURLProperties {
		public String pathname, protocol;
		public boolean slashes;
	}
}
