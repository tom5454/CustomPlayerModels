package com.tom.cpm.web.client.java;

import java.util.Random;

public class UUID implements Comparable<UUID> {
	private final long mostSigBits;
	private final long leastSigBits;

	public UUID(long mostSigBits, long leastSigBits) {
		this.mostSigBits = mostSigBits;
		this.leastSigBits = leastSigBits;
	}

	private UUID(byte[] data) {
		long msb = 0;
		long lsb = 0;
		assert data.length == 16 : "data must be 16 bytes in length";
		for (int i=0; i<8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i=8; i<16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		this.mostSigBits = msb;
		this.leastSigBits = lsb;
	}

	@Override
	public String toString() {
		return (digits(mostSigBits >> 32, 8) + "-" +
				digits(mostSigBits >> 16, 4) + "-" +
				digits(mostSigBits, 4) + "-" +
				digits(leastSigBits >> 48, 4) + "-" +
				digits(leastSigBits, 12));
	}

	public static UUID fromString(String name) {
		String[] components = name.split("-");
		if (components.length != 5)
			throw new IllegalArgumentException("Invalid UUID string: "+name);
		for (int i=0; i<5; i++)
			components[i] = "0x"+components[i];

		long mostSigBits = Long.decode(components[0]).longValue();
		mostSigBits <<= 16;
		mostSigBits |= Long.decode(components[1]).longValue();
		mostSigBits <<= 16;
		mostSigBits |= Long.decode(components[2]).longValue();

		long leastSigBits = Long.decode(components[3]).longValue();
		leastSigBits <<= 48;
		leastSigBits |= Long.decode(components[4]).longValue();

		return new UUID(mostSigBits, leastSigBits);
	}

	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

	@Override
	public int hashCode() {
		long hilo = mostSigBits ^ leastSigBits;
		return ((int)(hilo >> 32)) ^ (int) hilo;
	}

	@Override
	public boolean equals(Object obj) {
		if ((null == obj) || (obj.getClass() != UUID.class))
			return false;
		UUID id = (UUID)obj;
		return (mostSigBits == id.mostSigBits &&
				leastSigBits == id.leastSigBits);
	}

	@Override
	public int compareTo(UUID val) {
		// The ordering is intentionally set up so that the UUIDs
		// can simply be numerically compared as two numbers
		return (this.mostSigBits < val.mostSigBits ? -1 :
			(this.mostSigBits > val.mostSigBits ? 1 :
				(this.leastSigBits < val.leastSigBits ? -1 :
					(this.leastSigBits > val.leastSigBits ? 1 :
						0))));
	}

	public long getMostSignificantBits() {
		return mostSigBits;
	}

	public long getLeastSignificantBits() {
		return leastSigBits;
	}

	public static UUID randomUUID() {
		Random ng = new Random();

		byte[] randomBytes = new byte[16];
		ng.nextBytes(randomBytes);
		randomBytes[6]  &= 0x0f;  /* clear version        */
		randomBytes[6]  |= 0x40;  /* set to version 4     */
		randomBytes[8]  &= 0x3f;  /* clear variant        */
		randomBytes[8]  |= 0x80;  /* set to IETF variant  */
		return new UUID(randomBytes);
	}
}
