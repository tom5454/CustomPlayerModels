/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * This represents an OSC <i>Time-tag</i>.
 * It uses the NTP (Network Time Protocol) time-tag format,
 * and supports a different time range (smaller) and resolution (more precise)
 * then {@link Date}.
 * See <a href="http://opensoundcontrol.org/node/3/#timetags">
 * the OSC specification for Time-tags</a>
 * and <a href="https://en.wikipedia.org/wiki/Network_Time_Protocol#Timestamps">
 * the NTP time-stamp documentation on Wikipedia</a> for specification details.
 * OSC <i>Time-tag</i> aswell as <code>Date</code> are time-zone agnostic,
 * though <code>Date</code> might use the default {@link java.util.Locale}
 * in some cases, like when formatting as a <code>String</code>.
 * The epoch 0 starts in 1900 and ends in 2036.
 * Dates before or after the end of this first epoch are represented with
 * a rolled over (the same) value range again, which means one needs to
 * define the epoch to convert to,
 * when converting from the short epoch OSC Time-tag to a bigger range one like
 * Java <code>Date</code>.
 * We do this with the
 * {@link com.tom.cpmoscc.external.com.illposed.osc.argument.handler.DateTimeStampArgumentHandler#PROP_NAME_EPOCH_INDICATOR_TIME}
 * property.
 * By default, it uses the epoch we are currently in.
 * TODO Introduce 128bit version of this class (64bit seconds, 64bit fraction)
 *   as described by NTPv4.
 */
public class OSCTimeTag64 implements Cloneable, Serializable, Comparable<OSCTimeTag64> {

	// Public API
	/**
	 * OSC epoch length in milliseconds.
	 * An epoch is the maximum range of a 64bit OSC Time-tag,
	 * which is (uint32_max + 1) * 1000 milliseconds.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final long EPOCH_LENGTH_JAVA_TIME = 0x100000000L * 1000L;
	/**
	 * Start of the first epoch expressed in Java time
	 * (as used by {@link Date#Date(long)}
	 * and {@link Date#getTime()}).
	 * This is "1-Jan-1900 @ 00:00:00", and we use UTC as time-zone.
	 * Dates before this can not be represented with an OSC Time-tag.
	 */
	public static final long EPOCH_START_JAVA_TIME_0 = -2208992400000L;
	// Public API
	/**
	 * Start of the current epoch expressed in Java time.
	 */
	@SuppressWarnings("WeakerAccess")
	public static final long EPOCH_START_JAVA_TIME_CURRENT
			= findEpochStartJavaTime(new Date().getTime());
	// Public API
	/**
	 * The OSC time-tag with the semantics of "immediately"/"now".
	 */
	@SuppressWarnings("WeakerAccess")
	public static final long IMMEDIATE_RAW = 0x1L;
	/**
	 * The OSC time-tag with the semantics of "immediately"/"now".
	 * @see #immediateDate()
	 */
	public static final OSCTimeTag64 IMMEDIATE = valueOf(IMMEDIATE_RAW);
	/**
	 * Filter for the 32 lower/least-significant bits of a long.
	 */
	private static final long FILTER_LOWER_32 = 0xFFFFFFFFL;
	/**
	 * Number of bits for storing the "seconds" value in NTP time.
	 */
	private static final int NTP_SECONDS_BITS = 32;
	/**
	 * The Factor to multiply `fractions` with, to get to the number of nano-seconds.
	 * It is calculated as <code>10^9 / 2^32</code>.
	 */
	private static final double FRACTION_TO_NANOS_MULTIPLIER = 0.23283;
	private static final long serialVersionUID = 1L;

	/**
	 * The Time-tags value as specified by the OSC protocol standard.
	 * It is treated as a 64bit unsigned integer,
	 * with the higher-order 32bit representing the whole seconds
	 * from the beginning of the epoch,
	 * and the lower-order 32bit representing the fractions of a second
	 * from the beginning of the last full second.
	 */
	private final long ntpTime;

	protected OSCTimeTag64(final long ntpTime) {
		this.ntpTime = ntpTime;
	}

	/**
	 * Returns the OSC/NTP conform <i>Time-tag</i> value.
	 * @return 64bits:
	 *   32 higher bits (including MSB): seconds,
	 *   32 lower bits: fraction (of a second)
	 */
	public long getNtpTime() {
		return ntpTime;
	}

	// Public API
	/**
	 * Returns the number of whole seconds from the start of the epoch
	 * of this time-tag.
	 * @return high-order 32-bit unsigned value representing the "seconds" part
	 *   of this OSC <i>Time-tag</i>
	 */
	@SuppressWarnings("WeakerAccess")
	public long getSeconds() {
		return ntpTime >>> NTP_SECONDS_BITS;
	}

	// Public API
	/**
	 * Returns the fraction of a second from the start of the epoch + seconds
	 * of this time-tag.
	 * Denotes a number of seconds * (1 / 2^32),
	 * which allows to specify to a precision of about 233 pico seconds.
	 * @return lower-order 32-bit unsigned value representing the "fraction"
	 *   part of this OSC <i>Time-tag</i>
	 */
	@SuppressWarnings("WeakerAccess")
	public long getFraction() {
		return ntpTime & FILTER_LOWER_32;
	}

	// Public API
	/**
	 * Returns the rounded number of nanoseconds from the start of the epoch + seconds
	 * of this time-tag.
	 * @return 32-bit unsigned value representing the nano-seconds after the last full second
	 *   of this OSC <i>Time-tag</i>
	 */
	@SuppressWarnings("WeakerAccess")
	public int getNanos() {
		return (int) Math.round(getFraction() * FRACTION_TO_NANOS_MULTIPLIER);
	}

	public boolean isImmediate() {
		return ntpTime == IMMEDIATE_RAW;
	}

	@Override
	public boolean equals(final Object other) {

		return (other instanceof OSCTimeTag64)
				&& (getNtpTime() == ((OSCTimeTag64) other).getNtpTime());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = (89 * hash) + (int) (this.ntpTime ^ (this.ntpTime >>> 32));
		return hash;
	}

	@Override
	public int compareTo(final OSCTimeTag64 other) {

		return (10 * Long.signum(Long.compare(getSeconds(), other.getSeconds())))
				+ Long.signum(Long.compare(getFraction(), other.getFraction()));
	}

	@Override
	public OSCTimeTag64 clone() throws CloneNotSupportedException {
		return (OSCTimeTag64) super.clone();
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * @param epochIndicatorTime the resulting date will be
	 *   in the same OSC epoch as this Java date-stamp
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	public Date toDate(final long epochIndicatorTime) {
		return new Date(toJavaTime(epochIndicatorTime));
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * @param epochIndicator the resulting date will be
	 *   in the same OSC epoch as this
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	public Date toDate(final Date epochIndicator) {
		return (epochIndicator == null)
				? toDate()
				: toDate(epochIndicator.getTime());
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * The resulting date will be in the current OSC epoch.
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	private Date toDate() {
		return new Date(toJavaTimeInEpoch(EPOCH_START_JAVA_TIME_CURRENT));
	}

	private static long findEpochStartJavaTime(final long javaTime) {

		final long epochIndex = (javaTime - EPOCH_START_JAVA_TIME_0) / EPOCH_LENGTH_JAVA_TIME;
		return EPOCH_START_JAVA_TIME_0 + (epochIndex * EPOCH_LENGTH_JAVA_TIME);
	}

	private long toJavaTimeInEpoch(final long epochStart) {

		final long secondsInMs = getSeconds() * 1000L;
		// use round-off on the fractional part to preserve going to lower precision
		final long fractionInMs = Math.round((1000D * getFraction()) / 0x100000000L);

		return epochStart + (secondsInMs | fractionInMs);
	}

	private long toJavaTime(final Long epochIndicatorTime) {

		final long epochStart;
		if (epochIndicatorTime == null) {
			epochStart = EPOCH_START_JAVA_TIME_CURRENT;
		} else {
			epochStart = findEpochStartJavaTime(epochIndicatorTime);
		}

		return toJavaTimeInEpoch(epochStart);
	}

	private static long toNtpTimeTag(final long seconds, final long fraction) {
		return (seconds << NTP_SECONDS_BITS) | fraction;
	}

	/**
	 * Returns the Java instant closest to this time-tags value.
	 * {@link Instant} covers the range of the OSC time tag,
	 * and nearly covers the precision
	 * (1 nanosecond accuracy instead of 233 picoseconds).
	 * @see #valueOf(Instant)
	 * @return this time-tags value rounded to the closest full nanosecond
	 */
	public Instant toInstant() {
		return Instant.ofEpochSecond(getSeconds(), getNanos());
	}

	/**
	 * Returns the Java instant closest to this time-tags value.
	 *  {@link Instant} covers the range of the OSC time tag,
	 *  and nearly covers the precision
	 *  (1 nanosecond accuracy instead of 233 picoseconds).
	 * @param instant to be converted to an OSC time tag
	 * @see #toInstant()
	 * @return the closest
	 */
	public static OSCTimeTag64 valueOf(final Instant instant) {
		return valueOf(instant.getEpochSecond(), Math.round(instant.getNano() / FRACTION_TO_NANOS_MULTIPLIER));
	}

	public static OSCTimeTag64 valueOf(final long ntpTime) {
		return new OSCTimeTag64(ntpTime);
	}

	public static OSCTimeTag64 valueOf(final long seconds, final long fraction) {
		return new OSCTimeTag64(toNtpTimeTag(seconds, fraction));
	}

	public static OSCTimeTag64 valueOf(final Date javaTime) {
		return new OSCTimeTag64(javaToNtpTimeStamp(javaTime.getTime()));
	}

	@Override
	public String toString() {
		return toDate().toString();
	}

	/**
	 * Converts a Java time-stamp (milliseconds since 1970)
	 * to a 64-bit OSC time representation.
	 * This code was copied from the "Apache Jakarta Commons - Net" library,
	 * which is licensed under the
	 * <a href="http://www.apache.org/licenses/LICENSE-2.0.html">
	 * Apache-2.0</a>.
	 * The original source file can be found
	 * <a href="http://svn.apache.org/viewvc/commons/proper/net/trunk/src/main/java/org/apache/commons/net/ntp/TimeStamp.java?view=co">
	 * here</a>.
	 * @param javaTime Java time-stamp, as returned by {@link Date#getTime()}
	 * @return OSC time-tag representation of the Java time value.
	 */
	private static long javaToNtpTimeStamp(final long javaTime) {

		final long epochStart = findEpochStartJavaTime(javaTime);
		final long millisecondsInEpoch = javaTime - epochStart;
		final long seconds = millisecondsInEpoch / 1000L;
		final long fraction = Math.round(((millisecondsInEpoch % 1000L) * 0x100000000L) / 1000D);

		return toNtpTimeTag(seconds, fraction);
	}

	/**
	 * Creates the Java representation of an OSC time-tag with the semantics of
	 * "immediately"/"now".
	 *
	 * @return the created Java <code>Date</code>
	 * @see #IMMEDIATE
	 */
	public static Date immediateDate() {
		return IMMEDIATE.toDate(new Date(EPOCH_START_JAVA_TIME_0));
	}
}
