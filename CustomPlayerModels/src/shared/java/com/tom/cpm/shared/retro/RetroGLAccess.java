package com.tom.cpm.shared.retro;

public interface RetroGLAccess<RL> {
	RetroLayer texture(RL tex);
	RetroLayer linesNoDepth();
	RetroLayer eyes(RL tex);
	RetroLayer color();
	RL getDynTexture();

	public static interface RetroLayer {}
}