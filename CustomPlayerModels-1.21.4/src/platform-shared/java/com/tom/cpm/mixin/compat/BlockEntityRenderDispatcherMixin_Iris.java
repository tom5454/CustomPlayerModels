package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.irisshaders.batchedentityrendering.impl.Groupable;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;

@Mixin(value = BlockEntityRenderDispatcher.class, priority = 900)
public class BlockEntityRenderDispatcherMixin_Iris {

	@ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/state/BlockState;)Z"), allow = 1, require = 1)
	private MultiBufferSource prewrapBufferSource(MultiBufferSource bufferSource, BlockEntity blockEntity) {
		if (!(bufferSource instanceof Groupable) || !(blockEntity instanceof SkullBlockEntity)) {
			return bufferSource;
		} else {
			Object2IntFunction<NamespacedId> entityIds = WorldRenderingSettings.INSTANCE.getEntityIds();
			if (entityIds == null)
				return bufferSource;

			// Fake rendering an armor stand instead of a block entity for skulls
			// Fixes glowing effect not working
			ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ARMOR_STAND);
			int intId = entityIds.applyAsInt(new NamespacedId(entityId.getNamespace(), entityId.getPath()));
			CapturedRenderingState.INSTANCE.setCurrentEntity(intId);
			return type -> bufferSource.getBuffer(OuterWrappedRenderType.wrapExactlyOnce("iris:is_entity", type, EntityRenderStateShard.INSTANCE));
		}
	}

	@Inject(method = {
	"render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"}, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;setupAndRender(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", shift = At.Shift.AFTER)})
	private void postRender(BlockEntity blockEntity, float tickDelta, PoseStack matrix, MultiBufferSource bufferSource, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
	}
}

