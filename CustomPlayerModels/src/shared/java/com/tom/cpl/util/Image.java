package com.tom.cpl.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

public class Image {
	private final int[] data;
	private final int w, h;
	public Image(int w, int h) {
		this.w = w;
		this.h = h;
		data = new int[w * h];
	}

	public Image(Image cpyFrom) {
		this(cpyFrom.w, cpyFrom.h);
		System.arraycopy(cpyFrom.data, 0, data, 0, data.length);
	}

	public Image(int[] data, int w) {
		this.data = data;
		this.w = w;
		this.h = data.length / w;
	}

	public Image(BufferedImage bi) {
		this(bi.getWidth(), bi.getHeight());
		for(int y = 0;y<h;y++) {
			for(int x = 0;x<w;x++) {
				data[y * w + x] = bi.getRGB(x, y);
			}
		}
	}

	public void setRGB(int x, int y, int rgb) {
		data[y * w + x] = rgb;
	}

	public int getRGB(int x, int y) {
		return data[y * w + x];
	}

	public int[] getData() {
		return data;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public BufferedImage toBufferedImage() {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] to = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		System.arraycopy(data, 0, to, 0, data.length);
		return img;
	}

	public static Image loadFrom(File f) throws IOException {
		return new Image(ImageIO.read(f));
	}

	public static Image loadFrom(InputStream f) throws IOException {
		return new Image(ImageIO.read(f));
	}

	public void storeTo(File f) throws IOException {
		ImageIO.write(toBufferedImage(), "PNG", f);
	}

	public void storeTo(OutputStream f) throws IOException {
		ImageIO.write(toBufferedImage(), "PNG", f);
	}

	public void draw(Image i) {
		for(int x = 0;x<w && x<i.w;x++) {
			for(int y = 0;y<h && y<i.h;y++) {
				data[y * w + x] = i.data[y * i.w + x];
			}
		}
	}

	public void draw(Image i, int xs, int ys) {
		for(int x = 0;x + xs < w && x < i.w;x++) {
			for(int y = 0;y + ys < h && y < i.h;y++) {
				data[(y + ys) * w + x + xs] = i.data[y * i.w + x];
			}
		}
	}

	public static CompletableFuture<Image> download(String url) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new Image(ImageIO.read(new URL(url)));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void fill(int color) {
		for(int y = 0;y<h;y++) {
			for(int x = 0;x<w;x++) {
				data[y * w + x] = color;
			}
		}
	}
}
