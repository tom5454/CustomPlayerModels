package com.tom.cpm.web.client.util;

import java.util.Map;

import com.tom.cpm.shared.MinecraftObjectHolder;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.dom.Response;
import elemental2.promise.Promise;

public class CPMApi {

	@SuppressWarnings("unchecked")
	public static Promise<Map<String, Object>> fetch(String api, String value) {
		return DomGlobal.fetch(System.getProperty("cpm.webApiEndpoint") + "/" + api + "?v=" + Global.encodeURIComponent(value)).then(Response::text).then(r -> {
			Map<String, Object> data = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(r, Object.class);
			if(data.containsKey("error")) {
				String err = (String) data.get("error");
				return Promise.reject(err);
			}
			return Promise.resolve(data);
		});
	}

}
