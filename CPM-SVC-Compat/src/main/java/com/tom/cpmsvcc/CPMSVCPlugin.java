package com.tom.cpmsvcc;

import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class CPMSVCPlugin implements ICPMPlugin {

	@Override
	public String getOwnerModId() {
		return CPMSVCC.MOD_ID;
	}

	@Override
	public void initClient(IClientAPI api) {
		api.registerVoice(CPMSVCC::get);
		api.registerVoiceMute(CPMSVCC::isMuted);
		CPMSVCC.mutedSender = api.registerPluginStateMessage("mute", (u, t) -> {
			boolean m = t.getBoolean("muted");
			if(m)CPMSVCC.muted.add(u);
			else CPMSVCC.muted.remove(u);
		});
		CPMSVCC.LOGGER.info("CPM Plugin initialized!");
	}

	@Override
	public void initCommon(ICommonAPI api) {
	}
}

