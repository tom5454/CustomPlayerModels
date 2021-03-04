package com.tom.cpm.shared.editor.template;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.popup.NewTemplateArgPopup;
import com.tom.cpm.shared.editor.tree.TreeElement;

public class TemplateSettings implements TreeElement {
	public List<TemplateArgHandler> templateArgs = new ArrayList<>();
	public boolean hasTex;
	private Editor e;
	private List<TreeElement> elems;
	public String name;

	public TemplateSettings(Editor e) {
		this.e = e;
		elems = new ArrayList<>();
		elems.add(new ArgsList());
	}

	@Override
	public String getName() {
		return e.gui().i18nFormat("label.cpm.tree.template_settings");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		elems.forEach(c);
	}

	private class ArgsList implements TreeElement {

		@Override
		public String getName() {
			return e.gui().i18nFormat("label.cpm.tree.template_arguments");
		}

		@Override
		public void getTreeElements(Consumer<TreeElement> c) {
			templateArgs.forEach(c);
		}

		@Override
		public void addNew() {
			e.frame.openPopup(new NewTemplateArgPopup(e));
		}

		@Override
		public void updateGui() {
			e.setAddEn.accept(true);
		}
	}
}
