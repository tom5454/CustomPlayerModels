package com.tom.cpl.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.ImageIO.IImageIO;

public class AWTImageIO implements IImageIO {

	@Override
	public Image read(File f) throws IOException {
		return toImage(javax.imageio.ImageIO.read(f));
	}

	@Override
	public Image read(InputStream f) throws IOException {
		return toImage(javax.imageio.ImageIO.read(f));
	}

	@Override
	public void write(Image img, File f) throws IOException {
		javax.imageio.ImageIO.write(toBufferedImage(img), "PNG", f);
	}

	@Override
	public void write(Image img, OutputStream f) throws IOException {
		javax.imageio.ImageIO.write(toBufferedImage(img), "PNG", f);
	}

	@Override
	public Vec2i getSize(InputStream din) throws IOException {
		try(ImageInputStream in = javax.imageio.ImageIO.createImageInputStream(din)){
			final Iterator<ImageReader> readers = javax.imageio.ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					int w = reader.getWidth(0);
					int h = reader.getHeight(0);
					return new Vec2i(w, h);
				} finally {
					reader.dispose();
				}
			}
		}
		return new Vec2i();
	}

	public static BufferedImage toBufferedImage(Image i) {
		BufferedImage img = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int[] to = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		System.arraycopy(i.getData(), 0, to, 0, i.getData().length);
		return img;
	}

	public static Image toImage(BufferedImage bi) {
		Image i = new Image(bi.getWidth(), bi.getHeight());
		for(int y = 0;y<i.getHeight();y++) {
			for(int x = 0;x<i.getWidth();x++) {
				i.setRGB(x, y, bi.getRGB(x, y));
			}
		}
		return i;
	}
}
