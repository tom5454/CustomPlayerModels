package com.tom.cpm.web.client.fbxtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.web.client.fbxtool.FBXCreator.FBXMaterial;
import com.tom.cpm.web.client.fbxtool.three.Bone;
import com.tom.cpm.web.client.fbxtool.three.BufferAttribute;
import com.tom.cpm.web.client.fbxtool.three.BufferGeometry;
import com.tom.cpm.web.client.fbxtool.three.Float32BufferAttribute;
import com.tom.cpm.web.client.fbxtool.three.MeshBasicMaterial;
import com.tom.cpm.web.client.fbxtool.three.Object3D;
import com.tom.cpm.web.client.fbxtool.three.Skeleton;
import com.tom.cpm.web.client.fbxtool.three.SkinnedMesh;
import com.tom.cpm.web.client.fbxtool.three.Uint16BufferAttribute;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.Uint32Array;

public class GeometryBuffer {
	private List<SkinnedMesh> geos = new ArrayList<>();
	private Map<FBXMaterial, GeometryBufferBuilder> buffers = new HashMap<>();
	private JsArrayE<Bone> bones = new JsArrayE<>();
	private Stack<BoneInfo> boneStack = new Stack<>();
	private int nextInd;
	private int index;

	public GeometryBuffer() {
		Bone b = new Bone();
		boneStack.push(new BoneInfo(0, b, new Vec3f()));
		bones.push(b);
	}

	private static class GeometryBufferBuilder extends DirectBuffer<GeometryBuffer> {
		private final MeshBasicMaterial mat;
		private JsArrayE<Double> pos = new JsArrayE<>();
		private JsArrayE<Double> color = new JsArrayE<>();
		private JsArrayE<Double> uv = new JsArrayE<>();
		private JsArrayE<Double> normal = new JsArrayE<>();
		private JsArrayE<Double> skinIndices = new JsArrayE<>();
		private JsArrayE<Double> skinWeights = new JsArrayE<>();

		public GeometryBufferBuilder(GeometryBuffer buffer, MeshBasicMaterial mat) {
			super(buffer);
			this.mat = mat;
		}

		@Override
		protected void pushVertex(float x, float y, float z, float red, float green, float blue, float alpha, float u,
				float v, float nx, float ny, float nz) {
			pos.push(new Double[] {Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)});
			color.push(new Double[] {Double.valueOf(red), Double.valueOf(green), Double.valueOf(blue), Double.valueOf(alpha)});
			uv.push(new Double[] {Double.valueOf(u), Double.valueOf(v)});
			normal.push(new Double[] {Double.valueOf(nx), Double.valueOf(ny), Double.valueOf(nz)});
			skinIndices.push(new Double[] {(double) buffer.index, 0D, 0D, 0D});
			skinWeights.push(new Double[] {1D, 0D, 0D, 0D});
		}

		@Override
		public void finish() {
			if(pos.length < 1)return;
			BufferGeometry geometry = new BufferGeometry();

			geometry.setAttribute("position", new Float32BufferAttribute(pos, 3)); // Position (x, y, z)
			geometry.setAttribute("color", new Float32BufferAttribute(color, 4)); // Color (r, g, b, a)
			geometry.setAttribute("uv", new Float32BufferAttribute(uv, 2)); // Texture UV (u, v)
			geometry.setAttribute("normal", new Float32BufferAttribute(normal, 3)); // Normals (nx, ny, nz)
			int numQuads = pos.length / 12;
			Uint32Array indices = new Uint32Array(numQuads * 6);

			for (int i = 0, j = 0; i < numQuads; i++, j += 6) {
				int offset = i * 4;

				//0, 2, 3, 0, 2, 1
				indices.setAt(j    , (double) (offset    ));
				indices.setAt(j + 1, (double) (offset + 2));
				indices.setAt(j + 2, (double) (offset + 3));
				indices.setAt(j + 3, (double) (offset    ));
				indices.setAt(j + 4, (double) (offset + 1));
				indices.setAt(j + 5, (double) (offset + 2));
			}
			geometry.setIndex(new BufferAttribute(indices, 1));
			geometry.setAttribute("skinIndex", new Uint16BufferAttribute(skinIndices, 4));
			geometry.setAttribute("skinWeight", new Float32BufferAttribute(skinWeights, 4));

			buffer.geos.add(new SkinnedMesh(geometry, mat));

			pos = new JsArrayE<>();
			color = new JsArrayE<>();
			uv = new JsArrayE<>();
			normal = new JsArrayE<>();
			skinIndices = new JsArrayE<>();
			skinWeights = new JsArrayE<>();
		}
	}

	private static class BoneInfo {
		private int index;
		private Bone bone;
		private Vec3f abs = new Vec3f();

		public BoneInfo(int index, Bone bone, Vec3f abs) {
			this.index = index;
			this.bone = bone;
			this.abs = abs;
		}
	}

	public void push() {
		nextInd++;
		Bone b = new Bone();
		boneStack.peek().bone.add(b);
		boneStack.push(new BoneInfo(nextInd, b, new Vec3f(boneStack.peek().abs)));
		bones.push(b);
		index = nextInd;
	}

	public void pop() {
		boneStack.pop();
		index = boneStack.peek().index;
	}

	public VertexBuffer getBuffer(FBXMaterial mode) {
		return buffers.computeIfAbsent(mode, __ -> new GeometryBufferBuilder(this, mode.mat));
	}

	public Skeleton bake(Consumer<Object3D> scene) {
		buffers.values().forEach(VertexBuffer::finish);
		Skeleton sk = new Skeleton(bones.array());
		geos.forEach(mesh -> {
			mesh.add(sk.bones[0]);
			mesh.bind(sk);
			scene.accept(mesh);
		});
		bones.forEach(b -> {
			if(b.hidden)b.scale.set(0);
		});
		sk.bones[0].scale.set(-1, -1, 1);
		return sk;
	}

	public Bone getBone() {
		return boneStack.peek().bone;
	}

	public void setBoneAbsolutePos(float x, float y, float z) {
		BoneInfo b = boneStack.peek();
		Vec3f v = new Vec3f(x, y, z);
		b.bone.position.set(v.sub(b.abs));
		b.abs = v;
	}

	public void offsetAbsolute(float x, float y, float z) {
		BoneInfo b = boneStack.peek();
		b.abs = b.abs.add(new Vec3f(x, y, z));
	}
}
