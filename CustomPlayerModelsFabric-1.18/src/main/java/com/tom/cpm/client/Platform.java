package com.tom.cpm.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import com.mojang.math.Matrix4f;

import io.netty.channel.Channel;

public class Platform {

	public static void initPlayerProfile() {
		if(FabricLoader.getInstance().isModLoaded("firstperson")) {
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
