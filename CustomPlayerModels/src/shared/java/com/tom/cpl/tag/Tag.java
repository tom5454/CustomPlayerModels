package com.tom.cpl.tag;

import java.util.List;

public interface Tag<T> {
	boolean is(T stack);
	List<T> getAllStacks();
	String getId();
}
