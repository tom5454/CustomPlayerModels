package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.web.client.resources.Resources;
import com.tom.cpm.web.client.util.I18n;

import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
public class VueComponent {
	public String template;
	public JsPropertyMap<VueCallback<?>> methods;
	public JsPropertyMap<VueComponent> components;

	@JsProperty(name = "_dataConst")
	public JsPropertyMap<String> data;

	@JsProperty(name = "data")
	public VueDataGet dataGet;

	public JsPropertyMap<HTMLElement> $refs;
	public VueCallback<VueComponent> mounted;

	@JsFunction
	public static interface VueCallback<P> {
		Object call(P param);
	}

	@JsFunction
	public static interface VueDataGet {
		JsPropertyMap<String> get();
	}

	@JsOverlay
	public static VueComponent create(String name) {
		VueComponent comp = new VueComponent();
		comp.methods = Js.uncheckedCast(new JsObject());
		comp.data = Js.uncheckedCast(new JsObject());
		comp.dataGet = () -> comp.data;
		comp.template = DomGlobal.atob(Resources.getResource("assets/cpmblockbench/vue/" + name + ".html"));
		comp.methods.set("i18nGet", (String k) -> I18n.get(k));
		comp.methods.set("tl", (String k) -> Global.translate(k));
		return comp;
	}

	@JsOverlay
	public final void setMethod(String key, Runnable handler) {
		methods.set(key, __ -> {
			handler.run();
			return null;
		});
	}
}
