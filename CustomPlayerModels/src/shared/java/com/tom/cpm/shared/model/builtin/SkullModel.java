package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public class SkullModel extends SimpleModel implements IItemModel {
	private SimplePartRenderer base;

	public SkullModel() {
		textureWidth = 32;
		base = new SimplePartRenderer(this, 0, 0);
		base.setPos(8.0F, 24.0F, 8.0F);
		base.addBox(0, 0, 0, 8, 8, 8);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		base.render(stack, buf);
	}

	@Override
	public String getTexture() {
		return "skull";
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		if(inSlot == ItemSlot.HEAD) {
			float f = 1.2f;
			stack.scale(f, f, f);
			stack.translate(-0.75F, -1.999f, -0.75F);
		} else {
			float x = inSlot == ItemSlot.LEFT_HAND ? -1.25F : -1.41F;
			float z = inSlot == ItemSlot.LEFT_HAND ? -1.41F : -1.25F;
			stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(42.5F));
			stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45.5F));
			float f = 0.5f;
			stack.scale(f, f, f);
			stack.translate(x, -1f, z);
			stack.translate(-0.34F, 0, -0.34F);
		}
		new SkullModel().render(stack, buf);
		stack.pop();
	}

}
