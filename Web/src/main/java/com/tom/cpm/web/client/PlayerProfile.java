package com.tom.cpm.web.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.PlayerTextureLoader;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.web.client.util.GameProfile;

public class PlayerProfile extends Player<Object> {
	public static Map<GameProfile, PlayerInfo> infos = new HashMap<>();
	private GameProfile profile;
	private String skinType;

	public PlayerProfile(GameProfile profile) {
		this.profile = profile;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.get(skinType);
	}

	@Override
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				PlayerInfo i = infos.get(profile);
				if(i != null) {
					defineAll(i.textures, f -> f);
					skinType = i.skinType;
				}
				return CompletableFuture.completedFuture(null);
			}
		};
	}

	@Override
	public String getName() {
		return profile.getName();
	}

	@Override
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public void updateFromPlayer(Object player) {
	}

	@Override
	public Object getGameProfile() {
		return profile;
	}

	@Override
	public void updateFromModel(Object model) {
	}

	public static class PlayerInfo {
		private String skinType;
		private Map<TextureType, String> textures;

		public PlayerInfo(String skinType, Map<TextureType, String> textures) {
			this.skinType = skinType;
			this.textures = textures;
		}
	}
}
