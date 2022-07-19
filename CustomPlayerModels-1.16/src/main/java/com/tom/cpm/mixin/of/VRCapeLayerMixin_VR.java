package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.VRCapeLayer;
import org.vivecraft.render.VRPlayerModel;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(VRCapeLayer.class)
public abstract class VRCapeLayerMixin_VR extends LayerRenderer<AbstractClientPlayerEntity, VRPlayerModel<AbstractClientPlayerEntity>> {

	public VRCapeLayerMixin_VR(
			IEntityRenderer<AbstractClientPlayerEntity, VRPlayerModel<AbstractClientPlayerEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	@Inject(at = @At("HEAD"), method = "render"
			+ "(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
			+ "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFFFFF)V", remap = false, cancellable = true)
	public void onRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn,
			AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				ItemStack chestplate = entitylivingbaseIn.getItemBySlot(EquipmentSlotType.CHEST);
				if(!entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isModelPartShown(PlayerModelPart.CAPE) && chestplate.getItem() != Items.ELYTRA) {
					ResourceLocation defLoc = entitylivingbaseIn.getCloakTextureLocation();
					if(defLoc == null)defLoc = CustomPlayerModelsClient.DEFAULT_CAPE;
					ModelTexture mt = new ModelTexture(defLoc);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().rebindModel(getParentModel());
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getParentModel(), mt, TextureSheetType.CAPE);
					if(mt.getTexture() != null) {
						IVertexBuilder buffer = bufferIn.getBuffer(mt.getRenderType());
						CustomPlayerModelsClient.renderCape(matrixStackIn, buffer, packedLightIn, entitylivingbaseIn, partialTicks, getParentModel(), def);
					}
				}
				cbi.cancel();
			}
		}
	}
}
