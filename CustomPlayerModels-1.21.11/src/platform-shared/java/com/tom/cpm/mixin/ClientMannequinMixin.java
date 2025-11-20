package com.tom.cpm.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.PlayerSkinRenderCache.RenderInfo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.tom.cpm.client.MannequinAccess;
import com.tom.cpm.shared.util.Log;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin extends Mannequin implements ClientAvatarEntity, MannequinAccess {
	private CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>> cpm$renderInfoLookup;
	private PlayerSkinRenderCache.RenderInfo cpm$info;

	public ClientMannequinMixin(EntityType<Mannequin> p_446465_, Level p_446512_) {
		super(p_446465_, p_446512_);
	}

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PlayerSkinRenderCache;lookup(Lnet/minecraft/world/item/component/ResolvableProfile;)Ljava/util/concurrent/CompletableFuture;"), method = "updateSkin")
	public CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>> lookup(PlayerSkinRenderCache inst, ResolvableProfile profile,
			Operation<CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>>> op) {
		var info = op.call(inst, profile);
		cpm$renderInfoLookup = info;
		return info;
	}

	@Inject(at = @At("RETURN"), method = "tick")
	public void onTick(CallbackInfo cbi) {
		if (this.cpm$renderInfoLookup != null && this.cpm$renderInfoLookup.isDone()) {
			try {
				this.cpm$renderInfoLookup.get().ifPresent(this::cpm$setInfo);
				this.cpm$renderInfoLookup = null;
			} catch (Exception exception) {
				Log.error("Error when trying to look up skin", exception);
			}
		}
	}

	private void cpm$setInfo(PlayerSkinRenderCache.RenderInfo cpm$info) {
		this.cpm$info = cpm$info;
	}

	@Override
	public RenderInfo cpm$getInfo() {
		return cpm$info;
	}
}
