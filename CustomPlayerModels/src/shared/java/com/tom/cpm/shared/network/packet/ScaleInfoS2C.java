package com.tom.cpm.shared.network.packet;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.text.FormatText;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class ScaleInfoS2C extends NBTS2C {

	public ScaleInfoS2C() {
		super();
	}

	public ScaleInfoS2C(NBTTagCompound data) {
		super(data);
	}

	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH from) {
		if(tag.hasKey(NetworkUtil.SCALING) && !handler.hasScalingWarning()) {
			if (ModConfig.getCommonConfig().getBoolean(ConfigKeys.SHOW_INGAME_WARNINGS, true))
				handler.displayText(new FormatText("chat.cpm.scalingBlocked"));
			handler.setScalingWarning();
		}
	}
}
