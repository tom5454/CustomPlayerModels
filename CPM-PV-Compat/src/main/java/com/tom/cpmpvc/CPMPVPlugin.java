package com.tom.cpmpvc;

import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class CPMPVPlugin implements ICPMPlugin {

	@Override
	public String getOwnerModId() {
		return CPMPVC.MOD_ID;
	}

	@Override
	public void initClient(IClientAPI api) {
		api.registerVoice(CPMPVC::get);
		api.registerVoiceMute(CPMPVC::isMuted);
		CPMPVC.LOGGER.info("CPM Plugin initialized!");
	}

	@Override
	public void initCommon(ICommonAPI api) {
	}
}

