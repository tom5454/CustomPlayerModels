package com.tom.cpm.shared.editor.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.tom.cpl.gui.elements.ChooseElementPopup;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.template.Template.IArg;

public class TemplateArgHandler implements TreeElement {
	public String name;
	public String desc;
	public Tooltip tooltip;
	public Editor editor;
	public List<TreeElement> options;
	public List<ModelElement> effectedElems;
	public TemplateArgType type;
	public TemplateArg<?> handler;

	public TemplateArgHandler(Editor e, String name, String descIn, TemplateArgType type) {
		this.editor = e;
		this.name = name;
		this.desc = descIn;
		this.tooltip = desc.isEmpty() ? null : new Tooltip(e.frame, desc);
		this.type = type;
		options = new ArrayList<>();
		options.add(new TreeElement() {

			@Override
			public String getName() {
				return editor.gui().i18nFormat("label.cpm.desc");
			}

			@Override
			public void updateGui() {
				editor.updateName.accept(desc);
			}

			@Override
			public String getElemName() {
				return desc;
			}

			@Override
			public void setElemName(String name) {
				desc = name;
			}
		});
		handler = type.factory.get();
		if(handler.requiresParts()) {
			effectedElems = new ArrayList<>();
			options.add(new TreeElement() {
				private Map<ModelElement, TreeElement> wrappers = new HashMap<>();

				@Override
				public String getName() {
					return editor.gui().i18nFormat("label.cpm.arg_parts");
				}

				@Override
				public void getTreeElements(Consumer<TreeElement> c) {
					effectedElems.forEach(e -> c.accept(wrappers.computeIfAbsent(e, ArgElem::new)));
				}

				@Override
				public void addNew() {
					editor.frame.openPopup(new ChooseElementPopup<>(editor.frame,
							editor.gui().i18nFormat("label.cpm.arg_choose_part"),
							editor.gui().i18nFormat("label.cpm.arg_choose_part.desc"),
							Util.<ModelElement>listFromTree(c -> Editor.walkElements(editor.elements, c)).
							stream().filter(e -> e.type == ElementType.NORMAL && !e.templateElement).
							map(t -> new NamedElement<>(t, ModelElement::getName)).
							collect(Collectors.toList()),
							elem -> {
								if(elem != null) {
									editor.action("addToArg").addToList(effectedElems, elem.getElem()).execute();
									editor.updateGui();
								}
							}, null)
							);
				}

				@Override
				public void updateGui() {
					editor.setAddEn.accept(true);
				}
			});
		}
		handler.createTreeElements(options, e);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		options.forEach(c);
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public void delete() {
		editor.action("remove", "action.cpm.arg").removeFromList(editor.templateSettings.templateArgs, this).execute();
		editor.updateGui();
	}

	@Override
	public void updateGui() {
		editor.updateName.accept(name);
		tooltip = desc.isEmpty() ? null : new Tooltip(editor.frame, desc);
	}

	@Override
	public String getElemName() {
		return name;
	}

	@Override
	public void setElemName(String name) {
		this.name = name;
	}

	public class ArgElem implements TreeElement {
		public final ModelElement elem;

		public ArgElem(ModelElement elem) {
			this.elem = elem;
		}

		@Override
		public String getName() {
			return elem.getName();
		}

		@Override
		public void delete() {
			editor.action("rmFromArg").removeFromList(effectedElems, elem).execute();
			editor.updateGui();
		}

		@Override
		public void updateGui() {
			editor.setDelEn.accept(true);
		}
	}

	public void applyToModel() {
		if(effectedElems != null)
			handler.apply(effectedElems);
	}

	public static interface TemplateArg<T extends IArg> {
		default boolean requiresParts() { return true; }
		void saveProject(Map<String, Object> map);
		void loadProject(Map<String, Object> map);
		void loadTemplate(T arg);
		T export();
		void applyArgs(Map<String, Object> map, List<ModelElement> parts);
		default void createTreeElements(List<TreeElement> c, Editor editor) {}
		default void apply(List<? extends Cube> parts) {}
		default void applyToArg() {}
	}
}
