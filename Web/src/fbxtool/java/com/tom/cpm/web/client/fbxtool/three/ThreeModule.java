package com.tom.cpm.web.client.fbxtool.three;

import com.tom.cpm.web.client.fbxtool.three.MeshBasicMaterial.MeshBasicMaterialInit;
import com.tom.ugwt.client.GlobalFunc;

import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLScriptElement;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class ThreeModule {
	public static boolean isLoaded;
	private static Promise<Object> loaded;

	public static Scene scene;
	public static PerspectiveCamera camera;
	public static WebGLRenderer renderer;
	public static OrbitControls controls;

	public static Promise<Object> prepare() {
		if(loaded == null) {
			HTMLScriptElement three = (HTMLScriptElement) DomGlobal.document.createElement("script");
			three.id = "threejs";
			three.src = "https://unpkg.com/es-module-shims@1.6.3/dist/es-module-shims.js";
			three.async = true;
			DomGlobal.document.body.append(three);

			three = (HTMLScriptElement) DomGlobal.document.createElement("script");
			three.id = "threejs_map";
			three.type = "importmap";
			three.innerHTML = "{\r\n"
					+ "    \"imports\": {\r\n"
					+ "      \"three\": \"https://unpkg.com/three@0.153.0/build/three.module.js\",\r\n"
					+ "      \"three/addons/\": \"https://unpkg.com/three@0.153.0/examples/jsm/\"\r\n"
					+ "    }\r\n"
					+ "  }";
			DomGlobal.document.body.append(three);

			loaded = new Promise<>((res, rej) -> {
				GlobalFunc gf = GlobalFunc.pushGlobalFunc(ResolveCallbackFn.class, res);
				HTMLScriptElement threew = (HTMLScriptElement) DomGlobal.document.createElement("script");
				threew.id = "three_module_wrapper";
				threew.type = "module";
				threew.innerHTML = "import * as THREE from 'three';"
						+ "import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';"
						+ "import { OrbitControls } from 'three/addons/controls/OrbitControls.js';"
						+ "import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';"
						+ gf + "({THREE, OrbitControls, GLTFLoader, GLTFExporter});";
				DomGlobal.document.body.append(threew);
			}).then(v -> {
				JsPropertyMap<Object> window = Js.uncheckedCast(DomGlobal.window);
				JsPropertyMap<Object> proxy = Js.uncheckedCast(v);
				for (String e : JsObject.keys(v).asList()) {
					window.set(e, proxy.get(e));
				}
				scene = new Scene();
				camera = new PerspectiveCamera(75, DomGlobal.window.innerWidth / DomGlobal.window.innerHeight, 0.1F, 1000);
				camera.position.set(-0.5F, 8F, -30F);
				camera.rotation.set(-2.7199591066827757F, 0.012409033235898375F, 3.1360269892890926F, "XYZ");
				renderer = new WebGLRenderer();
				renderer.domElement.style.position = "absolute";
				controls = new OrbitControls(camera, renderer.domElement);
				DomGlobal.document.body.appendChild(renderer.domElement);

				BoxGeometry geometry = new BoxGeometry( 1, 1, 1 );
				MeshBasicMaterialInit mbmi = new MeshBasicMaterialInit();
				mbmi.color = 0x00ff00;
				MeshBasicMaterial material = new MeshBasicMaterial(mbmi);
				Mesh cube = new Mesh(geometry, material);
				scene.add(cube);

				isLoaded = true;
				return null;
			});
		}
		return loaded;
	}

	public static void render() {
		if(!isLoaded)return;
		renderer.render(scene, camera);
	}

	public static void clearScene() {
		scene.children.length = 0;
	}
}
