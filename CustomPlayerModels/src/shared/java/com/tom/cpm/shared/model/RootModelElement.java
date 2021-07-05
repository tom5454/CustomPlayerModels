package com.tom.cpm.shared.model;

import com.tom.cpl.math.BoundingBox;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class RootModelElement extends RenderedCube {
	private VanillaModelPart part;
	public Vec3f posN, rotN;
	private ModelDefinition def;

	public RootModelElement(VanillaModelPart part, ModelDefinition def) {
		this.part = part;
		this.posN = new Vec3f();
		this.rotN = new Vec3f();
		this.pos = new Vec3f();
		this.rotation = new Vec3f();
		this.def = def;
	}

	public VanillaModelPart getPart() {
		return part;
	}

	@Override
	public void reset() {
		display = !hidden;
	}

	@Override
	public void setColor(float x, float y, float z) {}

	@Override
	public void setVisible(boolean v) {}

	@Override
	public int getId() {
		return part.getId(this);
	}

	@Override
	public BoundingBox getBounds() {
		PartValues v = part.getDefaultSize(def.getSkinType());
		float f = 0.001f;
		float g = f * 2;
		float scale = 1 / 16f;
		Vec3f offset = v.getOffset();
		Vec3f size = v.getSize();
		return BoundingBox.create(offset.x * scale - f, offset.y * scale - f, offset.z * scale - f,
				size.x * scale + g, size.y * scale + g, size.z * scale + g);
	}

	public void setPosAndRot(float px, float py, float pz, float rx, float ry, float rz) {
		pos.x = px + posN.x;
		pos.y = py + posN.y;
		pos.z = pz + posN.z;
		rotation.x = rx + rotN.x;
		rotation.y = ry + rotN.y;
		rotation.z = rz + rotN.z;
	}
}
