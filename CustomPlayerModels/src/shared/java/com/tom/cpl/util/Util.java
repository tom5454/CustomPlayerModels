package com.tom.cpl.util;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Util {

	public static <T> List<T> listFromTree(Consumer<Consumer<T>> tree) {
		List<T> lst = new ArrayList<>();
		tree.accept(lst::add);
		return lst;
	}

	public static Map<String, Object> deepCopy(Map<String, Object> data) {
		Map<String, Object> map = new HashMap<>();
		for(Entry<String, Object> e : data.entrySet()) {
			map.put(e.getKey(), deepCopy0(e.getValue()));
		}
		return map;
	}

	public static List<Object> deepCopy(List<Object> data) {
		List<Object> list = new ArrayList<>();
		for (Object object : data) {
			list.add(deepCopy0(object));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static Object deepCopy0(Object data) {
		if(data instanceof Map)return deepCopy((Map<String, Object>) data);
		else if(data instanceof List)return deepCopy((List<Object>) data);
		else return data;
	}

	public static <T> Supplier<T> constructor(Class<T> clazz) {
		try {
			Constructor<T> c = clazz.getConstructor();
			c.setAccessible(true);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodHandle mh = lookup.unreflectConstructor(c);
			Method im = Supplier.class.getDeclaredMethods()[0];
			MethodType imt = MethodType.methodType(im.getReturnType(), im.getParameterTypes());
			return (Supplier<T>) LambdaMetafactory.metafactory(MethodHandles.lookup(), "get",
					MethodType.methodType(Supplier.class), imt, mh, mh.type()).getTarget().invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <A, T> Function<A, T> constructor(Class<T> clazz, Class<A> arg1) {
		try {
			Constructor<T> c = clazz.getConstructor(arg1);
			c.setAccessible(true);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodHandle mh = lookup.unreflectConstructor(c);
			return (Function<A, T>) LambdaMetafactory.metafactory(MethodHandles.lookup(), "apply",
					MethodType.methodType(Function.class),
					MethodType.methodType(Object.class, Object.class), mh, mh.type()).getTarget().invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
