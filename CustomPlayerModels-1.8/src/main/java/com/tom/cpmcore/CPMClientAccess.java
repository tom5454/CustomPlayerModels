package com.tom.cpmcore;

import net.minecraft.client.model.ModelBiped;

public class CPMClientAccess {

	public static void setNoSetup(ModelBiped model, boolean value) {
		throw new AbstractMethodError();//model.cpm$noModelSetup = value;
	}
}
