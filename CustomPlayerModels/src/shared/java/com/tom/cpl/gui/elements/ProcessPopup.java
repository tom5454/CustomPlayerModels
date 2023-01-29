package com.tom.cpl.gui.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.math.Box;

public class ProcessPopup<R> extends PopupPanel implements Runnable {
	protected String title;
	protected Thread processThread;
	protected final Frame frame;
	protected AtomicBoolean cancelled = new AtomicBoolean();
	protected Callable<R> function;
	protected Consumer<R> finished;
	protected Consumer<Throwable> error;
	protected Button cancel;

	public ProcessPopup(Frame frame, String title, String text, Callable<R> function, CompletableFuture<R> cf) {
		this(frame, title, text, function, cf::complete, cf::completeExceptionally);
	}

	public ProcessPopup(Frame frame, String title, String text, Callable<R> function, Consumer<R> finished, Consumer<Throwable> error) {
		this(frame);
		this.title = title;
		this.function = function;
		this.finished = finished;
		this.error = error;
		setupLabels(text);
	}

	protected ProcessPopup(Frame frame) {
		super(frame.getGui());
		this.frame = frame;

		cancel = new Button(gui, gui.i18nFormat("button.cpm.cancel"), () -> {
			cancelled.set(true);
			processThread.interrupt();
		});
		addElement(cancel);

		processThread = new Thread(this, "CPM Process Thread");
		processThread.setDaemon(true);
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

	public void start() {
		processThread.start();
		frame.openPopup(this);
	}

	@Override
	public void run() {
		try {
			R r = function.call();
			if(!cancelled.get()) {
				gui.executeLater(() -> finished.accept(r));
			} else {
				gui.executeLater(() -> error.accept(new InterruptedException()));
			}
		} catch (Throwable e) {
			if(!cancelled.get())gui.executeLater(() -> error.accept(e));
		} finally {
			gui.executeLater(this::close);
		}
	}
}
