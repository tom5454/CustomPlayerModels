package com.tom.cpm.web.client.fbxtool;

import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.web.client.fbxtool.three.Bone;
import com.tom.cpm.web.client.fbxtool.three.Object3D;
import com.tom.cpm.web.client.fbxtool.three.Raycaster;
import com.tom.cpm.web.client.fbxtool.three.Raycaster.RayResult;
import com.tom.cpm.web.client.fbxtool.three.SkeletonHelper;
import com.tom.cpm.web.client.fbxtool.three.ThreeModule;
import com.tom.cpm.web.client.fbxtool.three.ThreeVec2;
import com.tom.cpm.web.client.fbxtool.three.ThreeVec3;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.ugwt.client.JsArrayE;

import elemental2.dom.DomGlobal;

public class ThreePreview extends GuiElement {
	private Runnable update;
	private boolean dirty;
	private Raycaster raycast;
	private ThreeVec2 mouse;
	public Object3D hover;

	public ThreePreview(IGui gui, int x, int y, int width, int height, Runnable update) {
		super(gui);
		this.update = update;
		setBounds(new Box(x, y, width, height));
		ThreeModule.prepare().then(__ -> {
			ThreeModule.renderer.domElement.style.left = (RenderSystem.displayRatio * x) + "px";
			ThreeModule.renderer.domElement.style.top = (RenderSystem.displayRatio * y) + "px";
			ThreeModule.renderer.setSize(width * RenderSystem.displayRatio, height * RenderSystem.displayRatio);
			ThreeModule.camera.aspect = width / (float) height;
			raycast = new Raycaster();
			mouse = new ThreeVec2();
			dirty = true;
			return null;
		});
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if (ThreeModule.isLoaded) {
			if(dirty) {
				update.run();
				dirty = false;
			}
			ThreeModule.render();
			if (hover != null) {
				mouse.x = ((float) (event.x - bounds.x) / bounds.w) * 2 - 1;
				mouse.y = -((float) (event.y - bounds.y) / bounds.h) * 2 + 1;
				raycast.setFromCamera(mouse, ThreeModule.camera);
				JsArrayE<RayResult> i = raycast.intersectObject(hover);
				if (i.length > 0) {
					event.consume();
					String msg = i.asList().stream().map(r -> {
						/*if (r.object instanceof SkeletonHelper) {
							Bone b = ((SkeletonHelper)r.object).bones[r.index];
							if (b == null)return r.index + ": ~~NULL~~";
							return r.index + ": " + b.name;
						}*/
						if (hover instanceof SkeletonHelper) {
							SkeletonHelper sk = (SkeletonHelper) hover;
							Bone closest = null;
							double dist = Double.POSITIVE_INFINITY;
							for (int j = 0; j < sk.bones.length; j++) {
								Bone b = sk.bones[j];
								ThreeVec3 v3 = new ThreeVec3();
								b.getWorldPosition(v3);
								double d = r.point.distanceTo(v3);
								if (d < dist) {
									dist = d;
									closest = b;
								}
							}
							return closest;
						}
						return null;
					}).filter(e -> e != null).distinct().map(e -> e.name).collect(Collectors.joining("\\"));
					new Tooltip(gui.getFrame(), msg).set();
					DomGlobal.console.log(i);
				}
			}

			if(event.isConsumed()) {
				ThreeModule.renderer.domElement.style.visibility = "hidden";
				RenderSystem.renderCanvas(ThreeModule.renderer.domElement, bounds.x, bounds.y, bounds.w, bounds.h);
			} else {
				ThreeModule.renderer.domElement.style.visibility = "";
			}
		} else
			gui.drawText(bounds.w / 4, bounds.h * 3 / 4, gui.i18nFormat("fbxtool-label.threeLoading"), gui.getColors().label_text_color);
	}

	public void markDirty() {
		dirty = true;
	}
}
