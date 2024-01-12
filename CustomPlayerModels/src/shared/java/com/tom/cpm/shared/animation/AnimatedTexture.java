package com.tom.cpm.shared.animation;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;

public class AnimatedTexture {
	private final ModelDefinition def;
	private final TextureSheetType sheet;
	private final Vec2i uvStart, uvSize;
	private final Vec2i animStart;
	private final int frameTime, frameCount;
	private final boolean anX, interpolate;

	private int lastFrame;
	private long lastUpdate;

	public AnimatedTexture(ModelDefinition def, TextureSheetType sheet, Vec2i uvStart, Vec2i uvSize, Vec2i animStart,
			int frameCount, int frameTime, boolean anX, boolean interpolate) {
		this.def = def;
		this.sheet = sheet;
		this.uvStart = uvStart;
		this.uvSize = uvSize;
		this.animStart = animStart;
		this.frameCount = frameCount;
		this.frameTime = frameTime;
		this.anX = anX;
		this.interpolate = interpolate;
	}

	public void update(long time, boolean inGui) {
		if(frameCount == 0 || frameTime == 0)return;
		int frm = (int) (time / frameTime % frameCount);
		if(interpolate) {
			if(time - lastUpdate > 50) {
				TextureProvider tex = def.getTexture(sheet, inGui);
				Image img = tex.getImage();

				float t = frm + (time % frameTime) / (float) frameTime;
				int uvx = uvStart.x;
				int uvy = uvStart.y;
				int sx = uvSize.x;
				int sy = uvSize.y;
				int ax = animStart.x;
				int ay = animStart.y;
				copyTextureInt(img, img, uvx, uvy, sx, sy, ax, ay, t, anX, frameCount);

				if(tex.texture != null)
					tex.texture.markDirty();
			}
		} else if(frm != lastFrame) {
			TextureProvider tex = def.getTexture(sheet, inGui);
			Image img = tex.getImage();
			lastFrame = frm;
			int uvx = uvStart.x;
			int uvy = uvStart.y;
			int sx = uvSize.x;
			int sy = uvSize.y;
			int ax = animStart.x;
			int ay = animStart.y;
			copyTexture(img, img, uvx, uvy, sx, sy, ax, ay, frm, anX);
			if(tex.texture != null)
				tex.texture.markDirty();
		}
	}

	public static void copyTexture(Image dest, Image img, int uvx, int uvy, int sx, int sy, int ax, int ay, int frame, boolean anX) {
		if(anX)
			ax += sx * frame;
		else
			ay += sy * frame;
		for(int x = 0;x<sx;x++) {
			int x1 = x + ax;
			int x2 = x + uvx;
			if (x1 >= 0 && x1 < img.getWidth() && x2 >= 0 && x2 < dest.getWidth()) {
				for(int y = 0;y<sy;y++) {
					int y1 = y + ay;
					int y2 = y + uvy;
					if (y1 >= 0 && y1 < img.getHeight() && y2 >= 0 && y2 < dest.getHeight())
						dest.setRGB(x2, y2, img.getRGB(x1, y1));
				}
			}
		}
	}

	public static void copyTextureInt(Image dest, Image img, int uvx, int uvy, int sx, int sy, int axIn, int ayIn, float time, boolean anX, int frms) {
		int frame = (int) time;
		float subfrm = 1 - (time - frame);
		int axf = axIn;
		int ayf = ayIn;
		int axs = axIn;
		int ays = ayIn;
		if(anX) {
			axf = axIn + sx * frame;
			axs = axIn + sx * ((frame + 1) % frms);
		} else {
			ayf = ayIn + sy * frame;
			ays = ayIn + sy * ((frame + 1) % frms);
		}
		for(int x = 0;x<sx;x++) {
			int xf = x + axf;
			int xs = x + axs;
			int x2 = x + uvx;
			if (xf >= 0 && xf < img.getWidth() && xs >= 0 && xs < img.getWidth() && x2 >= 0 && x2 < dest.getWidth()) {
				for(int y = 0;y<sy;y++) {
					int yf = y + ayf;
					int ys = y + ays;
					int y2 = y + uvy;
					if (yf >= 0 && yf < img.getHeight() && ys >= 0 && ys < img.getHeight() && y2 >= 0 && y2 < dest.getHeight()) {
						int first = img.getRGB(xf, yf);
						int second = img.getRGB(xs, ys);
						int r = mix(subfrm, first >> 16 & 255, second >> 16 & 255);
						int g = mix(subfrm, first >> 8 & 255, second >> 8 & 255);
						int b = mix(subfrm, first & 255, second & 255);
						dest.setRGB(x2, y2, first & -16777216 | r << 16 | g << 8 | b);
					}
				}
			}
		}
	}

	private static int mix(float time, int first, int second) {
		return (int)(time * first + (1.0F - time) * second);
	}
}
