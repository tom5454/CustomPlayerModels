package com.tom.cpm.common;

public interface ServerNetHandler {
	default void cpm$handleCustomPayload(ByteArrayPayload payload) {}
}
