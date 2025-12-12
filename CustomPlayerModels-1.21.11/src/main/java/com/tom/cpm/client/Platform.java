package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.Avatar;
import net.neoforged.fml.ModList;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import io.netty.channel.Channel;

public class Platform {
	public static List<RenderPipeline> pipelines = new ArrayList<>();

	public static boolean isSitting(Avatar player) {
		return player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
	}

	public static void setHeight(AbstractWidget w, int h) {
		w.setHeight(h);
	}

	public static Channel getChannel(Connection conn) {
		return conn.channel();
	}

	public static boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

	public static Supplier<RenderPipeline> registerPipeline(Supplier<RenderPipeline> factory) {
		var pipeline = factory.get();
		pipelines.add(pipeline);
		return () -> pipeline;
	}
}
