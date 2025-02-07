package com.tom.cpm.shared.editor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.shared.editor.Editor;

public enum SafetyLevel {
	MEDIUM(
			SafetyChecks::animatedCheck,
			arg(SafetyChecks::textureCheck, 256),
			arg(SafetyChecks::sizeCheck, 100),
			arg(SafetyChecks::cubeCountCheck, 256)
			),
	LOW(
			arg(SafetyChecks::textureCheck, 512),
			arg(SafetyChecks::sizeCheck, 1024),
			arg(SafetyChecks::cubeCountCheck, 1024)
			),
	NONE(
			arg(SafetyChecks::textureCheck, Integer.MAX_VALUE),
			arg(SafetyChecks::sizeCheck, Integer.MAX_VALUE),
			arg(SafetyChecks::cubeCountCheck, Integer.MAX_VALUE)
			)
	;
	public static final SafetyLevel[] VALUES = values();
	private List<EditorPredicate> checks;

	private SafetyLevel(EditorPredicate... checks) {
		this.checks = Arrays.asList(checks);
	}

	private static <T> EditorPredicate arg(ArgEditorPredicate<T> check, T arg) {
		return (e, r) -> check.test(e, arg, r);
	}

	@FunctionalInterface
	public static interface EditorPredicate {
		public boolean test(Editor e, SafetyReport report);
	}

	@FunctionalInterface
	public static interface ArgEditorPredicate<A> {
		public boolean test(Editor e, A arg, SafetyReport report);
	}

	public static SafetyReport getLevel(Editor e) {
		for(SafetyLevel l : VALUES) {
			SafetyReport r = new SafetyReport(l);
			if(l.checks.stream().allMatch(p -> p.test(e, r)))
				return r;
		}
		return new SafetyReport(NONE);
	}

	public static class SafetyReport {
		public final SafetyLevel lvl;
		public List<FormatText> details = new ArrayList<>();

		public SafetyReport(SafetyLevel lvl) {
			this.lvl = lvl;
		}

		public String getLvl() {
			return lvl.name().toLowerCase(Locale.ROOT);
		}
	}
}
