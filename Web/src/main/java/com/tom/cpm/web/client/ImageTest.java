package com.tom.cpm.web.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpm.shared.io.SkinDataOutputStream;

import elemental2.core.Uint32Array;
import elemental2.core.Uint8ClampedArray;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.ImageData;
import jsinterop.base.Js;

public class ImageTest {

	@SuppressWarnings("resource")
	public static void runTest() throws Exception {
		System.out.println("Testing image transparency");
		Image tmp = ImageIO.read(ImageTest.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png"));
		Image img = ImageIO.read(ImageTest.class.getResourceAsStream("/assets/cpm/textures/template/default.png"));
		SkinDataOutputStream writeS = new SkinDataOutputStream(img, tmp, 1);
		byte[] data = new byte[2912];//4px: 2912 3px: 3360
		//Arrays.fill(data, (byte) 0xff);
		new Random(1234).nextBytes(data);
		OutputStream keepImport;
		//*
		new OutputStream() {
			int i;
			@Override
			public void write(int b) throws IOException {
				//System.out.println((i++) + " " + b);
				writeS.write(b);
			}
		}.write(data);/*/
		writeS.write(data);
		// */
		writeS.close();
		ImageIO.write(img, new File("image_test.png"));
		ImageIO.read(new File("image_test.png")).thenAccept(i -> {
			try {
				SkinDataInputStreamD din = new SkinDataInputStreamD(i, tmp, 1);
				byte[] in = new byte[data.length];
				din.read(in);
				System.out.println("Image read/write test");
				System.out.println(printHexBinary(data));
				System.out.println(printHexBinary(in));
				System.out.println(Arrays.equals(data, in));
				System.out.println("Image test finished");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		HTMLCanvasElement canvas = Js.uncheckedCast(DomGlobal.document.createElement("canvas"));
		canvas.width = img.getWidth();
		canvas.height = img.getHeight();
		CanvasRenderingContext2D ctx = Js.uncheckedCast(canvas.getContext("2d"));
		Uint8ClampedArray i = new Uint8ClampedArray(img.getData().buffer);
		ImageData im = new ImageData(i, img.getWidth(), img.getHeight());
		ctx.globalCompositeOperation = "copy";
		ctx.putImageData(im, 0, 0);
		Image image = new Image(new Uint32Array(ctx.getImageData(0, 0, img.getWidth(), img.getHeight()).data.buffer), img.getWidth(), img.getHeight());
		try {
			SkinDataInputStreamD din = new SkinDataInputStreamD(image, tmp, 1);
			byte[] in = new byte[data.length];
			din.read(in);
			System.out.println("Image canvas test");
			System.out.println(printHexBinary(data));
			System.out.println(printHexBinary(in));
			System.out.println(Arrays.equals(data, in));
			System.out.println("Image test finished");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final char[] hexCode = "0123456789ABCDEF".toCharArray();
	public static String printHexBinary(byte[] paramArrayOfByte) {
		StringBuilder stringBuilder = new StringBuilder(paramArrayOfByte.length * 2);
		for (byte b : paramArrayOfByte) {
			stringBuilder.append(hexCode[b >> 4 & 0xF]);
			stringBuilder.append(hexCode[b & 0xF]);
		}
		return stringBuilder.toString();
	}

	public static class SkinDataInputStreamD extends InputStream {
		private Image img, template;
		private int block;
		private int channel;
		private int x, y;
		private boolean finished;

		public SkinDataInputStreamD(Image img, Image template, int channel) {
			this.img = img;
			this.template = template;
			this.channel = channel;
			this.block = -1;
			this.x = -1;
		}

		@Override
		public int read() {
			if(finished)return -1;
			if(block == -1 || block > 3) {
				findNextBlock();
				if(finished)return -1;
				block = 0;
			}
			int dt = img.getRGB(x, y);
			int shift = (block++) * 8;
			if((template.getRGB(x, y) & 0xff) == 0xff && block > 2) {
				block++;
			}
			return ((dt & (0xff << shift)) >>> shift) & 0xff;
		}

		private void findNextBlock() {
			int shift = 8 * (2 - channel);
			for(int y = this.y;y<img.getHeight();y++) {
				for(int x = this.x + 1;x<img.getWidth();x++) {
					int t = template.getRGB(x, y);
					if((((t & (0xff << shift)) >>> shift) & 0xff) == 0xff) {
						this.x = x;
						this.y = y;
						return;
					}
				}
				this.x = -1;
			}
			finished = true;
		}

		public void setChannel(int channel) {
			this.channel = channel;
		}

		public int getChannel() {
			return channel;
		}

		@Override
		public void close() {}
	}
}
