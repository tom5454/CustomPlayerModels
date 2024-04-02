package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.shared.skin.TextureType;

@Mixin(targets = "net.minecraft.client.texture.ImageDownload$Thread")
public class ImageDownloadMixin {

	@ModifyArg(at = @At(value = "INVOKE", target = "Ljava/net/URL;<init>(Ljava/lang/String;)V"), method = "run()V")
	public String fixURL(String url) {
		if (url.startsWith("http://s3.amazonaws.com/MinecraftSkins/") && url.endsWith(".png")) {
			String username = url.substring(39, url.length() - 4);
			return GameProfileManager.getTextureUrlSync(username, TextureType.SKIN, url);
		} else if (url.startsWith("http://s3.amazonaws.com/MinecraftCloaks/") && url.endsWith(".png")) {
			String username = url.substring(40, url.length() - 4);
			return GameProfileManager.getTextureUrlSync(username, TextureType.CAPE, url);
		}
		return url;
	}
}
