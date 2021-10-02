package com.tom.cpm.mixin.of;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.model.ModelPart;

import com.tom.cpm.client.optifine.proxy.ModelPartOF;

@Mixin(ModelPart.class)
public class ModelPartMixin_OF implements ModelPartOF {

	@Shadow(remap = false)
	public List<ModelPart> childModelsList;

	@Shadow
	public @Final Map<String, ModelPart> children;

	@Override
	public void cpm$updateChildModelsList() {
		childModelsList.clear();
		childModelsList.addAll(children.values());
	}
}
