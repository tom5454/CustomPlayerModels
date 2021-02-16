package com.tom.cpm.client.optifine.proxy;

import net.optifine.shaders.MultiTexID;

public interface AbstractTextureOF {
	MultiTexID cpm$multiTex();
	void cpm$copyMultiTex(MultiTexID mt);
}
