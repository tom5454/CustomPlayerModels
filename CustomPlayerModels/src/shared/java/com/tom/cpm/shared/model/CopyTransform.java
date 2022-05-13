package com.tom.cpm.shared.model;

import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.definition.ModelDefinition;

public class CopyTransform implements IAnimation {
	private final RenderedCube from;
	private final RenderedCube to;
	private final boolean copyPX;
	private final boolean copyPY;
	private final boolean copyPZ;
	private final boolean copyRX;
	private final boolean copyRY;
	private final boolean copyRZ;
	private final boolean copySX;
	private final boolean copySY;
	private final boolean copySZ;

	public CopyTransform(RenderedCube from, RenderedCube to, short copy) {
		this.from = from;
		this.to = to;
		copyPX = (copy & (1 << 0)) != 0;
		copyPY = (copy & (1 << 1)) != 0;
		copyPZ = (copy & (1 << 2)) != 0;
		copyRX = (copy & (1 << 3)) != 0;
		copyRY = (copy & (1 << 4)) != 0;
		copyRZ = (copy & (1 << 5)) != 0;
		copySX = (copy & (1 << 6)) != 0;
		copySY = (copy & (1 << 7)) != 0;
		copySZ = (copy & (1 << 8)) != 0;
	}

	@Override
	public int getDuration() {
		return 1;
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public void animate(long millis, ModelDefinition def) {
		to.setPosition(false,
				copyPX ? from.getPosition().x : to.getPosition().x,
						copyPY ? from.getPosition().y : to.getPosition().y,
								copyPZ ? from.getPosition().z : to.getPosition().z);
		to.setRotation(false,
				copyRX ? from.getRotation().x : to.getRotation().x,
						copyRY ? from.getRotation().y : to.getRotation().y,
								copyRZ ? from.getRotation().z : to.getRotation().z);
		to.setRenderScale(false,
				copySX ? from.getRenderScale().x : to.getRenderScale().x,
						copySY ? from.getRenderScale().y : to.getRenderScale().y,
								copySZ ? from.getRenderScale().z : to.getRenderScale().z);
	}
}
