package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.Language;

import com.tom.cpm.client.Lang;

@Mixin(value = I18n.class, remap = false)
public class I18nMixin {
	private @Shadow Language currentLanguage;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/lang/Language;keySize()I"), method = "reload(Ljava/lang/String;Z)V")
	public void reload(final String languageCode, final boolean save, CallbackInfo cbi) {
		Lang.init(currentLanguage, languageCode);
	}
}
