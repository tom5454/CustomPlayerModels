package com.tom.cpm.client;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.Submit;
import net.minecraft.network.Connection;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Avatar;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import com.tom.cpm.mixinplugin.MixinModLoaded;

import io.netty.channel.Channel;

public class Platform {

	public static boolean isSitting(Avatar player) {
		return player.isPassenger();
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

	public static void submitAlwaysOnTop(SubmitNodeCollection st, Submit<Unit> submit) {
		st.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, submit);
	}
}
