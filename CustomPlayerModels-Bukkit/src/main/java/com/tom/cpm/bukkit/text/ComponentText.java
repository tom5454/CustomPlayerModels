package com.tom.cpm.bukkit.text;

import java.util.function.BiFunction;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import com.tom.cpl.text.TextRemapper;

public class ComponentText extends BukkitText.Simple implements BukkitText {
	private final BaseComponent comp;

	public ComponentText(BukkitText.Simple fallback, BaseComponent comp) {
		super(fallback.toString());
		this.comp = comp;
	}

	@Override
	public void sendTo(CommandSender pl) {
		pl.spigot().sendMessage(comp);
	}

	public static TextRemapper<ComponentText> make(TextRemapper<BukkitText.Simple> fallback, BiFunction<BukkitText.Simple, BaseComponent, ComponentText> make) {
		return new TextRemapper<>(
				(a, b) -> make.apply(fallback.translate(a, b), new TranslatableComponent(a, b)),
				text -> make.apply(fallback.string(text), new TextComponent(text)),
				(a, b) -> make.apply(fallback.combine(a, b), new ComponentBuilder().append(a.comp).append(b.comp).build()),
				k -> make.apply(fallback.string("??"), new KeybindComponent(k)),
				(a, b) -> make.apply(fallback.styling(a, b), new ComponentBuilder(a.comp).bold(b.bold).italic(b.italic).underlined(b.underline).strikethrough(b.strikethrough).build()));
	}
}
