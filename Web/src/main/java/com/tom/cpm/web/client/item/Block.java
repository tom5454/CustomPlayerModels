package com.tom.cpm.web.client.item;

public class Block {
	public static final Block AIR = new Block("minecraft:air");
	public final String id;
	public Item item;

	public Block(String id) {
		this.id = id;
		this.item = Item.AIR;
	}
}
