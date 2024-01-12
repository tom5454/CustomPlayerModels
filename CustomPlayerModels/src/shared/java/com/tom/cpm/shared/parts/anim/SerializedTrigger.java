package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.io.IOHelper;

public class SerializedTrigger {
	public static final int LAYER_CTRL   = 1 << 0;
	public static final int COMMAND_CTRL = 1 << 1;
	public static final int PROPERTY     = 1 << 2;
	public static final int GROUPPED     = 1 << 3;
	public static final int LOOPING      = 1 << 4;

	private boolean init;
	public int animID = -1, triggerID = -1;
	public AnimationType anim;
	public String name;
	public VanillaPose pose;
	public byte defaultValue;
	public int order;
	public boolean isProperty;
	public String group;
	public boolean command;
	public boolean layerCtrl;
	public boolean looping;
	private int gid = -1;

	public static void newTrigger(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger t = new SerializedTrigger();
		state.newTrigger(t);
	}

	public static void initBuiltin(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger t = state.getTrigger();
		t.pose = block.readEnum(VanillaPose.values());
		t.anim = AnimationType.POSE;
		t.init = true;
	}

	public static void initNamed(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = block.readEnum(AnimationType.VALUES);
		cT.name = block.readUTF();
		int flags = block.read();
		cT.command = (flags & COMMAND_CTRL) != 0;
		cT.layerCtrl = (flags & LAYER_CTRL) != 0;
		cT.isProperty = (flags & PROPERTY) != 0;
		cT.looping = (flags & LOOPING) != 0;
		cT.order = block.readVarInt();
		cT.defaultValue = block.readByte();
		if (cT.layerCtrl)cT.gid = block.read();
		if ((flags & GROUPPED) != 0)cT.group = block.readUTF();
		cT.init = true;
	}

	public static void initTriggerStaged(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = block.readEnum(AnimationType.VALUES);
		cT.triggerID = block.readVarInt();
		cT.init = true;
	}

	public static void initAnimStaged(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = block.readEnum(AnimationType.VALUES);
		cT.animID = block.readVarInt();
		cT.init = true;
	}

	public void write(IOHelper dout) throws IOException {
		try (IOHelper d = dout.writeNextObjectBlock(TagType.NEW_TRIGGER)) {
		}
		if (this.pose != null) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_BUILTIN_TRIGGER)) {
				d.writeEnum(this.pose);
			}
		} else if (this.name != null) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_NAMED_TRIGGER)) {
				d.writeEnum(this.anim);
				d.writeUTF(this.name);
				int flags = 0;
				if (this.group != null)flags |= GROUPPED;
				if (this.layerCtrl)flags |= LAYER_CTRL;
				if (this.command)flags |= COMMAND_CTRL;
				if (this.isProperty)flags |= PROPERTY;
				if (this.looping)flags |= LOOPING;
				d.write(flags);
				d.writeVarInt(this.order);
				d.writeByte(this.defaultValue);
				if (this.layerCtrl)d.write(this.gid);
				if (this.group != null)d.writeUTF(this.group);
			}
		} else if (this.anim.isStaged()) {
			if (this.triggerID != -1) {
				try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_TRIGGER_STAGED_TRIGGER)) {
					d.writeEnum(this.anim);
					d.writeVarInt(this.triggerID);
				}
			} else {
				try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_ANIM_STAGED_TRIGGER)) {
					d.writeEnum(this.anim);
					d.writeVarInt(this.animID);
				}
			}
		}
	}

	public static void handleLayers(Map<Integer, SerializedTrigger> triggers, List<PlayerSkinLayer> allLayers, int blank, int reset, int valMask, int defMask) {
		int layerId = 1;
		Deque<Integer> allIds = new ArrayDeque<>(triggers.size());
		IntStream.range(0, Math.min(triggers.size(), 255)).forEach(allIds::add);
		allIds.remove(reset);
		allIds.remove(blank);
		for (int I = 0;I<triggers.size();I++) {
			SerializedTrigger t = triggers.get(I);
			if ((t.anim == AnimationType.CUSTOM_POSE || t.anim == AnimationType.GESTURE) && t.layerCtrl) {
				if (allLayers.size() < 2)throw new Exporter.ExportException("error.cpm.custom_anims_not_supported");
				int gid = layerId++;
				Set<PlayerSkinLayer> encL = new HashSet<>();
				for (int i = 0; i < allLayers.size(); i++) {
					PlayerSkinLayer l = allLayers.get(i);
					if((gid & (1 << i)) != 0) {
						encL.add(l);
					}
				}
				if(encL.containsAll(allLayers))throw new Exporter.ExportException("error.cpm.too_many_animations");
				t.gid = PlayerSkinLayer.encode(encL);
				t.gid &= valMask;
				t.gid |= defMask;
				allIds.remove(t.gid);
			}
		}
		for (int i = 0;i<triggers.size();i++) {
			SerializedTrigger t = triggers.get(i);
			if ((t.anim == AnimationType.CUSTOM_POSE || t.anim == AnimationType.GESTURE) && !t.layerCtrl) {
				if (allIds.isEmpty())throw new Exporter.ExportException("error.cpm.too_many_animations");
				t.gid = allIds.remove();
			}
		}
	}

	public static void handleGestures(Map<Integer, SerializedTrigger> triggers, int blank, int reset) {
		Deque<Integer> allGIds = new ArrayDeque<>(triggers.size());
		Deque<Integer> allPIds = new ArrayDeque<>(triggers.size());
		IntStream.range(0, Math.min(triggers.size(), 255)).forEach(allGIds::add);
		allGIds.remove(blank);
		allGIds.remove(reset);
		allPIds.addAll(allGIds);
		List<SerializedTrigger> tL = new ArrayList<>();
		for (int i = 0;i<triggers.size();i++) {
			tL.add(triggers.get(i));
		}
		tL.forEach(s -> {
			if ((s.anim == AnimationType.CUSTOM_POSE || s.anim == AnimationType.GESTURE) && s.layerCtrl) {
				(s.anim == AnimationType.CUSTOM_POSE ? allPIds : allGIds).remove(s.gid);
			}
		});
		tL.forEach(s -> {
			if ((s.anim == AnimationType.CUSTOM_POSE || s.anim == AnimationType.GESTURE) && !s.layerCtrl) {
				s.gid = (s.anim == AnimationType.CUSTOM_POSE ? allPIds : allGIds).remove();
			}
		});
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anim == null) ? 0 : anim.hashCode());
		result = prime * result + animID;
		result = prime * result + triggerID;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pose == null) ? 0 : pose.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SerializedTrigger other = (SerializedTrigger) obj;
		if (anim != other.anim) return false;
		if (animID != other.animID) return false;
		if (triggerID != other.triggerID) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (pose == null) {
			if (other.pose != null) return false;
		} else if (!pose.equals(other.pose)) return false;
		return true;
	}

	public void registerOld(AnimationRegistry reg, List<IAnimation> a) {
		if (this.pose != null) {
			a.forEach(e -> reg.register(pose, e));
		} else if (this.name != null) {
			if (anim == AnimationType.CUSTOM_POSE) {
				CustomPose p = new CustomPose(name, order);
				a.forEach(e -> reg.register(p, e));
				p.command = command;
				p.layerCtrl = layerCtrl;
				reg.register(p);
				if(gid != -1)
					reg.register(gid, p);
			} else {
				Gesture g = new Gesture(anim, a, name, looping, order);
				g.defVal = defaultValue;
				g.isProperty = isProperty;
				g.group = group;
				g.command = command;
				g.layerCtrl = layerCtrl;
				reg.register(g);
				if(gid != -1)
					reg.register(gid, g);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Trigger");
		if (this.pose != null) {
			sb.append(" Pose ");
			sb.append(pose.name());
		} else if (this.name != null) {
			sb.append(" ");
			sb.append(anim.name());
			sb.append("\n\tName: ");
			sb.append(name);
			sb.append(" ");
			sb.append(layerCtrl);
			sb.append(" ");
			sb.append(gid);
			sb.append(" ");
			sb.append(looping);
		} else if (this.anim.isStaged()) {
			sb.append(" ");
			sb.append(anim.name());
			sb.append(" ");
			if (animID != -1) {
				sb.append(animID);
				sb.append(" Anim");
			} else {
				sb.append(triggerID);
				sb.append(" Trigger");
			}
		}
		return sb.toString();
	}
}