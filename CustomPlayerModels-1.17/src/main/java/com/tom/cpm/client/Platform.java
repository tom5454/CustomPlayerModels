package com.tom.cpm.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.fml.ModList;

import com.mojang.math.Matrix4f;

import io.netty.channel.Channel;

public class Platform {

	public static void initPlayerProfile() {
		if (ModList.get().isLoaded("firstperson"))
			FirstPersonDetector.init();
	}

	public static Matrix4f createMatrix(float[] array) {
		return new Matrix4f(array);
	}

	public static boolean isSitting(Player player) {
		return player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
	}

	public static void setHeight(AbstractWidget w, int h) {
		w.setHeight(h);
	}

	public static Channel getChannel(Connection conn) {
		return conn.channel();
	}
}
