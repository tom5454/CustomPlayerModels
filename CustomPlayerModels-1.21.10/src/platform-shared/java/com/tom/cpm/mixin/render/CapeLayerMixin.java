package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CapeTransformUtil;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AvatarRenderState, PlayerModel> {
	public CapeLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> p_117346_) {
		super(p_117346_);
	}

	private @Shadow @Final HumanoidModel<AvatarRenderState> model;
	@Shadow abstract boolean hasLayer(ItemStack p_372809_, EquipmentClientInfo.LayerType p_388089_);

	@Inject(at =
			@At("HEAD"),
			method = "submit("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
					+ "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF"
					+ ")V", cancellable = true)
	private void submitCape(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float p_433069_, float p_435707_,
			CallbackInfo cbi) {
		Player<Avatar> pl = ((PlayerRenderStateAccess) state).cpm$getPlayer();
		if (pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if (def != null && def.hasRoot(RootModelType.CAPE)) {
				if (!this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
					poseStack.pushPose();
					if (this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
						poseStack.translate(0.0F, -0.053125F, 0.06875F);
					}

					var cape = state.skin.cape();
					ResourceLocation defLoc = cape != null ? cape.texturePath() : CustomPlayerModelsClient.DEFAULT_CAPE;
					ModelTexture mt = new ModelTexture(defLoc);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSubModel(getParentModel(), model, null);
					CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.CAPE);
					if(mt.getTexture() != null) {
						this.model.setupAnim(state);
						CapeTransformUtil.applyTransform(model);
						CustomPlayerModelsClient.mc.getPlayerRenderManager().setModelPose(model);

						collector.submitModel(
								this.model,
								state,
								poseStack,
								mt.getRenderType(),
								light,
								OverlayTexture.NO_OVERLAY,
								state.outlineColor,
								null
								);
					}
					CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(model);
					poseStack.popPose();
				}
				cbi.cancel();
			}
		}
	}
}
