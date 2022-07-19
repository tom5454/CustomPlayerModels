package com.tom.cpl.text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;

public class CompositeText implements IText {
	private final IText[] parts;

	public CompositeText(IText... parts) {
		this.parts = parts;
	}

	@Override
	public <C> C remap(TextRemapper<C> remapper) {
		if(parts.length == 0)return remapper.string("");
		else {
			C r = parts[0].remap(remapper);
			for(int i = 1;i<parts.length;i++) {
				r = remapper.combine(r, parts[i].remap(remapper));
			}
			return r;
		}
	}

	@Override
	public String toString(IGui gui) {
		StringBuilder sb = new StringBuilder();
		for (IText iText : parts) {
			sb.append(iText.toString(gui));
		}
		return sb.toString();
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("parts", Arrays.stream(parts).map(IText::toMap).collect(Collectors.toList()));
		return m;
	}

	@Override
	public void walkParts(IGui gui, BiConsumer<String, TextStyle> renderer) {
		for (int i = 0;i<parts.length;i++) {
			parts[i].walkParts(gui, renderer);
		}
	}
}
