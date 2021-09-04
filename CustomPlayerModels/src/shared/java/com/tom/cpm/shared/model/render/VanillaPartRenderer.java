package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.SkinType;

public class VanillaPartRenderer {
	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	public boolean visible = true;

	private PartValues val;
	private Mesh renderer;

	public VanillaPartRenderer(VanillaPlayerModel model, VanillaModelPart part, SkinType type, int tw, int th) {
		val = part.getDefaultSize(type);
		x = val.getPos().x;
		y = val.getPos().y;
		z = val.getPos().z;
		renderer = BoxRender.createTextured(val.getOffset(), val.getSize(), new Vec3f(1, 1, 1), val.getMCScale(), val.getUV().x, val.getUV().y, val.isMirror() ? -1 : 1, tw, th);
		model.parts.add(this);
	}

	public VanillaPartRenderer() {
	}

	public void render(MatrixStack stack, VertexBuffer buf) {
		stack.push();
		translateRotatePart(stack);
		renderer.draw(stack, buf, 1, 1, 1, 1);
		stack.pop();
	}

	public void translateRotatePart(MatrixStack matrixStackIn) {
		matrixStackIn.translate(x / 16.0F, y / 16.0F, z / 16.0F);
		if (zRot != 0.0F) {
			matrixStackIn.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(zRot));
		}

		if (yRot != 0.0F) {
			matrixStackIn.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(yRot));
		}

		if (xRot != 0.0F) {
			matrixStackIn.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(xRot));
		}
	}

	public void reset() {
		x = val.getPos().x;
		y = val.getPos().y;
		z = val.getPos().z;
		xRot = 0;
		yRot = 0;
		zRot = 0;
		visible = true;
	}

	public void copyFrom(VanillaPartRenderer from) {
		this.xRot = from.xRot;
		this.yRot = from.yRot;
		this.zRot = from.zRot;
		this.x = from.x;
		this.y = from.y;
		this.z = from.z;
	}
}
