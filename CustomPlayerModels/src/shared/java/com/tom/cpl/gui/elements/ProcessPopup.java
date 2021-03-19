package com.tom.cpl.gui.elements;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.math.Box;

public class ProcessPopup<R> extends PopupPanel {
	private String title;
	private Thread processThread;
	private Frame frame;
	private AtomicBoolean cancelled = new AtomicBoolean();

	public ProcessPopup(Frame frame, String title, String text, Callable<R> function, Consumer<R> finished, Consumer<Throwable> error) {
		super(frame.getGui());
		this.title = title;
		this.frame = frame;
		String[] lines = text.split("\\\\");

		int wm = 180;

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			addElement(new Label(gui, lines[i]).setBounds(new Box(wm / 2 - w / 2 + 10, 5 + i * 10, 0, 0)));
		}
		setBounds(new Box(0, 0, wm + 20, 45 + lines.length * 10));

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.cancel"), () -> {
			cancelled.set(true);
			processThread.interrupt();
		});
		ok.setBounds(new Box(wm / 2 - 10, 20 + lines.length * 10, 40, 20));
		addElement(ok);

		processThread = new Thread(() -> {
			try {
				R r = function.call();
				if(!cancelled.get()) {
					gui.executeLater(() -> finished.accept(r));
				} else {
					gui.executeLater(() -> error.accept(null));
				}
			} catch (Throwable e) {
				gui.executeLater(() -> error.accept(e));
			} finally {
				gui.executeLater(this::close);
			}
		}, "CPM Process Thread");
		processThread.setDaemon(true);
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void start() {
		processThread.start();
		frame.openPopup(this);
	}
}
