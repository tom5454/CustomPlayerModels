package com.tom.cpm.blockbench.format;

import java.util.Locale;
import java.util.function.Consumer;

import com.tom.cpm.blockbench.proxy.Animation;
import com.tom.cpm.blockbench.proxy.Dialog;
import com.tom.cpm.blockbench.proxy.Undo;
import com.tom.cpm.blockbench.proxy.Undo.UndoData;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.web.client.java.JsBuilder;
import com.tom.cpm.web.client.util.I18n;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class AnimationWizard {

	public static void open(Animation anIn, Consumer<Boolean> close) {
		Dialog.DialogProperties dctr = new Dialog.DialogProperties();
		dctr.id = "cpm_new_animation";
		dctr.title = I18n.get("bb-label.newAnimation");
		Dialog.FormTextboxElement name = Dialog.FormTextboxElement.make("generic.name");
		if(anIn != null)name.value = anIn.name;
		Dialog.FormSelectElement loop = Dialog.FormSelectElement.make("menu.animation.loop");
		loop.options = new JsBuilder<>().
				put("once", "menu.animation.loop.once").
				put("hold", "menu.animation.loop.hold").
				put("loop", "menu.animation.loop.loop").
				build();
		if(anIn != null)loop.value = anIn.loop;
		Dialog.FormSelectElement type = Dialog.FormSelectElement.make(I18n.get("bb-label.animationType"));
		JsBuilder<String> typeB = new JsBuilder<>();
		for(AnimationType t : AnimationType.VALUES) {
			if(t.isCustom()) {
				String id = t.name().toLowerCase(Locale.ROOT);
				typeB.put(id, I18n.get("label.cpm.new_anim_" + id));
			}
		}
		for (VanillaPose p : VanillaPose.VALUES) {
			if(p == VanillaPose.CUSTOM)continue;
			String id = p.name().toLowerCase(Locale.ROOT);
			typeB.put(id, I18n.get("label.cpm.pose." + id));
		}
		type.options = typeB.build();
		if(anIn != null)type.value = anIn.type;
		Dialog.FormCheckboxElement add = Dialog.FormCheckboxElement.make(I18n.get("label.cpm.anim_additive"));
		if(anIn != null)add.value = anIn.additive;

		dctr.form = new JsBuilder<>().
				put("name", name).
				put("loop", loop).
				put("type", type).
				put("add", add).
				build();

		dctr.onConfirm = rIn -> {
			Animation a = anIn;
			DialogResult r = Js.uncheckedCast(rIn);
			boolean edit = false;
			if(a != null) {
				Undo.initEdit(UndoData.make(a));
				edit = true;
			} else {
				a = new Animation().add(false);
			}
			a.name = r.name;
			a.loop = r.loop;
			a.type = r.type;
			a.additive = r.add;

			if(edit)Undo.finishEdit("Edit animation properties");
			else Undo.finishEdit("Add animation", UndoData.make(a));
			if(close != null)close.accept(true);
			return true;
		};
		dctr.onCancel = () -> {
			if(close != null)close.accept(false);
			return true;
		};

		new Dialog(dctr).show();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	private static class DialogResult {
		public String name, loop, type;
		public boolean add;
	}
}
