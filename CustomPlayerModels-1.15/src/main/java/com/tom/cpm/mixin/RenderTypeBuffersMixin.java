package com.tom.cpm.mixin;

import java.util.SortedMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;

import com.tom.cpm.client.CustomRenderTypes;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

@Mixin(RenderTypeBuffers.class)
public class RenderTypeBuffersMixin {
	@Shadow @Final private SortedMap<RenderType, BufferBuilder> fixedBuffers;
	@Shadow private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> mapBuildersIn, RenderType renderTypeIn) {}

	@Inject(at = @At("RETURN"), method = "<init>()V")
	public void insertCustomBuffer(CallbackInfo cbi){
		put((Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder>) fixedBuffers, CustomRenderTypes.getLinesNoDepth());
		put((Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder>) fixedBuffers, CustomRenderTypes.getEntityColorTranslucentCull());
	}
}
