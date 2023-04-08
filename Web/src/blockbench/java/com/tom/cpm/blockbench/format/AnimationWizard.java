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

		Dialog.FormCheckboxElement lc = Dialog.FormCheckboxElement.make(I18n.get("label.cpm.anim_layerCtrl"), anIn != null ? anIn.layerCtrl : true);
		lc.description = I18n.formatNl("tooltip.cpm.anim_layerCtrl");

		Dialog.FormCheckboxElement cc = Dialog.FormCheckboxElement.make(I18n.get("label.cpm.anim_command"));
		if(anIn != null)cc.value = anIn.commandCtrl;
		cc.description = I18n.formatNl("tooltip.cpm.anim_command");

		Dialog.FormVectorElement priority = Dialog.FormVectorElement.make(I18n.get("label.cpm.anim_priority"), 1);
		if(anIn != null)priority.value[0] = anIn.getPriority();
		priority.description = I18n.formatNl("tooltip.cpm.anim_priority");

		Dialog.FormVectorElement order = Dialog.FormVectorElement.make(I18n.get("label.cpm.anim_order"), 1);
		if(anIn != null)order.value[0] = anIn.getOrder();
		order.description = I18n.formatNl("tooltip.cpm.anim_order");

		Dialog.FormCheckboxElement prop = Dialog.FormCheckboxElement.make(I18n.get("label.cpm.anim_is_property"));
		if(anIn != null)prop.value = anIn.isProperty;
		prop.description = I18n.formatNl("tooltip.cpm.anim_is_property");

		Dialog.FormTextboxElement group = Dialog.FormTextboxElement.make(I18n.get("label.cpm.layerGroup"));
		if(anIn != null)group.value = anIn.group;
		group.description = I18n.formatNl("tooltip.cpm.layerGroup");

		Dialog.FormVectorElement layerDefaultF = Dialog.FormVectorElement.make(I18n.get("label.cpm.defLayerSettings.value"), 1);
		if(anIn != null)layerDefaultF.value[0] = anIn.getLayerDefault();

		Dialog.FormCheckboxElement layerDefaultB = Dialog.FormCheckboxElement.make(I18n.get("label.cpm.defLayerSettings.toggle"));
		if(anIn != null)layerDefaultB.value = anIn.getLayerDefault() > 0.5f;

		dctr.form = new JsBuilder<>().
				put("name", name).
				put("loop", loop).
				put("type", type).
				put("add", add).
				put("layerCtrl", lc).
				put("commandCtrl", cc).
				put("priority", priority).
				put("order", order).
				put("prop", prop).
				put("group", group).
				put("layerDefaultF", layerDefaultF).
				put("layerDefaultB", layerDefaultB).
				build();

		Consumer<String> update = t -> {
			prop.bar.toggle(false);
			group.bar.toggle(false);
			layerDefaultF.bar.toggle(false);
			layerDefaultB.bar.toggle(false);
			lc.bar.toggle(false);
			cc.bar.toggle(false);
			order.bar.toggle(false);
			if(t.equals(AnimationType.LAYER.name().toLowerCase(Locale.ROOT))) {
				prop.bar.toggle(true);
				group.bar.toggle(true);
				layerDefaultB.bar.toggle(true);
				cc.bar.toggle(true);
				order.bar.toggle(true);
			} else if(t.equals(AnimationType.VALUE_LAYER.name().toLowerCase(Locale.ROOT))) {
				prop.bar.toggle(true);
				layerDefaultF.bar.toggle(true);
				cc.bar.toggle(true);
				order.bar.toggle(true);
			} else if(t.equals(AnimationType.CUSTOM_POSE.name().toLowerCase(Locale.ROOT))) {
				lc.bar.toggle(true);
				cc.bar.toggle(true);
				order.bar.toggle(true);
			} else if(t.equals(AnimationType.GESTURE.name().toLowerCase(Locale.ROOT))) {
				lc.bar.toggle(true);
				cc.bar.toggle(true);
				order.bar.toggle(true);
			}
		};

		dctr.onFormChange = rIn -> {
			DialogResult r = Js.uncheckedCast(rIn);
			update.accept(r.type);
		};

		dctr.onBuild = () -> {
			if(anIn != null)update.accept(anIn.type);
			else update.accept(AnimationType.CUSTOM_POSE.name().toLowerCase(Locale.ROOT));
		};

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
			a.layerCtrl = r.layerCtrl;
			a.commandCtrl = r.commandCtrl;
			a.setPriority(Math.round(r.priority));
			a.group = r.group;
			a.isProperty = r.prop;
			a.setOrder(Math.round(r.order));
			if(a.type.equals(AnimationType.LAYER.name().toLowerCase(Locale.ROOT)))
				a.setLayerDefault(r.layerDefaultB ? 1 : 0);
			else
				a.setLayerDefault(r.layerDefaultF);

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
		public String name, loop, type, group;
		public boolean add, layerCtrl, commandCtrl, prop, layerDefaultB;
		public float priority, order, layerDefaultF;
	}
}
