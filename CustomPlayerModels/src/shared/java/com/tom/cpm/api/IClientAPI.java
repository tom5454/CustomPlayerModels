package com.tom.cpm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.retro.RetroPlayerRendererImpl;

/**
 * Access client-side api for Customizable Player Models.
 *
 * The referenced minecraft class names are for official mappings, check the wiki for other mappings.
 * */
public interface IClientAPI extends ISharedAPI {

	/**
	 * Register a voice level provider.
	 *
	 * @param clazz the player entity class (Player.class) for your minecraft version
	 * @param getVoiceLevel function that returns the voice loudness for the specific player
	 * */
	<T> void registerVoice(Class<T> clazz, Function<T, Float> getVoiceLevel);

	/**
	 * Register a voice level provider.
	 *
	 * @param getVoiceLevel function that returns the voice loudness for the specific player uuid
	 * */
	<T> void registerVoice(Function<UUID, Float> getVoiceLevel);

	/**
	 * Register a voice muted provider.
	 *
	 * @param clazz the player entity class (Player.class) for your minecraft version
	 * @param getMuted function that returns the voice muted status for the specific player
	 * */
	<T> void registerVoiceMute(Class<T> clazz, Predicate<T> getMuted);

	/**
	 * Register a voice muted provider.
	 *
	 * @param getMuted function that returns the voice muted status for the specific player uuid
	 * */
	<T> void registerVoiceMute(Predicate<UUID> getMuted);

	/**
	 * Create a new player model renderer
	 * This method is for 1.16 and newer versions
	 * See {@link IClientAPI#createPlayerRenderer(Class, Class)} for older minecraft versions
	 *
	 * @param modelClass The base model class (Model.class)
	 * @param resourceLocationClass ResourceLocation.class
	 * @param renderTypeClass RenderType.class
	 * @param multiBufferSourceClass MultiBufferSource.class
	 * @param gameProfileClass GameProfile.class
	 *
	 * @return New player renderer
	 * */
	<M, RL, RT, MBS, GP> PlayerRenderer<M, RL, RT, MBS, GP> createPlayerRenderer(Class<M> modelClass, Class<RL> resourceLocationClass, Class<RT> renderTypeClass, Class<MBS> multiBufferSourceClass, Class<GP> gameProfileClass);

	/**
	 * Load a model from an {@link InputStream}.
	 *
	 * @param name The model name
	 * @param is the {@link InputStream}
	 *
	 * @throws IOException
	 * */
	LocalModel loadModel(String name, InputStream is) throws IOException;

	/**
	 * Register a model generator function that will be available under Edit/Tools
	 *
	 * @param unlocalized name for the generator
	 * @param unlocalized tooltip, use \ character to create line breaks in your language file
	 * @param func consumer of {@link EditorGui}
	 * */
	void registerEditorGenerator(String name, String tooltip, Consumer<EditorGui> func);

	/**
	 * Create a new player model renderer
	 * This method is for 1.12 and older versions
	 * See {@link IClientAPI#createPlayerRenderer(Class, Class, Class, Class, Class)} for newer minecraft versions
	 *
	 * @param modelClass The base model class (Model.class)
	 * @param gameProfileClass GameProfile.class
	 *
	 * @return New player renderer
	 * */
	default <M, GP> RetroPlayerRenderer<M, GP> createPlayerRenderer(Class<M> modelClass, Class<GP> gameProfileClass) {
		return new RetroPlayerRendererImpl<>(createPlayerRenderer(modelClass, Void.class, Void.class, Void.class, gameProfileClass));
	}

	/**
	 * Play the given command animation for a player.
	 *
	 * @param name animation name
	 * @param value for layers (value: 0-255, toggle: 0-1) or -1 to switch state, 0: reset pose/gesture
	 *
	 * @return true if the animation was found and started playing
	 * */
	<P> boolean playAnimation(String name, int value);

	/**
	 * Play the given command animation for a player. For custom poses and gestures only.
	 * For more control use {@link ICommonAPI#playAnimation(Class, Object, String, int)}
	 *
	 * @param name animation name
	 *
	 * @return true if the animation was found and started playing
	 * */
	<P> boolean playAnimation(String name);

	/**
	 * Player renderer for 1.16 and newer versions
	 *
	 * See {@link RetroPlayerRenderer} for 1.12 and older versions
	 * */
	public static interface PlayerRenderer<M, RL, RT, MBS, GP> {
		void setGameProfile(GP profile);
		void setLocalModel(LocalModel model);
		void setRenderModel(M model);
		void setRenderType(Function<RL, RT> renderTypeFactory);
		RT getDefaultRenderType();
		RL getDefaultTexture();
		RT getRenderTypeForSubModel(M model);
		AnimationState getAnimationState();
		void preRender(MBS buffers, AnimationMode renderMode);
		void prepareSubModel(M model, SubModelType type, RL tex);
		void postRender();
		void setActivePose(String pose);
		void setActiveGesture(String gesture);
	}

	/**
	 * Player renderer for 1.12 and older versions
	 *
	 * See {@link PlayerRenderer} for 1.16 and newer versions
	 * */
	public static interface RetroPlayerRenderer<M, GP> {
		void setGameProfile(GP profile);
		void setLocalModel(LocalModel model);
		void setRenderModel(M model);
		AnimationState getAnimationState();
		void preRender(AnimationMode renderMode);
		void prepareSubModel(M model, SubModelType type);
		void postRender();
		void setActivePose(String pose);
		void setActiveGesture(String gesture);
	}

	/**
	 * A model loaded with {@link IClientAPI#loadModel(String, InputStream)}
	 * */
	public static interface LocalModel {
		/**
		 * Returns the generated dummy GameProfile
		 * */
		<GP> GP getGameProfile();

		/**
		 * Called before rendering the model
		 * */
		void startRender();

		/**
		 * The unique key for this model
		 * */
		String getUniqueKey();
	}

	/**
	 * The type of the sub-model
	 * */
	public static enum SubModelType {
		ARMOR_OUTER,
		ARMOR_INNER,
		ELYTRA,
		CAPE
	}
}
