package com.tom.cpm.shared.parts.anim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;

public class LayerEncondingHelper {

	public static void handleLayers(Map<Integer, SerializedTrigger> triggers, List<PlayerSkinLayer> allLayers, List<AbstractGestureButtonData> gestureButtons, int blank, int reset, int valMask, int defMask) {
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
				gestureButtons.stream().
				filter(b -> b instanceof CustomPoseGestureButtonData && ((CustomPoseGestureButtonData) b).id == t.value).
				findFirst().ifPresent(btn -> {
					CustomPoseGestureButtonData b = (CustomPoseGestureButtonData) btn;
					b.gid = t.gid;
				});
			}
		}
	}
}
