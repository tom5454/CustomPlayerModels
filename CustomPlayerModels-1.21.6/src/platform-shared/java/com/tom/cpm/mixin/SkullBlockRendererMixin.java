package com.tom.cpm.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.RefHolder;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {
	@Shadow private @Final Function<SkullBlock.Type, SkullModelBase> modelByType;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;"
					+ "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "render(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;F"
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II"
					+ "Lnet/minecraft/world/phys/Vec3;)V",
					locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRender(final SkullBlockEntity skullBlockEntity, final float f, final PoseStack poseStack,
			final MultiBufferSource buffer, final int i, final int j, final Vec3 vec3, CallbackInfo ci, float g, BlockState blockState, boolean bl, Direction direction, int k, float h, SkullBlock.Type skullType, SkullModelBase model) {
		RefHolder.CPM_MODELS = modelByType;
		ResolvableProfile gameProfile = skullBlockEntity.getOwnerProfile();
		if(skullType == SkullBlock.Types.PLAYER && gameProfile != null) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile.gameProfile(), buffer);
		}
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent("
					+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;Lnet/minecraft/resources/ResourceLocation;)"
					+ "Lnet/minecraft/client/renderer/RenderType;")
	private static RenderType onGetRenderType(ResourceLocation resLoc, SkullBlock.Type skullType, ResolvableProfile gameProfileIn, ResourceLocation resourceLocation) {
		if(RefHolder.CPM_MODELS == null)return RenderType.entityTranslucent(resLoc);
		SkullModelBase model = RefHolder.CPM_MODELS.apply(skullType);
		RefHolder.CPM_MODELS = null;
		ModelTexture mt = new ModelTexture(resLoc);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
		return mt.getRenderType();
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCullZOffset("
					+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;Lnet/minecraft/resources/ResourceLocation;)"
					+ "Lnet/minecraft/client/renderer/RenderType;")
	private static RenderType onGetRenderTypeNoSkin(ResourceLocation resLoc, SkullBlock.Type skullType, ResolvableProfile gameProfileIn, ResourceLocation resourceLocation) {
		if(RefHolder.CPM_MODELS == null)return RenderType.entityCutoutNoCullZOffset(resLoc);
		SkullModelBase model = RefHolder.CPM_MODELS.apply(skullType);
		RefHolder.CPM_MODELS = null;
		ModelTexture mt = new ModelTexture(resLoc);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
		return mt.getRenderType();
	}

	@Inject(at = @At("RETURN"),
			method = "renderSkull(Lnet/minecraft/core/Direction;FF"
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;)V")
	private static void renderSkullPost(Direction direction, float yaw, float animationProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, SkullModelBase model, RenderType renderLayer, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderSkullPost(vertexConsumers, model);
	}
}
