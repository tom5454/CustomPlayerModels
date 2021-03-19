package com.tom.cpm.shared.editor.util;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.model.SkinType;

public class GetFreeSkinSlots {
	public static Set<PlayerSkinLayer> getFreeLayers(Image img, Image template, SkinType skinType) {
		Set<PlayerSkinLayer> used = new HashSet<>(EnumSet.allOf(PlayerSkinLayer.class));
		int shift = 8 * (2 - skinType.getChannel());
		for(int x = 0;x<template.getWidth();x++) {
			for(int y = 0;y<template.getHeight();y++) {
				int i = img.getRGB(x, y);
				int t = template.getRGB(x, y);
				int v = (((t & (0xff << shift)) >> shift) & 0xff);
				if((t & 0xff000000) != 0) {
					if((i & 0xff000000) > 0) {
						used.remove(PlayerSkinLayer.getEnc(v));
					}
				}
			}
		}
		return used;
	}

	public static void clearLayerArea(Image img, Image template, SkinType skinType, PlayerSkinLayer layer) {
		int shift = 8 * (2 - skinType.getChannel());
		for(int x = 0;x<template.getWidth();x++) {
			for(int y = 0;y<template.getHeight();y++) {
				int t = template.getRGB(x, y);
				int v = (((t & (0xff << shift)) >> shift) & 0xff);
				if((t & 0xff000000) != 0) {
					if(v == (1 << layer.ordinal()))
						img.setRGB(x, y, 0);
				}
			}
		}
	}
}
