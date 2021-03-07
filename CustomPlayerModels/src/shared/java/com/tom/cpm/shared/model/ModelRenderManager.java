package com.tom.cpm.shared.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.tom.cpm.shared.IPlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.math.Vec3f;

public abstract class ModelRenderManager<D, S, P, MB> implements IPlayerRenderManager {
	private Map<MB, RedirectHolder<MB, D, S, P>> holders = new HashMap<>();
	private RedirectHolderFactory<D, S, P> factory;
	private RedirectRendererFactory<MB, S, P> redirectFactory;
	private final ModelDefinitionLoader loader;
	private AnimationEngine animEngine = new AnimationEngine();
	private ModelPartVec3fSetter<P> posSet, rotSet;
	private ToFloatFunc<P> px, py, pz, rx, ry, rz;
	private Predicate<P> getVis;
	private BoolSetter<P> setVis;

	public ModelRenderManager(ModelDefinitionLoader loader) {
		this.loader = loader;
	}

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

	public void setModelPosGetters(ToFloatFunc<P> x, ToFloatFunc<P> y, ToFloatFunc<P> z) {
		this.px = x;
		this.py = y;
		this.pz = z;
	}

	public void setModelRotGetters(ToFloatFunc<P> x, ToFloatFunc<P> y, ToFloatFunc<P> z) {
		this.rx = x;
		this.ry = y;
		this.rz = z;
	}

	public void setVis(Predicate<P> getVis, BoolSetter<P> setVis) {
		this.getVis = getVis;
		this.setVis = setVis;
	}

	public void bindModel(MB model, D addDt, ModelDefinition def, Predicate<Object> unbindRule, Player player) {
		holders.computeIfAbsent(model, this::create).swapIn(def, unbindRule, addDt, player);
	}

	private RedirectHolder<MB, D, S, P> create(MB model) {
		RedirectHolder<MB, D, S, P> r = factory.create(model);
		if(r == null)throw new IllegalArgumentException("Tried to create RedirectHolder for unknown model type: " + model.getClass());
		return r;
	}

	public void unbindModel(MB model) {
		RedirectHolder<MB, D, S, P> h = holders.get(model);
		if(h != null)h.swapOut();
	}

	public void bindSkin(MB model, S cbi) {
		holders.computeIfAbsent(model, this::create).bindTexture(cbi);
	}

	public boolean isBound(MB model) {
		return holders.computeIfAbsent(model, this::create).swappedIn;
	}

	public RedirectHolder<MB, D, S, P> getHolder(MB model) {
		return holders.computeIfAbsent(model, this::create);
	}

	@FunctionalInterface
	public static interface RedirectHolderFactory<D, S, P> {
		<M> RedirectHolder<M, D, S, P> create(M model);
	}

	public static abstract class RedirectHolder<M, D, S, P> {
		protected final ModelRenderManager<D, S, P, M> mngr;
		public final M model;
		public ModelDefinition def;
		public Predicate<Object> unbindRule;
		public boolean swappedIn;
		public int sheetX, sheetY;
		public D addDt;
		public List<Field<P>> modelFields;
		public List<RedirectRenderer<P>> redirectRenderers;
		public boolean skinBound;
		public Map<RedirectRenderer<P>, RedirectRenderer<P>> copyMap;
		public Map<RedirectRenderer<P>, Predicate<Player>> testMap;
		public Player playerObj;

		public RedirectHolder(ModelRenderManager<D, S, P, M> mngr, M model) {
			this.model = model;
			this.mngr = mngr;
			modelFields = new ArrayList<>();
			redirectRenderers = new ArrayList<>();
			copyMap = new HashMap<>();
			testMap = new HashMap<>();
		}

		public final void swapIn(ModelDefinition def, Predicate<Object> unbindRule, D addDt, Player playerObj) {
			this.def = def;
			this.unbindRule = unbindRule;
			this.addDt = addDt;
			if(swappedIn)return;
			swapIn0();
			for (int i = 0; i < modelFields.size(); i++) {
				Field<P> field = modelFields.get(i);
				field.set.accept(redirectRenderers.get(i).swapIn());
			}
			this.playerObj = playerObj;
			swappedIn = true;
		}

		public final void swapOut() {
			this.def = null;
			this.unbindRule = null;
			this.addDt = null;
			skinBound = false;
			this.playerObj = null;
			if(!swappedIn)return;
			swapOut0();
			for (int i = 0; i < modelFields.size(); i++) {
				Field<P> field = modelFields.get(i);
				field.set.accept(redirectRenderers.get(i).swapOut());
			}
			swappedIn = false;
		}

		protected abstract void bindTexture(S cbi);
		protected abstract void swapIn0();
		protected abstract void swapOut0();
		protected void bindSkin() {}

		protected RedirectRenderer<P> register(Field<P> f) {
			return register(f, p -> true);
		}

		protected RedirectRenderer<P> register(Field<P> f, Predicate<Player> doRender) {
			RedirectRenderer<P> rd = mngr.redirectFactory.create(model, this, f.get, f.part);
			modelFields.add(f);
			redirectRenderers.add(rd);
			testMap.put(rd, doRender);
			return rd;
		}

		public void copyModel(P from, P to) {
			mngr.posSet.set(to, mngr.px.apply(from), mngr.py.apply(from), mngr.pz.apply(from));
			mngr.rotSet.set(to, mngr.rx.apply(from), mngr.ry.apply(from), mngr.rz.apply(from));
			mngr.setVis.set(to, mngr.getVis.test(from));
		}

		protected boolean skipTransform(RedirectRenderer<P> part) {
			return false;
		}
	}

	public static interface RedirectRenderer<P> {
		P swapIn();
		P swapOut();
		RedirectHolder<?, ?, ?, P> getHolder();
		P getParent();
		ModelPart getPart();
		void renderParent();
		void renderWithParent(RootModelElement elem);
		void doRender(RootModelElement elem);

		default RedirectRenderer<P> setCopyFrom(RedirectRenderer<P> from) {
			getHolder().copyMap.put(this, from);
			return this;
		}

		@SuppressWarnings("unchecked")
		default void render() {
			RedirectHolder<?, ?, ?, P> holder = getHolder();
			ModelRenderManager<?, ?, P, ?> mngr = holder.mngr;
			P tp = (P) this;
			if(holder.mngr.getVis.test(tp)) {
				P parent = getParent();
				if(holder.def != null) {
					ModelPart part = getPart();
					if(!holder.skinBound) {
						holder.skinBound = true;
						holder.bindSkin();
					}
					if(part == null) {
						RedirectRenderer<P> copyFrom = holder.copyMap.get(this);
						if(copyFrom != null) {
							holder.copyModel((P) copyFrom, tp);
						}
						if(holder.unbindRule != null && holder.unbindRule.test(this))
							holder.swapOut();
						return;
					}
					RootModelElement elem = holder.def.getModelElementFor(part);
					float px = mngr.px.apply(tp);
					float py = mngr.py.apply(tp);
					float pz = mngr.pz.apply(tp);
					float rx = mngr.rx.apply(tp);
					float ry = mngr.ry.apply(tp);
					float rz = mngr.rz.apply(tp);
					boolean skipTransform = holder.unbindRule != null || holder.skipTransform(this);
					if(!skipTransform) {
						if(elem.forcePos) {
							mngr.posSet.set(tp, elem.pos);
							mngr.rotSet.set(tp, elem.rotation);
						} else {
							mngr.posSet.set(tp, px + elem.pos.x, py + elem.pos.y, pz + elem.pos.z);
							mngr.rotSet.set(tp, rx + elem.rotation.x, ry + elem.rotation.y, rz + elem.rotation.z);
						}
					}
					if(holder.playerObj == null || holder.testMap.getOrDefault(this, p -> true).test(holder.playerObj)) {
						if(elem.doDisplay()) {
							holder.copyModel(tp, parent);
							renderWithParent(elem);
							mngr.posSet.set(parent, px, py, pz);
							mngr.rotSet.set(parent, rx, ry, rz);
						} else {
							doRender(elem);
						}
					}
				} else {
					holder.copyModel(tp, parent);
					renderParent();
				}
			}
			if(holder.unbindRule != null && holder.unbindRule.test(this))
				holder.swapOut();
		}
	}

	public static interface ModelPart {
		default int getId(RenderedCube id) {
			return id.getCube().id;
		}
		String getName();
	}

	public static class Field<V> {
		private Supplier<V> get;
		private Consumer<V> set;
		private ModelPart part;
		public Field(Supplier<V> get, Consumer<V> set, ModelPart part) {
			this.part = part;
			this.get = get;
			this.set = set;
		}
	}

	@FunctionalInterface
	public static interface RedirectRendererFactory<M, S, P> {
		RedirectRenderer<P> create(M model, RedirectHolder<M, ?, S, P> access, Supplier<P> modelPart, ModelPart part);
	}

	@FunctionalInterface
	public static interface ModelPartVec3fSetter<P> {
		void set(P model, float x, float y , float z);

		default void set(P p, Vec3f v) {
			set(p, v.x, v.y, v.z);
		}
	}

	@FunctionalInterface
	public static interface ToFloatFunc<P> {
		float apply(P v);
	}

	@FunctionalInterface
	public static interface BoolSetter<P> {
		void set(P p, boolean v);
	}

	@Override
	public ModelDefinitionLoader getLoader() {
		return loader;
	}

	@Override
	public AnimationEngine getAnimationEngine() {
		return animEngine;
	}
}
