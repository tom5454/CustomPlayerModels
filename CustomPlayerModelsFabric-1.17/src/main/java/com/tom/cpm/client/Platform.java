package com.tom.cpm.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import com.mojang.math.Matrix4f;

import io.netty.channel.Channel;

public class Platform {

	public static void initPlayerProfile() {
		if (FabricLoader.getInstance().isModLoaded("firstperson"))
			FirstPersonDetector.init();
	}

	public static Matrix4f createMatrix(float[] array) {
		return Mat4Access.load(array);
	}

	public static interface Mat4Access {
		void cpm$loadValue(float[] data);

		static Matrix4f load(float[] data) {
			Matrix4f m = new Matrix4f();
			((Mat4Access) (Object) m).cpm$loadValue(data);
			return m;
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
