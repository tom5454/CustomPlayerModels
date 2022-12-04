package com.tom.cpm.shared.editor.elements;

import java.util.function.Consumer;

import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;

public enum RootGroups {
	CAPE(TextureSheetType.CAPE, RootModelType.CAPE),
	ELYTRA(TextureSheetType.ELYTRA, RootModelType.ELYTRA_LEFT, RootModelType.ELYTRA_RIGHT),
	ARMOR(null, RootModelType.ARMOR_HELMET,
			RootModelType.ARMOR_BODY,
			RootModelType.ARMOR_LEFT_ARM,
			RootModelType.ARMOR_RIGHT_ARM,
			RootModelType.ARMOR_LEGGINGS_BODY,
			RootModelType.ARMOR_LEFT_LEG,
			RootModelType.ARMOR_RIGHT_LEG,
			RootModelType.ARMOR_LEFT_FOOT,
			RootModelType.ARMOR_RIGHT_FOOT) {

		@Override
		public TextureSheetType getTexSheet(RootModelType forType) {
			return forType == RootModelType.ARMOR_LEFT_LEG || forType == RootModelType.ARMOR_RIGHT_LEG || forType == RootModelType.ARMOR_LEGGINGS_BODY ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1;
		}
	}
	;
	public static final RootGroups[] VALUES = values();

	public final RootModelType[] types;
	private final TextureSheetType texSheet;
	private RootGroups(TextureSheetType tex, RootModelType... modelTypes) {
		types = modelTypes;
		this.texSheet = tex;
	}

	public static void forEach(Consumer<RootGroups> c) {
		for (int i = 0; i < VALUES.length; i++) {
			c.accept(VALUES[i]);
		}
	}

	public static RootGroups getGroup(RootModelType type) {
		for (int i = 0; i < VALUES.length; i++) {
			RootGroups rootGroups = VALUES[i];
			for (int j = 0; j < rootGroups.types.length; j++) {
				RootModelType ty = rootGroups.types[j];
				if(ty == type)return rootGroups;
			}
		}
		return null;
	}

	public TextureSheetType getTexSheet(RootModelType forType) {
		return texSheet;
	}
}
