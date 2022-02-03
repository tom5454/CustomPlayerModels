package com.tom.cpm.shared.config;

import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;

public class ConfigKeys {
	public static final String TITLE_SCREEN_BUTTON = "titleScreenButton";
	public static final String EDITOR_ROTATE_MOUSE_BUTTON = "editorRotateMouseButton";
	public static final String SELECTED_MODEL = "selectedModel";
	public static final String KEYBINDS = "keybinds";
	public static final String SERVER_SKINS = "skins";
	public static final String MODEL = "model";
	public static final String FORCED = "forced";
	public static final String SELECTED_MODEL_OLD = "selectedModelOld";
	public static final String REOPEN_PROJECT = "reopenProject";
	public static final String EDITOR_SCALE = "editorScale";
	public static final String PLAYER_SETTINGS = "playerSettings";
	public static final String FRIEND_SETTINGS = "friendSettings";
	public static final String SERVER_SETTINGS = "serverSettings";
	public static final String GLOBAL_SETTINGS = "globalSettings";
	public static final String FRIEND_LIST = "friendList";
	public static final String BLOCKED_LIST = "blockedList";
	public static final String EDITOR_POSITION_MODE = "editorPosMode";
	public static final String NAME = "name";
	public static final String SAFETY_PROFILE = "safetyProfile";
	public static final String SAFETY_PROFILES = "safetyProfiles";
	public static final String RECOMMEND_SAFETY_SETTINGS = "serverRecommendSafetySettings";
	public static final String SAFETY_SETTINGS = "safetySettings";
	public static final String IGNORE_SAFETY_RECOMMENDATIONS = "ignoreSafetyRec";
	public static final String DISABLE_NETWORK = "disableNet";
	public static final String KICK_PLAYERS_WITHOUT_MOD = "kickPlayersWithoutMod";
	public static final String KICK_MESSAGE = "kickMessage";
	public static final String DEFAULT_KICK_MESSAGE = "Customizable Player Models is requied on this server";
	public static final String IMPORTED = "imported";

	public static final PlayerSpecificConfigKey<Boolean> ENABLE_MODEL_LOADING = PlayerSpecificConfigKey.createBool("models", true);
	public static final PlayerSpecificConfigKey<Boolean> ENABLE_ANIMATED_TEXTURES = PlayerSpecificConfigKey.createBool("animatedTex", false, KeyGroup.FRIEND, true);
	public static final PlayerSpecificConfigKey<Integer> MAX_TEX_SHEET_SIZE = PlayerSpecificConfigKey.createIntLog2("maxTex", 64, 8192, v -> v + "x" + v, 256, KeyGroup.FRIEND, 512);
	public static final PlayerSpecificConfigKey<Integer> MAX_LINK_SIZE = PlayerSpecificConfigKey.createIntLog2("maxLink", 16, 256*1024, v -> {
		if(v < 1024)return v + " KB";
		else return (v / 1024) + " MB";
	}, 100, KeyGroup.FRIEND, 1024);
	public static final PlayerSpecificConfigKey<Integer> MAX_CUBE_COUNT = PlayerSpecificConfigKey.createIntF("maxCubeCount", 16, Integer.MAX_VALUE, Math::log10, v -> Math.pow(10, v), Object::toString, 0, 256, KeyGroup.FRIEND, (int) Short.MAX_VALUE);
	public static final PlayerSpecificConfigKey[] SAFETY_KEYS = new PlayerSpecificConfigKey[] {ENABLE_MODEL_LOADING, ENABLE_ANIMATED_TEXTURES, MAX_TEX_SHEET_SIZE, MAX_LINK_SIZE, MAX_CUBE_COUNT};
}
