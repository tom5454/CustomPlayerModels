package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.io.IOException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;

public class FileChooserGui extends PopupPanel {
	private File currDir;
	private File selected;
	private TextField path, name;
	private Runnable accept;
	private Button acceptBtn;
	private boolean saveDialog;
	private FileDisplay files;
	private BiPredicate<File, String> filter;
	private Function<String, String> extAdder;
	private Label fileDescLabel;
	private String title;

	public FileChooserGui(Frame frame) {
		super(frame.getGui());
		setBounds(new Box(0, 0, 210, 210));
		fileDescLabel = new Label(gui, "");
		fileDescLabel.setBounds(new Box(5, 175, 200, 10));
		addElement(fileDescLabel);
		currDir = MinecraftClientAccess.get().getGameDir().getAbsoluteFile();
		path = new TextField(gui);
		name = new TextField(gui);
		path.setBounds(new Box(5, 10, 200, 20));
		name.setBounds(new Box(5, 185, 160, 20));
		addElement(name);
		addElement(path);
		acceptBtn = new Button(gui, "", null);
		acceptBtn.setBounds(new Box(165, 185, 40, 20));
		addElement(acceptBtn);
		path.setText(currDir.getAbsolutePath());
		files = new FileDisplay();
		files.setBounds(new Box(5, 35, 200, 135));
		addElement(files);
		filter = (a, b) -> true;
		extAdder = Function.identity();

		acceptBtn.setAction(() -> {
			if(selected != null) {
				if(saveDialog) {
					if(!filter.test(selected, selected.getName())) {
						File ext = new File(selected.getParentFile(), extAdder.apply(selected.getName()));
						if(filter.test(ext, ext.getName())) {
							selected = ext;
							name.setText(ext.getName());
						}
					}
					if(selected.exists()) {
						frame.openPopup(new ConfirmPopup(frame, gui.i18nFormat("label.cpm.overwrite"), gui.i18nFormat("label.cpm.overwrite"), () -> {
							close();
							accept.run();
						}, () -> frame.openPopup(this)));
					} else {
						close();
						accept.run();
					}
				} else if(selected.exists()) {
					if(!filter.test(selected, selected.getName()))return;
					close();
					accept.run();
				}
			} else {
				if(!filter.test(currDir, currDir.getName()))return;
				selected = currDir;
				close();
				accept.run();
			}
		});
		path.setEventListener(() -> {
			File dir = new File(path.getText());
			if(dir.exists() && dir.isDirectory()) {
				currDir = dir;
				files.refresh();
			}
		});
		name.setEventListener(() -> {
			selected = new File(currDir, name.getText());
		});
		files.refresh();
	}

	public void setTitle(String text) {
		title = text;
	}

	public void setCurrentDirectory(File currDir) {
		this.currDir = currDir;
		selected = new File(currDir, name.getText());
		path.setText(currDir.getAbsolutePath());
		files.refresh();
	}

	public File getSelected() {
		return selected;
	}

	public void setAccept(Runnable accept) {
		this.accept = accept;
	}

	public void setAccept(Consumer<File> accept) {
		this.accept = () -> accept.accept(selected);
	}

	public void setButtonText(String text) {
		acceptBtn.setText(text);
	}

	public void setSaveDialog(boolean saveDialog) {
		this.saveDialog = saveDialog;
	}

	public void setFilter(BiPredicate<File, String> filter) {
		this.filter = filter;
		files.refresh();
	}

	public void setFileDescText(String text) {
		this.fileDescLabel.setText(text);
	}

	public void setExtAdder(Function<String, String> extAdder) {
		this.extAdder = extAdder;
	}

	public void setFileName(String name) {
		this.name.setText(name);
		selected = new File(currDir, name);
	}

	private class FileDisplay extends GuiElement {
		private int scroll;
		private String[] files;

		public FileDisplay() {
			super(getGui());
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			gui.pushMatrix();
			gui.setPosOffset(bounds);
			mouseX -= bounds.x;
			mouseY -= bounds.y;
			int y = 0;
			for (int i = scroll; i < files.length && (i - scroll) < (bounds.h / 10); i++) {
				String string = files[i];
				int yp = y++;
				int textColor = 0xffffffff;
				if(mouseX > 0 && mouseY > yp * 10 && mouseY < (yp+1) * 10 && mouseX < bounds.w) {
					textColor = 0xffffff00;
				}
				if(selected != null && selected.getName().equals(string) && selected.getParentFile().equals(currDir)) {
					gui.drawBox(0, yp * 10, bounds.w, 10, 0xff6666ff);
				}
				gui.drawText(2, yp * 10 + 1, string, textColor);
			}
			gui.popMatrix();
		}

		@Override
		public boolean mouseClick(int x, int y, int btn) {
			if(bounds.isInBounds(x, y)) {
				int yp = (y - bounds.y) / 10 + scroll;
				if(yp >= 0 && yp < files.length) {
					String string = files[yp];
					if(selected != null && selected.getName().equals(string) && selected.getParentFile().equals(currDir)) {
						if(selected.isDirectory()) {
							try {
								currDir = selected.getCanonicalFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
							selected = null;
							refresh();
							return true;
						}
					}
					selected = new File(currDir, string);
					if(filter.test(selected, string))
						name.setText(selected.getName());
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseWheel(int x, int y, int dir) {
			if(bounds.isInBounds(x, y)) {
				int newScroll = scroll - dir;
				if(newScroll >= 0 && newScroll <= (files.length - bounds.h / 10)) {
					scroll = newScroll;
				}
				return true;
			}
			return false;
		}

		private void refresh() {
			scroll = 0;
			String[] fs = currDir.list((a, name) -> {
				File file = new File(currDir, name);
				if(file.isHidden())return false;
				if(file.isDirectory())return true;
				return filter.test(file, name);
			});
			if(currDir.toPath().getNameCount() == 0) {
				files = fs;
			} else {
				files = new String[fs.length + 1];
				files[0] = "..";
				System.arraycopy(fs, 0, files, 1, fs.length);
			}
			path.setText(currDir.getAbsolutePath());
		}
	}

	@Override
	public String getTitle() {
		return title;
	}
}
