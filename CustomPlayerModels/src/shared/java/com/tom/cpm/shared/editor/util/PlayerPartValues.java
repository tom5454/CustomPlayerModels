package com.tom.cpm.shared.editor.util;

import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.PlayerModelParts;

public enum PlayerPartValues {//                                                px, py, pz, ox, oy, oz, sx, sy, sz,  u,  v, u2, v2, [type]
	HEAD       (PlayerModelParts.HEAD,      PlayerSkinLayer.HAT,                 0,  0,  0, -4, -8, -4,  8,  8,  8,  0,  0, 32,  0),
	BODY       (PlayerModelParts.BODY,      PlayerSkinLayer.JACKET,              0,  0,  0, -4,  0, -2,  8, 12,  4, 16, 16, 16, 32),
	LEFT_ARM   (PlayerModelParts.LEFT_ARM,  PlayerSkinLayer.LEFT_SLEEVE,         5,  2,  0, -1, -2, -2,  4, 12,  4, 32, 48, 48, 48, 1),
	RIGHT_ARM  (PlayerModelParts.RIGHT_ARM, PlayerSkinLayer.RIGHT_SLEEVE,       -5,  2,  0, -3, -2, -2,  4, 12,  4, 40, 16, 40, 32, 1),
	LEFT_ARM_S (PlayerModelParts.LEFT_ARM,  PlayerSkinLayer.LEFT_SLEEVE,         5,  2,  0, -1, -2, -2,  3, 12,  3, 32, 48, 48, 48, 0),
	RIGHT_ARM_S(PlayerModelParts.RIGHT_ARM, PlayerSkinLayer.RIGHT_SLEEVE,       -5,  2,  0, -2, -2, -2,  3, 12,  3, 40, 16, 40, 32, 0),
	LEFT_LEG   (PlayerModelParts.LEFT_LEG,  PlayerSkinLayer.LEFT_PANTS_LEG,   1.9f, 12,  0, -2,  0, -2,  4, 12,  4, 16, 48,  0, 48),
	RIGHT_LEG  (PlayerModelParts.RIGHT_LEG, PlayerSkinLayer.RIGHT_PANTS_LEG, -1.9f, 12,  0, -2,  0, -2,  4, 12,  4,  0, 16,  0, 32),
	;
	public static final PlayerPartValues[] VALUES = values();
	public final PlayerModelParts part;
	public final PlayerSkinLayer layer;
	public final float px, py, pz, ox, oy, oz, sx, sy, sz;
	public final int u, v, skinType, u2, v2;

	private PlayerPartValues(PlayerModelParts part, PlayerSkinLayer layer, float px, float py, float pz, float ox, float oy, float oz, float sx, float sy, float sz, int u, int v, int u2, int v2) {
		this(part, layer, px, py, pz, ox, oy, oz, sx, sy, sz, u, v, u2, v2, -1);
	}

	private PlayerPartValues(PlayerModelParts part, PlayerSkinLayer layer, float px, float py, float pz, float ox, float oy, float oz,
			float sx, float sy, float sz, int u, int v, int u2, int v2, int skinType) {
		this.part = part;
		this.layer = layer;
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
		this.u2 = u2;
		this.v2 = v2;
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

	public Vec3f getOffset() {
		return new Vec3f(ox, oy, oz);
	}

	public Vec3f getSize() {
		return new Vec3f(sx, sy, sz);
	}
}
