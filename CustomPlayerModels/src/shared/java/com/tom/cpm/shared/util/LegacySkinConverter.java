package com.tom.cpm.shared.util;

import com.tom.cpl.util.Image;

public class LegacySkinConverter {

	public static Image processLegacySkin(Image nativeImageIn) {
		if (nativeImageIn == null) {
			return null;
		} else {
			boolean flag = nativeImageIn.getHeight() == 32;
			if (flag) {
				Image nativeimage = new Image(64, 64);
				nativeimage.draw(nativeImageIn);
				nativeImageIn = nativeimage;
				fillAreaRGBA(nativeimage, 0, 32, 64, 32, 0);
				copyAreaRGBA(nativeimage, 4, 16, 16, 32, 4, 4, true, false);
				copyAreaRGBA(nativeimage, 8, 16, 16, 32, 4, 4, true, false);
				copyAreaRGBA(nativeimage, 0, 20, 24, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 4, 20, 16, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 8, 20, 8, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 12, 20, 16, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 44, 16, -8, 32, 4, 4, true, false);
				copyAreaRGBA(nativeimage, 48, 16, -8, 32, 4, 4, true, false);
				copyAreaRGBA(nativeimage, 40, 20, 0, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 44, 20, -8, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 48, 20, -16, 32, 4, 12, true, false);
				copyAreaRGBA(nativeimage, 52, 20, -8, 32, 4, 12, true, false);
			}

			setAreaOpaque(nativeImageIn, 0, 0, 32, 16);
			if (flag) {
				setAreaTransparent(nativeImageIn, 32, 0, 64, 32);
			}

			setAreaOpaque(nativeImageIn, 0, 16, 64, 32);
			setAreaOpaque(nativeImageIn, 16, 48, 48, 64);
			return nativeImageIn;
		}
	}

	private static void setAreaTransparent(Image image, int x, int y, int width, int height) {
		for(int i = x; i < width; ++i) {
			for(int j = y; j < height; ++j) {
				int k = image.getRGB(i, j);
				if ((k >> 24 & 255) < 128) {
					return;
				}
			}
		}

		for(int l = x; l < width; ++l) {
			for(int i1 = y; i1 < height; ++i1) {
				image.setRGB(l, i1, image.getRGB(l, i1) & 16777215);
			}
		}

	}

	private static void setAreaOpaque(Image image, int x, int y, int width, int height) {
		for(int i = x; i < width; ++i) {
			for(int j = y; j < height; ++j) {
				image.setRGB(i, j, image.getRGB(i, j) | -16777216);
			}
		}
	}

	private static void fillAreaRGBA(Image image, int x, int y, int widthIn, int heightIn, int value) {
		for(int i = y; i < y + heightIn; ++i) {
			for(int j = x; j < x + widthIn; ++j) {
				image.setRGB(j, i, value);
			}
		}
	}

	private static void copyAreaRGBA(Image image, int xFrom, int yFrom, int xToDelta, int yToDelta, int widthIn, int heightIn, boolean mirrorX, boolean mirrorY) {
		for(int i = 0; i < heightIn; ++i) {
			for(int j = 0; j < widthIn; ++j) {
				int k = mirrorX ? widthIn - 1 - j : j;
				int l = mirrorY ? heightIn - 1 - i : i;
				int i1 = image.getRGB(xFrom + j, yFrom + i);
				image.setRGB(xFrom + xToDelta + k, yFrom + yToDelta + l, i1);
			}
		}
	}
}
