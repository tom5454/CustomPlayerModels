package com.tom.cpm.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.ModList;

import io.netty.channel.Channel;

public class Platform {
	public static final ResourceLocation WHITE = new ResourceLocation("forge:textures/white.png");

	public static void initPlayerProfile() {
		if(ModList.get().isLoaded("firstpersonmod")) {
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
		return new Matrix4f(array);
	}

	public static boolean isSitting(PlayerEntity player) {
		return player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
	}

	public static void setHeight(Widget w, int h) {
		w.setHeight(h);
	}

	public static Channel getChannel(NetworkManager conn) {
		return conn.channel();
	}

	public static ResourceLocation getName(SCustomPayloadPlayPacket p) {
		return p.getName();
	}

	public static ResourceLocation getName(CCustomPayloadPacket p) {
		return p.getName();
	}

	public static PacketBuffer getData(SCustomPayloadPlayPacket p) {
		return p.getInternalData();
	}

	public static PacketBuffer getData(CCustomPayloadPacket p) {
		return p.getInternalData();
	}
}
