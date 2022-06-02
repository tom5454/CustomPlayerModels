package com.tom.cpmsvcc.platform.forge116;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;
import com.tom.cpmsvcc.CPMSVCC;

public class CPMSVCPlugin implements ICPMPlugin {

	@Override public String getOwnerModId() { return CPMSVCC.MOD_ID; }

	@Override public void initClient(IClientAPI api) {
		api.registerVoice(PlayerEntity.class, p -> CPMSVCC.get(p.getUUID()));
		CPMSVCC.setLocal(() -> Minecraft.getInstance().player.getUUID());
		CPMSVCC.LOGGER.info("CPM Plugin initialized!");
	}

	@Override public void initCommon(ICommonAPI arg0) {}
}

