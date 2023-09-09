package com.tom.cpm.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

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
		return FabricLoader.getInstance().isModLoaded(id);
	}
}
