package com.tom.cpl.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import elemental2.core.Uint32Array;

public class Image {
	private final Uint32Array data;
	private final int w, h;
	public Image(int w, int h) {
		this.w = w;
		this.h = h;
		data = new Uint32Array(w * h);
	}

	public Image(Image cpyFrom) {
		this.w = cpyFrom.w;
		this.h = cpyFrom.h;
		data = new Uint32Array(cpyFrom.data);
	}

	public Image(Uint32Array data, int w, int h) {
		this.data = data;
		this.w = w;
		this.h = h;
	}

	public void setRGB(int x, int y, int rgb) {
		dataSet(data, y * w + x, argb2rgba(rgb));
	}

	public void loadRGB(int x, int y, int rgb) {
		dataSet(data, y * w + x, rgb);
	}

	public int storeRGB(int x, int y) {
		return dataGet(data, y * w + x);
	}

	public int getRGB(int x, int y) {
		return rgba2argb(dataGet(data, y * w + x));
	}

	public static native int dataSet(Uint32Array array, int index, int val) /*-{
		array[index] = val;
	}-*/;

	public static native int dataGet(Uint32Array array, int index) /*-{
		return array[index];
	}-*/;

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
				data.setAt(y * w + x, i.data.getAt(y * i.w + x));
			}
		}
	}

	public void draw(Image i, int xs, int ys) {
		for(int x = 0;x + xs < w && x < i.w;x++) {
			for(int y = 0;y + ys < h && y < i.h;y++) {
				int p = (y + ys) * w + x + xs;
				if(p < 0)continue;
				data.setAt(p, i.data.getAt(y * i.w + x));
			}
		}
	}

	public void draw(Image i, int xs, int ys, int w, int h) {
		for(int y = ys;y<h;y++) {
			for(int x = xs;x<w;x++) {
				float fx = x / (float) w;
				float fy = y / (float) h;
				dataSet(data, y * w + x, 0);
				int sx = Math.min((int) (fx * i.getWidth()), i.getWidth()-1);
				int sy = Math.min((int) (fy * i.getHeight()), i.getHeight()-1);
				data.setAt(y * w + x, i.data.getAt(sy * i.w + sx));
			}
		}
	}

	public static CompletableFuture<Image> download(String url) {
		CompletableFuture<Image> cf = new CompletableFuture<>();
		if(!url.startsWith("data:")) {
			cf.completeExceptionally(new IOException("Only data protocol is allowed"));
			return cf;
		}
		url = url.substring(url.indexOf(',') + 1);
		try {
			cf.complete(ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(url))));
		} catch (IOException e) {
			cf.completeExceptionally(e);
		}
		return cf;
	}

	public void fill(int color) {
		color = argb2rgba(color);
		data.fill(color);
	}

	public void fill(int xs, int ys, int w, int h, int color) {
		int iw = getWidth();
		int ih = getHeight();
		color = argb2rgba(color);
		for(int x = 0;x + xs < iw && x < w;x++) {
			for(int y = 0;y + ys < ih && y < h;y++) {
				int index = ((y + ys) * iw + x + xs) * 4;
				dataSet(data, index, color);
			}
		}
	}

	public Uint32Array getData() {
		return data;
	}

	public static int argb2rgba(int in) {
		int r = ((in & 0x00FF0000) >>> 16);
		int b = in & 0x000000FF;
		return (in & 0xFF00FF00) | (b << 16) | r;
	}

	public static int rgba2argb(int in) {
		int b = ((in & 0x00FF0000) >>> 16);
		int r = in & 0x000000FF;
		return (in & 0xFF00FF00) | (r << 16) | b;
	}
}
