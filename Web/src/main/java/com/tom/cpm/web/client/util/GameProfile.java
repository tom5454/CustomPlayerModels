package com.tom.cpm.web.client.util;

import java.util.UUID;

public class GameProfile {
	private final UUID id;
	private final String name;

	public GameProfile(final UUID id, final String name) {
		if (id == null && name.isEmpty()) {
			throw new IllegalArgumentException("Name and ID cannot both be blank");
		}

		this.id = id;
		this.name = name;
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
}
