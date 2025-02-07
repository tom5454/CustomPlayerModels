package com.tom.cpm.shared.editor.gui.popup;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.elements.ModelElement;

public class CopyTransformSettingsPopup extends PopupPanel {

	public CopyTransformSettingsPopup(Frame frm, Editor e, CopyTransformEffect cte) {
		super(frm.getGui());
		setBounds(new Box(0, 0, 300, 400));

		FlowLayout layout = new FlowLayout(this, 4, 1);

		List<CTOrigin> elems = new ArrayList<>();
		walkElements(0, e.elements, elems);
		ListPicker<CTOrigin> picker = new ListPicker<>(frm, elems);
		if(cte.from != null)picker.setSelected(elems.stream().filter(c -> c.elem == cte.from).findFirst().orElse(null));
		picker.setListLoader(l -> {
			//l.setComparator((a, b) -> 0);
			l.setRenderer(CTOrigin::draw);
			l.setGetWidth(CTOrigin::width);
		});
		picker.setBounds(new Box(5, 0, 280, 20));
		addElement(picker);

		boolean[] pos = new boolean[] {cte.copyPX, cte.copyPY, cte.copyPZ};
		boolean[] rot = new boolean[] {cte.copyRX, cte.copyRY, cte.copyRZ};
		boolean[] rsc = new boolean[] {cte.copySX, cte.copySY, cte.copySZ};

		createPanel("position", pos);
		createPanel("rotation", rot);
		createPanel("render_scale", rsc);

		Checkbox cbxV = new Checkbox(gui, gui.i18nFormat("label.cpm.visible"));
		cbxV.setBounds(new Box(5, 0, 170, 20));
		cbxV.setSelected(cte.copyVis);
		cbxV.setAction(() -> cbxV.setSelected(!cbxV.isSelected()));
		addElement(cbxV);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			ActionBuilder ab = e.action("set", "label.cpm.copyTransformSettings");
			ab.updateValueOp(cte, cte.from, picker.getSelected().getElem(), (a, b) -> a.from = b);
			ab.updateValueOp(cte, cte.copyPX, pos[0], (a, b) -> a.copyPX = b);
			ab.updateValueOp(cte, cte.copyPY, pos[1], (a, b) -> a.copyPY = b);
			ab.updateValueOp(cte, cte.copyPZ, pos[2], (a, b) -> a.copyPZ = b);
			ab.updateValueOp(cte, cte.copyRX, rot[0], (a, b) -> a.copyRX = b);
			ab.updateValueOp(cte, cte.copyRY, rot[1], (a, b) -> a.copyRY = b);
			ab.updateValueOp(cte, cte.copyRZ, rot[2], (a, b) -> a.copyRZ = b);
			ab.updateValueOp(cte, cte.copySX, rsc[0], (a, b) -> a.copySX = b);
			ab.updateValueOp(cte, cte.copySY, rsc[1], (a, b) -> a.copySY = b);
			ab.updateValueOp(cte, cte.copySZ, rsc[2], (a, b) -> a.copySZ = b);
			ab.updateValueOp(cte, cte.copyVis, cbxV.isSelected(), (a, b) -> a.copyVis = b);
			ab.execute();
			close();
		});
		ok.setBounds(new Box(5, 0, 60, 20));
		addElement(ok);

		layout.reflow();
	}

	private class CTOrigin {
		private ModelElement elem;
		private int d;

		public CTOrigin(ModelElement elem, int d) {
			this.elem = elem;
			this.d = d;
		}

		public ModelElement getElem() {
			return elem;
		}

		@Override
		public String toString() {
			return elem.getName();
		}

		private void draw(int x, int y, int w, int h, boolean hovered, boolean selected) {
			int bg = gui.getColors().select_background;
			if(hovered)bg = gui.getColors().popup_background;
			if(selected || hovered)gui.drawBox(x, y, w, h, bg);
			int c = elem.textColor(gui);
			if (c == 0) {
				c = gui.getColors().button_text_color;
				if(hovered)c = gui.getColors().button_text_hover;
			}
			gui.drawText(x + 3, y + h / 2 - 4, toString(), c);
		}

		private int width() {
			return gui.textWidth(elem.getName());
		}
	}

	private void walkElements(int d, List<ModelElement> elem, List<CTOrigin> elems) {
		for (ModelElement modelElement : elem) {
			elems.add(new CTOrigin(modelElement, d));
			walkElements(d + 1, modelElement.children, elems);
		}
	}

	private void createPanel(String name, boolean[] v) {
		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 0, 170, 30));

		panel.addElement(new Label(gui, gui.i18nFormat("label.cpm." + name)).setBounds(new Box(5, 0, 100, 10)));

		Checkbox cbx = new Checkbox(gui, "X");
		Checkbox cby = new Checkbox(gui, "Y");
		Checkbox cbz = new Checkbox(gui, "Z");

		cbx.setSelected(v[0]);
		cby.setSelected(v[1]);
		cbz.setSelected(v[2]);

		cbx.setAction(() -> {
			v[0] = !v[0];
			cbx.setSelected(v[0]);
		});
		cby.setAction(() -> {
			v[1] = !v[1];
			cby.setSelected(v[1]);
		});
		cbz.setAction(() -> {
			v[2] = !v[2];
			cbz.setSelected(v[2]);
		});

		cbx.setBounds(new Box(5, 10, 50, 20));
		cby.setBounds(new Box(60, 10, 50, 20));
		cbz.setBounds(new Box(115, 10, 50, 20));

		panel.addElement(cbx);
		panel.addElement(cby);
		panel.addElement(cbz);

		addElement(panel);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.copyTransformSettings");
	}
}
