package com.tom.cpm.common;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class ChiseledMeScaler implements ScalerInterface<EntityPlayerMP, ChiseledMeScaler> {
	private static final BiConsumer<EntityPlayerMP, Float> APPLY;

	static {
		BiConsumer<EntityPlayerMP, Float> apply;
		try {
			Method setSize = Class.forName("dev.necauqua.mods.cm.api.ISized").getDeclaredMethod("setSizeCM", double.class);
			apply = (a, b) -> {
				try {
					setSize.invoke(a, b.doubleValue());
				} catch (Exception e) {
				}
			};
			Log.info("Chiseled Me 3.0 scaler loaded");
		} catch (Exception e) {
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
				Log.info("Chiseled Me 2.0 scaler loaded");
			} catch (Exception ex) {
				apply = (a, b) -> {};
				e.addSuppressed(ex);
				Log.warn("Chiseled Me was detected, but scaler couldn't initialize", e);
			}
		}
		APPLY = apply;
	}

	@Override
	public void setScale(ChiseledMeScaler key, EntityPlayerMP player, float value) {
		APPLY.accept(player, value);
	}

	@Override
	public ChiseledMeScaler toKey(ScalingOptions opt) {
		if(opt == ScalingOptions.ENTITY) {
			return this;
		}
		return null;
	}

	@Override
	public String getMethodName() {
		return "chiseledme";
	}
}
