package com.tom.cpm.shared.model;

import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.parts.ModelPartPlayer;

public class PlayerModelElement extends RenderedCube {
	private PlayerModelParts part;
	private ModelPartPlayer player;
	public Vec3f posN, rotN;
	public boolean forcePos;

	public PlayerModelElement(PlayerModelParts part, ModelPartPlayer player) {
		this.part = part;
		this.player = player;
		this.posN = new Vec3f();
		this.rotN = new Vec3f();
		this.pos = new Vec3f();
		this.rotation = new Vec3f();
	}

	protected PlayerModelElement(PlayerModelParts part) {
		this.part = part;
	}

	public PlayerModelParts getPart() {
		return part;
	}

	@Override
	public boolean doDisplay() {
		return player.doRenderPart(part) && super.doDisplay();
	}

	@Override
	public void reset() {
		rotation.x = rotN.x;
		rotation.y = rotN.y;
		rotation.z = rotN.z;
		pos.x = posN.x;
		pos.y = posN.y;
		pos.z = posN.z;
		forcePos = false;
	}

	@Override
	public void setColor(float x, float y, float z) {}

	@Override
	public void setVisible(boolean v) {}

	@Override
	public int getId() {
		return part.ordinal();
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
