package com.tom.cpm.api;

/**
 * Implement this class in your mod to make a Customizable Player Models plugin.
 *
 * Check the wiki on how to register your plugin:
 * <a href="https://github.com/tom5454/CustomPlayerModels/wiki/API">https://github.com/tom5454/CustomPlayerModels/wiki/API</a>
 * */
public interface ICPMPlugin {

	/**
	 * Init client
	 *
	 * @param api The api
	 * */
	void initClient(IClientAPI api);

	/**
	 * Init common
	 *
	 * @param api The api
	 * */
	void initCommon(ICommonAPI api);

	/**
	 * Get you mod id
	 * */
	String getOwnerModId();
}
