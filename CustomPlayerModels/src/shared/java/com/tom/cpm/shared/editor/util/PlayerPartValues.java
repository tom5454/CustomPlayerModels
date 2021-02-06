package com.tom.cpm.shared.editor.util;

import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.PlayerModelParts;

public enum PlayerPartValues {//               px, py, pz, ox, oy, oz, sx, sy, sz,  u,  v
	HEAD       (PlayerModelParts.HEAD,          0,  0,  0, -4, -8, -4,  8,  8,  8,  0,  0),
	BODY       (PlayerModelParts.BODY,          0,  0,  0, -4,  0, -2,  8, 12,  4, 16, 16),
	LEFT_ARM   (PlayerModelParts.LEFT_ARM,      5,  2,  0, -1, -2, -2,  4, 12,  4, 32, 48, 1),
	RIGHT_ARM  (PlayerModelParts.RIGHT_ARM,    -5,  2,  0, -3, -2, -2,  4, 12,  4, 40, 16, 1),
	LEFT_ARM_S (PlayerModelParts.LEFT_ARM,      5,  2,  0, -1, -2, -2,  3, 12,  3, 32, 48, 0),
	RIGHT_ARM_S(PlayerModelParts.RIGHT_ARM,    -5,  2,  0, -2, -2, -2,  3, 12,  3, 40, 16, 0),
	LEFT_LEG   (PlayerModelParts.LEFT_LEG,   1.9f, 12,  0, -2,  0, -2,  4, 12,  4, 16, 48),
	RIGHT_LEG  (PlayerModelParts.RIGHT_LEG, -1.9f, 12,  0, -2,  0, -2,  4, 12,  4,  0, 16),
	;
	public static final PlayerPartValues[] VALUES = values();
	public final PlayerModelParts part;
	public final float px, py, pz, ox, oy, oz, sx, sy, sz;
	public final int u, v, skinType;

	private PlayerPartValues(PlayerModelParts part, float px, float py, float pz, float ox, float oy, float oz, float sx, float sy, float sz, int u, int v) {
		this(part, px, py, pz, ox, oy, oz, sx, sy, sz, u, v, -1);
	}

	private PlayerPartValues(PlayerModelParts part, float px, float py, float pz, float ox, float oy, float oz,
			float sx, float sy, float sz, int u, int v, int skinType) {
		this.part = part;
		this.px = px;
		this.py = py;
		this.pz = pz;
		this.ox = ox;
		this.oy = oy;
		this.oz = oz;
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
		this.u = u;
		this.v = v;
		this.skinType = skinType;
	}

	public static PlayerPartValues getFor(PlayerModelParts part, int skinType) {
		for (PlayerPartValues v : VALUES) {
			if(v.part == part && (v.skinType == skinType || v.skinType == -1))
				return v;
		}
		return null;
	}

	public Vec3f getPos() {
		return new Vec3f(px, py, pz);
	}
}
