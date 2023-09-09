package com.tom.cpm.mixin.compat;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixinFabric_OF {
	private @Shadow Map<SkullBlock.Type, SkullModelBase> skullModels;

	@Inject(at = @At("HEAD"), method = "renderRaw", require = 0, remap = false)
	//Optifine
	public void onRenderOF(ItemStack itemStack, PoseStack matrices, MultiBufferSource multiBufferSource, int light, int arg5, CallbackInfo ci) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			if (block instanceof AbstractSkullBlock) {
				GameProfile gameProfile = null;
				if (itemStack.hasTag()) {
					CompoundTag compoundTag = itemStack.getTag();
					if (compoundTag.contains("SkullOwner", 10)) {
						gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
					} else if (compoundTag.contains("SkullOwner", 8)
							&& !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
						gameProfile = new GameProfile((UUID) null, compoundTag.getString("SkullOwner"));
						compoundTag.remove("SkullOwner");
						SkullBlockEntity.updateGameprofile(gameProfile, (p_172560_) -> {
							compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), p_172560_));
						});
					}
				}
				SkullBlock.Type skullType = ((AbstractSkullBlock)block).getType();
				SkullModelBase skullmodelbase = this.skullModels.get(skullType);
				RefHolder.CPM_MODELS = skullModels;
				if(skullType == SkullBlock.Types.PLAYER && gameProfile != null) {
					CustomPlayerModelsClient.INSTANCE.renderSkull(skullmodelbase, gameProfile, multiBufferSource);
				}
			}
		}
	}
}
