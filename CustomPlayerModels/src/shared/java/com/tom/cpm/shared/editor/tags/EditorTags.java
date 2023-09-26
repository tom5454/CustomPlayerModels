package com.tom.cpm.shared.editor.tags;

import java.util.List;
import java.util.Map;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.item.Stack;
import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpl.tag.CPMTag;
import com.tom.cpl.tag.IAllTags;
import com.tom.cpl.tag.TagType;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.Editor;

public class EditorTags implements IAllTags {
	private final Editor editor;
	private final AllTagManagers builtinTags = MinecraftClientAccess.get().getBuiltinTags();
	private final EditorTagManager<BlockState> blockTags = new EditorTagManager<>(this, builtinTags.getBlockTags());
	private final EditorTagManager<Stack> itemTags = new EditorTagManager<>(this, builtinTags.getItemTags());
	private final EditorTagManager<EntityType> entityTags = new EditorTagManager<>(this, builtinTags.getEntityTags());
	private final EditorTagManager<Biome> biomeTags = new EditorTagManager<>(this, builtinTags.getBiomeTags());

	public EditorTags(Editor editor) {
		this.editor = editor;
	}

	@Override
	public EditorTagManager<BlockState> getBlockTags() {
		return blockTags;
	}

	@Override
	public EditorTagManager<Stack> getItemTags() {
		return itemTags;
	}

	@Override
	public EditorTagManager<EntityType> getEntityTags() {
		return entityTags;
	}

	@Override
	public EditorTagManager<Biome> getBiomeTags() {
		return biomeTags;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EditorTagManager<T> getByType(TagType type) {
		return (EditorTagManager<T>) IAllTags.super.getByType(type);
	}

	public void clear() {
		blockTags.clear();
		itemTags.clear();
		entityTags.clear();
		biomeTags.clear();
	}

	public void markDirty() {
		editor.markDirty();
	}

	public <T> void addTagAction(Map<String, CPMTag<T>> tags, String id, CPMTag<T> tag) {
		editor.action("add", "button.cpm.tags").addToMap(tags, id, tag).execute();
	}

	public <T> void removeTagAction(Map<String, CPMTag<T>> tags, String id) {
		editor.action("remove", "button.cpm.tags").removeFromMap(tags, id).execute();
	}

	public <T> void addTagElemAction(List<String> elems, String elem, Runnable clear) {
		editor.action("edit", "button.cpm.tags").addToList(elems, elem).onAction(clear).execute();
	}

	public <T> void removeTagElemAction(List<String> elems, String elem, Runnable clear) {
		editor.action("edit", "button.cpm.tags").removeFromList(elems, elem).onAction(clear).execute();
	}
}
