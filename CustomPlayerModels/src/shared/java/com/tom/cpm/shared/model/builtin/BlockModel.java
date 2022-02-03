package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public class BlockModel extends SimpleModel implements IItemModel {
	private SimplePartRenderer base;

	public BlockModel() {
		base = new SimplePartRenderer(this, 0, 0);
		base.setPos(0.0F, 24.0F, 0.0F);
		base.addBox(0, 0, 0, 16, 16, 16);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		base.render(stack, buf);
	}

	@Override
	public String getTexture() {
		return "block";
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		if(inSlot == ItemSlot.HEAD) {
			float f = 0.625f;
			stack.scale(f, f, f);
			stack.translate(-0.5F, -2.4f, -0.5F);
		} else {
			float x = inSlot == ItemSlot.LEFT_HAND ? -1.18F : -1.41F;
			float z = inSlot == ItemSlot.LEFT_HAND ? -1.41F : -1.18F;
			stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(14.4F));
			stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45F));
			float f = 0.379f;
			stack.scale(f, f, f);
			stack.translate(x, -0.59f, z);
		}
		render(stack, buf);
		stack.pop();
	}
}
