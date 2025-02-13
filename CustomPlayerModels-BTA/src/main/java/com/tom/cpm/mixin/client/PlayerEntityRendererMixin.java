package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ImageParser;
import net.minecraft.client.render.camera.EntityCamera;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.client.render.model.ModelPlayer;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RetroGL;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(value = MobRendererPlayer.class, remap = false)
public class PlayerEntityRendererMixin extends MobRenderer {
	public PlayerEntityRendererMixin(ModelBase arg, float f) {
		super(arg, f);
	}

	public @Shadow ModelBiped modelBipedMain;
	public @Shadow ModelBiped modelArmorChestplate;
	public @Shadow ModelBiped modelArmor;
	private @Shadow @Final ModelPlayer modelThick;
	private @Shadow @Final ModelPlayer modelSlim;

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/model/ModelBiped;onGround:F"), method = "drawFirstPersonHand(Lnet/minecraft/core/entity/player/Player;Z)V")
	public void onHandPre(Player player, boolean isLeft, CallbackInfo cbi) {
		RetroGL.renderCallLoc = 0;
		ModelBiped model = player.slimModel ? this.modelSlim : this.modelThick;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(Minecraft.INSTANCE.thePlayer, null, model);
		CustomPlayerModelsClient.INSTANCE.manager.bindSkin(model, TextureSheetType.SKIN);
	}

	@Inject(at = @At(value = "RETURN"), method = "drawFirstPersonHand(Lnet/minecraft/core/entity/player/Player;Z)V")
	public void onHandPost(Player player, boolean isLeft, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(modelBipedMain);
	}

	@Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/core/entity/player/Player;DDDFF)V")
	public void onRenderPre(Tessellator tessellator, Player player, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		ModelBiped model = player.slimModel ? this.modelSlim : this.modelThick;
		CustomPlayerModelsClient.INSTANCE.playerRenderPre((MobRendererPlayer) (Object) this, player, model);
	}

	@Inject(at = @At(value = "RETURN"), method = "render(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/core/entity/player/Player;DDDFF)V")
	public void onRenderPost(Tessellator tessellator, Player arg, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost((MobRendererPlayer) (Object) this);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/MobRendererPlayer;bindDownloadableTexture(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/client/render/ImageParser;)Z"), method = "renderSpecials(Lnet/minecraft/core/entity/player/Player;F)V")
	public boolean onRenderCape(MobRendererPlayer r, String string, String string2, ImageParser imageParser, Player player, float partialTicks) {
		com.tom.cpm.shared.config.Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				ModelBiped model = modelBipedMain;
				CustomPlayerModelsClient.mc.getPlayerRenderManager().rebindModel(model);
				CustomPlayerModelsClient.INSTANCE.manager.bindSkin(model, TextureSheetType.CAPE);
				CustomPlayerModelsClient.renderCape(player, partialTicks, model, def);
				return false;
			}
		}
		return bindDownloadableTexture(string, string2, imageParser);
	}

	@Inject(at = @At("HEAD"), method = "renderSpecials(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/core/entity/player/Player;DDD)V", cancellable = true)
	protected void onRenderName(Tessellator tessellator, Player entity, double x, double y, double z, CallbackInfo cbi) {
		if (Minecraft.getMinecraft().thePlayer.getGamemode() != Gamemode.spectator
				&& entity.getGamemode() == Gamemode.spectator) {
			return;
		}
		if (Minecraft.getMinecraft().gameSettings.immersiveMode.drawNames() && this.renderDispatcher.camera != null) {
			if (this.renderDispatcher.camera instanceof EntityCamera
					&& ((EntityCamera) this.renderDispatcher.camera).mob == entity) {
				return;
			}
		}

		CustomPlayerModelsClient.INSTANCE.onRenderName(this, entity, x, y, z);

		if (!com.tom.cpm.shared.config.Player.isEnableNames()) {
			cbi.cancel();
		}
	}
}
