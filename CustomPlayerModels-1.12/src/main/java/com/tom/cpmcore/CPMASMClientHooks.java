package com.tom.cpmcore;

import net.minecraft.client.model.ModelBase;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.ClientProxy;

public class CPMASMClientHooks {
	public static void renderSkull(ModelBase skullModel, GameProfile profile) {
		if(profile != null) {
			ClientProxy.instance.renderSkull(skullModel, profile);
		}
	}
}
