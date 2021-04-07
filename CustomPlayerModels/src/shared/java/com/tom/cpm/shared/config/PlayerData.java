package com.tom.cpm.shared.config;

public class PlayerData {
	public byte[] data;
	public boolean forced, save;
	public PlayerData(byte[] data, boolean forced, boolean save) {
		this.data = data;
		this.forced = forced;
		this.save = save;
	}
}
