package com.tom.cpm.shared.model.builtin;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.model.render.BoxRender;

public class SimplePartRenderer extends VanillaPartRenderer {
	public boolean mirror;
	private int u, v;
	private List<SimplePartRenderer> children = new ArrayList<>();
	private SimpleModel model;

	public SimplePartRenderer(SimpleModel model, int u, int v) {
		this.model = model;
		this.u = u;
		this.v = v;
	}

	public void addBox(float x, float y, float z, float w, float h, float d) {
		addBox(x, y, z, w, h, d, 0);
	}

	public void addBox(float x, float y, float z, float w, float h, float d, float delta) {
		renderer = BoxRender.createTextured(new Vec3f(x, y, z), new Vec3f(w, h, d), new Vec3f(1, 1, 1), delta, u, v, mirror ? -1 : 1, model.textureWidth, model.textureHeight);
	}

	public void setPos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void addChild(SimplePartRenderer child) {
		children.add(child);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		stack.push();
		translateRotatePart(stack);
		if(renderer != null)
			renderer.draw(stack, buf, 1, 1, 1, 1);
		children.forEach(c -> c.render(stack, buf));
		stack.pop();
	}
}
