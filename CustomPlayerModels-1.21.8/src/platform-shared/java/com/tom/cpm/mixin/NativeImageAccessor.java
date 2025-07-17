package com.tom.cpm.mixin;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.platform.NativeImage;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {

	@Invoker
	boolean callWriteToChannel(final WritableByteChannel writableByteChannel) throws IOException;
}
