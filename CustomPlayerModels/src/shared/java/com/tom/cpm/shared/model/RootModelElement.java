package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;

public class RootModelElement extends RenderedCube {
	private ModelPart part;
	public Vec3f posN, rotN;
	public boolean forcePos;

	public RootModelElement(ModelPart part) {
		this.part = part;
		this.posN = new Vec3f();
		this.rotN = new Vec3f();
		this.pos = new Vec3f();
		this.rotation = new Vec3f();
	}

	public ModelPart getPart() {
		return part;
	}

	@Override
	public void reset() {
		rotation.x = rotN.x;
		rotation.y = rotN.y;
		rotation.z = rotN.z;
		pos.x = posN.x;
		pos.y = posN.y;
		pos.z = posN.z;
		display = !hidden;
		forcePos = false;
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
	public void setRotation(boolean add, float x, float y, float z) {
		super.setRotation(add, x, y, z);
		if(!add)forcePos = true;
	}

	@Override
	public void setPosition(boolean add, float x, float y, float z) {
		super.setPosition(add, x, y, z);
		if(!add)forcePos = true;
	}
}
