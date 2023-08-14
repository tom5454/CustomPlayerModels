package com.tom.cpm.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import com.tom.cpm.CustomPlayerModels;

import io.netty.channel.Channel;

public class Platform {

	public static void initPlayerProfile() {
		if(CustomPlayerModels.isModLoaded("firstperson")) {
			try {
				MethodHandle h = MethodHandles.lookup().unreflectGetter(Class.forName("dev.tr7zw.firstperson.FirstPersonModelCore").getDeclaredField("isRenderingPlayer"));
				PlayerProfile.inFirstPerson = () -> {
					try {
						return (boolean) h.invoke();
					} catch (Throwable e) {
						PlayerProfile.inFirstPerson = () -> false;
						return false;
					}
				};
				PlayerProfile.inFirstPerson.getAsBoolean();
			} catch (Throwable e) {
			}
		}
	}

	public static boolean isSitting(Player player) {
		return player.isPassenger();
	}

	public static void setHeight(AbstractWidget w, int h) {
		w.height = h;
	}

	public static Channel getChannel(Connection conn) {
		return conn.channel;
	}
}
