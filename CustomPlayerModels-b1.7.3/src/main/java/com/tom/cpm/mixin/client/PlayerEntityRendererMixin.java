package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.player.PlayerEntity;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RetroGL;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin extends LivingEntityRenderer {
	public PlayerEntityRendererMixin(EntityModel arg, float f) {
		super(arg, f);
	}

	public @Shadow BipedEntityModel bipedModel;
	public @Shadow BipedEntityModel field_295;
	public @Shadow BipedEntityModel field_296;

	@Inject(at = @At(value = "HEAD"), method = "renderHand()V")
	public void onHandPre(CallbackInfo cbi) {
		RetroGL.renderCallLoc = 0;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(Minecraft.INSTANCE.player, null, bipedModel);
		CustomPlayerModelsClient.INSTANCE.manager.bindSkin(bipedModel, TextureSheetType.SKIN);
	}

	@Inject(at = @At(value = "RETURN"), method = "renderHand()V")
	public void onHandPost(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(bipedModel);
	}

	@Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/entity/player/PlayerEntity;DDDFF)V")
	public void onRenderPre(PlayerEntity arg, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPre((PlayerEntityRenderer) (Object) this, arg);
	}

	@Inject(at = @At(value = "RETURN"), method = "render(Lnet/minecraft/entity/player/PlayerEntity;DDDFF)V")
	public void onRenderPost(PlayerEntity arg, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost((PlayerEntityRenderer) (Object) this);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;method_2027(Ljava/lang/String;Ljava/lang/String;)Z"), method = "method_827(Lnet/minecraft/entity/player/PlayerEntity;F)V")
	public boolean onRenderCape(PlayerEntityRenderer r, String string, String string2, PlayerEntity player, float partialTicks) {
		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				BipedEntityModel model = bipedModel;
				CustomPlayerModelsClient.mc.getPlayerRenderManager().rebindModel(model);
				CustomPlayerModelsClient.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
				CustomPlayerModelsClient.renderCape(player, partialTicks, model, def);
				return false;
			}
		}
		return method_2027(string, string2);
	}

	@Inject(at = @At("HEAD"), method = "method_821(Lnet/minecraft/entity/player/PlayerEntity;DDD)V", cancellable = true)
	protected void onRenderName(PlayerEntity entity, double x, double y, double z, CallbackInfo cbi) {
		if (CustomPlayerModelsClient.INSTANCE.onRenderName(this, entity, x, y, z)) {
			cbi.cancel();
		}
	}
}
