package com.tom.cpm.shared.editor;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.util.TextureStitcher;

public class ETextures implements TreeElement {
	private final Editor e;
	private TextureSheetType type;
	public EditorTexture provider;
	private EditorTexture renderTexture;
	public TextureStitcher stitcher;
	public Consumer<TextureStitcher> textureLoader;
	public File file;
	private Image defaultImg;

	public ETextures(Editor e, TextureSheetType type) {
		this(e, type, (Consumer<TextureStitcher>) null);
	}

	public ETextures(Editor e, TextureSheetType type, Consumer<TextureStitcher> textureLoader) {
		this(e, new EditorTexture());
		this.type = type;
		this.textureLoader = textureLoader;
		if(textureLoader != null) {
			renderTexture = new EditorTexture();
			stitcher = new TextureStitcher();
		}
	}

	public ETextures(Editor e, EditorTexture provider) {
		this.e = e;
		this.provider = provider;
	}

	public void free() {
		provider.free();
		if(textureLoader != null)renderTexture.free();
	}

	public void clean() {
		this.provider.texture = null;
		this.provider.setEdited(false);
		this.file = null;
	}

	public void markDirty() {
		provider.markDirty();
		refreshTexture();
	}

	public boolean isEdited() {
		return provider.isEdited();
	}

	public void setEdited(boolean edited) {
		provider.setEdited(edited);
	}

	public void refreshTexture(EditorTexture tex) {
		if(textureLoader == null)return;
		if(stitcher.refresh(tex.getImage()))
			renderTexture.markDirty();
	}

	public void restitchTexture() {
		if(textureLoader == null)return;
		stitcher = new TextureStitcher();
		stitcher.setBase(provider);
		textureLoader.accept(stitcher);
		stitcher.finish(renderTexture);
		renderTexture.markDirty();
	}

	public void setRGB(int x, int y, int rgb) {
		provider.setRGB(x, y, rgb);
	}

	public void write(IOHelper dout) throws IOException {
		provider.write(dout);
	}

	public void setImage(Image image) {
		provider.setImage(image);
	}

	public void refreshTexture() {
		refreshTexture(provider);
	}

	public Image getImage() {
		return provider.getImage();
	}

	public boolean hasStitches() {
		if(textureLoader == null)return false;
		return stitcher.hasStitches();
	}

	public EditorTexture getRenderTexture() {
		return textureLoader != null ? renderTexture : provider;
	}

	@Override
	public String getName() {
		return e.gui().i18nFormat("label.cpm.texture." + type.name().toLowerCase());
	}

	@Override
	public ETextures getTexture() {
		return this;
	}

	public void setDefaultImg(Image defaultImg) {
		this.defaultImg = defaultImg;
	}

	public Image copyDefaultImg() {
		return new Image(defaultImg);
	}

	public boolean isEditable() {
		return type != null ? type.editable : false;
	}
}
