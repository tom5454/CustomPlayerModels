package com.tom.cpm.client.vr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.vivecraft.client.render.VRPlayerModel;
import org.vivecraft.client.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager.Field;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;
import com.tom.cpm.shared.util.Log;

public class RedirectHolderVRPlayer extends RDH {
	private RedirectRenderer<ModelPart> head;
	private RedirectRenderer<ModelPart> leftArm;
	private RedirectRenderer<ModelPart> rightArm;
	private boolean seated;
	private static boolean VC120;
	private static java.lang.reflect.Field[] OLD_FIELDS;
	static {
		try {
			OLD_FIELDS = new java.lang.reflect.Field[] {
					VRPlayerModel_WithArms.class.getDeclaredField("leftShoulder"),
					VRPlayerModel_WithArms.class.getDeclaredField("rightShoulder"),
					VRPlayerModel_WithArms.class.getDeclaredField("leftShoulder_sleeve"),
					VRPlayerModel_WithArms.class.getDeclaredField("rightShoulder_sleeve"),
			};
			VC120 = false;
		} catch (Throwable e) {
			VC120 = true;
		}
	}

	public RedirectHolderVRPlayer(PlayerRenderManager mngr, VRPlayerModel<AbstractClientPlayer> model) {
		super(mngr, model);

		seated = !(model instanceof VRPlayerModel_WithArms);

		head = registerHead(new Field<>(    () -> model.head     , v -> model.head      = v, PlayerModelParts.HEAD));
		register(new Field<>(           () -> model.body     , v -> model.body      = v, PlayerModelParts.BODY));
		if(seated) {
			rightArm = register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
		} else {
			VRPlayerModel_WithArms<AbstractClientPlayer> w = (VRPlayerModel_WithArms<AbstractClientPlayer>) model;
			rightArm = register(new Field<>(() -> w.rightHand, v -> w.rightHand = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(new Field<>( () -> w.leftHand , v -> w.leftHand  = v, PlayerModelParts.LEFT_ARM));
		}
		register(new Field<>(           () -> model.rightLeg , v -> model.rightLeg  = v, PlayerModelParts.RIGHT_LEG));
		register(new Field<>(           () -> model.leftLeg  , v -> model.leftLeg   = v, PlayerModelParts.LEFT_LEG));

		register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);
		register(new Field<>(() -> model.leftSleeve , v -> model.leftSleeve  = v, null));
		register(new Field<>(() -> model.rightSleeve, v -> model.rightSleeve = v, null));
		register(new Field<>(() -> model.leftPants  , v -> model.leftPants   = v, null));
		register(new Field<>(() -> model.rightPants , v -> model.rightPants  = v, null));
		register(new Field<>(() -> model.jacket     , v -> model.jacket      = v, null));

		register(new Field<>(() -> model.vrHMD, v -> model.vrHMD = v, null));//disable
		if(!seated) {
			VRPlayerModel_WithArms<AbstractClientPlayer> w = (VRPlayerModel_WithArms<AbstractClientPlayer>) model;
			if (VC120) {
				register(new Field<>(() -> w.leftArm , v -> w.leftArm  = v, null));//disable
				register(new Field<>(() -> w.rightArm, v -> w.rightArm = v, null));//disable

				register(new Field<>(() -> w.leftHandSleeve , v -> w.leftHandSleeve  = v, null));//disable
				register(new Field<>(() -> w.rightHandSleeve, v -> w.rightHandSleeve = v, null));//disable
			} else {
				try {
					var lookup = MethodHandles.lookup();
					for (int i = 0; i < OLD_FIELDS.length; i++) {
						var field = OLD_FIELDS[i];
						MethodHandle get = lookup.unreflectGetter(field).bindTo(w);
						MethodHandle set = lookup.unreflectSetter(field).bindTo(w);

						register(new Field<>(() -> {
							try {
								return (ModelPart) get.invoke();
							} catch (Throwable e) {
								return model.vrHMD;//Fallback nonull
							}
						}, v -> {
							try {
								set.invoke(v);
							} catch (Throwable e) {
								// Do nothing
							}
						}, null));//disable
					}
				} catch (Throwable e) {
					Log.warn("Failed to bind legacy field accessors for VRPlayerModel_WithArms class", e);
				}
			}
		}

		register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
	}

	@Override
	protected void setupTransform(MatrixStack stack, RedirectRenderer<ModelPart> part, boolean pre) {
		if (VC120)return;
		if(!pre && (leftArm == part || rightArm == part) && !seated) {
			stack.translate(0, -8 / 16f, 0);
		}
	}

	public static interface GetOld extends Function<VRPlayerModel_WithArms<AbstractClientPlayer>, ModelPart> {
		@Override
		ModelPart apply(VRPlayerModel_WithArms<AbstractClientPlayer> t);

		default Supplier<ModelPart> bind(VRPlayerModel_WithArms<AbstractClientPlayer> v) {
			return () -> apply(v);
		}
	}

	public static interface SetOld extends BiConsumer<VRPlayerModel_WithArms<AbstractClientPlayer>, ModelPart> {
		@Override
		void accept(VRPlayerModel_WithArms<AbstractClientPlayer> t, ModelPart part);

		default Consumer<ModelPart> bind(VRPlayerModel_WithArms<AbstractClientPlayer> v) {
			return e -> accept(v, e);
		}
	}
}
