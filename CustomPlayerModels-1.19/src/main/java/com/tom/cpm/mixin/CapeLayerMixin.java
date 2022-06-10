package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> p_116602_) {
		super(p_116602_);
	}

	@Inject(at = @At("HEAD"), method = "render"
			+ "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
			+ "Lnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", cancellable = true)
	public void onRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
			AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		Player<?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				ItemStack chestplate = entitylivingbaseIn.getItemBySlot(EquipmentSlot.CHEST);
				if(!entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isModelPartShown(PlayerModelPart.CAPE) && chestplate.getItem() != Items.ELYTRA) {
					ResourceLocation defLoc = entitylivingbaseIn.getCloakTextureLocation();
					if(defLoc == null)defLoc = ClientProxy.DEFAULT_CAPE;
					ModelTexture mt = new ModelTexture(defLoc);
					ClientProxy.mc.getPlayerRenderManager().rebindModel(getParentModel());
					ClientProxy.mc.getPlayerRenderManager().bindSkin(getParentModel(), mt, TextureSheetType.CAPE);
					if(mt.getTexture() != null) {
						VertexConsumer buffer = bufferIn.getBuffer(mt.getRenderType());
						ClientProxy.renderCape(matrixStackIn, buffer, packedLightIn, entitylivingbaseIn, partialTicks, getParentModel(), def);
					}
				}
				cbi.cancel();
			}
		}
	}
}
