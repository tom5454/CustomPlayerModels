package com.tom.cpmcore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.network.INetworkManager;

public class CPMClientAccess {

	public static void setNoSetup(ModelBiped model, boolean value) {
		throw new AbstractMethodError();//model.cpm$noModelSetup = value;
	}

	public static INetworkManager getNetMngr(Minecraft mc) {
		throw new AbstractMethodError();//return mc.myNetworkManager;
	}
}
