package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.EmbeddedLocalizations;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.MultiSelector;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.util.OpenRasterFile;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;

public class ExportUVMapPopup extends PopupPanel {
	private EditorGui eg;
	private Checkbox layeredExport, onlySelected;

	public ExportUVMapPopup(EditorGui eg) {
		super(eg.getGui());
		this.eg = eg;

		setBounds(new Box(0, 0, 200, 80));

		layeredExport = new Checkbox(gui, gui.i18nFormat("label.cpm.uvexport.layered"));
		layeredExport.setBounds(new Box(5, 5, 190, 20));
		layeredExport.setSelected(true);
		layeredExport.setAction(() -> layeredExport.setSelected(!layeredExport.isSelected()));
		layeredExport.setTooltip(new Tooltip(eg, gui.i18nFormat("tooltip.cpm.uvexport.layered")));
		addElement(layeredExport);

		onlySelected = new Checkbox(gui, gui.i18nFormat("label.cpm.uvexport.selected"));
		onlySelected.setBounds(new Box(5, 30, 190, 20));
		onlySelected.setAction(() -> onlySelected.setSelected(!onlySelected.isSelected()));
		addElement(onlySelected);

		String okTxt = gui.i18nFormat("button.cpm.ok");
		String cancelTxt = gui.i18nFormat("button.cpm.cancel");

		Button btn = new Button(gui, okTxt, () -> {
			close();
			if (layeredExport.isSelected())exportUVMapLayers();
			else exportUVMapPNG();
		});
		Button btnNo = new Button(gui, cancelTxt, this::close);
		btn.setBounds(new Box(5, 55, 25 + gui.textWidth(okTxt), 20));
		btnNo.setBounds(new Box(35 + gui.textWidth(okTxt), 55, 25 + gui.textWidth(cancelTxt), 20));
		addElement(btn);
		addElement(btnNo);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.tools.exportUVMap");
	}

	private void exportUVMapLayers() {
		Set<ModelElement> me = onlySelected.isSelected() ? getSelected() : null;
		FileChooserPopup fc = new FileChooserPopup(eg);
		fc.setTitle(EmbeddedLocalizations.exportUV);
		fc.setFileDescText(EmbeddedLocalizations.fileOra);
		fc.setFilter(new FileFilter("ora"));
		fc.setSaveDialog(true);
		fc.setExtAdder(n -> n + ".ora");
		fc.setAccept(f -> exportUVMapLayers(f, me));
		fc.setButtonText(eg.getGui().i18nFormat("button.cpm.ok"));
		eg.openPopup(fc);
	}

	private void exportUVMapPNG() {
		Set<ModelElement> me = onlySelected.isSelected() ? getSelected() : null;
		FileChooserPopup fc = new FileChooserPopup(eg);
		fc.setTitle(EmbeddedLocalizations.exportUV);
		fc.setFileDescText(EmbeddedLocalizations.filePng);
		fc.setFilter(new FileFilter("png"));
		fc.setSaveDialog(true);
		fc.setExtAdder(n -> n + ".png");
		fc.setAccept(f -> exportUVMap(f, me));
		fc.setButtonText(eg.getGui().i18nFormat("button.cpm.ok"));
		eg.openPopup(fc);
	}

	private void exportUVMapLayers(File out, Set<ModelElement> sel) {
		Editor editor = eg.getEditor();
		ETextures tex = editor.getTextureProvider();
		if(tex == null)return;
		OpenRasterFile o = new OpenRasterFile(tex.provider.getImage().getWidth(), tex.provider.getImage().getHeight());
		o.addLayer(0, tex.getTexture().provider.getImage(), 0, 0, "current");

		OpenRasterFile.Stack st = o.addStack("UVMap", 1);

		for (int i = 0; i < editor.elements.size(); i++) {
			ModelElement m = editor.elements.get(i);
			OpenRasterFile.Stack s = st.addStack(m.getName(), i + 1);
			exportUVMapLayers(tex, m, s, sel);
		}

		CompletableFuture<Void> cf;
		editor.setInfoMsg.accept(Pair.of(200000, eg.getGui().i18nFormat("tooltip.cpm.saving", out.getName())));
		try {
			cf = o.write(out).thenRunAsync(() -> {
				editor.setInfoMsg.accept(Pair.of(2000, eg.getGui().i18nFormat("tooltip.cpm.saveSuccess", out.getName())));
			}, eg.getGui()::executeLater);
		} catch (Exception e) {
			cf = new CompletableFuture<>();
			cf.completeExceptionally(e);
		}

		cf.handleAsync((v, e) -> {
			if(e != null) {
				Log.warn("Error exporting UV map", e);
				ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.save", e);
				eg.openPopup(new MessagePopup(eg, eg.getGui().i18nFormat("label.cpm.error.save"), e.toString()));
				editor.setInfoMsg.accept(Pair.of(0, ""));
				return null;
			}
			return null;
		}, eg.getGui()::executeLater);
	}

	private void exportUVMap(File out, Set<ModelElement> sel) {
		Editor editor = eg.getEditor();
		ETextures tex = editor.getTextureProvider();
		if(tex == null)return;
		Image res = new Image(tex.provider.getImage().getWidth(), tex.provider.getImage().getHeight());

		for (int i = 0; i < editor.elements.size(); i++) {
			ModelElement m = editor.elements.get(i);
			exportUVMap(tex, m, res, sel);
		}

		editor.setInfoMsg.accept(Pair.of(200000, eg.getGui().i18nFormat("tooltip.cpm.saving", out.getName())));
		try {
			ImageIO.write(res, out);
			editor.setInfoMsg.accept(Pair.of(2000, eg.getGui().i18nFormat("tooltip.cpm.saveSuccess", out.getName())));
		} catch (Exception e) {
			Log.warn("Error exporting UV map", e);
			ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.save", e);
			eg.openPopup(new MessagePopup(eg, eg.getGui().i18nFormat("label.cpm.error.save"), e.toString()));
			editor.setInfoMsg.accept(Pair.of(0, ""));
		}
	}

	private Set<ModelElement> getSelected() {
		Set<ModelElement> me = new HashSet<>();
		TreeElement te = eg.getEditor().selectedElement;
		if (te instanceof MultiSelector) {
			((MultiSelector) te).forEachSelected(t -> {
				if (t instanceof ModelElement)
					me.add((ModelElement) t);
			});
		} else {
			ModelElement m = eg.getEditor().getSelectedElement();
			if (m != null)me.add(m);
		}
		return me;
	}

	private void exportUVMapLayers(ETextures tex, ModelElement me, OpenRasterFile.Stack st, Set<ModelElement> sel) {
		boolean inc = sel == null || sel.contains(me);
		if (inc && me.type == ElementType.NORMAL && me.texture) {
			if (me.faceUV != null) {
				OpenRasterFile.Stack pf = st.addStack("Per-Face", 0);
				for (Direction d : Direction.VALUES) {
					Face f = me.faceUV.get(d);
					if (f != null) {
						Box box = Box.fromArea(f.sx, f.sy, f.ex, f.ey);
						if (box.w == 0 || box.h == 0)continue;
						Image img = new Image(box.w, box.h);
						img.fill(0, 0, box.w, box.h, 0xff888888);
						pf.addLayer(d.ordinal(), img, box.x, box.y, d.name());
					}
				}
			} else {
				Box box = me.getTextureBox();
				if (box.w != 0 && box.h != 0) {
					int ts = Math.abs(me.texSize);
					int bx = me.u * ts;
					int by = me.v * ts;
					int dx = MathHelper.ceil(me.size.x * ts);
					int dy = MathHelper.ceil(me.size.y * ts);
					int dz = MathHelper.ceil(me.size.z * ts);

					Image img = new Image(box.w, box.h);
					img.fill(dx + dz, dz, dz, dy, 0xffff0000);
					img.fill(0, dz, dz, dy, 0xffdd0000);
					img.fill(dz, 0, dx, dz, 0xff00ff00);
					img.fill(dz + dx, 0, dx, dz, 0xff00dd00);
					img.fill(dz, dz, dx, dy, 0xff0000ff);
					img.fill(dz * 2 + dx, dz, dx, dy, 0xff0000dd);

					st.addLayer(0, img, bx, by, me.getName());
				}
			}
		}

		for (int i = 0; i < me.children.size(); i++) {
			ModelElement m = me.children.get(i);
			OpenRasterFile.Stack s = st.addStack(m.getName(), i + 1);
			exportUVMapLayers(tex, m, s, sel);
		}
	}

	private void exportUVMap(ETextures tex, ModelElement me, Image img, Set<ModelElement> sel) {
		boolean inc = sel == null || sel.contains(me);
		if (inc && me.type == ElementType.NORMAL && me.texture) {
			if (me.faceUV != null) {
				for (Direction d : Direction.VALUES) {
					Face f = me.faceUV.get(d);
					if (f != null) {
						Box box = Box.fromArea(f.sx, f.sy, f.ex, f.ey);
						if (box.w == 0 || box.h == 0)continue;
						img.fill(box.x, box.y, box.w, box.h, 0xff888888);
					}
				}
			} else {
				Box box = me.getTextureBox();
				if (box.w != 0 && box.h != 0) {
					int ts = Math.abs(me.texSize);
					int bx = me.u * ts;
					int by = me.v * ts;
					int dx = MathHelper.ceil(me.size.x * ts);
					int dy = MathHelper.ceil(me.size.y * ts);
					int dz = MathHelper.ceil(me.size.z * ts);

					img.fill(bx + dx + dz, by + dz, dz, dy, 0xffff0000);
					img.fill(bx, by + dz, dz, dy, 0xffdd0000);
					img.fill(bx + dz, by, dx, dz, 0xff00ff00);
					img.fill(bx + dz + dx, by, dx, dz, 0xff00dd00);
					img.fill(bx + dz, by + dz, dx, dy, 0xff0000ff);
					img.fill(bx + dz * 2 + dx, by + dz, dx, dy, 0xff0000dd);
				}
			}
		}

		for (int i = 0; i < me.children.size(); i++) {
			ModelElement m = me.children.get(i);
			exportUVMap(tex, m, img, sel);
		}
	}
}
