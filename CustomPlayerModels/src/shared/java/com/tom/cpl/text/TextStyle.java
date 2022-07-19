package com.tom.cpl.text;

import java.util.HashMap;
import java.util.Map;

public class TextStyle {
	public boolean italic, bold, underline, strikethrough;

	public TextStyle() {
	}

	public TextStyle(boolean italic, boolean bold, boolean underline, boolean strikethrough) {
		this.italic = italic;
		this.bold = bold;
		this.underline = underline;
		this.strikethrough = strikethrough;
	}

	public TextStyle(TextStyle copy) {
		this.italic = copy.italic;
		this.bold = copy.bold;
		this.underline = copy.underline;
		this.strikethrough = copy.strikethrough;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		//TODO
		return m;
	}

	@Override
	public String toString() {
		return String.format("TextStyle [italic=%s, bold=%s, underline=%s, strikethrough=%s]", italic, bold, underline,
				strikethrough);
	}
}
