package com.tom.cpm.shared.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.tag.TagType;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.util.ExportHelper;
import com.tom.cpm.shared.effects.EffectColor;
import com.tom.cpm.shared.effects.EffectCopyTransform;
import com.tom.cpm.shared.effects.EffectExtrude;
import com.tom.cpm.shared.effects.EffectFirstPersonHandPos;
import com.tom.cpm.shared.effects.EffectGlow;
import com.tom.cpm.shared.effects.EffectHideSkull;
import com.tom.cpm.shared.effects.EffectInvisGlow;
import com.tom.cpm.shared.effects.EffectModelScale;
import com.tom.cpm.shared.effects.EffectPerFaceUV;
import com.tom.cpm.shared.effects.EffectRemoveArmorOffset;
import com.tom.cpm.shared.effects.EffectRemoveBedOffset;
import com.tom.cpm.shared.effects.EffectRenderItem;
import com.tom.cpm.shared.effects.EffectScaling;
import com.tom.cpm.shared.effects.EffectSingleTexture;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartAnimatedTexture;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartCollection;
import com.tom.cpm.shared.parts.ModelPartCubes;
import com.tom.cpm.shared.parts.ModelPartRenderEffect;
import com.tom.cpm.shared.parts.ModelPartRootInfo;
import com.tom.cpm.shared.parts.ModelPartTags;
import com.tom.cpm.shared.parts.ModelPartTemplate;
import com.tom.cpm.shared.parts.ModelPartTexture;
import com.tom.cpm.shared.parts.ModelPartUUIDLockout;
import com.tom.cpm.shared.parts.anim.ModelPartAnimationNew;
import com.tom.cpm.shared.util.Log;

public class ExporterImpl {
	public static ModelPartCollection prepareExport(Editor e) throws IOException {
		ModelPartCollection parts = new ModelPartCollection();
		List<IModelPart> otherParts2 = new ArrayList<>();
		List<Cube> flatList = new ArrayList<>();
		ExportHelper.flattenElements(e.elements, new int[] {10}, flatList);
		parts.add(new ModelPartCubes(flatList));
		for (ModelElement el : e.elements) {
			if(el.type == ElementType.ROOT_PART) {
				if (el.typeData instanceof PlayerModelParts && !el.duplicated) {
					if (el.hidden || !el.pos.epsilon(0.1f) || !el.rotation.epsilon(0.1f) || el.disableVanillaAnim) {
						parts.add(new ModelPartRootInfo((VanillaModelPart) el.typeData, el.pos, el.rotation, el.hidden, el.disableVanillaAnim));
					}
				} else {
					parts.add(new ModelPartRootInfo((VanillaModelPart) el.typeData, el.id, el.hidden, el.disableVanillaAnim));
				}
			}
		}
		ExportHelper.walkElements(e.elements, el -> {
			if(el.type == ElementType.NORMAL) {
				if(el.glow) {
					parts.add(new ModelPartRenderEffect(new EffectGlow(el.id)));
				}
				if(el.recolor) {
					parts.add(new ModelPartRenderEffect(new EffectColor(el.id, el.rgb)));
				}
				if(el.singleTex) {
					parts.add(new ModelPartRenderEffect(new EffectSingleTexture(el.id)));
				}
				if(el.extrude) {
					parts.add(new ModelPartRenderEffect(new EffectExtrude(el.id)));
				}
				if(el.faceUV != null) {
					parts.add(new ModelPartRenderEffect(new EffectPerFaceUV(el.id, el.faceUV)));
				}
				if(el.itemRenderer != null) {
					parts.add(new ModelPartRenderEffect(new EffectRenderItem(el.id, el.itemRenderer.slot, el.itemRenderer.slotID)));
				}
				if(el.copyTransform != null && el.copyTransform.from != null) {
					otherParts2.add(new ModelPartRenderEffect(new EffectCopyTransform(el.copyTransform.from.id, el.id, el.copyTransform.toShort())));
				}
			}
		});
		parts.addAll(otherParts2);
		if(!e.animations.isEmpty()) {
			parts.add(new ModelPartAnimationNew(e));
		}
		e.textures.forEach((type, tex) -> {
			if(type.editable) {
				parts.add(new ModelPartTexture(e, type));
				if(!tex.animatedTexs.isEmpty())
					tex.animatedTexs.forEach(at -> parts.add(new ModelPartAnimatedTexture(type, at)));
			}
		});
		for (EditorTemplate et : e.templates) {
			parts.add(new ModelPartTemplate(et));
		}
		if(e.scalingElem.enabled) {
			parts.add(new ModelPartRenderEffect(new EffectScaling(e.scalingElem.scaling)));
			if(e.scalingElem.hasTransform())
				parts.add(new ModelPartRenderEffect(new EffectModelScale(e.scalingElem.pos, e.scalingElem.rotation, e.scalingElem.scale)));
		}
		//TODO global flags effect
		if(!e.hideHeadIfSkull)parts.add(new ModelPartRenderEffect(new EffectHideSkull(e.hideHeadIfSkull)));
		if(e.removeArmorOffset)parts.add(new ModelPartRenderEffect(new EffectRemoveArmorOffset(e.removeArmorOffset)));
		if(e.removeBedOffset)parts.add(new ModelPartRenderEffect(new EffectRemoveBedOffset()));
		if(e.enableInvisGlow)parts.add(new ModelPartRenderEffect(new EffectInvisGlow()));
		if(e.leftHandPos.isChanged() || e.rightHandPos.isChanged())
			parts.add(new ModelPartRenderEffect(new EffectFirstPersonHandPos(e.leftHandPos, e.rightHandPos)));
		for (TagType t : TagType.VALUES) {
			if (e.tags.getByType(t).hasTags()) {
				parts.add(new ModelPartTags(t, e.tags.getByType(t)));
			}
		}

		if(e.description != null) {
			switch (e.description.copyProtection) {
			case CLONEABLE:
				parts.add(new ModelPartCloneable(e.description.name, e.description.desc, e.description.icon));
				break;
			case NORMAL:
				break;
			case UUID_LOCK:
				parts.add(new ModelPartUUIDLockout(e.description.uuid != null ? e.description.uuid : MinecraftClientAccess.get().getClientPlayer().getUUID()));
				break;
			default:
				break;
			}
		}
		if(MinecraftObjectHolder.DEBUGGING)Log.info(parts);
		return parts;
	}
}
