package com.tom.cpm.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import com.tom.cpm.mixinplugin.MixinModLoaded;

import io.netty.channel.Channel;

public class Platform {

	public static boolean isSitting(Player player) {
		return player.isPassenger();
	}

	public static void setHeight(AbstractWidget w, int h) {
		w.height = h;
	}

	public static Channel getChannel(Connection conn) {
		return conn.channel;
	}

	public static boolean isModLoaded(String id) {
		return MixinModLoaded.isLoaded(id);
	}
}
