package com.tom.cpm.api;

import com.tom.cpm.shared.io.ModelFile;

/**
 * Access common api for Customizable Player Models.
 *
 * The referenced minecraft class names are for official mappings, check the wiki for other mappings.
 * */
public interface ICommonAPI extends ISharedAPI {
	/**
	 * Set the player model, has the same effect as the /cpm setskin command
	 *
	 * @param playerClass The player entity class (Player.class)
	 * @param player the player object
	 * @param b64 Base64 encoded model, from the Base64 export option.
	 * @param forced Force the model, players can't change models when the server forces it.
	 * @param persistent Keep the model after re-logging
	 * */
	<P> void setPlayerModel(Class<P> playerClass, P player, String b64, boolean forced, boolean persistent);

	/**
	 * Set the player model, has the same effect as the /cpm setskin command
	 *
	 * <p>Note: the loaded model won't be kept between re-logging,
	 * use the Base64 variant {@link ICommonAPI#setPlayerModel(Class, Object, String, boolean, boolean)}
	 * for persistence.</p>
	 *
	 * @param playerClass The player entity class (Player.class)
	 * @param player the player object
	 * @param model The model to use. Load a {@link ModelFile} using {@link ModelFile#load(java.io.File)} or {@link ModelFile#load(java.io.InputStream)}
	 * @param forced Force the model, players can't change models when the server forces it.
	 * */
	<P> void setPlayerModel(Class<P> playerClass, P player, ModelFile model, boolean forced);

	/**
	 * Reset the player model, has the same effect as the /cpm setskin -r command
	 *
	 * @param playerClass The player entity class (Player.class)
	 * @param player the player object
	 * */
	<P> void resetPlayerModel(Class<P> playerClass, P player);

	/**
	 * Send player jump packet to play the jumping animation
	 *
	 * @param playerClass The player entity class (Player.class)
	 * @param player the player object
	 * */
	<P> void playerJumped(Class<P> playerClass, P player);

}
