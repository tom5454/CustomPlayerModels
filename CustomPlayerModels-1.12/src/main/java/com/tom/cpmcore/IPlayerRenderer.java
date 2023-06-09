package com.tom.cpmcore;

import net.minecraft.client.entity.AbstractClientPlayer;

public interface IPlayerRenderer {
	boolean cpm$isVisible(AbstractClientPlayer player);
	boolean cpm$bindEntityTexture(AbstractClientPlayer player);
}
