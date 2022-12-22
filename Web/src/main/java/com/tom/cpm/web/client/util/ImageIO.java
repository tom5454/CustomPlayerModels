package com.tom.cpm.web.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.shared.editor.project.ProjectFile.BAIS;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.java.Java.ResourceInputStream;
import com.tom.cpm.web.client.java.io.FileNotFoundException;
import com.tom.cpm.web.client.render.RenderSystem;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import elemental2.core.Uint32Array;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.ImageBitmapOptions;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class ImageIO implements IImageIO {
	public static final Cache<String, Image> loadedImages = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

	@Override
	public Image read(InputStream f) throws IOException {
		if(f instanceof ResourceInputStream) {
			Image j = RenderSystem.preloadedAssets.get(((ResourceInputStream)f).path);
			if(j == null)throw new IOException("Resource not found: " + ((ResourceInputStream)f).path);
			return new Image(j);
		} else if (f instanceof BAIS) {
			Image j = loadedImages.getIfPresent(Base64.getEncoder().encodeToString(((BAIS)f).getBuf()));
			if(j != null)return new Image(j);
		} else {
			return load(f);
		}
		throw new IOException("Image not found");
	}

	@Override
	public void write(Image img, File f) throws IOException {
		try(FileOutputStream fo = new FileOutputStream(f)) {
			write(img, fo);
		}
	}

	@Override
	public void write(Image img, OutputStream f) throws IOException {
		save(img, f);
	}

	@Override
	public Vec2i getSize(InputStream din) throws IOException {
		return new Vec2i();
	}

	@Override
	public Image read(File f) throws IOException {
		try(FileInputStream fi = new FileInputStream(f)) {
			return read(fi);
		}
	}

	@Override
	public CompletableFuture<Image> readF(File file) {
		CompletableFuture<Image> f = new CompletableFuture<>();
		try {
			f.complete(read(file));
			return f;
		} catch (Exception e) {
		}
		String v;
		try {
			v = FS.getContent(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			f.completeExceptionally(e);
			return f;
		}
		Java.promiseToCf(loadImage(v, true, false), f);
		return f;
	}

	public static CompletableFuture<Image> loadImage(byte[] f) {
		String b64 = Base64.getEncoder().encodeToString(f);
		Image j = loadedImages.getIfPresent(b64);
		if(j == null) {
			CompletableFuture<Image> cf = new CompletableFuture<>();
			Java.promiseToCf(loadImage(b64, true, true), cf);
			return cf;
		} else {
			return CompletableFuture.completedFuture(new Image(j));
		}
	}

	public static Promise<Image> loadImage(String data, boolean b64, boolean cache) {
		ImageBitmapOptions opt = ImageBitmapOptions.create();
		opt.setColorSpaceConversion("none");
		opt.setPremultiplyAlpha("none");
		return DomGlobal.fetch(b64 ? "data:image/png;base64," + data : data).then(r -> r.blob()).
				then(b -> DomGlobal.createImageBitmap(b, opt)).then(img -> {
					HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
					canvas.width = img.getWidth();
					canvas.height = img.getHeight();
					CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
					ctx.globalCompositeOperation = "copy";
					ctx.drawImage(img, 0, 0);
					Image image = new Image(new Uint32Array(ctx.getImageData(0, 0, img.getWidth(), img.getHeight()).data.buffer), img.getWidth(), img.getHeight());
					if(cache)loadedImages.put(data, image);
					return Promise.resolve(image);
				});
	}

	public static Image load(InputStream din) throws IOException {
		try {
			PngReader pngr = new PngReader(din);
			int channels = pngr.imgInfo.channels;
			if (channels < 3 || pngr.imgInfo.bitDepth != 8)
				throw new RuntimeException("This method is for RGB8/RGBA8 images");

			Image img = new Image(pngr.imgInfo.cols, pngr.imgInfo.rows);
			for (int row = 0; row < pngr.imgInfo.rows; row++) {
				ImageLineInt l1 = (ImageLineInt) pngr.readRow();
				int[] scanline = l1.getScanline();
				for (int j = 0; j < pngr.imgInfo.cols; j++) {
					int offset = j * channels;
					img.loadRGB(j, row, (scanline[offset + 3] << 24) | (scanline[offset + 2] << 16) | (scanline[offset + 1] << 8)
							| (scanline[offset]));
				}
			}
			pngr.end();
			return img;
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	public static void save(Image img, OutputStream os) throws IOException {
		try {
			ImageInfo imi = new ImageInfo(img.getWidth(), img.getHeight(), 8, true);
			PngWriter pngw = new PngWriter(os, imi);
			for(int y = 0;y<img.getHeight();y++) {
				int[] d = new int[img.getWidth() * 4];
				for(int x = 0;x<img.getWidth();x++) {
					int c = img.storeRGB(x, y);
					int off = x * 4;
					d[off] = c & 0xff;
					d[off + 1] = ((c & 0xff00) >> 8);
					d[off + 2] = ((c & 0xff0000) >> 16);
					d[off + 3] = ((c & 0xff000000) >> 24);
				}
				pngw.writeRowInt(d);
			}
			pngw.end();
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}
}
