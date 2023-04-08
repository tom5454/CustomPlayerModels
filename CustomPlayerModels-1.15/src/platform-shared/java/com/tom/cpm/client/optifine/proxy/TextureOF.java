package com.tom.cpm.client.optifine.proxy;

import net.optifine.shaders.MultiTexID;

public interface TextureOF {
	MultiTexID cpm$multiTex();
	void cpm$copyMultiTex(MultiTexID mt);
}
