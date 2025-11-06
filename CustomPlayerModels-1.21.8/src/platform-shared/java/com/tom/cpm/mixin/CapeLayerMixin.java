package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.client.CapeTransformUtil;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<PlayerRenderState, PlayerModel> {
	private @Shadow @Final HumanoidModel<PlayerRenderState> model;

	public CapeLayerMixin(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent) {
		super(renderLayerParent);
	}

	@Inject(at = @At("HEAD"), method = "render"
			+ "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
			+ "Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", cancellable = true)
	public void onRender(final PoseStack poseStack, final MultiBufferSource bufferIn, final int i,
			final PlayerRenderState state, final float f, final float g, CallbackInfo cbi) {
		Player<?> pl = ((PlayerRenderStateAccess) state).cpm$getPlayer();
		if (pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if (def != null && def.hasRoot(RootModelType.CAPE)) {
				ItemStack chestplate = state.chestEquipment;

				if (!state.isInvisible && state.showCape && chestplate.getItem() != Items.ELYTRA) {
					ResourceLocation defLoc = state.skin.capeTexture();
					if(defLoc == null)defLoc = CustomPlayerModelsClient.DEFAULT_CAPE;
					ModelTexture mt = new ModelTexture(defLoc);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSubModel(getParentModel(), model, null);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.CAPE);
					if(mt.getTexture() != null) {
						VertexConsumer buffer = bufferIn.getBuffer(mt.getRenderType());
						this.model.setupAnim(state);
						CapeTransformUtil.applyTransform(model);
						CustomPlayerModelsClient.mc.getPlayerRenderManager().setModelPose(model);
						this.model.renderToBuffer(poseStack, buffer, i, OverlayTexture.NO_OVERLAY);
					}
					CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(model);
				}
				cbi.cancel();
			}
		}
	}
}
