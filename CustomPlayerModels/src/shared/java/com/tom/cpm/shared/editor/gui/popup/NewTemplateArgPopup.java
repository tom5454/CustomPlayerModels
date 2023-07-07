package com.tom.cpm.shared.editor.gui.popup;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.template.TemplateArgType;

public class NewTemplateArgPopup extends PopupPanel {

	public NewTemplateArgPopup(IGui gui, Editor e) {
		super(gui);

		TextField name = new TextField(gui);
		TextField desc = new TextField(gui);

		addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 5, 100, 10)));
		name.setBounds(new Box(5, 15, 190, 20));
		addElement(new Label(gui, gui.i18nFormat("label.cpm.arg_desc")).setBounds(new Box(5, 40, 100, 10)));
		desc.setBounds(new Box(5, 50, 190, 20));

		addElement(name);
		addElement(desc);

		DropDownBox<NamedElement<TemplateArgType>> argType = new DropDownBox<>(gui.getFrame(), Arrays.stream(TemplateArgType.values()).
				filter(t -> t.canBeAdded).
				map(t -> new NamedElement<>(t, v -> gui.i18nFormat("label.cpm.template_arg." + v.name().toLowerCase(Locale.ROOT)))).
				collect(Collectors.toList()));
		argType.setBounds(new Box(5, 80, 190, 20));
		addElement(argType);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			e.templateSettings.templateArgs.add(new TemplateArgHandler(e, name.getText(), desc.getText(), argType.getSelected().getElem()));
			e.markDirty();
			e.updateGui();
		});
		ok.setBounds(new Box(5, 110, 50, 20));
		addElement(ok);

		setBounds(new Box(0, 0, 200, 140));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.new_template_arg");
	}
}
