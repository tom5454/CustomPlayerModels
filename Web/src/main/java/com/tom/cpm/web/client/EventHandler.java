package com.tom.cpm.web.client;

import java.io.File;
import java.util.List;

public interface EventHandler {
	void render(int mouseX, int mouseY);
	void init(int w, int h);
	boolean mouseClicked(int mouseX, int mouseY, int btn);
	boolean mouseDragged(int mouseX, int mouseY, int button);
	boolean mouseReleased(int mouseX, int mouseY, int button);
	boolean mouseScrolled(int mouseX, int mouseY, int delta);
	boolean charTyped(int codePoint);
	boolean keyPressed(int keyCode, String key);
	boolean canClose();
	void tick();
	void filesDropped(List<File> files);
}