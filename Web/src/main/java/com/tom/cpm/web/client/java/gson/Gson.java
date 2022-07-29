package com.tom.cpm.web.client.java.gson;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpm.web.client.java.FakeReader;
import com.tom.cpm.web.client.java.JsBuilder;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;
import jsinterop.base.JsPropertyMap;

public class Gson {
	private boolean pretty;

	public Gson() {
	}

	public Gson(boolean pretty) {
		this.pretty = pretty;
	}

	public void toJson(Object src, Appendable writer) {
		try {
			writer.append(toJson(src));
		} catch (IOException e) {
		}
	}

	public String toJson(Object src) {
		JsObject o = Js.uncheckedCast(toJson0(src));
		if(pretty)
			return Global.JSON.stringify(o, (String[]) Js.undefined(), "  ");
		else
			return Global.JSON.stringify(o);
	}

	@SuppressWarnings("unchecked")
	private Object toJson0(Object obj) {
		if(obj instanceof Map) {
			JsBuilder<Object> b = new JsBuilder<>();
			for (Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
				b.put(e.getKey(), toJson0(e.getValue()));
			}
			return b.build();
		} else if(obj instanceof List) {
			JsArray<Object> array = new JsArray<>();
			List<Object> l = (List<Object>) obj;
			for (int i = 0; i < l.size(); i++) {
				array.setAt(i, toJson0(l.get(i)));
			}
			return array;
		} else if (obj instanceof Number) {
			return Js.asAny(((Number)obj).doubleValue());
		} else if (obj instanceof String) {
			return Js.asAny(obj);
		} else if (obj instanceof Boolean) {
			return Js.asAny(obj);
		} else {
			throw new RuntimeException("Unknown type");
		}
	}

	public Object fromJson(Reader json, Class<Object> classOfT) {
		if(json instanceof FakeReader) {
			return fromJson(((FakeReader)json).getContent(), classOfT);
		}
		return null;
	}

	public Object fromJson(String json, Class<Object> classOfT) {
		return fromJson(Global.JSON.parse(json));
	}

	private Object fromJson(Object obj) {
		if(JsArray.isArray(obj)) {
			JsArrayLike<Object> array = Js.asArrayLike(obj);
			int l = array.getLength();
			List<Object> list = new ArrayList<>();
			for(int i = 0;i<l;i++) {
				list.add(fromJson(array.getAt(i)));
			}
			return list;
		} else if(Js.typeof(obj).equals("object")) {
			Map<String, Object> map = new HashMap<>();
			JsPropertyMap<Object> mapIn = Js.uncheckedCast(obj);
			for(String key : JsObject.keys(obj).asList()) {
				map.put(key, fromJson(mapIn.get(key)));
			}
			return map;
		} else if(Js.typeof(obj).equals("number")) {
			return Double.valueOf(Js.asDouble(obj));
		} else if(Js.typeof(obj).equals("string")) {
			return Js.asString(obj);
		} else if(Js.typeof(obj).equals("boolean")) {
			return Js.asBoolean(obj);
		} else {
			DomGlobal.console.error("Unknown object", obj);
			throw new IllegalArgumentException();
		}
	}
}
