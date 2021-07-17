package com.tom.cpm.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;

public interface ERDAccess {
	BipedEntityModel<AbstractClientPlayerEntity> cpm$armorLegs();
	BipedEntityModel<AbstractClientPlayerEntity> cpm$armorBody();
	ElytraEntityModel<AbstractClientPlayerEntity> cpm$elytra();
}
