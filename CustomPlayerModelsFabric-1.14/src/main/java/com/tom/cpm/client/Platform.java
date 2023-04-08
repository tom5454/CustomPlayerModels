package com.tom.cpm.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;

import io.netty.channel.Channel;

public class Platform {
	public static final ResourceLocation WHITE = new ResourceLocation("cpm:textures/white.png");
	public static float lastBrightnessX, lastBrightnessY;

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

	public static float lastBrightnessX() {
		return lastBrightnessX;
	}

	public static float lastBrightnessY() {
		return lastBrightnessY;
	}
}
