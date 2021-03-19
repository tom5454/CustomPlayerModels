package com.tom.cpm.shared.skin;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.DynamicTexture;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;

public class TextureProvider {
	public DynamicTexture texture;
	public Vec2i size;

	public TextureProvider() {
		size = new Vec2i(64, 64);
	}

	public TextureProvider(IOHelper in, int sizeLimit) throws IOException {
		size = in.read2s();
		ImageBlock block = in.readImage();
		if(block.getWidth() > sizeLimit || block.getHeight() > sizeLimit)
			throw new IOException("Texture size too large");
		block.doReadImage();
		texture = new DynamicTexture(block.getImage());
	}

	public TextureProvider(Image imgIn, Vec2i size) {
		this.size = size;
		texture = new DynamicTexture(imgIn);
	}

	public void write(IOHelper dout) throws IOException {
		dout.write2s(size);
		try (OutputStream baos = dout.writeNextBlock().getDout()) {
			ImageIO.write(texture.getImage().toBufferedImage(), "PNG", baos);
		}
	}

	public void bind() {
		if(texture == null)return;
		texture.bind();
	}

	public void free() {
		if(texture != null)texture.free();
	}

	public Vec2i getSize() {
		return size;
	}

	public Image getImage() {
		return texture == null ? null : texture.getImage();
	}

	public void setImage(Image image) {
		if(texture == null)texture = new DynamicTexture(image);
		else texture.setImage(image);
	}
}
