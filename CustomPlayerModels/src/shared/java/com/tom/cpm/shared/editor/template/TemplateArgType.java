package com.tom.cpm.shared.editor.template;

import java.util.function.Supplier;

import com.tom.cpm.shared.editor.template.TemplateArgHandler.TemplateArg;
import com.tom.cpm.shared.editor.template.args.ColorEditorArg;
import com.tom.cpm.shared.editor.template.args.TexEditorArg;
import com.tom.cpm.shared.template.TemplateArgumentType;

public enum TemplateArgType {
	COLOR(TemplateArgumentType.COLOR, true, ColorEditorArg::new),
	TEX(TemplateArgumentType.TEX, false, TexEditorArg::new),
	;
	public static final TemplateArgType[] VALUES = values();
	public final TemplateArgumentType baseType;
	public final Supplier<TemplateArg<?>> factory;
	public final boolean canBeAdded;

	private TemplateArgType(TemplateArgumentType type, boolean canBeAdded, Supplier<TemplateArg<?>> factory) {
		this.baseType = type;
		this.canBeAdded = canBeAdded;
		this.factory = factory;
	}

	public static TemplateArgType lookup(String name) {
		for (TemplateArgType t : VALUES) {
			if(t.name().equalsIgnoreCase(name))return t;
		}
		return null;
	}
}
