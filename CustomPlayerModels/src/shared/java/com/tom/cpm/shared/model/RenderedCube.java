package com.tom.cpm.shared.model;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.IModelComponent;

public class RenderedCube implements IModelComponent {
	private Cube cube;
	private RenderedCube parent;
	public List<RenderedCube> children;
	public Vec3f offset;
	public Vec3f rotation;
	public Vec3f pos;
	public Object renderObject;
	public boolean display = true;
	public boolean useDynamic = false;
	public int color;

	//Render Effect Glow
	public boolean glow = false;
	//Render Effect Hide
	public boolean hidden = false;
	//Render Effect Recolor
	public boolean recolor = false;

	protected RenderedCube() {
	}

	public RenderedCube(Cube cube) {
		this.cube = cube;
		reset();
	}

	@Override
	public void reset() {
		if(cube.offset != null)this.offset = new Vec3f(cube.offset);
		if(cube.rotation != null)this.rotation = new Vec3f(cube.rotation);
		if(cube.pos != null)this.pos = new Vec3f(cube.pos);
		this.color = recolor || cube.texSize == 0 ? cube.rgb : 0xffffff;
		this.display = !hidden;
	}

	public void setParent(RenderedCube parent) {
		this.parent = parent;
	}

	public void addChild(RenderedCube cube) {
		if(children == null)children = new ArrayList<>();
		children.add(cube);
	}

	public Cube getCube() {
		return cube;
	}

	public boolean doDisplay() {
		return display;
	}

	public RenderedCube getParent() {
		return parent;
	}

	public ElementSelectMode getSelected() {
		return ElementSelectMode.NULL;
	}

	public void setCube(Cube cube) {
		this.cube = cube;
	}

	public int getId() {
		return cube.id;
	}

	@Override
	public void setPosition(boolean add, float x, float y, float z) {
		if(add) {
			pos.x += x;
			pos.y += y;
			pos.z += z;
		} else {
			pos.x = x;
			pos.y = y;
			pos.z = z;
		}
	}

	@Override
	public void setRotation(boolean add, float x, float y, float z) {
		//System.out.println(this + ".setRotation(" + add + ", " + x + ", " + y + ", " + z + ")");
		if(add) {
			rotation.x += x;
			rotation.y += y;
			rotation.z += z;
		} else {
			rotation.x = x;
			rotation.y = y;
			rotation.z = z;
		}
	}

	@Override
	public void setVisible(boolean v) {
		display = v;
	}

	@Override
	public void setColor(float r, float g, float b) {
		//System.out.println(this + ".setColor(" + r + ", " + g + ", " + b + ")");
		if(recolor || cube.texSize == 0) {
			color = (MathHelper.clamp((int) r, 0, 255) << 16) |
					(MathHelper.clamp((int) g, 0, 255) << 8)  |
					MathHelper.clamp((int) b, 0, 255);
			//System.out.println(r + " " + g + " " + b + " " + Integer.toHexString(color));
		}
	}

	@Override
	public Vec3f getPosition() {
		return pos;
	}

	@Override
	public Vec3f getRotation() {
		return rotation;
	}

	@Override
	public boolean isVisible() {
		return display;
	}

	@Override
	public int getRGB() {
		if(cube == null)return -1;
		return recolor || cube.texSize == 0 ? cube.rgb : -1;
	}

	public static enum ElementSelectMode {
		NULL(false, true),
		SELECTED(true, true),
		SEL_CHILDREN(true, true),
		SEL_ONLY(true, true),
		PAINT_MODE(false, false),
		;
		private boolean renderOutline, applyColor;

		private ElementSelectMode(boolean renderOutline, boolean applyColor) {
			this.renderOutline = renderOutline;
			this.applyColor = applyColor;
		}

		public boolean isRenderOutline() {
			return renderOutline;
		}

		public boolean applyColor() {
			return applyColor;
		}
	}
}
