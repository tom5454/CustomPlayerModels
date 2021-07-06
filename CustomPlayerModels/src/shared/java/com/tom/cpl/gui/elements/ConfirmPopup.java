package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.math.Box;

public class ConfirmPopup extends PopupPanel implements Runnable {
	private boolean okPressed;
	private Frame frm;
	private String title;

	public ConfirmPopup(Frame frame, String msg, Runnable ok, Runnable cancel) {
		this(frame, frame.getGui().i18nFormat("label.cpm.confirm"), msg, ok, cancel);
	}

	public ConfirmPopup(Frame frame, String title, String msg, Runnable ok, Runnable cancel) {
		this(frame,title, msg, ok, cancel, frame.getGui().i18nFormat("button.cpm.ok"));
	}

	public ConfirmPopup(Frame frame, String title, String msg, Runnable ok, Runnable cancel, String okTxt) {
		this(frame,title, msg, ok, cancel, okTxt, frame.getGui().i18nFormat("button.cpm.cancel"));
	}

	public ConfirmPopup(Frame frame, String title, String msg, Runnable ok, Runnable cancel, String okTxt, String cancelTxt) {
		super(frame.getGui());
		this.frm = frame;
		this.title = title;

		String[] lines = msg.split("\\\\");

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

		Button btn = new Button(gui, okTxt, () -> {
			okPressed = true;
			close();
			ok.run();
		});
		Button btnNo = new Button(gui, cancelTxt, () -> {
			close();
			if(cancel != null)cancel.run();
		});
		btn.setBounds(new Box(5, 20 + lines.length * 10, 25 + gui.textWidth(okTxt), 20));
		btnNo.setBounds(new Box(35 + gui.textWidth(okTxt), 20 + lines.length * 10, 25 + gui.textWidth(cancelTxt), 20));
		addElement(btn);
		addElement(btnNo);
		if(cancel != null) {
			setOnClosed(() -> {
				if(!okPressed)cancel.run();
			});
		}
	}

	@Override
	public void run() {
		frm.openPopup(this);
	}

	@Override
	public String getTitle() {
		return title;
	}
}
