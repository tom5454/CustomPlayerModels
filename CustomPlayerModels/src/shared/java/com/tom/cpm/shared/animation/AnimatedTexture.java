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

	public void update(long time) {
		if(frameCount == 0 || frameTime == 0)return;
		int frm = (int) (time / frameTime % frameCount);
		if(interpolate) {
			if(time - lastUpdate > 50) {
				TextureProvider tex = def.getTexture(sheet, false);
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
			TextureProvider tex = def.getTexture(sheet, false);
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
			for(int y = 0;y<sy;y++) {
				dest.setRGB(x + uvx, y + uvy, img.getRGB(x + ax, y + ay));
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
			for(int y = 0;y<sy;y++) {
				int first = img.getRGB(x + axf, y + ayf);
				int second = img.getRGB(x + axs, y + ays);
				int r = mix(subfrm, first >> 16 & 255, second >> 16 & 255);
				int g = mix(subfrm, first >> 8 & 255, second >> 8 & 255);
				int b = mix(subfrm, first & 255, second & 255);
				dest.setRGB(x + uvx, y + uvy, first & -16777216 | r << 16 | g << 8 | b);
			}
		}
	}

	private static int mix(float time, int first, int second) {
		return (int)(time * first + (1.0F - time) * second);
	}
}
