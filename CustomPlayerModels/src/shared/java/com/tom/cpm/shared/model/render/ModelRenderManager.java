package com.tom.cpm.shared.model.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.IPlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.EditorDefinition;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public abstract class ModelRenderManager<D, S, P, MB> implements IPlayerRenderManager {
	public static final Predicate<Player<?, ?>> ALWAYS = p -> true;
	protected Map<MB, RedirectHolder<MB, D, S, P>> holders = new HashMap<>();
	private RedirectHolderFactory<D, S, P> factory;
	private RedirectRendererFactory<MB, S, P> redirectFactory;
	private AnimationEngine animEngine = new AnimationEngine();
	private ModelPartVec3fSetter<P> posSet, rotSet;
	private ToFloatFunction<P> px, py, pz, rx, ry, rz;
	private Predicate<P> getVis;
	private BoolSetter<P> setVis;

	public void setFactory(RedirectHolderFactory<D, S, P> factory) {
		this.factory = factory;
	}

	public void setRedirectFactory(RedirectRendererFactory<MB, S, P> redirectFactory) {
		this.redirectFactory = redirectFactory;
	}

	public void setModelSetters(ModelPartVec3fSetter<P> posSet, ModelPartVec3fSetter<P> rotSet) {
		this.posSet = posSet;
		this.rotSet = rotSet;
	}

	public void setModelPosGetters(ToFloatFunction<P> x, ToFloatFunction<P> y, ToFloatFunction<P> z) {
		this.px = x;
		this.py = y;
		this.pz = z;
	}

	public void setModelRotGetters(ToFloatFunction<P> x, ToFloatFunction<P> y, ToFloatFunction<P> z) {
		this.rx = x;
		this.ry = y;
		this.rz = z;
	}

	public void setVis(Predicate<P> getVis, BoolSetter<P> setVis) {
		this.getVis = getVis;
		this.setVis = setVis;
	}

	public void bindModel(MB model, String arg, D addDt, ModelDefinition def, Player<?, MB> player, AnimationMode mode) {
		getHolderSafe(model, arg, h -> h.swapIn(def, addDt, player, mode));
	}

	public void bindModel(MB model, D addDt, ModelDefinition def, Player<?, MB> player, AnimationMode mode) {
		bindModel(model, null, addDt, def, player, mode);
	}

	public void unbindModel(MB model) {
		RedirectHolder<MB, D, S, P> h = holders.get(model);
		if(h != null)h.swapOut();
	}

	public void bindSkin(MB model, String arg, S cbi, TextureSheetType tex) {
		getHolderSafe(model, arg, h -> h.bindTexture0(cbi, tex));
	}

	public void bindSkin(MB model, S cbi, TextureSheetType tex) {
		bindSkin(model, null, cbi, tex);
	}

	public boolean isBound(MB model, String arg) {
		return getHolderSafe(model, arg, h -> h.swappedIn, false);
	}

	public boolean isBound(MB model) {
		return isBound(model, null);
	}

	@SuppressWarnings("unchecked")
	public <R> R getHolderSafe(MB model, String arg, Function<RedirectHolder<MB, D, S, P>, R> func, R def) {
		RedirectHolder<MB, D, S, P> h = holders.get(model);
		if(h == null) {
			h = (RedirectHolder<MB, D, S, P>) factory.create(model, arg);
			if(h != null)holders.put(model, h);
		}
		if(h == null)return def;
		else return func.apply(h);
	}

	@SuppressWarnings("unchecked")
	public void getHolderSafe(MB model, String arg, Consumer<RedirectHolder<MB, D, S, P>> func) {
		RedirectHolder<MB, D, S, P> h = holders.get(model);
		if(h == null) {
			h = (RedirectHolder<MB, D, S, P>) factory.create(model, arg);
			if(h != null)holders.put(model, h);
		}
		if(h != null)func.accept(h);
	}

	@SuppressWarnings("unchecked")
	private RedirectHolder<MB, D, S, P> create(MB model) {
		RedirectHolder<MB, D, S, P> r = (RedirectHolder<MB, D, S, P>) factory.create(model, null);
		if(r == null)throw new IllegalArgumentException("Tried to create RedirectHolder for unknown model type: " + model.getClass());
		return r;
	}

	public RedirectHolder<MB, D, S, P> getHolder(MB model) {
		return holders.computeIfAbsent(model, this::create);
	}

	@Override
	public RedirectHolder<?, ?, ?, ?> getHolderFor(Object model) {
		return holders.get(model);
	}

	public void copyModelForArmor(P from, P to) {
		this.posSet.set(to, this.px.apply(from), this.py.apply(from), this.pz.apply(from));
		this.rotSet.set(to, this.rx.apply(from), this.ry.apply(from), this.rz.apply(from));
	}

	public static interface RedirectHolderFactory<D, S, P> {
		<M> RedirectHolder<?, D, S, P> create(M model, String arg);
	}

	public static abstract class RedirectHolder<M, D, S, P> {
		protected final ModelRenderManager<D, S, P, M> mngr;
		public final M model;
		public ModelDefinition def;
		public boolean swappedIn;
		public int sheetX, sheetY;
		public D addDt;
		public List<Field<P>> modelFields;
		public List<RedirectRenderer<P>> redirectRenderers;
		public boolean preRenderSetup, skinBound;
		public Map<RedirectRenderer<P>, RedirectDataHolder<P>> partData;
		public Player<?, M> playerObj;
		public AnimationMode mode;
		public RenderTypes<RenderMode> renderTypes;
		private boolean loggedWarning;

		public RedirectHolder(ModelRenderManager<D, S, P, M> mngr, M model) {
			this.model = model;
			this.mngr = mngr;
			modelFields = new ArrayList<>();
			redirectRenderers = new ArrayList<>();
			partData = new HashMap<>();
			renderTypes = new RenderTypes<>(RenderMode.class);
		}

		public final void swapIn(ModelDefinition def, D addDt, Player<?, M> playerObj, AnimationMode mode) {
			this.def = def;
			this.addDt = addDt;
			this.mode = mode;
			if(swappedIn)return;
			swapIn0();
			for (int i = 0; i < modelFields.size(); i++) {
				Field<P> field = modelFields.get(i);
				RedirectRenderer<P> rd = redirectRenderers.get(i);
				field.set.accept(rd.swapIn());
			}
			this.playerObj = playerObj;
			swappedIn = true;
		}

		public final void swapOut() {
			this.def = null;
			this.addDt = null;
			skinBound = false;
			preRenderSetup = false;
			this.renderTypes.clear();
			this.playerObj = null;
			if(!swappedIn)return;
			swapOut0();
			for (int i = 0; i < modelFields.size(); i++) {
				Field<P> field = modelFields.get(i);
				field.set.accept(redirectRenderers.get(i).swapOut());
			}
			swappedIn = false;
		}

		private void bindTexture0(S cbi, TextureSheetType tex) {
			if(def == null)return;
			TextureProvider skin = def.getTexture(tex, isInGui());
			if(skin != null && skin.texture != null) {
				bindTexture(cbi, skin);
				sheetX = skin.getSize().x;
				sheetY = skin.getSize().y;
			} else {
				Vec2i s = tex.getDefSize();
				sheetX = s.x;
				sheetY = s.y;
			}
			setupRenderSystem(cbi, tex);
		}

		protected void setupRenderSystem(S cbi, TextureSheetType tex) {}
		protected void bindTexture(S cbi, TextureProvider skin) {}
		protected abstract void swapIn0();
		protected abstract void swapOut0();
		protected void bindSkin() {}

		@SuppressWarnings("unchecked")
		protected void bindFirstSetup() {
			for (int i = 0; i < redirectRenderers.size(); i++) {
				RedirectRenderer<P> re = redirectRenderers.get(i);
				VanillaModelPart part = re.getPart();
				if(part == null)continue;
				PartRoot elems = def.getModelElementFor(part);
				if(elems == null)continue;
				P tp = (P) re;
				float px = mngr.px.apply(tp);
				float py = mngr.py.apply(tp);
				float pz = mngr.pz.apply(tp);
				float rx = mngr.rx.apply(tp);
				float ry = mngr.ry.apply(tp);
				float rz = mngr.rz.apply(tp);
				elems.setRootPosAndRot(px, py, pz, rx, ry, rz);
			}
		}

		protected RedirectRenderer<P> register(Field<P> f) {
			RedirectRenderer<P> rd = mngr.redirectFactory.create(model, this, f.get, f.part);
			modelFields.add(f);
			redirectRenderers.add(rd);
			partData.put(rd, new RedirectDataHolder<>());
			return rd;
		}

		protected RedirectRenderer<P> register(Field<P> f, Predicate<Player<?, ?>> doRender) {
			return register(f).setRenderPredicate(doRender);
		}

		public void copyModel(P from, P to) {
			mngr.posSet.set(to, mngr.px.apply(from), mngr.py.apply(from), mngr.pz.apply(from));
			mngr.rotSet.set(to, mngr.rx.apply(from), mngr.ry.apply(from), mngr.rz.apply(from));
			mngr.setVis.set(to, mngr.getVis.test(from));
		}

		protected boolean skipTransform(RedirectRenderer<P> part) {
			return false;
		}

		public void logWarning() {
			if(!loggedWarning) {
				Log.warn("Failed to render element for model " + model.getClass() + " render system isn't initialized correctly, please report this to the mod author");
				loggedWarning = true;
			}
		}

		protected boolean isInGui() {return false;}
	}

	private static class RedirectDataHolder<P> {
		private RedirectRenderer<P> copyFrom;
		private Predicate<Player<?, ?>> renderPredicate = ALWAYS;
	}

	public static interface RedirectRenderer<P> {
		P swapIn();
		P swapOut();
		RedirectHolder<?, ?, ?, P> getHolder();
		P getParent();
		VanillaModelPart getPart();
		void renderParent();
		VBuffers getVBuffers();
		Vec4f getColor();

		default RedirectRenderer<P> setCopyFrom(RedirectRenderer<P> from) {
			getHolder().partData.get(this).copyFrom = from;
			return this;
		}

		default RedirectRenderer<P> setRenderPredicate(Predicate<Player<?, ?>> renderPredicate) {
			getHolder().partData.get(this).renderPredicate = renderPredicate;
			return this;
		}

		default boolean noReset() {
			return false;
		}

		@SuppressWarnings("unchecked")
		default void render() {
			RedirectHolder<?, ?, ?, P> holder = getHolder();
			ModelRenderManager<?, ?, P, ?> mngr = holder.mngr;
			P tp = (P) this;
			P parent = getParent();
			if(holder.mngr.getVis.test(tp)) {
				if(holder.def != null) {
					RedirectDataHolder<P> dh = holder.partData.get(this);
					VanillaModelPart part = getPart();
					if(!holder.preRenderSetup) {
						holder.preRenderSetup = true;
						holder.bindFirstSetup();
					}
					if(!holder.skinBound) {
						holder.skinBound = true;
						holder.bindSkin();
					}
					if(part == null) {
						if(dh.copyFrom != null) {
							holder.copyModel((P) dh.copyFrom, tp);
						}
						return;
					}
					PartRoot elems = holder.def.getModelElementFor(part);
					if(elems != null) {
						if(elems.isEmpty())return;
						boolean skipTransform = holder.mode == AnimationMode.SKULL || holder.mode == AnimationMode.HAND || holder.skipTransform(this);
						float px = mngr.px.apply(tp);
						float py = mngr.py.apply(tp);
						float pz = mngr.pz.apply(tp);
						float rx = mngr.rx.apply(tp);
						float ry = mngr.ry.apply(tp);
						float rz = mngr.rz.apply(tp);
						boolean doRender = holder.playerObj == null || dh.renderPredicate.test(holder.playerObj) || !holder.def.isHideHeadIfSkull();
						elems.forEach(elem -> {
							if(!elem.renderPart())return;
							if(holder.def.isRemoveArmorOffset() && elems.getMainRoot() != elem && part.getCopyFrom() != null) {
								elem.setPosAndRot(holder.def.getModelElementFor(part.getCopyFrom()));
							}
							if(!skipTransform) {
								mngr.posSet.set(tp, elem.getPos());
								mngr.rotSet.set(tp, elem.getRot());
							}
							if(elem.doDisplay() && doRender) {
								holder.copyModel(tp, parent);
								renderParent();
							}
							doRender0(elem, doRender);
						});
						if(!noReset()) {
							mngr.posSet.set(parent, px, py, pz);
							mngr.rotSet.set(parent, rx, ry, rz);
						}
						if(!skipTransform) {
							RootModelElement elem = elems.getMainRoot();
							mngr.posSet.set(tp, elem.getPos());
							mngr.rotSet.set(tp, elem.getRot());
						}
					} else {
						holder.copyModel(tp, parent);
						renderParent();
					}
				} else {
					holder.copyModel(tp, parent);
					renderParent();
				}
			}
		}

		public static void translateRotate(RenderedCube rc, MatrixStack matrixStackIn) {
			translateRotate(rc.pos.x, rc.pos.y, rc.pos.z, rc.rotation.x, rc.rotation.y, rc.rotation.z, matrixStackIn);
		}

		public static void translateRotate(float px, float py, float pz, float rx, float ry, float rz, MatrixStack matrixStackIn) {
			matrixStackIn.translate(px / 16.0F, py / 16.0F, pz / 16.0F);
			if (rz != 0.0F) {
				matrixStackIn.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rz));
			}

			if (ry != 0.0F) {
				matrixStackIn.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(ry));
			}

			if (rx != 0.0F) {
				matrixStackIn.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rx));
			}
		}

		@SuppressWarnings("unchecked")
		public default void translateRotate(MatrixStack matrixStackIn) {
			RedirectHolder<?, ?, ?, P> holder = getHolder();
			ModelRenderManager<?, ?, P, ?> m = holder.mngr;
			P tp = (P) this;
			translateRotate(m.px.apply(tp), m.py.apply(tp), m.pz.apply(tp), m.rx.apply(tp), m.ry.apply(tp), m.rz.apply(tp), matrixStackIn);
		}

		public default void render(RenderedCube elem, MatrixStack matrixStackIn, VBuffers buf, float red, float green, float blue, float alpha) {
			RedirectHolder<?, ?, ?, P> holder = getHolder();
			if(holder.def instanceof EditorDefinition && buf != null) {
				((EditorDefinition)holder.def).render(matrixStackIn, buf, holder.renderTypes, elem);
			}
			if(elem.children == null)return;
			for(RenderedCube cube : elem.children) {
				if(!cube.display) {
					continue;
				}
				matrixStackIn.push();
				translateRotate(cube, matrixStackIn);
				if(cube.itemRenderer != null) {
					Cube c = cube.getCube();
					if(holder.def instanceof EditorDefinition && buf != null) {
						((EditorDefinition)holder.def).render(matrixStackIn, buf, holder.renderTypes, cube);
					}
					matrixStackIn.translate(c.offset.x / 16f, c.offset.y / 16f, c.offset.z / 16f);
					matrixStackIn.scale(c.scale.x, c.scale.y, c.scale.z);
					if(cube.itemRenderer.slot == ItemSlot.ANY_SLOT) {
						//TODO render item here
					} else {
						holder.def.storeTransform(cube.itemRenderer.slot, matrixStackIn);
					}
					matrixStackIn.pop();
					continue;
				}
				if(buf != null) {
					float r = red;
					float g = green;
					float b = blue;
					if(cube.color != 0xffffff) {
						r *= ((cube.color & 0xff0000) >> 16) / 255f;
						g *= ((cube.color & 0x00ff00) >> 8 ) / 255f;
						b *= ( cube.color & 0x0000ff       ) / 255f;
					}
					if(cube.useDynamic || cube.renderObject == null) {
						if(cube.renderObject != null)cube.renderObject.free();
						cube.renderObject = createBox(cube, holder);
					}
					Mesh mesh = cube.renderObject;
					VertexBuffer buffer = buf.getBuffer(holder.renderTypes, mesh.getLayer());
					if(holder.def.isEditor()) {
						ElementSelectMode sel = cube.getSelected();
						if(!sel.applyColor()) {
							r = 1;
							g = 1;
							b = 1;
						}else if(cube.glow) {
							buffer = buf.getBuffer(holder.renderTypes, RenderMode.GLOW);
						}
					} else if(cube.glow) {
						buffer = buf.getBuffer(holder.renderTypes, RenderMode.GLOW);
					}
					mesh.draw(matrixStackIn, buffer, r, g, b, alpha);
				}
				render(cube, matrixStackIn, buf, red, green, blue, alpha);
				matrixStackIn.pop();
			}
		}

		public default void doRender0(RootModelElement elem, boolean doRender) {
			MatrixStack stack = new MatrixStack();
			translateRotate(stack);
			if(doRender) {
				Vec4f color = getColor();
				VBuffers buf = getVBuffers().replay();
				render(elem, stack, buf, color.x, color.y, color.z, color.w);
				buf.finishAll();
			} else {
				render(elem, stack, null, 1, 1, 1, 1);
			}
		}

		public default MatrixStack.Entry getPartTransform() {
			RedirectHolder<?, ?, ?, P> holder = getHolder();
			VanillaModelPart part = getPart();
			if(part == PlayerModelParts.LEFT_ARM) {
				return holder.def.getTransform(ItemSlot.LEFT_HAND);
			} else if(part == PlayerModelParts.RIGHT_ARM) {
				return holder.def.getTransform(ItemSlot.RIGHT_HAND);
			} else if(part == PlayerModelParts.HEAD) {
				return holder.def.getTransform(ItemSlot.HEAD);
			}
			return null;
		}
	}

	private static Mesh createBox(RenderedCube elem, RedirectHolder<?, ?, ?, ?> holder) {
		Cube c = elem.getCube();
		if(c.texSize == 0) {
			return BoxRender.createColored(
					c.offset.x, c.offset.y, c.offset.z,
					c.size.x * c.scale.x, c.size.y * c.scale.y, c.size.z * c.scale.z,
					c.mcScale, holder.sheetX, holder.sheetY
					);
		} else {
			if(elem.singleTex)
				return BoxRender.createTexturedSingle(
						c.offset, c.size, c.scale,
						c.mcScale,
						c.u, c.v, c.texSize, holder.sheetX, holder.sheetY
						);
			else if(elem.faceUVs != null)
				return BoxRender.createTextured(
						c.offset, c.size, c.scale,
						c.mcScale,
						elem.faceUVs, c.texSize, holder.sheetX, holder.sheetY
						);
			else
				return BoxRender.createTextured(
						c.offset, c.size, c.scale,
						c.mcScale,
						c.u, c.v, c.texSize, holder.sheetX, holder.sheetY
						);
		}
	}

	public static class Field<V> {
		private Supplier<V> get;
		private Consumer<V> set;
		private VanillaModelPart part;
		public Field(Supplier<V> get, Consumer<V> set, VanillaModelPart part) {
			this.part = part;
			this.get = get;
			this.set = set;
		}
	}

	@FunctionalInterface
	public static interface RedirectRendererFactory<M, S, P> {
		RedirectRenderer<P> create(M model, RedirectHolder<M, ?, S, P> access, Supplier<P> modelPart, VanillaModelPart part);
	}

	@FunctionalInterface
	public static interface ModelPartVec3fSetter<P> {
		void set(P model, float x, float y , float z);

		default void set(P p, Vec3f v) {
			set(p, v.x, v.y, v.z);
		}
	}

	@FunctionalInterface
	public static interface BoolSetter<P> {
		void set(P p, boolean v);
	}

	@Override
	public AnimationEngine getAnimationEngine() {
		return animEngine;
	}
}
