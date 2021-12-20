package com.tom.cpm.shared.model;

import com.tom.cpl.math.BoundingBox;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;

public enum PlayerPartValues implements PartValues {//                          px, py, pz, ox, oy, oz, sx, sy, sz,  u,  v, u2, v2, [type]
	HEAD       (PlayerModelParts.HEAD,      PlayerSkinLayer.HAT,                 0,  0,  0, -4, -8, -4,  8,  8,  8,  0,  0, 32,  0),
	BODY       (PlayerModelParts.BODY,      PlayerSkinLayer.JACKET,              0,  0,  0, -4,  0, -2,  8, 12,  4, 16, 16, 16, 32),
	LEFT_ARM   (PlayerModelParts.LEFT_ARM,  PlayerSkinLayer.LEFT_SLEEVE,         5,  2,  0, -1, -2, -2,  4, 12,  4, 32, 48, 48, 48, SkinType.DEFAULT),
	RIGHT_ARM  (PlayerModelParts.RIGHT_ARM, PlayerSkinLayer.RIGHT_SLEEVE,       -5,  2,  0, -3, -2, -2,  4, 12,  4, 40, 16, 40, 32, SkinType.DEFAULT),
	LEFT_ARM_S (PlayerModelParts.LEFT_ARM,  PlayerSkinLayer.LEFT_SLEEVE,         5,  2,  0, -1, -2, -2,  3, 12,  4, 32, 48, 48, 48, SkinType.SLIM),
	RIGHT_ARM_S(PlayerModelParts.RIGHT_ARM, PlayerSkinLayer.RIGHT_SLEEVE,       -5,  2,  0, -2, -2, -2,  3, 12,  4, 40, 16, 40, 32, SkinType.SLIM),
	LEFT_LEG   (PlayerModelParts.LEFT_LEG,  PlayerSkinLayer.LEFT_PANTS_LEG,   1.9f, 12,  0, -2,  0, -2,  4, 12,  4, 16, 48,  0, 48),
	RIGHT_LEG  (PlayerModelParts.RIGHT_LEG, PlayerSkinLayer.RIGHT_PANTS_LEG, -1.9f, 12,  0, -2,  0, -2,  4, 12,  4,  0, 16,  0, 32),
	;
	public static final BoundingBox PLAYER_BOUNDING_BOX = new BoundingBox(-0.3F, 0, -0.3F, 0.3F, 1.8F, 0.3F);
	public static final BoundingBox PLAYER_SNEAKING_BOUNDING_BOX = new BoundingBox(-0.3F, 0, -0.3F, 0.3F, 1.5F, 0.3F);
	public static final BoundingBox PLAYER_SMALL_BOUNDING_BOX = new BoundingBox(-0.3F, 0, -0.3F, 0.3F, 0.6F, 0.3F);
	public static final BoundingBox PLAYER_SKULL = new BoundingBox(-0.25F, 0, -0.25F, 0.25F, 0.5F, 0.25F).mul(1 / 1.1f);
	public static final float PLAYER_EYE_HEIGHT = 1.62F;
	public static final float PLAYER_EYE_HEIGHT_SNEAKING = 1.27F;
	public static final float PLAYER_EYE_HEIGHT_SMALL = 0.4F;

	public static final PlayerPartValues[] VALUES = values();
	public final PlayerModelParts part;
	public final PlayerSkinLayer layer;
	public final float px, py, pz, ox, oy, oz, sx, sy, sz;
	public final int u, v, u2, v2;
	public final SkinType skinType;

	private PlayerPartValues(PlayerModelParts part, PlayerSkinLayer layer, float px, float py, float pz, float ox, float oy, float oz, float sx, float sy, float sz, int u, int v, int u2, int v2) {
		this(part, layer, px, py, pz, ox, oy, oz, sx, sy, sz, u, v, u2, v2, SkinType.UNKNOWN);
	}

	private PlayerPartValues(PlayerModelParts part, PlayerSkinLayer layer, float px, float py, float pz, float ox, float oy, float oz,
			float sx, float sy, float sz, int u, int v, int u2, int v2, SkinType skinType) {
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

	public static PlayerPartValues getFor(PlayerModelParts part, SkinType skinType) {
		for (PlayerPartValues v : VALUES) {
			if(v.part == part && (v.skinType == skinType || v.skinType == SkinType.UNKNOWN))
				return v;
		}
		return null;
	}

	@Override
	public Vec3f getPos() {
		return new Vec3f(px, py, pz);
	}

	@Override
	public Vec3f getOffset() {
		return new Vec3f(ox, oy, oz);
	}

	@Override
	public Vec3f getSize() {
		return new Vec3f(sx, sy, sz);
	}

	@Override
	public Vec2i getUV() {
		return new Vec2i(u, v);
	}

	@Override
	public boolean isMirror() {
		return false;
	}

	@Override
	public float getMCScale() {
		return 0;
	}

	public static float getEyeHeight(VanillaPose pose) {
		if(pose == null)pose = VanillaPose.STANDING;
		switch(pose) {
		case FLYING:
		case SWIMMING:
			return PLAYER_EYE_HEIGHT_SMALL;

		case SNEAKING:
			return PLAYER_EYE_HEIGHT_SNEAKING;

		case SKULL_RENDER:
			return -1;

		default:
			return PLAYER_EYE_HEIGHT;
		}
	}

	public static BoundingBox getBounds(VanillaPose pose) {
		if(pose == null)return PLAYER_BOUNDING_BOX;
		switch(pose) {
		case FLYING:
		case SWIMMING:
			return PLAYER_SMALL_BOUNDING_BOX;

		case SNEAKING:
			return PLAYER_SNEAKING_BOUNDING_BOX;

		case SKULL_RENDER:
			return PLAYER_SKULL;

		default:
			return PLAYER_BOUNDING_BOX;
		}
	}
}
