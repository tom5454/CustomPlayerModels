package com.tom.cpm.shared.util;

import com.tom.cpm.shared.math.Vec2i;

public class PaintImageCreator {

	public static Image createImage() {
		Image img = new Image(1024, 1024);
		for(int x = 0;x<1024;x++) {
			for(int y = 0;y<1024;y++) {
				img.setRGB(x, y, 0xff000000 | 0x800000 | (x << 10) | y);
			}
		}
		return img;
	}

	public static Vec2i getImageCoords(int rgb, int w, int h) {
		if((rgb & 0x800000) == 0)return null;
		int x = (rgb >> 10) & 0x3ff;
		int y = rgb & 0x3ff;
		return new Vec2i((int) Math.floor(x / 1024f * w), (int) Math.floor(y / 1024f * h));
	}
}
