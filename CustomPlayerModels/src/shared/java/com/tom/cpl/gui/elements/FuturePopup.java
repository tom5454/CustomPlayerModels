package com.tom.cpl.gui.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.math.Box;

public class FuturePopup<R> extends PopupPanel {
	protected String title;
	protected CompletableFuture<R> cf;
	protected final Frame frame;
	protected AtomicBoolean cancelled = new AtomicBoolean();
	protected Button cancel;

	public FuturePopup(Frame frame, String title, String text, CompletableFuture<R> cf) {
		super(frame.getGui());
		this.frame = frame;
		this.title = title;
		this.cf = cf;
		cancel = new Button(gui, gui.i18nFormat("button.cpm.cancel"), () -> {
			cancelled.set(true);
			cf.completeExceptionally(new InterruptedException());
		});
		addElement(cancel);

		cf.handleAsync((a, b) -> {
			close();
			return null;
		}, gui::executeLater);
		setupLabels(text);
	}

	protected List<GuiElement> setupLabels(String text) {
		List<GuiElement> labels = new ArrayList<>();
		String[] lines = text.split("\\\\");

		int wm = 180;

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			labels.add(addElement(new Label(gui, lines[i]).setBounds(new Box(wm / 2 - w / 2 + 10, 15 + i * 10, 0, 0))));
		}
		setBounds(new Box(0, 0, wm + 20, 55 + lines.length * 10));

		cancel.setBounds(new Box(wm / 2 - 10, 30 + lines.length * 10, 40, 20));
		return labels;
	}

	@Override
	public String getTitle() {
		return title;
	}
}
