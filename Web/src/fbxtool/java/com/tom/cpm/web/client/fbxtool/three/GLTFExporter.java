package com.tom.cpm.web.client.fbxtool.three;

import elemental2.core.Global;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "GLTFExporter")
public class GLTFExporter {
	public native void parse(Scene scene, CallbackExport cb, GLTFExporterExportInit prop);

	@JsFunction
	public interface CallbackExport {
		void finishExport(Object gltf);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class GLTFExporterExportInit {
		public boolean binary;
	}

	@JsOverlay
	public final Promise<String> export(Scene sc) {
		return new Promise<>((res, rej) -> {
			GLTFExporterExportInit i = new GLTFExporterExportInit();
			i.binary = false;
			parse(sc, gltf -> res.onInvoke(Global.JSON.stringify(gltf)), i);
		});
	}
}
