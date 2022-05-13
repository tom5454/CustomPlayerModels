package com.tom.cpl.render;

public class OptionalBuffer extends ListBuffer implements Runnable {
	protected VertexBuffer parent;

	public OptionalBuffer(VertexBuffer parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		draw(parent);
	}
}
