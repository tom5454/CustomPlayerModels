package com.tom.cpm.shared.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class LegacySkinConverter {
	private int[] imageData;
	private int imageWidth;
	private int imageHeight;

	public BufferedImage convertSkin(BufferedImage image) {
		if (image == null) {
			return null;
		} else {
			this.imageWidth = 64;
			this.imageHeight = 64;
			BufferedImage bufferedimage = new BufferedImage(this.imageWidth, this.imageHeight, 2);
			Graphics graphics = bufferedimage.getGraphics();
			graphics.drawImage(image, 0, 0, (ImageObserver)null);
			boolean flag = image.getHeight() == 32;

			if (flag) {
				graphics.setColor(new Color(0, 0, 0, 0));
				graphics.fillRect(0, 32, 64, 32);
				graphics.drawImage(bufferedimage, 24, 48, 20, 52, 4, 16, 8, 20, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 28, 48, 24, 52, 8, 16, 12, 20, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 20, 52, 16, 64, 8, 20, 12, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 24, 52, 20, 64, 4, 20, 8, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 28, 52, 24, 64, 0, 20, 4, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 32, 52, 28, 64, 12, 20, 16, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 40, 48, 36, 52, 44, 16, 48, 20, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 44, 48, 40, 52, 48, 16, 52, 20, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 36, 52, 32, 64, 48, 20, 52, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 40, 52, 36, 64, 44, 20, 48, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 44, 52, 40, 64, 40, 20, 44, 32, (ImageObserver)null);
				graphics.drawImage(bufferedimage, 48, 52, 44, 64, 52, 20, 56, 32, (ImageObserver)null);
			}

			graphics.dispose();
			this.imageData = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
			this.setAreaOpaque(0, 0, 32, 16);

			if (flag) {
				this.setAreaTransparent(32, 0, 64, 32);
			}

			this.setAreaOpaque(0, 16, 64, 32);
			this.setAreaOpaque(16, 48, 48, 64);
			return bufferedimage;
		}
	}

	private void setAreaTransparent(int x, int y, int width, int height) {
		for (int i = x; i < width; ++i) {
			for (int j = y; j < height; ++j) {
				int k = this.imageData[i + j * this.imageWidth];

				if ((k >> 24 & 255) < 128) {
					return;
				}
			}
		}

		for (int l = x; l < width; ++l) {
			for (int i1 = y; i1 < height; ++i1) {
				this.imageData[l + i1 * this.imageWidth] &= 16777215;
			}
		}
	}

	/**
	 * Makes the given area of the image opaque
	 */
	private void setAreaOpaque(int x, int y, int width, int height) {
		for (int i = x; i < width; ++i) {
			for (int j = y; j < height; ++j) {
				this.imageData[i + j * this.imageWidth] |= -16777216;
			}
		}
	}
}
