package com.tom.cpm.web.client.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.java.Java.ResourceInputStream;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.render.ViewerGui;

import elemental2.core.Uint32Array;
import elemental2.core.Uint8ClampedArray;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.ImageBitmapOptions;
import elemental2.dom.ImageData;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class ImageIO implements IImageIO {
	public static final Cache<String, Image> loadedImages = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

	@Override
	public CompletableFuture<Image> readF(File file) {
		CompletableFuture<Image> f = new CompletableFuture<>();
		FS.getContent(file.getAbsolutePath()).then(v -> {
			Java.promiseToCf(loadImage(v, true, false), f);
			return null;
		}).catch_(ex -> {
			if(ex instanceof Throwable)f.completeExceptionally((Throwable) ex);
			else f.completeExceptionally(new IOException(String.valueOf(ex)));
			return null;
		});
		return f;
	}

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
			//return load(f);/*

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOHelper.copy(f, baos);
			String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
			Image j = loadedImages.getIfPresent(b64);
			if(j == null) {
				CompletableFuture<Image> cf = new CompletableFuture<>();
				Java.promiseToCf(loadImage(b64, true, true), cf);
				ViewerGui.addBgLoad(cf);
				throw new AsyncResourceException();
			} else {
				return new Image(j);
			}//*/
		}
		throw new IOException("Image not found");
	}

	@Override
	public void write(Image img, File f) throws IOException {
		String base64 = toBase64(img);
		if(!FS.setContent(f.getAbsolutePath(), base64))throw new IOException("Failed to write file");
	}

	private String toBase64(Image img) {
		HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
		canvas.width = img.getWidth();
		canvas.height = img.getHeight();
		CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
		Uint8ClampedArray i = new Uint8ClampedArray(img.getData().buffer);
		ImageData im = new ImageData(i, img.getWidth(), img.getHeight());
		ctx.globalCompositeOperation = "copy";
		ctx.putImageData(im, 0, 0);
		String base64 = canvas.toDataURL();
		base64 = base64.substring(base64.indexOf(',') + 1);
		return base64;
	}

	@Override
	public void write(Image img, OutputStream f) throws IOException {
		String base64 = toBase64(img);
		f.write(Base64.getDecoder().decode(base64));
	}

	@Override
	public Vec2i getSize(InputStream din) throws IOException {
		return new Vec2i();
	}

	@Override
	public Image read(File f) throws IOException {
		throw new IOException("Unsupported exception");
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
		/*return new Promise<>((res, rej) -> {
			HTMLImageElement img = Js.uncheckedCast(DomGlobal.document.createElement("img"));
			img.src = b64 ? "data:image/png;base64," + data : data;
			img.onload = e -> {
				HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
				canvas.width = img.width;
				canvas.height = img.height;
				CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
				ctx.globalCompositeOperation = "copy";
				ctx.drawImage(img, 0, 0);
				Image image = new Image(new Uint32Array(ctx.getImageData(0, 0, img.width, img.height).data.buffer), img.width, img.height);
				if(cache)loadedImages.put(data, image);
				res.onInvoke(image);
				return null;
			};
			img.onerror = e -> {
				rej.onInvoke(e);
				return null;
			};
		});*/
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

	/* TODO emulate java features(port zlib, and pngj) to use this ? or web assembly ?
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
					img.loadRGB(j, row, (scanline[offset + 3] << 24) | (scanline[offset] << 16) | (scanline[offset + 1] << 8)
							| (scanline[offset + 2]));
				}
			}
			pngr.end();
			return img;
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}//*/
}
