package com.tom.cpm.shared.paste;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ProcessPopup;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpl.util.ThrowingFunction;
import com.tom.cpl.util.ThrowingRunnable;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.paste.PasteClient.LocalizedIOException;
import com.tom.cpm.shared.paste.PasteClient.Paste;

public class PastePopup extends PopupPanel {
	private static PasteClient client = new PasteClient(MinecraftClientAccess.get().getMojangAPI(), PasteClient.URL);
	private final Frame frm;
	private Panel panel;
	private ScrollPanel scp;
	private Label statusLbl;

	public PastePopup(Frame frame) {
		super(frame.getGui());
		this.frm = frame;
		setBounds(new Box(0, 0, 400, 300));

		scp = new ScrollPanel(gui);
		scp.setBounds(new Box(5, 5, 390, 270));
		addElement(scp);

		panel = new Panel(gui);
		panel.setBackgroundColor(0xff777777);
		scp.setDisplay(panel);
		panel.setBounds(new Box(0, 0, scp.getBounds().w, 16));

		statusLbl = new Label(gui, gui.i18nFormat("label.cpm.loading"));
		statusLbl.setBounds(new Box(100, 280, 200, 10));
		addElement(statusLbl);
	}

	public void open() {
		frm.openPopup(this);
		refreshGui();
	}


	private static void handleException(Frame frm, Runnable retry, Runnable close, Throwable e) {
		IGui gui = frm.getGui();
		if(e instanceof LocalizedIOException) {
			frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("label.cpm.error"),
					gui.i18nFormat(((LocalizedIOException) e).getLoc()),
					retry, () -> {
						if(!client.isConnected())
							close.run();
					}, gui.i18nFormat("button.cpm.retry")));
		} else {
			frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("label.cpm.error"),
					gui.i18nFormat("error.cpm.paste.unknownNetworkError", e.toString()),
					retry, () -> {
						if(!client.isConnected())
							close.run();
					}, gui.i18nFormat("button.cpm.retry")));
		}
	}

	private void refreshGui() {
		runRequest(client::listFiles,
				entries -> {
					panel.getElements().clear();

					panel.setBounds(new Box(0, 0, bounds.w - 10, entries.size() * 32));

					entries.sort(Comparator.comparing(p -> p.time));

					for (int i = 0; i < entries.size(); i++) {
						Paste e = entries.get(i);
						panel.addElement(new PastePanel(e, i * 32, scp.getBounds().w, i));
					}

					int mx = client.getMaxSize() / 1024;
					String maxSize = mx > 1024 ? (mx / 1024) + " MB" : mx + " kB";

					statusLbl.setText(gui.i18nFormat("label.cpm.paste.status", entries.size(), client.getMaxPastes(), maxSize));
				}, "listing");
	}

	public static <T> void runRequest(Frame frm, ThrowingFunction<PasteClient, T, Exception> task, Consumer<T> finish, Runnable close, String name) {
		if(!client.isConnected()) {
			runRequest0(frm, client -> {
				client.connect();
				return null;
			}, v -> runRequest0(frm, task, finish, close, name), close, "connecting.message");
		} else {
			runRequest0(frm, task, finish, close, name);
		}
	}

	public static void runRequest(Frame frm, ThrowingConsumer<PasteClient, Exception> task, Runnable finish, Runnable close, String name) {
		runRequest(frm, client -> {
			task.accept(client);
			return null;
		}, v -> finish.run(), close, name);
	}

	private static <T> void runRequest0(Frame frm, ThrowingFunction<PasteClient, T, Exception> task, Consumer<T> finish, Runnable close, String name) {
		IGui gui = frm.getGui();
		new ProcessPopup<>(frm, gui.i18nFormat("label.cpm.paste.connecting.title"), gui.i18nFormat("label.cpm.paste." + name),
				() -> task.apply(client), finish, e -> handleException(frm,
						() -> runRequest(frm, task, finish, close, name), close, e)).start();
	}

	private <T> void runRequest(Callable<T> task, Consumer<T> finish, String name) {
		runRequest(frm, _c -> task.call(), finish, this::close, name);
	}

	private void runRequest(ThrowingRunnable<Exception> task, Runnable finish, String name) {
		runRequest(frm, _c -> task.run(), finish, this::close, name);
	}

	private class PastePanel extends Panel {
		private Tooltip tooltip;

		public PastePanel(Paste entry, int y, int w, int i) {
			super(PastePopup.this.getGui());

			addElement(new Label(gui, entry.name).setBounds(new Box(5, 4, 200, 10)));

			tooltip = new Tooltip(frm, gui.i18nFormat("tooltip.cpm.paste.paste", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", entry.time), entry.id));

			Button btn = new Button(gui, gui.i18nFormat("button.cpm.delete"), new ConfirmPopup(frm, gui.i18nFormat("label.cpm.confirm"), gui.i18nFormat("label.cpm.confirmDel"), () -> {
				runRequest(() -> client.deleteFile(entry.id), PastePopup.this::refreshGui, "deleting");
			}, null));
			btn.setBounds(new Box(w - 55, 5, 45, 20));
			addElement(btn);

			setBounds(new Box(0, y, w, 31));

			if(i % 2 == 1) {
				setBackgroundColor(gui.getColors().menu_bar_background);
			}
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);
			if(event.isHovered(bounds))tooltip.set();
		}
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.edit.pastes");
	}
}
