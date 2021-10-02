package com.tom.cpm.mixin;

import java.util.SortedMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;

import com.tom.cpm.client.CustomRenderTypes;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {
	@Shadow @Final private SortedMap<RenderLayer, BufferBuilder> entityBuilders;
	@Shadow private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {}

	@Inject(at = @At("RETURN"), method = "<init>()V")
	public void insertCustomBuffer(CallbackInfo cbi){
		assignBufferBuilder((Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>) entityBuilders, CustomRenderTypes.getEntityColorTranslucentCull());
	}
}
