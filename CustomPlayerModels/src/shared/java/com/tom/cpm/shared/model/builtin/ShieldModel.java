package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public class ShieldModel extends SimpleModel implements IItemModel {
	private final SimplePartRenderer plate;
	private final SimplePartRenderer handle;

	public ShieldModel() {
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.plate = new SimplePartRenderer(this, 0, 0);
		this.plate.addBox(-6.0F, -11.0F, -2.0F, 12.0F, 22.0F, 1.0F, 0.0F);
		this.handle = new SimplePartRenderer(this, 26, 0);
		this.handle.addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F, 0.0F);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		plate.render(stack, buf);
		handle.render(stack, buf);
	}

	@Override
	public String getTexture() {
		return "shield";
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		float r = inSlot == ItemSlot.LEFT_HAND ? -45F : 45F;
		stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(50F));
		stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(r));
		float x = inSlot == ItemSlot.LEFT_HAND ? -0.15F : 0.15F;
		stack.translate(x, 0.42F, -0.5F);
		render(stack, buf);
		stack.pop();
	}
}
