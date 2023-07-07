package com.tom.cpm.web.client.fbxtool;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.web.client.fbxtool.FBXRenderer.FBXDefinition;
import com.tom.cpm.web.client.fbxtool.FBXRenderer.FBXStack;
import com.tom.cpm.web.client.fbxtool.three.DataTexture;
import com.tom.cpm.web.client.fbxtool.three.MeshBasicMaterial;
import com.tom.cpm.web.client.fbxtool.three.MeshBasicMaterial.MeshBasicMaterialInit;
import com.tom.cpm.web.client.fbxtool.three.Object3D;
import com.tom.cpm.web.client.fbxtool.three.Skeleton;
import com.tom.cpm.web.client.fbxtool.three.Three;

import elemental2.core.Uint8Array;

public class FBXCreator {
	private FBXRenderer renderer = new FBXRenderer();
	public RenderTypes<RenderMode> types;
	private FBXMaterial color = new FBXMaterial("color");
	private FBXMaterial colorGlow = new FBXMaterial("colorGlow");
	private Map<TextureSheetType, Image> allTextures = new HashMap<>();
	private boolean humanoidRig;

	public void loadSkin(TextureProvider skin, TextureSheetType tx) {
		types = getRenderTypes0(tx, skin.getImage());
	}

	public void loadFallback(TextureSheetType tx) {
		Image img;
		try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tx.name().toLowerCase(Locale.ROOT) + ".png")) {
			img = Image.loadFrom(is);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		types = getRenderTypes0(tx, img);
	}

	protected RenderTypes<RenderMode> getRenderTypes0(TextureSheetType tx, Image img) {
		String name = tx.name().toLowerCase(Locale.ROOT);
		allTextures.put(tx, img);
		RenderTypes<RenderMode> renderTypes = new RenderTypes<>(RenderMode.class);
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(0));
		renderTypes.put(RenderMode.DEFAULT, new NativeRenderType(new FBXMaterial(name, "def", img), 0));
		renderTypes.put(RenderMode.GLOW, new NativeRenderType(new FBXMaterial(name, "glow", img), 1));
		renderTypes.put(RenderMode.COLOR, new NativeRenderType(color, 0));
		renderTypes.put(RenderMode.COLOR_GLOW, new NativeRenderType(colorGlow, 1));
		renderTypes.disableType(RenderMode.OUTLINE);
		return renderTypes;
	}

	public void putRenderTypes(RenderTypes<RenderMode> renderTypes) {
		renderTypes.putAll(this.types);
	}

	public void render(FBXDefinition def, Consumer<Object3D> sc) {
		allTextures = new HashMap<>();
		GeometryBuffer buffer = new GeometryBuffer();
		FBXStack stack = new FBXStack(buffer);
		def.setStack(stack);
		def.renderPre();
		stack.scale(16, 16, 16);
		VanillaPlayerModel p = renderer.getModel(def.getSkinType());
		p.reset();
		p.setAllVisible(true);
		PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
		p.rightArm.zRot = (float) Math.toRadians(90);
		p.leftArm.zRot = (float) Math.toRadians(-90);
		VBuffers buf = new VBuffers(r -> buffer.getBuffer(r.getNativeType()));
		renderer.setupSkin(this, p, buf, def, AnimationMode.PLAYER);
		VertexBuffer b = buf.getBuffer(types, RenderMode.DEFAULT);
		buffer.getBone().name = "Root";
		if(humanoidRig) {
			buffer.setBoneAbsolutePos(0, 24, 0);
			buffer.push();
			buffer.getBone().name = "Hips";
			buffer.setBoneAbsolutePos(0, 9, 0);
			buffer.push();
			buffer.getBone().name = "Spine";
			buffer.setBoneAbsolutePos(0, 5, 0);
			buffer.push();
			buffer.getBone().name = "Chest";
			buffer.setBoneAbsolutePos(0, 2, 0);
			p.body.render(stack, b);
			{
				buffer.push();
				buffer.getBone().name = "Neck";
				buffer.setBoneAbsolutePos(0, -1f, 0);
				p.head.render(stack, b);
				buffer.pop();
			}
			{
				buffer.push();
				buffer.getBone().name = "Left Shoulder";
				buffer.setBoneAbsolutePos(5f, 1F, 0);
				p.leftArm.render(stack, b);
				buffer.pop();
			}
			{
				buffer.push();
				buffer.getBone().name = "Right Shoulder";
				buffer.setBoneAbsolutePos(-5f, 1F, 0);
				p.rightArm.render(stack, b);
				buffer.pop();
			}
			buffer.pop();
			buffer.pop();
			buffer.offsetAbsolute(0, 3, 0);
			p.leftLeg.render(stack, b);
			p.rightLeg.render(stack, b);
		} else {
			p.render(stack, b);
		}
		renderer.unbindModel(p);
		types = null;
		def.setStack(null);
		Skeleton sk = buffer.bake(sc);
		if(humanoidRig)sk.bones[0].position.set(0, -24, 0);
	}

	public static class FBXMaterial {
		public MeshBasicMaterial mat;

		public FBXMaterial(String name) {
			MeshBasicMaterialInit mbmi = new MeshBasicMaterialInit();
			mbmi.vertexColors = true;
			mbmi.side = Three.DoubleSide;
			mat = new MeshBasicMaterial(mbmi);
			mat.name = name;
		}

		public FBXMaterial(String name, String type, Image img) {
			MeshBasicMaterialInit mbmi = new MeshBasicMaterialInit();
			mbmi.vertexColors = true;
			mbmi.alphaTest = 0.5f;
			mbmi.side = Three.DoubleSide;
			mat = new MeshBasicMaterial(mbmi);
			mat.name = name + "-" + type;
			DataTexture texture = new DataTexture(new Uint8Array(img.getData().buffer), img.getWidth(), img.getHeight(), Three.RGBAFormat);
			texture.name = name;
			texture.minFilter = Three.NearestFilter;
			texture.magFilter = Three.NearestFilter;
			texture.needsUpdate = true;
			mat.map = texture;
		}
	}

	public Map<TextureSheetType, Image> getTextures() {
		return allTextures;
	}

	public void setHumanoidRig(boolean humanoidRig) {
		this.humanoidRig = humanoidRig;
	}
}