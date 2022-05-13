package com.tom.cpm.shared.parts;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.Animation;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.util.Log;

public class ModelPartAnimation implements IModelPart, IResolvedModelPart {
	private Map<Integer, ResolvedData> parsedData = new HashMap<>();
	private int blankId, resetId;

	public ModelPartAnimation(IOHelper din, ModelDefinition def) throws IOException {
		boolean done = false;
		while(!done) {
			Type type = din.readEnum(Type.VALUES);
			if(type == Type.END)break;
			IOHelper block = din.readNextBlock();
			if(type == null)continue;
			switch (type) {
			case END:
				break;
			case GESTURE:
			{
				int id = block.read();
				String name = block.readUTF();
				int flags = block.read();
				parsedData.put(id, new ResolvedData(name, (flags & 2) != 0, (flags & 1) != 0));
			}
			break;
			case POSE:
			{
				VanillaPose pose = block.readEnum(VanillaPose.values());
				int id = block.read();
				int flags = block.read();
				if(pose == VanillaPose.CUSTOM) {
					String name = block.readUTF();
					CustomPose p = parsedData.values().stream().map(k -> k.pose).
							filter(k -> k instanceof CustomPose && ((CustomPose)k).getName().equals(name)).
							map(k -> (CustomPose) k).findFirst().orElse(null);
					if(p == null)p = new CustomPose(name);
					parsedData.put(id, new ResolvedData(p, (flags & 1) != 0));
				} else {
					parsedData.put(id, new ResolvedData(pose, (flags & 1) != 0));
				}
			}
			break;
			case ANIMATION_DATA_COLOR:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int cid = block.read();
				rd.color[cid] = new Vec3f[rd.frames];
				for(int i = 0;i<rd.frames;i++) {
					int r = block.read();
					int g = block.read();
					int b = block.read();
					if ((r | g | b) < 0)
						throw new EOFException();
					rd.color[cid][i] = new Vec3f(r, g, b);
				}
			}
			break;
			case ANIMATION_DATA_POS:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int cid = block.read();
				rd.pos[cid] = new Vec3f[rd.frames];
				for(int i = 0;i<rd.frames;i++) {
					rd.pos[cid][i] = block.readVec6b();
				}
			}
			break;
			case ANIMATION_DATA_ROTATION:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int cid = block.read();
				rd.rot[cid] = new Vec3f[rd.frames];
				for(int i = 0;i<rd.frames;i++) {
					rd.rot[cid][i] = block.readAngle();
				}
			}
			break;
			case ANIMATION_DATA_VIS:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int cid = block.read();
				rd.show[cid] = new Boolean[rd.frames];
				for(int i = 0;i<rd.frames;i += 8) {
					int dt = block.read();
					for(int j = 0;i+j < rd.frames && j < 8;j++) {
						rd.show[cid][i+j] = (dt & (1 << j)) != 0;
					}
				}
			}
			break;
			case ANIMATION_INFO:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int count = block.read();
				int frames = block.read();
				int duration = block.readShort();
				rd.components = new int[count];
				for (int i = 0; i < count; i++) {
					rd.components[i] = block.readVarInt();
				}
				rd.pos = new Vec3f[count][];
				rd.rot = new Vec3f[count][];
				rd.scale = new Vec3f[count][];
				rd.color = new Vec3f[count][];
				rd.show = new Boolean[count][];
				rd.frames = frames;
				rd.duration = duration;
			}
			break;
			case ENCODING:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				rd.gid = block.read();
			}
			break;
			case CTRL_IDS:
				blankId = block.read();
				resetId = block.read();
				break;

			case ANIMATION_DATA_EXTRA:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				rd.priority = block.readByte();
			}
			break;

			case ANIMATION_DATA_INTERPOLATION:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				rd.it = block.readEnum(InterpolatorType.VALUES);
			}
			break;

			case ANIMATION_DATA_SCALE:
			{
				int id = block.read();
				ResolvedData rd = parsedData.get(id);
				if(rd == null)continue;
				int cid = block.read();
				rd.scale[cid] = new Vec3f[rd.frames];
				for(int i = 0;i<rd.frames;i++) {
					rd.scale[cid][i] = block.readVec6b();
				}
			}
			break;

			default:
				break;
			}
		}
	}

	public ModelPartAnimation(Editor e) {
		int[] idc = new int[] {0, 1};
		int valMask = e.animEnc == null ? 0 :  PlayerSkinLayer.encode(e.animEnc.freeLayers);
		int defMask = e.animEnc == null ? 0 : (PlayerSkinLayer.encode(e.animEnc.defaultLayerValue) & (~valMask));
		resetId = defMask | valMask;
		blankId = defMask | 0;
		List<PlayerSkinLayer> allLayers = e.animEnc != null ? new ArrayList<>(e.animEnc.freeLayers) : new ArrayList<>();
		Collections.sort(allLayers);
		e.animations.forEach(ea -> {
			int id = idc[0]++;
			ResolvedData rd;
			if(ea.pose instanceof VanillaPose) {
				rd = new ResolvedData((VanillaPose) ea.pose, ea.add);
			} else if(ea.pose != null) {
				rd = new ResolvedData(ea.pose, ea.add);
				resolveEncID(rd, idc[1]++, allLayers);
			} else {
				rd = new ResolvedData(ea.getId(), ea.loop, ea.add);
				resolveEncID(rd, idc[1]++, allLayers);
			}
			rd.gid &= valMask;
			rd.gid |= defMask;
			parsedData.put(id, rd);
			Log.debug(ea.filename + " " + Integer.toString(rd.gid, 2));
			List<ModelElement> elems = ea.getComponentsFiltered();
			List<AnimFrame> frames = ea.getFrames();
			int fc = frames.size();
			rd.frames = fc;
			int cs = elems.size();
			rd.duration = ea.duration;
			rd.components = new int[cs];
			rd.pos = new Vec3f[cs][];
			rd.rot = new Vec3f[cs][];
			rd.scale = new Vec3f[cs][];
			rd.color = new Vec3f[cs][];
			rd.show = new Boolean[cs][];
			rd.loop = ea.loop;
			rd.priority = ea.priority;
			rd.it = ea.intType;
			for (int i = 0; i < cs; i++) {
				ModelElement elem = elems.get(i);
				rd.components[i] = elem.id;
				if(frames.stream().anyMatch(f -> f.hasPosChanges(elem))) {
					rd.pos[i] = new Vec3f[fc];
					fillArray(rd.pos[i], frames, elem, IElem::getPosition, ea.add, new Vec3f());
				}
				if(frames.stream().anyMatch(f -> f.hasRotChanges(elem))) {
					rd.rot[i] = new Vec3f[fc];
					fillArray(rd.rot[i], frames, elem, IElem::getRotation, ea.add, new Vec3f());
				}
				if(frames.stream().anyMatch(f -> f.hasColorChanges(elem))) {
					rd.color[i] = new Vec3f[fc];
					fillArray(rd.color[i], frames, elem, IElem::getColor, ea.add, new Vec3f());
				}
				if(frames.stream().anyMatch(f -> f.hasVisChanges(elem))) {
					rd.show[i] = new Boolean[fc];
					fillArray(rd.show[i], frames, elem, IElem::isVisible, ea.add, !elem.hidden);
				}
				if(frames.stream().anyMatch(f -> f.hasScaleChanges(elem))) {
					rd.scale[i] = new Vec3f[fc];
					fillArray(rd.scale[i], frames, elem, IElem::getScale, ea.add, new Vec3f(1, 1, 1));
				}
			}
		});
	}

	private static void resolveEncID(ResolvedData rd, int id, List<PlayerSkinLayer> allLayers) {
		if(allLayers.size() < 2)throw new Exporter.ExportException("error.cpm.custom_anims_not_supported");
		Set<PlayerSkinLayer> encL = new HashSet<>();
		for (int i = 0; i < allLayers.size(); i++) {
			PlayerSkinLayer l = allLayers.get(i);
			if((id & (1 << i)) != 0) {
				encL.add(l);
			}
		}
		if(encL.containsAll(allLayers))throw new Exporter.ExportException("error.cpm.too_many_animations");
		rd.gid = PlayerSkinLayer.encode(encL);
	}

	private static <T> void fillArray(T[] array, List<AnimFrame> frames, ModelElement elem, Function<IElem, T> func, boolean add, T empty) {
		for (int i = 0; i < frames.size(); i++) {
			AnimFrame frm = frames.get(i);
			IElem dt = frm.getData(elem);
			if(dt == null) {
				if(add)array[i] = empty;
				else array[i] = func.apply(elem);
			} else array[i] = func.apply(dt);
		}
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		for (Entry<Integer, ResolvedData> entry : parsedData.entrySet()) {
			int id = entry.getKey();
			ResolvedData dt = entry.getValue();
			if(dt.pose != null) {
				dout.writeEnum(Type.POSE);
				try(IOHelper d = dout.writeNextBlock()) {
					if(dt.pose instanceof VanillaPose) {
						d.writeEnum((VanillaPose) dt.pose);
					} else {
						d.writeEnum(VanillaPose.CUSTOM);
					}
					d.write(id);
					int flags = 0;
					if(dt.add)flags |= 1;
					d.write(flags);
					if(dt.pose instanceof CustomPose) {
						d.writeUTF(((CustomPose) dt.pose).getId());
					}
				}
			} else {
				dout.writeEnum(Type.GESTURE);
				try(IOHelper d = dout.writeNextBlock()) {
					d.write(id);
					d.writeUTF(dt.name);
					int flags = 0;
					if(dt.add)flags |= 1;
					if(dt.loop)flags |= 2;
					d.write(flags);
				}
			}
			dout.writeEnum(Type.ANIMATION_INFO);
			try(IOHelper d = dout.writeNextBlock()) {
				d.write(id);
				d.write(dt.components.length);
				d.write(dt.frames);
				d.writeShort(dt.duration);
				for (int i = 0; i < dt.components.length; i++) {
					d.writeVarInt(dt.components[i]);
				}
			}
			if(dt.gid != -1) {
				dout.writeEnum(Type.ENCODING);
				try(IOHelper d = dout.writeNextBlock()) {
					d.write(id);
					d.write(dt.gid);
				}
			}
			for (int i = 0; i < dt.components.length; i++) {
				if(dt.pos[i] != null) {
					dout.writeEnum(Type.ANIMATION_DATA_POS);
					try(IOHelper d = dout.writeNextBlock()) {
						d.write(id);
						d.write(i);
						for(int f = 0;f<dt.frames;f++) {
							d.writeVec6b(dt.pos[i][f]);
						}
					}
				}
				if(dt.rot[i] != null) {
					dout.writeEnum(Type.ANIMATION_DATA_ROTATION);
					try(IOHelper d = dout.writeNextBlock()) {
						d.write(id);
						d.write(i);
						for(int f = 0;f<dt.frames;f++) {
							d.writeAngle(dt.rot[i][f]);
						}
					}
				}
				if(dt.color[i] != null) {
					dout.writeEnum(Type.ANIMATION_DATA_COLOR);
					try(IOHelper d = dout.writeNextBlock()) {
						d.write(id);
						d.write(i);
						for(int f = 0;f<dt.frames;f++) {
							Vec3f c = dt.color[i][f];
							d.write((int) c.x);
							d.write((int) c.y);
							d.write((int) c.z);
						}
					}
				}
				if(dt.show[i] != null) {
					dout.writeEnum(Type.ANIMATION_DATA_VIS);
					try(IOHelper d = dout.writeNextBlock()) {
						d.write(id);
						d.write(i);
						for(int f = 0;f<dt.frames;f+=8) {
							int flgs = 0;
							for(int j = 0;f+j < dt.frames && j < 8;j++) {
								if(dt.show[i][f+j])flgs |= (1 << j);
							}
							d.write(flgs);
						}
					}
				}
				if(dt.scale[i] != null) {
					dout.writeEnum(Type.ANIMATION_DATA_SCALE);
					try(IOHelper d = dout.writeNextBlock()) {
						d.write(id);
						d.write(i);
						for(int f = 0;f<dt.frames;f++) {
							d.writeVec6b(dt.scale[i][f]);
						}
					}
				}
			}
			if(dt.priority != 0) {
				dout.writeEnum(Type.ANIMATION_DATA_EXTRA);
				try(IOHelper d = dout.writeNextBlock()) {
					d.write(id);
					d.writeByte(dt.priority);
				}
			}
			if(dt.it != InterpolatorType.POLY_LOOP) {
				dout.writeEnum(Type.ANIMATION_DATA_INTERPOLATION);
				try(IOHelper d = dout.writeNextBlock()) {
					d.write(id);
					d.writeEnum(dt.it);
				}
			}
		}
		dout.writeEnum(Type.CTRL_IDS);
		try(IOHelper d = dout.writeNextBlock()) {
			d.write(blankId);
			d.write(resetId);
		}
		dout.writeEnum(Type.END);
	}

	@Override
	public void apply(ModelDefinition def) {
		parsedData.values().forEach(rd -> {
			IModelComponent[] comp = new IModelComponent[rd.components.length];
			for (int i = 0; i < comp.length; i++) {
				comp[i] = def.getElementById(rd.components[i]);
			}
			float[][][] data = new float[rd.components.length][InterpolatorChannel.VALUES.length][rd.frames];
			for (int i = 0; i < comp.length; i++) {
				Vec3f[] pos = rd.pos[i];
				Vec3f[] rot = rd.rot[i];
				Vec3f[] scale = rd.scale[i];
				Vec3f[] color = rd.color[i];
				float[][] dt = data[i];
				IModelComponent c = comp[i];
				for(int f = 0;f<rd.frames;f++) {
					if(pos != null) {
						dt[0][f] = pos[f].x;
						dt[1][f] = pos[f].y;
						dt[2][f] = pos[f].z;
					} else if(!rd.add) {
						dt[0][f] = c.getPosition().x;
						dt[1][f] = c.getPosition().y;
						dt[2][f] = c.getPosition().z;
					}
					if(rot != null) {
						dt[3][f] = rot[f].x;
						dt[4][f] = rot[f].y;
						dt[5][f] = rot[f].z;
					} else if(!rd.add) {
						dt[3][f] = c.getRotation().x;
						dt[4][f] = c.getRotation().y;
						dt[5][f] = c.getRotation().z;
					}
					if(color != null) {
						dt[6][f] = color[f].x;
						dt[7][f] = color[f].y;
						dt[8][f] = color[f].z;
					} else if(c.getRGB() != -1){
						dt[6][f] = ((c.getRGB() & 0xff0000) >> 16);
						dt[7][f] = ((c.getRGB() & 0x00ff00) >> 8);
						dt[8][f] = c.getRGB() & 0x0000ff;
					}
					if(scale != null) {
						dt[9 ][f] = scale[f].x;
						dt[10][f] = scale[f].y;
						dt[11][f] = scale[f].z;
					} else if(!rd.add) {
						dt[9 ][f] = 1;
						dt[10][f] = 1;
						dt[11][f] = 1;
					}
				}
				if(rd.show[i] == null) {
					rd.show[i] = new Boolean[rd.frames];
					Arrays.fill(rd.show[i], c.isVisible());
				}
			}
			if(rd.dynamicProgress())rd.duration = VanillaPose.DYNAMIC_DURATION_DIV;
			rd.anim = new Animation(comp, data, rd.show, rd.duration, rd.priority, rd.add, rd.it);
		});
		Map<String, List<IAnimation>> gestures = new HashMap<>();
		parsedData.values().forEach(rd -> {
			if(rd.pose instanceof VanillaPose) {
				def.getAnimations().register(rd.pose, rd.anim);
			} else if(rd.name != null) {
				gestures.computeIfAbsent(rd.name, k -> {
					List<IAnimation> l = new ArrayList<>();
					Gesture g = new Gesture(l, rd.name, rd.loop);
					def.getAnimations().register(g);
					if(rd.gid != -1)
						def.getAnimations().register(rd.gid, g);
					return l;
				}).add(rd.anim);
			} else {
				def.getAnimations().register(rd.pose, rd.anim);
				def.getAnimations().register((CustomPose) rd.pose);
				if(rd.gid != -1)
					def.getAnimations().register(rd.gid, rd.pose);
			}
		});
		def.getAnimations().setBlankGesture(blankId);
		def.getAnimations().setPoseResetId(resetId);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.ANIMATION_DATA;
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("Animations:");
		for (ResolvedData r : parsedData.values()) {
			bb.append("\n\t");
			bb.append(r.toString().replace("\n", "\n\t\t"));
		}
		return bb.toString();
	}

	public static enum Type {
		END,
		POSE,
		GESTURE,
		ANIMATION_DATA_ROTATION,
		ANIMATION_DATA_POS,
		ANIMATION_DATA_VIS,
		ANIMATION_DATA_COLOR,
		ANIMATION_INFO,
		ENCODING,
		CTRL_IDS,
		ANIMATION_DATA_EXTRA,
		ANIMATION_DATA_INTERPOLATION,
		ANIMATION_DATA_SCALE,
		;
		public static final Type[] VALUES = values();
	}

	private static class ResolvedData {
		private IPose pose;
		private int gid = -1;
		private String name;
		private int[] components;
		private Vec3f[][] pos;
		private Vec3f[][] rot;
		private Vec3f[][] scale;
		private Vec3f[][] color;
		private Boolean[][] show;
		private int frames, duration;
		private Animation anim;
		private boolean loop;
		private boolean add;
		private int priority;
		private InterpolatorType it = InterpolatorType.POLY_LOOP;

		public ResolvedData(VanillaPose pose, boolean add) {
			this.pose = pose;
			this.add = add;
		}

		public ResolvedData(IPose pose, boolean add) {
			this.pose = pose;
			this.add = add;
		}

		public ResolvedData(String name, boolean loop, boolean add) {
			this.name = name;
			this.loop = loop;
			this.add = add;
		}

		@Override
		public String toString() {
			StringBuilder bb = new StringBuilder("Animation: ");
			if(pose != null)bb.append(pose);
			else bb.append(name);
			bb.append("\n\tAdd: ");
			bb.append(add);
			bb.append("\n\tLoop: ");
			bb.append(loop);
			bb.append("\n\tFrame Count: ");
			bb.append(frames);
			bb.append("\n\tDuration: ");
			bb.append(duration);
			bb.append("ms\n\tComponents: ");
			bb.append(components.length);
			bb.append("\n\t\t");
			bb.append(Arrays.toString(components));
			bb.append("\n\tPriority: ");
			bb.append(priority);
			bb.append("\n\tColorValues: \n\t\t[");
			for (Vec3f[] vec3fs : color) {
				bb.append(Arrays.toString(vec3fs));
				bb.append(", ");
			}
			bb.append("]");
			return bb.toString();
		}

		public boolean dynamicProgress() {
			return pose instanceof VanillaPose && ((VanillaPose)pose).hasStateGetter();
		}
	}
}
