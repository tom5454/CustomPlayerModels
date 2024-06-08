package com.tom.cpl.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

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

	public static CompletableFuture<Image> loadFrom(File f) {
		return ImageIO.read(f);
	}

	public static Image loadFrom(InputStream f) throws IOException {
		return ImageIO.read(f);
	}

	public void storeTo(File f) throws IOException {
		ImageIO.write(this, f);
	}

	public void storeTo(OutputStream f) throws IOException {
		ImageIO.write(this, f);
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
				int p = (y + ys) * w + x + xs;
				if(p < 0)continue;
				data[p] = i.data[y * i.w + x];
			}
		}
	}

	public void draw(Image i, int xs, int ys, int w, int h) {
		for (int y = 0;y<h;y++) {
			int yp = (y + ys) * w;
			for (int x = 0;x<w;x++) {
				int ix = x * i.getWidth() / w;
				int iy = y * i.getHeight() / h;
				data[yp + x + xs] = i.getRGB(Math.min(ix, i.getWidth()-1), Math.min(iy, i.getHeight()-1));
			}
		}
	}

	public static CompletableFuture<Image> download(String url) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				URL input = new URL(url);
				InputStream istream = null;
				try {
					istream = input.openStream();
				} catch (IOException e) {
					throw new IOException("Can't get input stream from URL!", e);
				}
				try {
					return ImageIO.read(istream);
				} finally {
					istream.close();
				}
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

	public void fill(int xs, int ys, int w, int h, int color) {
		for(int x = 0;x + xs < this.w && x < w;x++) {
			for(int y = 0;y + ys < this.h && y < h;y++) {
				data[(y + ys) * this.w + x + xs] = color;
			}
		}
	}
}
