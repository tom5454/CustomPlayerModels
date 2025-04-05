package com.tom.cpm.client;

import java.util.function.Supplier;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.pipeline.RenderPipeline;

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

	public static Supplier<RenderPipeline> registerPipeline(Supplier<RenderPipeline> factory) {
		var p = factory.get();
		RenderPipelines.register(p);
		return () -> p;
	}
}
