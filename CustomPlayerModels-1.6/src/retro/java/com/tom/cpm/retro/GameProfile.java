package com.tom.cpm.retro;

import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.skin.TextureType;

public class GameProfile {
	private final UUID id;
	private final String name;
	private String skinType;
	private Map<TextureType, String> textureURLMap = new EnumMap<>(TextureType.class);

	public GameProfile(final UUID id, final String name) {
		if (id == null && name.isEmpty()) {
			throw new IllegalArgumentException("Name and ID cannot both be blank");
		}

		this.id = id;
		this.name = name;
		this.skinType = "default";
	}

	@SuppressWarnings("unchecked")
	public GameProfile(Map<String, Object> data) {
		id = fromString((String) data.get("id"));
		name = (String) data.get("name");
		this.skinType = "default";

		if (id == null || name == null) {
			throw new IllegalArgumentException("Invalid details");
		}

		List<Map<String, Object>> pr = (List<Map<String, Object>>) data.get("properties");
		for (Map<String, Object> map : pr) {
			if ("textures".equals(map.get("name"))) {
				String val = new String(Base64.getDecoder().decode((String) map.get("value")));
				Map<String, Object> p = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(val, Object.class);
				Map<String, Map<String, Object>> tex = (Map<String, Map<String, Object>>) p.get("textures");
				for (Entry<String, Map<String, Object>> entry : tex.entrySet()) {
					TextureType tt = TextureType.valueOf(entry.getKey());
					String url = (String) entry.getValue().get("url");
					Object meta = entry.getValue().get("metadata");
					if(tt == TextureType.SKIN && meta != null) {
						skinType = ((Map<String, String>)meta).get("model");
					}
					if (skinType == null) {
						skinType = "default";
					}
					if (url.startsWith("http://textures.minecraft.net/texture/")) {
						textureURLMap.put(tt, url);
					}
				}
			}
		}
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final GameProfile that = (GameProfile) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}
		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "GameProfile[id=" + id + ", name=" + name + "]";
	}

	private static UUID fromString(final String input) {
		return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	public Map<TextureType, String> getTextureURLMap() {
		return textureURLMap;
	}
}
