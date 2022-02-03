package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Quaternion;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public interface IItemModel {
	void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot);
	String getTexture();

	public static class ItemRenderTransform {
		public Vec3f translation, rotation, scale;

		public ItemRenderTransform(Vec3f translation, Vec3f rotation, Vec3f scale) {
			this.translation = translation;
			this.rotation = rotation;
			this.scale = scale;
		}

		public void apply(MatrixStack stack) {
			float f = this.rotation.x;
			float f1 = this.rotation.y;
			float f2 = this.rotation.z;

			stack.translate(this.translation.x / 16F, this.translation.y / 16F, this.translation.z / 16F);
			stack.rotate(new Quaternion(f, f1, f2, true));
			stack.scale(this.scale.x, -this.scale.y, -this.scale.z);
		}
	}
}
