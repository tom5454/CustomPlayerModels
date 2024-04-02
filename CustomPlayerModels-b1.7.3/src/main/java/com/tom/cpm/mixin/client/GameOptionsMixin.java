package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.option.GameOptions;

import com.tom.cpm.client.IGameOptions;

@Mixin(GameOptions.class)
public class GameOptionsMixin implements IGameOptions {
	public boolean cpm$thirdPersonF;
	public @Shadow boolean thirdPerson;

	@Override
	public void cpm$onToggleThirdPerson() {
		if (cpm$thirdPersonF) {
			cpm$thirdPersonF = false;
			thirdPerson = false;
		} else if (!thirdPerson) {
			cpm$thirdPersonF = true;
			thirdPerson = true;
		}
	}

	@Override
	public boolean cpm$getThirdPerson2() {
		return cpm$thirdPersonF;
	}
}
