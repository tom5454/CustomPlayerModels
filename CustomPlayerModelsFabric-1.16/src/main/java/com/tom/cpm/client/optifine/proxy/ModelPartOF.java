package com.tom.cpm.client.optifine.proxy;

import java.util.List;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import net.optifine.entity.model.anim.ModelUpdater;

public interface ModelPartOF {

	public default Identifier cpm$textureLocation() {
		throw new AbstractMethodError();
	}

	public default WorldRenderer cpm$renderGlobal() {
		throw new AbstractMethodError();
	}

	public default ModelUpdater cpm$modelUpdater() {
		throw new AbstractMethodError();
	}

	public default List cpm$spriteList() {
		throw new AbstractMethodError();
	}

	public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd);
}
