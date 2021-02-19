package com.tom.cpm.shared.skin;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;
import com.tom.cpm.shared.math.Vec2i;
import com.tom.cpm.shared.util.DynamicTexture;
import com.tom.cpm.shared.util.Image;

public class SkinProvider {
	public DynamicTexture texture;
	private boolean edited;
	public Vec2i size;

	public SkinProvider() {
		size = new Vec2i(64, 64);
	}

	public SkinProvider(IOHelper in, int sizeLimit) throws IOException {
		size = in.read2s();
		ImageBlock block = in.readImage();
		if(block.getWidth() > sizeLimit || block.getHeight() > sizeLimit)
			throw new IOException("Texture size too large");
		block.doReadImage();
		texture = new DynamicTexture(block.getImage());
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

	public void setRGB(int x, int y, int rgb) {
		texture.getImage().setRGB(x, y, rgb);
		markDirty();
	}

	public void setImage(Image image) {
		if(texture == null)texture = new DynamicTexture(image);
		else texture.setImage(image);
	}

	public void markDirty() {
		if(texture != null)texture.markDirty();
		this.edited = true;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}
}
