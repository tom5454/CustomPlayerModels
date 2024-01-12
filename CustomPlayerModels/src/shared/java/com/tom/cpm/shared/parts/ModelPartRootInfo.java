package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class ModelPartRootInfo implements IModelPart, IResolvedModelPart {
	public static final int ROOT_HIDDEN          = 1 << 0;
	public static final int ROOT_CREATE          = 1 << 1;
	public static final int ROOT_MODEL           = 1 << 2;
	public static final int ROOT_TRANSFORM       = 1 << 3;
	public static final int ROOT_DISABLE_VANILLA = 1 << 4;
	private VanillaModelPart root;
	private Vec3f pos, rot;
	private int createFrom;
	private boolean hidden, disableVanilla;

	public ModelPartRootInfo(IOHelper is, ModelDefinition def) throws IOException {
		byte flags = is.readByte();
		if ((flags & ROOT_MODEL) != 0) {
			root = is.readEnum(RootModelType.VALUES);
		} else {
			root = is.readEnum(PlayerModelParts.VALUES);
		}
		if ((flags & ROOT_TRANSFORM) != 0) {
			pos = is.readVarVec3();
			rot = is.readAngle();
		} else {
			pos = new Vec3f();
			rot = new Vec3f();
		}
		if ((flags & ROOT_CREATE) != 0) {
			createFrom = is.readVarInt();
		}
		hidden = (flags & ROOT_HIDDEN) != 0;
		disableVanilla = (flags & ROOT_DISABLE_VANILLA) != 0;
	}

	public ModelPartRootInfo(VanillaModelPart root, Vec3f pos, Vec3f rot, boolean hidden, boolean disableVanilla) {
		this.root = root;
		this.pos = pos;
		this.rot = rot;
		this.hidden = hidden;
		this.disableVanilla = disableVanilla;
	}

	public ModelPartRootInfo(VanillaModelPart root, int createFrom, boolean hidden, boolean disableVanilla) {
		this.root = root;
		this.pos = Vec3f.ZERO;
		this.rot = Vec3f.ZERO;
		this.createFrom = createFrom;
		this.hidden = hidden;
		this.disableVanilla = disableVanilla;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void write(IOHelper dout) throws IOException {
		boolean transform = !pos.epsilon(0.1f) || !rot.epsilon(0.1f);
		byte flags = 0;
		if (root instanceof RootModelType)flags |= ROOT_MODEL;
		if (transform)flags |= ROOT_TRANSFORM;
		if (hidden)flags |= ROOT_HIDDEN;
		if (createFrom != 0)flags |= ROOT_CREATE;
		if (disableVanilla)flags |= ROOT_DISABLE_VANILLA;
		dout.writeByte(flags);
		dout.writeEnum((Enum) root);
		if (transform) {
			dout.writeVarVec3(pos);
			Vec3f rot = new Vec3f(this.rot);
			ActionBuilder.limitVec(rot, 0, 360, true);
			dout.writeAngle(rot);
		}
		if (createFrom != 0) {
			dout.writeVarInt(createFrom);
		}
	}

	@Override
	public void preApply(ModelDefinition def) {
		if (createFrom == 0) {
			RootModelElement elem = def.getModelElementFor(root).getMainRoot();
			elem.setHidden(hidden);
			elem.posN = pos;
			elem.rotN = new Rotation(rot, false);
			elem.disableVanilla = disableVanilla;
		}
	}

	@Override
	public void apply(ModelDefinition def) {
		if (createFrom != 0) {
			RootModelElement elem = def.getModelElementFor(PlayerModelParts.CUSTOM_PART).get();
			for (RenderedCube rc : elem.children) {
				if (rc.getId() == createFrom) {
					elem.children.remove(rc);
					RootModelElement e = def.addRoot(createFrom, root);
					e.posN = rc.pos;
					e.rotN = rc.rotation;
					e.setHidden(hidden);
					e.disableVanilla = disableVanilla;
					if(rc.children != null) {
						e.children.addAll(rc.children);
						rc.children.forEach(p -> p.setParent(e));
					}
					break;
				}
			}
		}
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.ROOT_INFO;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Part Root Info: ");
		sb.append(root);
		if (hidden) sb.append(" [H]");
		if (disableVanilla)sb.append(" [DV]");
		if (!pos.epsilon(0.1f)) {
			sb.append("\n\tPos: ");
			sb.append(pos);
		}
		if (!rot.epsilon(0.1f)) {
			sb.append("\n\tRot: ");
			sb.append(rot);
		}
		if (createFrom != 0)sb.append("\n\tNew Root");
		return sb.toString();
	}
}
