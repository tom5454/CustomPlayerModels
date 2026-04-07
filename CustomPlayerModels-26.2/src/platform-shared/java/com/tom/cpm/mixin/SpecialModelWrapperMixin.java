package com.tom.cpm.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

@Mixin(SpecialModelWrapper.class)
public class SpecialModelWrapperMixin {

	@Inject(at = @At("RETURN"), method = "update")
	public void onUpdate(
			ItemStackRenderState itemStackRenderState,
			ItemStack itemStack,
			ItemModelResolver itemModelResolver,
			ItemDisplayContext itemDisplayContext,
			@Nullable ClientLevel clientLevel,
			@Nullable ItemOwner itemOwner,
			int i,
			CallbackInfo cbi
			) {
		if (itemStack.getItem() == Items.PLAYER_HEAD) {
			ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
			if (resolvableProfile != null) {
				itemStackRenderState.setAnimated();
			}
		}
	}
}
