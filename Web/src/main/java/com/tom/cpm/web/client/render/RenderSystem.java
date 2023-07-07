package com.tom.cpm.web.client.render;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.util.FormattedTextRenderer;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.retro.RetroGLAccess;
import com.tom.cpm.web.client.EventHandler;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.JSKeyCodes;
import com.tom.cpm.web.client.Stylesheet;
import com.tom.cpm.web.client.WebMC.Texture;
import com.tom.cpm.web.client.resources.Resources;
import com.tom.cpm.web.client.util.I18n;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.ExceptionUtil;
import com.tom.ugwt.client.UGWTContext;

import elemental2.core.Float32Array;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import elemental2.core.Uint16Array;
import elemental2.core.Uint8Array;
import elemental2.dom.BaseRenderingContext2D.FillStyleUnionType;
import elemental2.dom.CSSProperties.FontSizeUnionType;
import elemental2.dom.CSSProperties.MarginUnionType;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.DataTransferItem;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.EventTarget;
import elemental2.dom.EventTarget.AddEventListenerOptionsUnionType;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLStyleElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import elemental2.dom.WheelEvent;
import elemental2.dom.Window;
import elemental2.promise.Promise;
import elemental2.webgl.WebGLBuffer;
import elemental2.webgl.WebGLProgram;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLShader;
import elemental2.webgl.WebGLTexture;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@SuppressWarnings("unchecked")
public class RenderSystem implements RetroGLAccess<String> {
	private static HTMLCanvasElement canvas3d, canvasText;
	private static WebGLRenderingContext gl;
	private static CanvasRenderingContext2D txtCtx;
	public static Mat4f proj, mat2d, proj2d;
	private static boolean proj3d;
	private static int dynTexC = 0;
	private static WindowEx window;
	private static HTMLDocument document;
	public static int width, height, displayRatio = -1, sizeRequest = -1, maxScale, fontSize;
	private static EventHandler handler;
	private static boolean passEvent;
	private static double lastFrameTime;
	private static double frameRate;
	private static float mouseX, mouseY;
	public static VBuffers buffers;
	private static boolean loop;

	private static final float Z_NEAR = 1f;
	private static final float Z_FAR = 50f;
	private static final int FLOAT = (int) WebGLRenderingContext.FLOAT;
	private static final Uint8Array CLEAR = new Uint8Array(new double[] {0, 0, 255, 255});
	private static final Map<String, WebGLTexture> textures = new HashMap<>();
	private static List<Runnable> eventListeners = new ArrayList<>();
	public static Map<String, Image> preloadedAssets;
	private static Promise<Void> preloadComplete;
	private static float blitOffset = 0;
	private static double ticker;
	private static JsPropertyMap<Object> keyDown = JsPropertyMap.of();
	private static String lastTyped;
	private static WebGLProgram usingProgram;

	static {
		proj2d = new Mat4f();
		proj2d.setIdentity();
		ExceptionUtil.init();

		preloadComplete = Resources.loaded.then(__ -> {
			preloadedAssets = new HashMap<>();
			JsArray<Promise<Object>> promises = new JsArray<>();
			promises.push(I18n.loaded);
			for(String res : Resources.listResources()) {
				if(res.endsWith(".png")) {
					promises.push(ImageIO.loadImage(Resources.getResource(res), true, false).then(img -> {
						preloadedAssets.put(res, img);
						return null;
					}));
				}
			}
			return Promise.all(Js.cast(promises)).then(v -> null);
		});
	}

	public static void init(Window windowIn, Supplier<EventHandler> preload) {
		window = Js.uncheckedCast(windowIn);
		document = window.document;

		preloaded(() -> init0(windowIn, preload));
	}

	private static void init0(Window windowIn, Supplier<EventHandler> preload) {
		HTMLStyleElement style = Js.uncheckedCast(document.createElement("style"));
		style.innerHTML = Stylesheet.MAIN_SOURCE;
		document.head.appendChild(style);
		document.body.style.margin = MarginUnionType.of(0);
		document.body.style.overflow = "hidden";
		document.body.background = "#333333";

		canvas3d = Js.uncheckedCast(document.createElement("canvas"));
		canvasText = Js.uncheckedCast(document.createElement("canvas"));
		canvasText.style.top = "0";
		canvasText.style.left = "0";
		canvasText.style.position = "absolute";
		document.body.append(canvas3d);
		document.body.append(canvasText);
		WebGLConfig c = new WebGLConfig();
		c.premultipliedAlpha = false;
		gl = Js.uncheckedCast(canvas3d.getContext("webgl2", c));

		txtCtx = Js.uncheckedCast(canvasText.getContext("2d"));
		txtCtx.textAlign = "left";
		txtCtx.textBaseline = "top";
		txtCtx.imageSmoothingEnabled = false;
		txtCtx.globalCompositeOperation = "copy";
		txtCtx.font = "5px Minecraftia";
		lastFrameTime = 0;
		dynTexC = 0;

		for(Entry<String, Image> e : preloadedAssets.entrySet()) {
			WebGLTexture tex = gl.createTexture();
			textures.put(e.getKey(), tex);
			upload(e.getKey(), e.getValue());
		}
		builders.forEach(RenderStageBuilder::glInit);
		gl.lineWidth(4);

		document.fonts.load("5px Minecraftia").then(__ -> withContext(() -> {
			buffers = new VBuffers(RenderSystem::buffer);

			handler = preload.get();

			addEventListener(window, "resize", e -> resize(), true);
			addEventListener(window, "beforeunload", e -> {
				if(!handler.canClose()) {
					e.preventDefault();
					e.returnValue = "";
				}
			}, true);
			bindEventListeners(document, true);
			addEventListener(document.body, "drop", ev -> {
				ev.preventDefault();
				List<elemental2.dom.File> f = new ArrayList<>();
				MouseEvent e = Js.uncheckedCast(ev);
				if (e.dataTransfer.items != null) {
					for(int i = 0;i<e.dataTransfer.items.length;i++) {
						DataTransferItem dti = e.dataTransfer.items.getAt(i);
						if("file".equals(dti.kind))
							f.add(dti.getAsFile());
					}
				} else {
					for(int i = 0;i<e.dataTransfer.files.length;i++) {
						f.add(e.dataTransfer.files.getAt(i));
					}
				}
				Promise<JsArray<File>> p = Js.cast(Promise.all((Promise<File>[]) f.stream().map(FS::mount).toArray(Promise[]::new)));
				p.then(fs -> {
					List<File> files = fs.asList();
					withContext(() -> handler.filesDropped(files));
					return null;
				});
			}, true);
			addEventListener(document.body, "dragover", e -> {
				e.preventDefault();
			}, true);

			resize();

			loop = true;

			window.requestAnimationFrame(RenderSystem::draw);

			ticker = window.setInterval(() -> withContext(handler::tick), 50);

			return null;
		}));
	}

	public static void bindEventListeners(EventTarget l, boolean registerRemove) {
		addEventListener(l, "contextmenu", e -> {
			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "mousemove", evIn -> {
			passEvent = false;
			MouseEvent e = Js.uncheckedCast(evIn);
			DOMRect r = canvas3d.getBoundingClientRect();
			float sx = canvas3d.scrollWidth / (float) width;
			float sy = canvas3d.scrollHeight / (float) height;

			mouseX = (float) ((e.clientX - r.left) / sx);
			mouseY = (float) ((e.clientY - r.top) / sy);
			for (int i = 0;i<=2;i++) {
				if(Js.isTruthy(keyDown.getAsAny("mouse:" + i))) {
					if(!handler.mouseDragged((int) (mouseX / displayRatio), (int) (mouseY / displayRatio), mapMouseButton(i)))
						passEvent();
					break;
				}
			}
			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "mousedown", evIn -> {
			passEvent = false;
			MouseEvent e = Js.uncheckedCast(evIn);
			if(Js.isTruthy(keyDown.getAsAny("mouse:" + e.button)))
				return;
			DOMRect r = canvas3d.getBoundingClientRect();
			float sx = canvas3d.scrollWidth / (float) width;
			float sy = canvas3d.scrollHeight / (float) height;

			keyDown.set("mouse:" + e.button, true);

			mouseX = (float) ((e.clientX - r.left) / sx);
			mouseY = (float) ((e.clientY - r.top) / sy);
			if(!handler.mouseClicked((int) (mouseX / displayRatio), (int) (mouseY / displayRatio), mapMouseButton(e.button)))
				passEvent();

			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "mouseup", evIn -> {
			passEvent = false;
			MouseEvent e = Js.uncheckedCast(evIn);
			DOMRect r = canvas3d.getBoundingClientRect();
			float sx = canvas3d.scrollWidth / (float) width;
			float sy = canvas3d.scrollHeight / (float) height;

			keyDown.set("mouse:" + e.button, false);

			mouseX = (float) ((e.clientX - r.left) / sx);
			mouseY = (float) ((e.clientY - r.top) / sy);
			if(!handler.mouseReleased((int) (mouseX / displayRatio), (int) (mouseY / displayRatio), mapMouseButton(e.button)))
				passEvent();

			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "keydown", evIn -> {
			passEvent = false;
			KeyboardEvent e = Js.uncheckedCast(evIn);

			if(Js.isTruthy(keyDown.getAsAny(e.code)) && !e.key.equals("Tab"))
				return;

			keyDown.set(e.code, true);

			if(!handler.keyPressed(JSKeyCodes.codeHack(e.code), e.key))
				passEvent();

			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "keyup", evIn -> {
			passEvent = false;
			KeyboardEvent e = Js.uncheckedCast(evIn);
			keyDown.set(e.code, false);
			lastTyped = null;
		}, registerRemove);

		addEventListener(l, "keypress", evIn -> {
			passEvent = false;
			KeyboardEvent e = Js.uncheckedCast(evIn);

			if(Objects.equals(lastTyped, e.key))
				return;

			lastTyped = e.key;

			if(!handler.charTyped(e.key.charAt(0)))
				passEvent();

			if(!passEvent)e.preventDefault();
		}, registerRemove);

		addEventListener(l, "wheel", evIn -> {
			passEvent = false;
			WheelEvent e = Js.uncheckedCast(evIn);
			DOMRect r = canvas3d.getBoundingClientRect();
			float sx = canvas3d.scrollWidth / (float) width;
			float sy = canvas3d.scrollHeight / (float) height;

			mouseX = (float) ((e.clientX - r.left) / sx);
			mouseY = (float) ((e.clientY - r.top) / sy);
			if(!handler.mouseScrolled((int) (mouseX / displayRatio), (int) (mouseY / displayRatio), (int) (-e.deltaY / 100)))
				passEvent();

			if(!passEvent)e.preventDefault();
		}, registerRemove);
	}

	private static int mapMouseButton(int btn) {
		switch (btn) {
		case 1://Middle
			return 2;

		case 2://Right
			return 1;

		case 0://Left
		default:
			return 0;
		}
	}

	private static double lastEventTime;
	private static String lastEventType;
	private static void addEventListener(EventTarget l, String name, Consumer<EventEx> listener, boolean registerRemove) {
		EventListener ev = e -> {
			if(lastEventTime == e.timeStamp && name.equals(lastEventType))return;
			lastEventTime = e.timeStamp;
			lastEventType = name;
			withContext(() -> listener.accept(Js.uncheckedCast(e)));
		};
		if(registerRemove)
			eventListeners.add(() -> l.removeEventListener(name, ev));
		EventOptions opt = new EventOptions();
		opt.passive = false;
		l.addEventListener(name, ev, AddEventListenerOptionsUnionType.of(opt));
	}

	private static void draw(double time) {
		if(!loop)return;
		double now = window.performance.now();
		double time_since_last = now - lastFrameTime;
		double target_time_between_frames = 1000 / 30f;

		if(time_since_last >= target_time_between_frames - 5) {
			withContext(() -> {
				draw0();

				frameRate = 1000.0 / (now - lastFrameTime);
				lastFrameTime = now;
			});
		}

		window.requestAnimationFrame(RenderSystem::draw);
	}

	private static void draw0() {
		gl.viewport(0, 0, width, height);
		gl.clearColor(0, 0, 0, 0);
		gl.clear((int)WebGLRenderingContext.COLOR_BUFFER_BIT | (int)WebGLRenderingContext.DEPTH_BUFFER_BIT);
		proj = Mat4f.perspective(90, width / (float) height, Z_NEAR, Z_FAR);
		gl.disable((int) WebGLRenderingContext.DEPTH_TEST);
		txtCtx.clearRect(0, 0, width, height);
		txtCtx.save();

		handler.render((int) (mouseX / displayRatio), (int) (mouseY / displayRatio));
		usingProgram = null;

		txtCtx.restore();

		document.body.scrollTop = 0;
		document.documentElement.scrollTop = 0;
	}

	private static void resize() {
		width = window.innerWidth;
		height = window.innerHeight;
		canvas3d.width = width;
		canvas3d.height = height;
		canvasText.width = width;
		canvasText.height = height;

		int i;
		for(i = 1; i != 0 && i < width && i < height && width / (i + 1) >= 320 && height / (i + 1) >= 240; ++i);
		maxScale = i;
		if(sizeRequest < 1 || sizeRequest >= maxScale)
			displayRatio = Math.max(maxScale - 1, 1);
		else
			displayRatio = Math.max(sizeRequest, 1);

		setupFont();

		if(width != 0)handler.init(width / displayRatio, height / displayRatio);

		MatrixStack stack = new MatrixStack();

		stack.translate(-1, 1, -1f);

		stack.scale(1f / width * displayRatio * 2, -1f / height * displayRatio * 2, 0.1f);

		mat2d = stack.getLast().getMatrix();

		DomGlobal.console.log("Init complete. Scale: " + displayRatio + " " + maxScale + " " + width + "x" + height);
	}

	private static void setupFont() {
		fontSize = 5 * (displayRatio + 1);
		for (Element e : document.getElementsByClassName("textBox").asList()) {
			((HTMLElement)e).style.fontSize = FontSizeUnionType.of(fontSize);
		}

		txtCtx.font = fontSize + "px Minecraftia";
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "window")
	public static class WindowEx extends Window {
		public HTMLDocument document;

		public native int requestAnimationFrame(FrameRequestCallback callback);
		public native double setInterval(IntervalCallback callback, double timeout);
		public native void clearInterval(double timerId);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Event")
	public static class EventEx extends Event {
		public EventEx(String type) {
			super(type);
		}

		public String returnValue;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class WindowMessageEvent implements JsPropertyMap<String> {
		private String __uuid;

		private WindowMessageEvent() {
		}

		@JsOverlay
		protected final String getUUID() {
			return __uuid;
		}

		@JsOverlay
		public static WindowMessageEvent make(UUID uuid) {
			WindowMessageEvent wme = new WindowMessageEvent();
			wme.__uuid = uuid.toString();
			return wme;
		}

		@JsOverlay
		public final void send(Window target) {
			target.postMessage(Global.JSON.stringify(this), "*");
		}
	}

	@JsFunction
	public interface FrameRequestCallback {
		void onInvoke(double timestamp);
	}

	@JsFunction
	public interface IntervalCallback {
		void onInvoke();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class WebGLConfig {
		public boolean premultipliedAlpha;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class EventOptions {
		public boolean passive;
	}

	private static class RenderStageBuilder {
		private boolean color, texture, normal;
		private Runnable begin, end;
		private GlMode glMode;
		public int entrySize;

		private WebGLBuffer vertex;
		private WebGLBuffer index;
		private WebGLShader vertexShader;
		private WebGLShader fragmentShader;
		private WebGLProgram program;
		private WebGLUniformLocation projLoc, texLoc;

		private Float32Array vertexBuffer;
		private Uint16Array indexBuffer;
		private int pointerV, pointerI, pointer;
		private int pos, tex, col, norm;
		private int texO, colO, normO;
		private String shaderV, shaderF;

		public RenderStageBuilder(String shaderV, String shaderF, boolean color, boolean texture, boolean normal, Runnable begin, Runnable end, GlMode glMode) {
			this.color = color;
			this.texture = texture;
			this.normal = normal;
			this.begin = begin;
			this.end = end;
			this.glMode = glMode;
			entrySize = 3;
			if(color)entrySize += 4;
			if(texture)entrySize += 2;
			if(normal)entrySize += 3;
			this.shaderV = shaderV;
			this.shaderF = shaderF;
			builders.add(this);
		}

		public void glInit() {
			vertex = gl.createBuffer();
			index = gl.createBuffer();

			vertexShader = gl.createShader((int) WebGLRenderingContext.VERTEX_SHADER);

			fragmentShader = gl.createShader((int) WebGLRenderingContext.FRAGMENT_SHADER);

			gl.shaderSource(vertexShader, shaderV.trim());

			gl.shaderSource(fragmentShader, shaderF.trim());

			gl.compileShader(vertexShader);
			if(Js.isFalsy(gl.getShaderParameter(vertexShader, (int) WebGLRenderingContext.COMPILE_STATUS)))
				throw new RuntimeException(gl.getShaderInfoLog(vertexShader));

			gl.compileShader(fragmentShader);
			if(Js.isFalsy(gl.getShaderParameter(fragmentShader, (int) WebGLRenderingContext.COMPILE_STATUS)))
				throw new RuntimeException(gl.getShaderInfoLog(fragmentShader));

			program = gl.createProgram();

			gl.attachShader(program, vertexShader);
			gl.attachShader(program, fragmentShader);
			gl.linkProgram(program);
			gl.useProgram(program);

			projLoc = gl.getUniformLocation(program, "projectionMatrix");

			gl.bindBuffer((int) WebGLRenderingContext.ARRAY_BUFFER, vertex);

			gl.bindBuffer((int) WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, index);

			pos = gl.getAttribLocation(program, "position");
			gl.enableVertexAttribArray(pos);
			gl.vertexAttribPointer(pos, 3, FLOAT, false, entrySize*4, 0);

			int i = 3;

			if(texture) {
				tex = gl.getAttribLocation(program, "texture");
				gl.enableVertexAttribArray(tex);
				gl.vertexAttribPointer(tex, 2, FLOAT, false, entrySize * 4, i * 4);
				texO = i;
				i += 2;

				texLoc = gl.getUniformLocation(program, "uSampler");
			}

			if(color) {
				col = gl.getAttribLocation(program, "color");
				gl.enableVertexAttribArray(col);
				gl.vertexAttribPointer(col, 4, FLOAT, false, entrySize * 4, i * 4);
				colO = i;
				i += 4;
			}

			if(normal) {
				norm = gl.getAttribLocation(program, "normal");
				gl.enableVertexAttribArray(norm);
				gl.vertexAttribPointer(norm, 3, FLOAT, false, entrySize * 4, i * 4);
				normO = i;
				i += 3;
			}

			vertexBuffer = new Float32Array(1024);
			indexBuffer = new Uint16Array(256);
		}

		public RenderStage make(String img) {
			return new RenderStage(this, img);
		}

		public RenderStage make() {
			return new RenderStage(this, null);
		}

		public void pushVertex(double[] data) {
			if(vertexBuffer.length <= pointerV + data.length) {
				Float32Array n = new Float32Array(vertexBuffer.length * 2);
				n.set(vertexBuffer);
				vertexBuffer = n;
			}
			vertexBuffer.set(data, pointerV);
			pointerV += data.length;

			if(indexBuffer.length <= pointerI + glMode.indexes.length) {
				Uint16Array n = new Uint16Array(indexBuffer.length * 2);
				n.set(indexBuffer);
				indexBuffer = n;
			}

			if(pointer % glMode.verts == 0) {
				double[] ind = new double[glMode.indexes.length];
				int off = pointer - pointer % glMode.verts;
				for (int i = 0; i < glMode.indexes.length; i++) {
					ind[i] = glMode.indexes[i] + off;
				}
				indexBuffer.set(ind, pointerI);
				pointerI += glMode.indexes.length;
			}
			pointer++;
		}

		private void setupShader() {
			if(usingProgram != program) {
				gl.useProgram(program);
				gl.bindBuffer((int) WebGLRenderingContext.ARRAY_BUFFER, vertex);
				gl.enableVertexAttribArray(pos);
				gl.vertexAttribPointer(pos, 3, FLOAT, false, entrySize*4, 0);
				if(texture) {
					gl.enableVertexAttribArray(tex);
					gl.vertexAttribPointer(tex, 2, FLOAT, false, entrySize * 4, texO * 4);
				}
				if(color) {
					gl.enableVertexAttribArray(col);
					gl.vertexAttribPointer(col, 4, FLOAT, false, entrySize * 4, colO * 4);
				}
				if(normal) {
					gl.enableVertexAttribArray(norm);
					gl.vertexAttribPointer(norm, 3, FLOAT, false, entrySize * 4, normO * 4);
				}
				usingProgram = program;
			}
		}

		public void init() {
			pointerI = 0;
			pointerV = 0;
			pointer = 0;

			setupShader();
			if(proj3d)
				gl.uniformMatrix4fv(projLoc, false, new Float32Array(proj.toArrayD()));
			else
				gl.uniformMatrix4fv(projLoc, false, new Float32Array(proj2d.toArrayD()));
		}

		public void draw() {
			gl.bufferData((int) WebGLRenderingContext.ARRAY_BUFFER, vertexBuffer, (int) WebGLRenderingContext.DYNAMIC_DRAW);

			gl.bindBuffer((int) WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, index);
			gl.bufferData((int) WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer, (int) WebGLRenderingContext.STATIC_DRAW);

			gl.drawElements(glMode.mode, pointerI, (int) WebGLRenderingContext.UNSIGNED_SHORT, 0);
		}
	}

	public static void bindTex(RenderStageBuilder data, String img) {
		WebGLTexture tex = textures.get(img);
		if(tex != null) {
			gl.activeTexture((int) WebGLRenderingContext.TEXTURE0);
			gl.bindTexture((int) WebGLRenderingContext.TEXTURE_2D, tex);
			gl.uniform1i(data.texLoc, 0);
		} else {
			System.out.println("texture not found: " + img);
		}
	}

	private static class RenderStage implements RetroLayer {
		private RenderStageBuilder data;
		private String tex;

		private RenderStage(RenderStageBuilder builder, String tex) {
			this.data = builder;
			this.tex = tex;
		}

		public void begin() {
			data.init();
			data.begin.run();
			if(tex != null && data.texture)bindTex(data, tex);
		}

		public void end() {
			data.end.run();
		}
	}

	public static class VBuffer extends DirectBuffer<RenderStage> {
		private double[] data;

		public VBuffer(RenderStage stage) {
			super(stage);
			stage.begin();
			data = new double[stage.data.entrySize];
		}

		@Override
		protected void pushVertex(float x, float y, float z, float red, float green,
				float blue, float alpha, float u, float v, float nx, float ny, float nz) {
			int i = 0;
			data[i++] = x;
			data[i++] = y;
			data[i++] = z;
			if(buffer.data.texture) {
				data[i++] = u;
				data[i++] = v;
			}
			if(buffer.data.color) {
				data[i++] = red;
				data[i++] = green;
				data[i++] = blue;
				data[i++] = alpha;
			}
			if(buffer.data.normal) {
				data[i++] = nx;
				data[i++] = ny;
				data[i++] = nz;
			}
			buffer.data.pushVertex(data);
		}

		@Override
		public void finish() {
			buffer.data.draw();
			buffer.end();
		}
	}

	private static final GlMode LINES = new GlMode(WebGLRenderingContext.LINES, 2, 0, 1);
	private static final GlMode QUADS = new GlMode(WebGLRenderingContext.TRIANGLES, 4, 0, 2, 3, 0, 2, 1);
	private static final List<RenderStageBuilder> builders = new ArrayList<>();

	private static final RenderStage lines = new RenderStageBuilder(
			//vertex
			"#version 300 es\n"
			+ "\n"
			+ "in vec3 position;\n"
			+ "in vec4 color;\n"
			+ "\n"
			+ "out vec4 thecolor;\n"
			+ "\n"
			+ "uniform mat4 projectionMatrix;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    gl_Position = projectionMatrix * vec4(position, 1.0);\n"
			+ "    thecolor = color;\n"
			+ "}",
			//fragment
			"#version 300 es\n"
			+ "precision mediump float;\n"
			+ "\n"
			+ "in vec4 thecolor;\n"
			+ "\n"
			+ "out vec4 color;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    color = thecolor;\n"
			+ "}",
			//properties
			true, false, false,
			//before
			() -> {
				gl.disable((int) WebGLRenderingContext.DEPTH_TEST);
			},
			//after
			() -> {
				gl.enable((int) WebGLRenderingContext.DEPTH_TEST);
			},
			//mode
			LINES).make();

	private static final RenderStage color = new RenderStageBuilder(
			//vertex
			"#version 300 es\n"
			+ "\n"
			+ "in vec3 position;\n"
			+ "in vec4 color;\n"
			+ "\n"
			+ "out vec4 thecolor;\n"
			+ "\n"
			+ "uniform mat4 projectionMatrix;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    gl_Position = projectionMatrix * vec4(position, 1.0);\n"
			+ "    thecolor = color;\n"
			+ "}",
			//fragment
			"#version 300 es\n"
			+ "precision mediump float;\n"
			+ "\n"
			+ "in vec4 thecolor;\n"
			+ "\n"
			+ "out vec4 color;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    color = thecolor;\n"
			+ "}",
			//properties
			true, false, false,
			//before
			() -> {
				gl.enable((int) WebGLRenderingContext.BLEND);
				gl.blendFunc((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
				//gl.blendFuncSeparate((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA, (int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
			},
			//after
			() -> {
				gl.disable((int) WebGLRenderingContext.BLEND);
				gl.blendFuncSeparate((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA, (int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ZERO);
			},
			//mode
			QUADS).make();

	private static final RenderStageBuilder texture = new RenderStageBuilder(
			//vertex
			"#version 300 es\n"
			+ "\n"
			+ "in vec3 position;\n"
			+ "in vec4 color;\n"
			+ "in highp vec2 texture;\n"
			+ "in vec3 normal;\n"
			+ "\n"
			+ "out vec4 thecolor;\n"
			+ "out highp vec2 texuv;\n"
			+ "out vec3 normOut;\n"
			+ "\n"
			+ "uniform mat4 projectionMatrix;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    gl_Position = projectionMatrix * vec4(position, 1.0);\n"
			+ "    thecolor = color;\n"
			+ "    texuv = texture;\n"
			+ "    normOut = normal;\n"
			+ "}",
			//fragment
			"#version 300 es\n"
			+ "precision mediump float;\n"
			+ "\n"
			+ "in vec4 thecolor;\n"
			+ "in highp vec2 texuv;\n"
			+ "\n"
			+ "out vec4 color;\n"
			+ "\n"
			+ "uniform sampler2D uSampler;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    color = thecolor * texture(uSampler, texuv);\n"
			+ "    if(color.a < 0.1)discard;\n"
			+ "}",
			//properties
			true, true, true,
			//before
			() -> {
				gl.enable((int) WebGLRenderingContext.BLEND);
				gl.blendFunc((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
				//gl.blendFuncSeparate((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA, (int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
			},
			//after
			() -> {
				gl.disable((int) WebGLRenderingContext.BLEND);
				gl.blendFuncSeparate((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA, (int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ZERO);
			},
			//mode
			QUADS);

	private static final RenderStageBuilder eyes = new RenderStageBuilder(
			//vertex
			"#version 300 es\n"
			+ "\n"
			+ "in vec3 position;\n"
			+ "in vec4 color;\n"
			+ "in vec2 texture;\n"
			+ "in vec3 normal;\n"
			+ "\n"
			+ "out vec4 thecolor;\n"
			+ "out vec2 texuv;\n"
			+ "out vec3 normOut;\n"
			+ "\n"
			+ "uniform mat4 projectionMatrix;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    gl_Position = projectionMatrix * vec4(position, 1.0);\n"
			+ "    thecolor = color;\n"
			+ "    texuv = texture;\n"
			+ "    normOut = normal;\n"
			+ "}",
			//fragment
			"#version 300 es\n"
			+ "precision mediump float;\n"
			+ "\n"
			+ "in vec4 thecolor;\n"
			+ "in vec2 texuv;\n"
			+ "\n"
			+ "out vec4 color;\n"
			+ "\n"
			+ "uniform sampler2D uSampler;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    color = thecolor * texture(uSampler, texuv);\n"
			+ "}",
			//properties
			true, true, true,
			//before
			() -> {
				gl.enable((int) WebGLRenderingContext.BLEND);
				gl.blendFunc((int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ONE);
				//gl.depthMask(true);
			},
			//after
			() -> {
				gl.disable((int) WebGLRenderingContext.BLEND);
				gl.blendFuncSeparate((int) WebGLRenderingContext.SRC_ALPHA, (int) WebGLRenderingContext.ONE_MINUS_SRC_ALPHA, (int) WebGLRenderingContext.ONE, (int) WebGLRenderingContext.ZERO);
			},
			//mode
			QUADS);

	@Override
	public RenderStage texture(String tex) {
		return texture.make(tex);
	}

	@Override
	public RetroLayer linesNoDepth() {
		return lines;
	}

	@Override
	public RetroLayer eyes(String tex) {
		return eyes.make(tex);
	}

	@Override
	public RetroLayer color() {
		return color;
	}

	@Override
	public String getDynTexture() {
		return Texture.bound.getId();
	}

	public static class GlMode {
		private int mode;
		private int verts;
		private int[] indexes;

		public GlMode(double mode, int verts, int... indexes) {
			this.mode = (int) mode;
			this.verts = verts;
			this.indexes = indexes;
		}
	}

	public static VertexBuffer buffer(NativeRenderType type) {
		RenderStage stage = type.getNativeType();
		return new VBuffer(stage);
	}

	public static String newTexture() {
		String id = "dyn_" + (dynTexC++);
		textures.put(id, gl.createTexture());
		return id;
	}

	public static void upload(String id, Image img) {
		WebGLTexture tex = textures.get(id);
		if(tex != null) {
			gl.bindTexture((int) WebGLRenderingContext.TEXTURE_2D, tex);
			gl.texImage2D((int) WebGLRenderingContext.TEXTURE_2D, 0, (int) WebGLRenderingContext.RGBA,
					1, 1, 0, (int) WebGLRenderingContext.RGBA, (int) WebGLRenderingContext.UNSIGNED_BYTE,
					CLEAR);

			gl.texImage2D((int) WebGLRenderingContext.TEXTURE_2D, 0, (int) WebGLRenderingContext.RGBA, img.getWidth(), img.getHeight(), 0,
					(int) WebGLRenderingContext.RGBA, (int) WebGLRenderingContext.UNSIGNED_BYTE, new Uint8Array(img.getData().buffer));

			gl.texParameteri((int) WebGLRenderingContext.TEXTURE_2D, (int) WebGLRenderingContext.TEXTURE_WRAP_S, (int) WebGLRenderingContext.REPEAT);
			gl.texParameteri((int) WebGLRenderingContext.TEXTURE_2D, (int) WebGLRenderingContext.TEXTURE_WRAP_T, (int) WebGLRenderingContext.REPEAT);
			gl.texParameteri((int) WebGLRenderingContext.TEXTURE_2D, (int) WebGLRenderingContext.TEXTURE_MIN_FILTER, (int) WebGLRenderingContext.NEAREST);
			gl.texParameteri((int) WebGLRenderingContext.TEXTURE_2D, (int) WebGLRenderingContext.TEXTURE_MAG_FILTER, (int) WebGLRenderingContext.NEAREST);
		}
	}

	public static void freeTexture(String id) {
		WebGLTexture tex = textures.get(id);
		if(tex != null)gl.deleteTexture(tex);
	}

	public static VertexBuffer2d getColor() {
		return new VB2d(buffers.getBuffer(new NativeRenderType(RenderSystem.color, 0)));
	}

	public static VertexBuffer2d getTexture(String texture) {
		return new VB2d(buffers.getBuffer(new NativeRenderType(RenderSystem.texture.make(texture), 0)));
	}

	public static VertexBuffer2d getTextureDyn() {
		return new VB2d(buffers.getBuffer(new NativeRenderType(RenderSystem.texture.make(Texture.bound.getId()), 0)));
	}

	public static interface VertexBuffer2d {
		VertexBuffer2d pos(float x, float y);
		VertexBuffer2d tex(float u, float v);
		VertexBuffer2d color(float red, float green, float blue, float alpha);
		void endVertex();
		void finish();
	}

	private static class VB2d implements VertexBuffer2d {
		private final VertexBuffer buf;

		public VB2d(VertexBuffer buf) {
			this.buf = buf;
		}

		@Override
		public VertexBuffer2d pos(float x, float y) {
			buf.pos(mat2d, x, y, blitOffset).normal(0, 0, 1);
			return this;
		}

		@Override
		public VertexBuffer2d tex(float u, float v) {
			buf.tex(u, v);
			return this;
		}

		@Override
		public VertexBuffer2d color(float red, float green, float blue, float alpha) {
			buf.color(red, green, blue, alpha);
			return this;
		}

		@Override
		public void endVertex() {
			buf.endVertex();
		}

		@Override
		public void finish() {
			buf.finish();
		}
	}

	public static void text(String text, float x, float y, int color) {
		txtCtx.textAlign = "left";
		txtCtx.textBaseline = "top";
		String c = new JsString(Integer.toString(color & 0xffffff, 16)).padStart(6, "0");
		txtCtx.fillStyle = FillStyleUnionType.of("#" + c);
		txtCtx.fillText(text, x * displayRatio, y * displayRatio);
	}

	public static void textFormatted(IGui gui, IText text, float x, float y, int color, float scale) {
		txtCtx.textAlign = "left";
		txtCtx.textBaseline = "top";
		String c = new JsString(Integer.toString(color & 0xffffff, 16)).padStart(6, "0");
		txtCtx.fillStyle = FillStyleUnionType.of("#" + c);
		String font = (fontSize * scale) + "px Minecraftia";
		new FormattedTextRenderer(gui, text) {

			@Override
			public void processText(int xp, int w, String content, TextStyle style) {
				float ax = x + xp * scale;
				txtCtx.fillText(content, ax * displayRatio, y * displayRatio);
				if(style.underline) {
					txtCtx.fillRect(ax * displayRatio, (y + 7) * displayRatio, w * scale * displayRatio, displayRatio);
				}
				if(style.strikethrough) {
					txtCtx.fillRect(ax * displayRatio, (y + 3) * displayRatio, w * scale * displayRatio, displayRatio);
				}
			}

			@Override
			public int width(String content, TextStyle style) {
				String f = font;
				if(style.italic)f = "italic " + f;
				if(style.bold)f = "bold " + f;
				if(txtCtx.font != f)txtCtx.font = f;
				return (int) (getTextWidth(content) / scale);
			}

		}.render();
		txtCtx.font = fontSize + "px Minecraftia";
	}

	public static int getTextWidthFormatted(IGui gui, IText text) {
		String font = fontSize + "px Minecraftia";
		int w = new FormattedTextRenderer(gui, text) {

			@Override
			public void processText(int xp, int w, String content, TextStyle style) {}

			@Override
			public int width(String content, TextStyle style) {
				String f = font;
				if(style.italic)f = "italic " + f;
				if(style.bold)f = "bold " + f;
				if(txtCtx.font != f)txtCtx.font = f;
				return getTextWidth(content);
			}

		}.width();
		txtCtx.font = fontSize + "px Minecraftia";
		return w;
	}

	public static void boxDrawn(float x, float y, float w, float h) {
		txtCtx.clearRect(x * displayRatio, y * displayRatio, w * displayRatio, h * displayRatio);
	}

	public static int getTextWidth(String text) {
		return (int) txtCtx.measureText(text).width / displayRatio;
	}

	public static float getFPS() {
		return (float) frameRate;
	}

	public static void remove() {
		canvas3d.remove();
		canvasText.remove();
		loop = false;
		window.clearInterval(ticker);
		eventListeners.forEach(Runnable::run);
		eventListeners.clear();
		usingProgram = null;
	}

	public static void scissor(int x, int y, int w, int h) {
		gl.scissor(x * displayRatio, height - (y + h) * displayRatio, w * displayRatio, h * displayRatio);
		txtCtx.restore();
		txtCtx.save();
		txtCtx.beginPath();
		txtCtx.rect(x * displayRatio, y * displayRatio, w * displayRatio, h * displayRatio);
		txtCtx.clip();
	}

	public static void setScale(int scale) {
		sizeRequest = scale;
		resize();
	}

	public static void passEvent() {
		passEvent = true;
	}

	public static void setProj3d(boolean proj3d) {
		RenderSystem.proj3d = proj3d;
		if(proj3d) {
			gl.enable((int) WebGLRenderingContext.DEPTH_TEST);
			gl.depthFunc((int) WebGLRenderingContext.LEQUAL);
		} else {
			gl.disable((int) WebGLRenderingContext.DEPTH_TEST);
		}
	}

	public static void enableScissor() {
		gl.enable((int) WebGLRenderingContext.SCISSOR_TEST);
	}

	public static void disableScissor() {
		gl.disable((int) WebGLRenderingContext.SCISSOR_TEST);
	}

	public static boolean isKeyDown(int key) {
		return Js.isTruthy(keyDown.getAsAny(JSKeyCodes.codeHack(key)));
	}

	public static HTMLDocument getDocument() {
		return document;
	}

	public static void preloaded(Runnable r) {
		preloadComplete.then(__ -> {
			if(window != null)
				withContext(r);
			else
				r.run();
			return null;
		});
	}

	public static WindowEx getWindow() {
		return window;
	}

	private static boolean setCtx;
	public static void withContext(Runnable r) {
		if(setCtx) {
			r.run();
		} else {
			try {
				UGWTContext.setContext(window);
				setCtx = true;
				r.run();
			} finally {
				UGWTContext.resetContext();
				setCtx = false;
			}
		}
	}

	public static <R> R withContext(Supplier<R> r) {
		if(setCtx) {
			return r.get();
		} else {
			try {
				UGWTContext.setContext(window);
				setCtx = true;
				return r.get();
			} finally {
				UGWTContext.resetContext();
				setCtx = false;
			}
		}
	}

	public static Image screenshot(Box box) {
		int width = displayRatio * box.w;
		int height = displayRatio * box.h;
		return withContext(() -> {
			draw0();
			Uint8Array buffer = new Uint8Array(width * height * 4);
			gl.readPixels(displayRatio * box.x, RenderSystem.height - height - displayRatio * box.y, width, height, (int) WebGLRenderingContext.RGBA, (int) WebGLRenderingContext.UNSIGNED_BYTE, buffer);
			Image img = new Image(width, height);
			for(int y = 0;y<height;y++) {
				for(int x = 0;x<width;x++) {
					Double r = buffer.getAt((x + y * width) * 4);
					Double g = buffer.getAt((x + y * width) * 4 + 1);
					Double b = buffer.getAt((x + y * width) * 4 + 2);
					int color = 0xff000000 | (r.intValue() << 16) | (g.intValue() << 8) | b.intValue();
					img.setRGB(x, height - y - 1, color);
				}
			}
			return img;
		});
	}

	public static void renderCanvas(HTMLCanvasElement c, int x, int y, int w, int h) {
		txtCtx.drawImage(c, x * displayRatio, y * displayRatio, w * displayRatio, h * displayRatio);
	}
}
