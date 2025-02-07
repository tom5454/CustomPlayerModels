package com.tom.cpm.shared.config;

import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.animation.ServerAnimationState;
import com.tom.cpm.shared.network.ModelEventType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetHandler.Scaler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.network.NetworkUtil.ScalingSettings;
import com.tom.cpm.shared.parts.anim.menu.CommandAction.ServerCommandAction;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class PlayerData {
	public long ticksSinceLogin;
	public byte[] data;
	public boolean forced, save;
	public final Map<ScalingOptions, Float> scale = new EnumMap<>(ScalingOptions.class);
	public final Map<ScalingOptions, Float> targetScale = new EnumMap<>(ScalingOptions.class);
	public final EnumSet<ModelEventType> eventSubs = EnumSet.noneOf(ModelEventType.class);
	public final EnumSet<ModelEventType> selfSubs = EnumSet.noneOf(ModelEventType.class);
	public byte[] gestureData = new byte[0];
	public Map<String, ServerCommandAction> animNames = new HashMap<>();
	public final Map<String, NBTTagCompound> pluginStates = new HashMap<>();
	public final ServerAnimationState state = new ServerAnimationState();

	public PlayerData() {
	}

	public void setModel(byte[] data, boolean forced, boolean save) {
		this.data = data;
		this.forced = forced;
		this.save = save;
	}

	public void setModel(String data, boolean forced, boolean save) {
		this.data = data != null ? Base64.getDecoder().decode(data) : null;
		this.forced = forced;
		this.save = save;
	}

	public boolean canChangeModel() {
		return data == null || !forced;
	}

	public void load(String id) {
		ConfigEntry e = ModConfig.getWorldConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(id);
		boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
		String b64 = e.getString(ConfigKeys.MODEL, null);
		if(b64 != null) {
			setModel(b64, forced, true);
		}
		ConfigEntry sc = ModConfig.getWorldConfig().getEntry(ConfigKeys.PLAYER_SCALING);
		if(sc.hasEntry(id)) {
			sc = sc.getEntry(id);
			for(ScalingOptions opt : ScalingOptions.VALUES) {
				float v = sc.getFloat(opt.getNetKey(), 1F);
				if(v != 1)scale.put(opt, v);
			}
		}
	}

	public void save(String id) {
		ConfigEntry e = ModConfig.getWorldConfig().getEntry(ConfigKeys.SERVER_SKINS);
		if(save) {
			if(data == null)
				e.clearValue(id);
			else {
				e = e.getEntry(id);
				e.setString(ConfigKeys.MODEL, Base64.getEncoder().encodeToString(data));
				e.setBoolean(ConfigKeys.FORCED, forced);
			}
		} else {
			e.clearValue(id);
		}
		ConfigEntry sc = ModConfig.getWorldConfig().getEntry(ConfigKeys.PLAYER_SCALING).getEntry(id);
		sc.clear();
		scale.forEach((k, v) -> sc.setFloat(k.getNetKey(), v));
		ModConfig.getWorldConfig().save();
	}

	public <P> void rescale(NetHandler<?, P, ?> handler, P player) {
		Map<ScalingOptions, Float> map = new EnumMap<>(scale);
		scale.clear();
		rescale(handler, player, map, null, false);
	}

	public <P> void rescaleToTarget(NetHandler<?, P, ?> handler, P player, List<ScalingOptions> blocked) {
		rescale(handler, player, targetScale, blocked, true);
	}

	public <P> void resetScale(NetHandler<?, P, ?> handler, P player) {
		rescale(handler, player, Collections.emptyMap(), null, true);
	}

	public <P> void rescale(NetHandler<?, P, ?> handler, P player, Map<ScalingOptions, Float> values, List<ScalingOptions> blocked) {
		rescale(handler, player, values, blocked, false);
	}

	private <P> void rescale(NetHandler<?, P, ?> handler, P player, Map<ScalingOptions, Float> values, List<ScalingOptions> blocked, boolean force) {
		String id = handler.getID(player);
		for (Entry<ScalingOptions, Map<String, Scaler<P>>> e : handler.getScaleSetters().entrySet()) {
			float oldV = scale.getOrDefault(e.getKey(), 1F);
			float selNewV = values.getOrDefault(e.getKey(), 1F);
			float newV = selNewV;
			ScalingSettings l = NetworkUtil.getScalingLimits(e.getKey(), id);
			String scaler = l.scaler;
			if (scaler == null)scaler = Iterables.getLast(e.getValue().keySet());
			if (ConfigKeys.SCALING_METHOD_OFF.equals(scaler))continue;
			newV = newV == 0 ? 1F : MathHelper.clamp(newV, l.min, l.max);
			if(newV != oldV || force) {
				Map<String, Scaler<P>> scl = e.getValue();
				Scaler<P> s = scl.get(scaler);
				if (s != null) {
					Log.debug("Scaling " + e.getKey() + " " + oldV + " -> " + newV);
					s.applyScaling(player, newV);
					scale.put(e.getKey(), newV);
				}
			}
			if(blocked != null && newV != selNewV && selNewV != 0) {
				blocked.add(e.getKey());
			}
		}
	}
}
