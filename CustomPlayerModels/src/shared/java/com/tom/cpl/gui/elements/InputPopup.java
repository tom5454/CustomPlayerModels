package com.tom.cpl.gui.elements;

import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.math.Box;

public class InputPopup extends PopupPanel implements Runnable {
	private boolean okPressed;
	private Frame frm;
	private String title;
	private TextField inputField;
	private Consumer<String> ok;

	public InputPopup(Frame frame, String msg, Consumer<String> ok, Runnable cancel) {
		this(frame, frame.getGui().i18nFormat("label.cpm.input"), msg, ok, cancel);
	}

	public InputPopup(Frame frame, String title, String msg, Consumer<String> ok, Runnable cancel) {
		super(frame.getGui());
		this.frm = frame;
		this.title = title;
		this.ok = ok;

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
		setBounds(new Box(0, 0, wm + 20, 70 + lines.length * 10));

		inputField = new TextField(gui);
		inputField.setBounds(new Box(5, 20 + lines.length * 10, wm + 10, 20));
		addElement(inputField);

		Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), this::okPressed);
		Button btnNo = new Button(gui, gui.i18nFormat("button.cpm.cancel"), () -> {
			close();
			if(cancel != null)cancel.run();
			inputField.setText("");
		});
		btn.setBounds(new Box(5, 45 + lines.length * 10, 40, 20));
		btnNo.setBounds(new Box(50, 45 + lines.length * 10, 40, 20));
		addElement(btn);
		addElement(btnNo);
		setOnClosed(() -> {
			if(cancel != null) {
				if(!okPressed)cancel.run();
			}
			inputField.setText("");
		});
	}

	private void okPressed() {
		okPressed = true;
		String text = inputField.getText();
		close();
		ok.accept(text);
		inputField.setText("");
	}

	@Override
	public void run() {
		frm.openPopup(this);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.matches(gui.getKeyCodes().KEY_ENTER)) {
			okPressed();
			event.consume();
		}
		super.keyPressed(event);
	}
}
