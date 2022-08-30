package com.tom.cpm.shared.network.packet;

import java.util.EnumMap;
import java.util.Map;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.KeybindText;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.util.ScalingOptions;

public class HelloS2C extends NBTS2C {

	public HelloS2C(NBTTagCompound data) {
		super(data);
	}

	public HelloS2C() {
	}

	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH net) {
		NBTTagCompound scaling = tag.getCompoundTag(NetworkUtil.SCALING);
		Map<ScalingOptions, Float> scalingMap = new EnumMap<>(ScalingOptions.class);
		for(ScalingOptions o : ScalingOptions.VALUES) {
			float v = scaling.getFloat(o.getNetKey());
			scalingMap.put(o, v);
		}
		handler.handleServerCaps(tag.getCompoundTag(NetworkUtil.SERVER_CAPS));
		String server = MinecraftClientAccess.get().getConnectedServer();
		ConfigEntry cc = ModConfig.getCommonConfig();
		ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
		if(server != null && ss.hasEntry(server)) {
			if(ss.getEntry(server).getBoolean(ConfigKeys.DISABLE_NETWORK, false)) {
				int kickTime = tag.getInteger(NetworkUtil.KICK_TIME);
				if(kickTime > 0) {
					handler.getRecommendedSettingChanges().clear();
					handler.getRecommendedSettingChanges().add(new ConfigChangeRequest<>(ConfigKeys.DISABLE_NETWORK, true, false));
					handler.displayText(new FormatText("chat.cpm.serverRequiresCPM", new KeybindText("key.cpm.gestureMenu", "gestureMenu")));
				}
				return;
			}
		}
		net.cpm$setHasMod(true);
		MinecraftClientAccess.get().getDefinitionLoader().clearServerData();
		handler.sendPacketToServer(new HelloC2S(0));
		MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setServerScaling(scalingMap);
	}
}
