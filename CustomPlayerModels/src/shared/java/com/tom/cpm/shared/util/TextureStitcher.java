package com.tom.cpm.shared.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.definition.SafetyException;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.skin.TextureProvider;

public class TextureStitcher {
	private Image image;
	private Map<Image, Box> images = new HashMap<>();
	private Set<Box> usedSpaces;
	private Vec2i size;
	private boolean hasStitches = false;
	private TextureProvider provider;
	private float xs, ys;
	private int maxSize;
	private boolean overflow;

	public TextureStitcher(int maxSize) {
		this.maxSize = maxSize;
	}

	public void setBase(Image base) {
		image = new Image(base);
		this.size = new Vec2i(64, 64);
		usedSpaces = new HashSet<>();
		usedSpaces.add(new Box(0, 0, 64, 64));
		xs = 1;
		ys = 1;
		this.provider = null;
	}

	public void setBase(TextureProvider provider) {
		image = new Image(provider.getImage());
		this.size = provider.size;
		usedSpaces = new HashSet<>();
		Box b = new Box(0, 0, provider.size.x, provider.size.y);
		usedSpaces.add(b);
		images.put(provider.getImage(), b);
		xs = provider.getImage().getWidth()  / (float) provider.size.x;
		ys = provider.getImage().getHeight() / (float) provider.size.y;
		this.provider = provider;
	}

	public void stitchImage(EditorTexture provider) {
		stitchImage(provider, uv -> provider.stitchPos = new Box(uv.x, uv.y, provider.getImage().getWidth(), provider.getImage().getHeight()));
	}

	public void stitchImage(TextureProvider provider, Consumer<Vec2i> uvUpdater) {
		float xs = provider.getImage().getWidth()  / (float) provider.size.x;
		float ys = provider.getImage().getHeight() / (float) provider.size.y;
		stitchImage(provider.getImage(), uv -> {
			if(uvUpdater != null) {
				uvUpdater.accept(new Vec2i(uv.x / xs, uv.y / ys));
			}
		});
	}

	public void stitchImage(Image img, Consumer<Vec2i> uvUpdater) {
		if(overflow)return;
		Box b = new Box(0, 0, img.getWidth(), img.getHeight());
		hasStitches = true;
		for(int i = 0;i<maxSize;i+=16*xs) {
			b.x = i;
			for(int y = 0;y<i;y+=16*ys) {
				b.y = y;
				if(usedSpaces.stream().noneMatch(u -> u.intersects(b))) {
					usedSpaces.add(b);
					checkImageSize(b);
					image.draw(img, b.x, b.y);
					images.put(img, b);
					if(uvUpdater != null)uvUpdater.accept(new Vec2i(b.x, b.y));
					return;
				}
			}
			b.y = i;
			for(int x = 0;x<i;x+=16*xs) {
				b.x = x;
				if(usedSpaces.stream().noneMatch(u -> u.intersects(b))) {
					usedSpaces.add(b);
					checkImageSize(b);
					image.draw(img, b.x, b.y);
					images.put(img, b);
					if(uvUpdater != null)uvUpdater.accept(new Vec2i(b.x / xs, b.y / ys));
					return;
				}
			}
		}
		overflow = true;
	}

	public void allocSingleColor(Vec2i size, int color, Consumer<Vec2i> uvUpdater) {
		Image img = new Image(size.x, size.y);
		img.fill(color);
		stitchImage(img, uvUpdater);
	}

	public void checkImageSize(Box box) {
		if(image.getWidth() < box.x + box.w || image.getHeight() < box.y + box.h) {
			Image newImg = new Image(Math.max(smallestEncompassingPowerOfTwo(box.x + box.w), image.getWidth()),
					Math.max(smallestEncompassingPowerOfTwo(box.y + box.h), image.getHeight()));
			newImg.draw(image);
			image = newImg;
		}
	}

	public static int smallestEncompassingPowerOfTwo(int value) {
		int i = value - 1;
		i = i | (i >> 1);
		i = i | (i >> 2);
		i = i | (i >> 4);
		i = i | (i >> 8);
		i = i | (i >> 16);
		return i + 1;
	}

	public TextureProvider finish() throws SafetyException {
		if(overflow)throw new SafetyException(BlockReason.TEXTURE_OVERFLOW);
		if(hasStitches)return new TextureProvider(image, new Vec2i(image.getWidth() / xs, image.getHeight() / ys));
		else return provider;
	}

	public void finish(TextureProvider provider) {
		provider.setImage(image);
		provider.size = new Vec2i(image.getWidth() / xs, image.getHeight() / ys);
	}

	public boolean hasStitches() {
		return hasStitches;
	}

	public boolean refresh(Image img) {
		Box box = images.get(img);
		if(box == null)return false;
		image.draw(img, box.x, box.y);
		return true;
	}
}
