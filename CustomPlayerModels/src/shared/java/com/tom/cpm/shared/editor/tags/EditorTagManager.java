package com.tom.cpm.shared.editor.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.tag.CPMTag;
import com.tom.cpl.tag.TagManager;

public class EditorTagManager<T> extends TagManager<T> {
	private final EditorTags allTags;

	public EditorTagManager(EditorTags allTags, TagManager<T> parent) {
		super(parent);
		this.allTags = allTags;
	}

	@Override
	public EditableTag create(String id, String... strings) {
		EditableTag tag = new EditableTag(id, Arrays.asList(strings));
		allTags.addTagAction(tags, id, tag);
		return tag;
	}

	public EditableTag load(String id, String[] strings) {
		EditableTag tag = new EditableTag(id, Arrays.asList(strings));
		tags.put(id, tag);
		return tag;
	}

	public EditableTag create(String id) {
		EditableTag tag = new EditableTag(id);
		allTags.addTagAction(tags, id, tag);
		return tag;
	}

	public void deleteTag(String tag) {
		allTags.removeTagAction(tags, tag);
	}

	public void addElemToTag(EditableTag tag, String elem) {
		allTags.addTagElemAction(tag.getEntries(), elem, tag::clearCache);
	}

	public void removeElemFromTag(EditableTag tag, String elem) {
		allTags.removeTagElemAction(tag.getEntries(), elem, tag::clearCache);
	}

	public void clear() {
		tags.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<EditableTag> getTags() {
		return (Collection) tags.values();
	}

	public class EditableTag extends CPMTag<T> {

		public EditableTag(String id) {
			super(EditorTagManager.this, id, new ArrayList<>());
		}

		public EditableTag(String id, List<String> entries) {
			super(EditorTagManager.this, id, new ArrayList<>(entries));
		}

		private void clearCache() {
			stacks = null;
		}

		public String getRawId() {
			return id;
		}
	}

	public static String formatTag(String name) {
		String[] strings = new String[]{"model", name};
		int i = name.indexOf(":");
		if (i >= 0) {
			strings[1] = name.substring(i + 1, name.length());
			if (i >= 1) {
				strings[0] = name.substring(0, i);
			}
		}
		if (!isValidNamespace(strings[0]))return null;
		if (!isValidPath(strings[1]))return null;
		return strings[0] + ":" + strings[1];
	}

	private static boolean isValidPath(String string) {
		for (int i = 0; i < string.length(); ++i) {
			if (validPathChar(string.charAt(i))) continue;
			return false;
		}
		return true;
	}

	private static boolean isValidNamespace(String string) {
		for (int i = 0; i < string.length(); ++i) {
			if (validNamespaceChar(string.charAt(i))) continue;
			return false;
		}
		return true;
	}

	private static boolean validPathChar(char c) {
		return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
	}

	private static boolean validNamespaceChar(char c) {
		return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
	}

	public boolean hasTags() {
		return !tags.isEmpty();
	}

	public Map<String, List<String>> toMap() {
		Map<String, List<String>> map = new HashMap<>();
		tags.forEach((id, tag) -> map.put(id, new ArrayList<>(tag.getEntries())));
		return map;
	}
}
