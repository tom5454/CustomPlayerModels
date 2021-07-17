package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public CapeFeatureRendererMixin(
			FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
		super(context);
	}

	@Inject(at = @At("HEAD"), method = "render"
			+ "(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
			+ "Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", cancellable = true)
	public void onRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn,
			AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		Player<?, ?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				ItemStack chestplate = entitylivingbaseIn.getEquippedStack(EquipmentSlot.CHEST);
				if(!entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isPartVisible(PlayerModelPart.CAPE) && chestplate.getItem() != Items.ELYTRA) {
					CallbackInfoReturnable<Identifier> rl = new CallbackInfoReturnable<>(null, true, null);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getContextModel(), rl, TextureSheetType.CAPE);
					if(rl.getReturnValue() != null) {
						VertexConsumer buffer = bufferIn.getBuffer(RenderLayer.getEntityTranslucent(rl.getReturnValue()));
						CustomPlayerModelsClient.renderCape(matrixStackIn, buffer, packedLightIn, entitylivingbaseIn, partialTicks, getContextModel(), def);
					}
				}
				cbi.cancel();
			}
		}
	}
}
