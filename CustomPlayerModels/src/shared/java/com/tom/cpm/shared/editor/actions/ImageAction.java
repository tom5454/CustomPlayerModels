package com.tom.cpm.shared.editor.actions;

import java.util.function.Consumer;

import com.tom.cpl.math.Box;
import com.tom.cpl.util.Image;

public class ImageAction extends Action {
	private Image img, undo;
	private Box box;
	private Consumer<Image> draw;

	public ImageAction(Image img, Box box, Consumer<Image> draw) {
		this.img = img;
		undo = new Image(box.w, box.h);
		undo.draw(img, -box.x, -box.y);
		this.box = box;
		this.draw = draw;
	}

	@Override
	public void undo() {
		img.draw(undo, box.x, box.y);
	}

	@Override
	public void run() {
		draw.accept(img);
	}
}
