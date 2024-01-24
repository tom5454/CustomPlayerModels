package com.tom.cpm.client;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import net.minecraftforge.fml.ModList;

import io.netty.channel.Channel;

public class Platform {
	public static final ResourceLocation WHITE = new ResourceLocation("forge:textures/white.png");

	public static void initPlayerProfile() {
		if (ModList.get().isLoaded("firstperson"))
			FirstPersonDetector.init();
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
