package com.tom.cpm.shared.paste;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.UI;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FuturePopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.paste.PasteClient.Paste;

public class PastePopup extends PopupPanel {
	private static PasteClient client = new PasteClient(MinecraftClientAccess.get().getMojangAPI(), PasteClient.URL, PasteClient.URL_CF);
	private final Frame frm;
	private Panel panel;
	private ScrollPanel scp;
	private Label statusLbl;

	public PastePopup(Frame frame) {
		super(frame.getGui());
		this.frm = frame;
		setBounds(new Box(0, 0, 400, 325));

		scp = new ScrollPanel(gui);
		scp.setBounds(new Box(5, 30, 390, 270));
		addElement(scp);

		panel = new Panel(gui);
		panel.setBackgroundColor(gui.getColors().button_border);
		scp.setDisplay(panel);
		panel.setBounds(new Box(0, 0, scp.getBounds().w, 16));

		statusLbl = new Label(gui, gui.i18nFormat("label.cpm.loading"));
		statusLbl.setBounds(new Box(100, 305, 200, 10));
		addElement(statusLbl);

		if (PasteClient.CAN_OPEN_BROWSER) {
			Button btnOpenBrowser = new Button(gui, gui.i18nFormat("button.cpm.paste.openBrowser"), () -> {
				runRequest(gui, PasteClient::createBrowserLoginURL, id -> {
					gui.openURL(client.getUrl() + "/login.html?id=" + id);
				}, () -> {}, "openBrowser");
			});
			btnOpenBrowser.setBounds(new Box(275, 5, 120, 20));
			addElement(btnOpenBrowser);
		} else {
			Button btnOpenBrowser = new Button(gui, gui.i18nFormat("button.cpm.paste.logout"), () -> {
				runRequest(gui, PasteClient::logout, __ -> close(), () -> {}, "logout");
			});
			btnOpenBrowser.setBounds(new Box(275, 5, 120, 20));
			addElement(btnOpenBrowser);
		}
	}

	public void open() {
		frm.openPopup(this);
		refreshGui();
	}

	private static void handleException(UI gui, Runnable retry, Runnable close, Throwable e) {
		if (e instanceof CompletionException)e = ((CompletionException)e).getCause();
		if (e instanceof InterruptedException)return;
		if (e instanceof LocalizedIOException) {
			gui.displayConfirm(gui.i18nFormat("label.cpm.error"),
					gui.i18nFormat("label.cpm.paste.error", ((LocalizedIOException) e).getLocalizedText().toString(gui)),
					retry, () -> {
						if(!client.isConnected())
							close.run();
					}, gui.i18nFormat("button.cpm.retry"));
		} else {
			gui.displayConfirm(gui.i18nFormat("label.cpm.error"),
					gui.i18nFormat("error.cpm.paste.unknownNetworkError", e.toString()),
					retry, () -> {
						if(!client.isConnected())
							close.run();
					}, gui.i18nFormat("button.cpm.retry"));
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

	public static <T> void runRequest(UI frm, Function<PasteClient, CompletableFuture<T>> task, Consumer<T> finish, Runnable close, String name) {
		if(!client.isConnected()) {
			runRequest0(frm, PasteClient::connect, v -> runRequest0(frm, task, finish, close, name), close, "connecting.message");
		} else {
			runRequest0(frm, task, finish, close, name);
		}
	}

	public static void runRequest(UI frm, Function<PasteClient, CompletableFuture<Void>> task, Runnable finish, Runnable close, String name) {
		runRequest(frm, task, v -> finish.run(), close, name);
	}

	private static <T> void runRequest0(UI gui, Function<PasteClient, CompletableFuture<T>> task, Consumer<T> finish, Runnable close, String name) {
		CompletableFuture<T> cf = task.apply(client);
		gui.displayPopup(frm -> new FuturePopup<>(frm, gui.i18nFormat("label.cpm.paste.connecting.title"), gui.i18nFormat("label.cpm.paste." + name), cf));
		cf.thenAcceptAsync(finish, gui::executeLater).exceptionally(e -> {
			gui.executeLater(() -> handleException(gui, () -> runRequest(gui, task, finish, close, name), close, e));
			return null;
		});
	}

	private <T> void runRequest(Supplier<CompletableFuture<T>> task, Consumer<T> finish, String name) {
		runRequest(gui, _c -> task.get(), finish, this::close, name);
	}

	private void runRequest(Supplier<CompletableFuture<Void>> task, Runnable finish, String name) {
		runRequest(gui, _c -> task.get(), finish, this::close, name);
	}

	private class PastePanel extends Panel {
		private Tooltip tooltip;

		public PastePanel(Paste entry, int y, int w, int i) {
			super(PastePopup.this.getGui());

			addElement(new Label(gui, entry.name).setBounds(new Box(5, 4, 200, 10)));

			tooltip = new Tooltip(frm, gui.i18nFormat("tooltip.cpm.paste.paste", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", entry.time), entry.id));

			Button btn = new Button(gui, gui.i18nFormat("button.cpm.delete"), ConfirmPopup.confirmHandler(frm, gui.i18nFormat("label.cpm.confirm"), gui.i18nFormat("label.cpm.confirmDel"), () -> {
				runRequest(() -> client.deleteFile(entry.id), PastePopup.this::refreshGui, "deleting");
			}));
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
