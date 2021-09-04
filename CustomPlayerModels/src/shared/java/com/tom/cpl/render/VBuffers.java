package com.tom.cpl.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tom.cpl.math.MatrixStack;

public class VBuffers {
	private Function<NativeRenderType, VertexBuffer> bufferFactory;
	private Map<NativeRenderType, VertexBuffer> buffers = new HashMap<>();
	private VertexBuffer normalBuffer;

	public VBuffers(Function<NativeRenderType, VertexBuffer> bufferFactory, VertexBuffer normalBuffer) {
		this.bufferFactory = bufferFactory;
		this.normalBuffer = normalBuffer;
	}

	public VBuffers(Function<NativeRenderType, VertexBuffer> bufferFactory) {
		this.bufferFactory = bufferFactory;
	}

	public VertexBuffer getBuffer(NativeRenderType type) {
		if(type.nativeType == null && normalBuffer != null) {
			buffers.put(type, normalBuffer);
			return new VBuf(normalBuffer, this, type);
		}
		return new VBuf(buffers.computeIfAbsent(type, bufferFactory), this, type);
	}

	public <E extends Enum<E>> VertexBuffer getBuffer(RenderTypes<E> types, E type) {
		return getBuffer(types.get(type));
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

		public NativeRenderType(int layer) {
			this.nativeType = null;
			this.layer = layer;
		}

		@SuppressWarnings("unchecked")
		public <RT> RT getNativeType() {
			return (RT) nativeType;
		}
	}

	public VBuffers replay() {
		return new VBuffers(rt -> new ReplayBuffer(() -> getBuffer(rt)), normalBuffer);
	}

	public VBuffers transform(MatrixStack stack) {
		return wrapped(b -> new TransformedBuffer(b, stack));
	}

	public VBuffers wrapped(UnaryOperator<VertexBuffer> func) {
		return new VBuffers(rt -> func.apply(getBuffer(rt)), normalBuffer != null ? func.apply(normalBuffer) : null);
	}

	public VBuffers finish(Consumer<VertexBuffer> finisher) {
		return wrapped(b -> new FinishBuffer(b, finisher));
	}

	public VBuffers normal(VertexBuffer normal) {
		return new VBuffers(this::getBuffer, normal);
	}

	private static class FinishBuffer extends WrappedBuffer {
		private final Consumer<VertexBuffer> finisher;

		public FinishBuffer(VertexBuffer buffer, Consumer<VertexBuffer> finisher) {
			super(buffer);
			this.finisher = finisher;
		}

		@Override
		public void finish() {
			finisher.accept(buffer);
		}
	}

	private static class VBuf extends WrappedBuffer {
		private final VBuffers bufs;
		private final NativeRenderType rt;

		public VBuf(VertexBuffer buffer, VBuffers bufs, NativeRenderType rt) {
			super(buffer);
			this.bufs = bufs;
			this.rt = rt;
		}

		@Override
		public void finish() {
			bufs.finish(rt);
		}
	}
}
