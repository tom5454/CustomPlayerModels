package com.tom.cpm.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import io.netty.channel.Channel;

public class Platform {
	public static final ResourceLocation WHITE = new ResourceLocation("cpm:textures/white.png");

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

	public static boolean isSitting(PlayerEntity player) {
		return player.isPassenger();
	}

	public static void setHeight(Widget w, int h) {
		w.height = h;
	}

	public static Channel getChannel(NetworkManager conn) {
		return conn.channel;
	}

	public static ResourceLocation getName(SCustomPayloadPlayPacket p) {
		return p.getIdentifier();
	}

	public static ResourceLocation getName(CCustomPayloadPacket p) {
		return p.identifier;
	}

	public static PacketBuffer getData(SCustomPayloadPlayPacket p) {
		return p.getData();
	}

	public static PacketBuffer getData(CCustomPayloadPacket p) {
		return p.data;
	}
}
