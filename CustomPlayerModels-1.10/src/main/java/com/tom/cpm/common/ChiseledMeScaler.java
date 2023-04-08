package com.tom.cpm.common;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class ChiseledMeScaler implements ScalerInterface<EntityPlayerMP, ScalingOptions> {
	private static final BiConsumer<EntityPlayerMP, Float> APPLY;

	static {
		BiConsumer<EntityPlayerMP, Float> apply;
		try {
			Object ci = Class.forName("dev.necauqua.mods.cm.api.ChiseledMeAPI").getDeclaredField("interaction").get(null);
			Method sync = Class.forName("dev.necauqua.mods.cm.Network").getDeclaredMethod("sendSetSizeToClients", Entity.class, float.class, boolean.class);
			Method setSizeOf = ci.getClass().getDeclaredMethod("setSizeOf", Entity.class, float.class, boolean.class);
			apply = (a, b) -> {
				try {
					setSizeOf.invoke(ci, a, b, false);
					sync.invoke(null, a, b, false);
				} catch (Exception ex) {
				}
			};
			Log.info("Chiseled Me scaler loaded");
		} catch (Exception ex) {
			apply = (a, b) -> {};
			Log.warn("Chiseled Me was detected, but scaler couldn't initialize", ex);
		}
		APPLY = apply;
	}

	@Override
	public void setScale(ScalingOptions key, EntityPlayerMP player, float value) {
		if(key == ScalingOptions.ENTITY) {
			APPLY.accept(player, value);
		}
	}

	@Override
	public ScalingOptions toKey(ScalingOptions opt) { return opt; }
}
