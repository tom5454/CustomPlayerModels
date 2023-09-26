package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.tag.TagType;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.tags.EditorTagManager;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartTags implements IModelPart, IResolvedModelPart {
	private TagType type;
	private Map<String, List<String>> tags;

	public ModelPartTags(IOHelper in, ModelDefinition def) throws IOException {
		type = in.readEnum(TagType.VALUES);
		tags = new HashMap<>();
		int c = in.readVarInt();
		for (int i = 0;i<c;i++) {
			String id = in.readUTF();
			int ec = in.readVarInt();
			List<String> elems = new ArrayList<>();
			tags.put(id, elems);
			for (int j = 0;j<ec;j++) {
				String el = in.readUTF();
				elems.add(el);
			}
		}
	}

	public ModelPartTags(TagType type, EditorTagManager<?> tagMngr) {
		this.type = type;
		tags = tagMngr.toMap();
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeEnum(type);
		dout.writeVarInt(tags.size());
		for (Entry<String, List<String>> e : tags.entrySet()) {
			dout.writeUTF(e.getKey());
			dout.writeVarInt(e.getValue().size());
			for (String el : e.getValue()) {
				dout.writeUTF(el);
			}
		}
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.TAGS;
	}

	@Override
	public void apply(ModelDefinition def) {
		def.modelTagManager.getByType(type).fromMap(tags);
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("Tags: ");
		bb.append(type);
		for (Entry<String, List<String>> e : tags.entrySet()) {
			bb.append("\n\t");
			bb.append(e.getKey());
			bb.append(":");
			for (String el : e.getValue()) {
				bb.append("\n\t\t");
				bb.append(el);
			}
		}
		return bb.toString();
	}
}
