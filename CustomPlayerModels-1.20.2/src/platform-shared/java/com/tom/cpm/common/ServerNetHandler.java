package com.tom.cpm.common;

import com.tom.cpm.common.ByteArrayPayload;

public interface ServerNetHandler {
	default void cpm$handleCustomPayload(ByteArrayPayload payload) {}
}
