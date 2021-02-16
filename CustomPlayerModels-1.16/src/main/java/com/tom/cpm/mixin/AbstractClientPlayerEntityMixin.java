package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

	public AbstractClientPlayerEntityMixin(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_,
			GameProfile p_i241920_4_) {
		super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
	}

	/*@Inject(at = @At("HEAD"), method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", cancellable = true)
	public void getLocationSkin(CallbackInfoReturnable<ResourceLocation> cbi) {
		ClientProxy.INSTANCE.getSkin(this, cbi);
	}*/
}
