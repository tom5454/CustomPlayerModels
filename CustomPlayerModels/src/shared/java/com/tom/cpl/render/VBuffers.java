package com.tom.cpl.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VBuffers {
	private Function<NativeRenderType, VertexBuffer> bufferFactory;
	private Map<NativeRenderType, VertexBuffer> buffers = new HashMap<>();

	public VBuffers(Function<NativeRenderType, VertexBuffer> bufferFactory) {
		this.bufferFactory = bufferFactory;
	}

	public VertexBuffer getBuffer(NativeRenderType type) {
		return buffers.computeIfAbsent(type, bufferFactory);
	}

	public <E extends Enum<E>> VertexBuffer getBuffer(RenderTypes<E> types, E type) {
		return buffers.computeIfAbsent(types.get(type), bufferFactory);
	}

	public void finish(NativeRenderType type) {
		VertexBuffer buf = buffers.remove(type);
		if(buf != null) {
			buf.finish();
		}
	}

	public void finishAll() {
		List<NativeRenderType> rts = new ArrayList<>(buffers.keySet());
		rts.sort((a, b) -> Integer.compare(a.layer, b.layer));
		rts.forEach(rt -> buffers.get(rt).finish());
		buffers.clear();
	}

	public static class NativeRenderType {
		private final Object nativeType;
		private int layer;

		public NativeRenderType(Object nativeType, int layer) {
			this.nativeType = nativeType;
			this.layer = layer;
		}

		@SuppressWarnings("unchecked")
		public <RT> RT getNativeType() {
			return (RT) nativeType;
		}
	}

	public VBuffers replay() {
		return new VBuffers(rt -> new ReplayBuffer<>(() -> bufferFactory.apply(rt)));
	}
}
