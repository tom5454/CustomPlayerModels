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
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;

public class CopyTransformSettingsPopup extends PopupPanel {

	public CopyTransformSettingsPopup(Frame frm, Editor e, CopyTransformEffect cte) {
		super(frm.getGui());
		setBounds(new Box(0, 0, 300, 400));

		FlowLayout layout = new FlowLayout(this, 4, 1);

		List<ModelElement> elems = new ArrayList<>();
		Editor.walkElements(e.elements, elems::add);
		NameMapper<ModelElement> mapper = new NameMapper<>(elems, ModelElement::getName);
		ListPicker<NamedElement<ModelElement>> picker = new ListPicker<>(frm, mapper.asList());
		mapper.setSetter(picker::setSelected);
		if(cte.from != null)mapper.setValue(cte.from);

		picker.setBounds(new Box(5, 0, 280, 20));
		addElement(picker);

		boolean[] pos = new boolean[] {cte.copyPX, cte.copyPY, cte.copyPZ};
		boolean[] rot = new boolean[] {cte.copyRX, cte.copyRY, cte.copyRZ};
		boolean[] rsc = new boolean[] {cte.copySX, cte.copySY, cte.copySZ};

		createPanel("position", pos);
		createPanel("rotation", rot);
		createPanel("render_scale", rsc);

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
			ab.execute();
			close();
		});
		ok.setBounds(new Box(5, 0, 60, 20));
		addElement(ok);

		layout.reflow();
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
