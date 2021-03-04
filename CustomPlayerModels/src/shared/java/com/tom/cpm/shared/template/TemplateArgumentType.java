package com.tom.cpm.shared.template;

import java.util.function.Supplier;

import com.tom.cpm.shared.template.Template.IArg;
import com.tom.cpm.shared.template.args.AngleArg;
import com.tom.cpm.shared.template.args.BoolArg;
import com.tom.cpm.shared.template.args.ColorArg;
import com.tom.cpm.shared.template.args.Float1Arg;
import com.tom.cpm.shared.template.args.Float2Arg;
import com.tom.cpm.shared.template.args.TexArg;

public enum TemplateArgumentType {
	COLOR(ColorArg::new),
	TEX(TexArg::new),
	FLOAT1(Float1Arg::new),
	FLOAT2(Float2Arg::new),
	ANGLE(AngleArg::new),
	BOOL(BoolArg::new),
	;
	public static final TemplateArgumentType[] VALUES = values();
	private final Supplier<IArg> factory;
	private TemplateArgumentType(Supplier<IArg> factory) {
		this.factory = factory;
	}

	public IArg create() {
		return factory.get();
	}

	public static TemplateArgumentType lookup(String name) {
		for (TemplateArgumentType t : VALUES) {
			if(t.name().equalsIgnoreCase(name))return t;
		}
		return null;
	}

	public static IArg create(String name) {
		TemplateArgumentType type = lookup(name);
		if(type == null)throw new RuntimeException("Missing arg type");
		return type.create();
	}
}
