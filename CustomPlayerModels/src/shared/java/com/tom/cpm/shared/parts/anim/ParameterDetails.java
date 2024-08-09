package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tom.cpm.shared.io.IOHelper;

public class ParameterDetails {
	public static final ParameterDetails DEFAULT = new ParameterDetails(2, 0);
	private byte[] syncDefault, localDefault;

	private ParameterDetails() {
	}

	public ParameterDetails(int syncSize, int localSize) {
		this.syncDefault = new byte[syncSize];
		this.localDefault = new byte[localSize];
	}

	public ParameterDetails(byte[] syncDefault, byte[] localDefault) {
		this.syncDefault = syncDefault;
		this.localDefault = localDefault;
	}

	public static void parse(IOHelper block, AnimLoaderState state) throws IOException {
		ParameterDetails params = new ParameterDetails();
		params.syncDefault = block.readByteArray();
		params.localDefault = block.readByteArray();
		state.setParameters(params);
	}

	public void write(IOHelper block) throws IOException {
		block.writeByteArray(syncDefault);
		block.writeByteArray(localDefault);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(localDefault);
		result = prime * result + Arrays.hashCode(syncDefault);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ParameterDetails other = (ParameterDetails) obj;
		if (!Arrays.equals(localDefault, other.localDefault)) return false;
		if (!Arrays.equals(syncDefault, other.syncDefault)) return false;
		return true;
	}

	public static class ParameterAllocator {
		private List<Integer> syncBytes = new ArrayList<>();

		private int poseCounter = 1;
		private int gestureCounter = 1;

		{
			syncBytes.add(0);
			syncBytes.add(0);
		}

		private int bitCount;
		private int bitMask;
		private int bitParam;

		public ParameterDetails finish() {
			if (bitParam != 0)syncBytes.set(bitParam, bitMask);
			byte[] dt = new byte[syncBytes.size()];
			for (int i = 0; i < dt.length; i++) {
				dt[i] = syncBytes.get(i).byteValue();
			}
			return new ParameterDetails(dt, new byte[0]);//TODO local parameters later
		}

		public int allocByteSync(String id, byte defaultValue) {//TODO store id
			int i = syncBytes.size();
			syncBytes.add(Byte.toUnsignedInt(defaultValue));
			return i;
		}

		public BitInfo allocBitSync(String id, boolean defaultValue) {
			if (bitCount == 0) {
				if (bitParam != 0)syncBytes.set(bitParam, bitMask);
				bitParam = allocByteSync("", (byte) 0);
				bitMask = 0;
				bitCount = 8;
			}
			bitCount--;
			bitMask |= (defaultValue ? 1 : 0) << bitCount;
			int valMask = 1 << bitCount;
			return new BitInfo(bitParam, valMask);
		}

		public static class BitInfo {
			public final int param;
			public final int mask;

			public BitInfo(int param, int mask) {
				this.param = param;
				this.mask = mask;
			}
		}

		public int newPose(String id) {
			return poseCounter++;
		}

		public int newGesture(String id) {
			return gestureCounter++;
		}
	}

	@Override
	public String toString() {
		return "Sync Parameter Count: " + syncDefault.length + "\n\t" + Arrays.toString(syncDefault);
	}

	public byte[] createSyncParams() {
		return Arrays.copyOf(syncDefault, syncDefault.length);
	}

	public byte getDefaultParam(int parameter) {
		return syncDefault[parameter];
	}
}
