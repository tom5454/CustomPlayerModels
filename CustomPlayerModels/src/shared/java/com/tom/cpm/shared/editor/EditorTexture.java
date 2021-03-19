package com.tom.cpm.shared.editor;

import java.io.File;
import java.io.IOException;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.skin.TextureProvider;

public class EditorTexture extends TextureProvider {
	private boolean edited;
	public File file;
	public Box stitchPos;

	public EditorTexture() {
		super();
	}

	public EditorTexture(IOHelper in, int sizeLimit) throws IOException {
		super(in, sizeLimit);
	}

	public EditorTexture(TextureProvider texture) {
		super(texture.getImage(), new Vec2i(texture.size));
	}

	public void setRGB(int x, int y, int rgb) {
		texture.getImage().setRGB(x, y, rgb);
		markDirty();
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

	@Override
	public void free() {
		super.free();
		file = null;
	}
	//TODO editable?
}
