package com.tom.cpm.shared.editor.util;

import com.tom.cpl.util.FormatText;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.util.SafetyLevel.SafetyReport;

public class SafetyChecks {

	public static boolean textureCheck(Editor e, Integer max, SafetyReport report) {
		int size = e.textures.values().stream().mapToInt(ETextures::getMaxSize).max().orElse(64);
		report.details.add(new FormatText("label.cpm.safetyLvl.texture", size));
		return size <= max;
	}

	public static boolean animatedCheck(Editor e, SafetyReport report) {
		boolean an = e.textures.values().stream().anyMatch(t -> !t.animatedTexs.isEmpty());
		if(an)report.details.add(new FormatText("label.cpm.safetyLvl.animated"));
		return !an;
	}

	public static boolean cubeCountCheck(Editor e, Integer max, SafetyReport report) {
		int[] c = new int[] {0};
		Editor.walkElements(e.elements, __ -> {c[0]++;});
		int count = c[0];
		report.details.add(new FormatText("label.cpm.safetyLvl.cubes", count));
		return count <= max;
	}

	public static boolean sizeCheck(Editor e, Integer max, SafetyReport report) {
		if(e.exportSize < 1)return true;
		report.details.add(new FormatText("label.cpm.safetyLvl.export", e.exportSize));
		return e.exportSize <= max;
	}
}
