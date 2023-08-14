/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.argument.handler;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import com.tom.cpmoscc.external.com.illposed.osc.BytesReceiver;
import com.tom.cpmoscc.external.com.illposed.osc.OSCParseException;
import com.tom.cpmoscc.external.com.illposed.osc.OSCSerializeException;
import com.tom.cpmoscc.external.com.illposed.osc.argument.ArgumentHandler;
import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCTimeTag64;

/**
 * Parses and serializes an OSC <i>Time-tag</i>,
 * using {@link Date} as a wrapper,
 * and thus loosing out on the resolution.
 * You should consider using {@link TimeTag64ArgumentHandler} instead;
 */
public class DateTimeStampArgumentHandler implements ArgumentHandler<Date>, Cloneable {

	public static final ArgumentHandler<Date> INSTANCE = new DateTimeStampArgumentHandler();
	/**
	 * This {@link Date} property indicates the epoch into which we parse
	 * OSC Time-tags.
	 * The Java dates parsed by this handler will be in the same OSC/NTP epoch
	 * as this properties value.
	 */
	public static final String PROP_NAME_EPOCH_INDICATOR_TIME = "epoch-indicator";

	private Long epochIndicatorTime;

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected DateTimeStampArgumentHandler() {

		// now
		this.epochIndicatorTime = new Date().getTime();
	}

	@Override
	public char getDefaultIdentifier() {
		return 't';
	}

	@Override
	public Class<Date> getJavaClass() {
		return Date.class;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public void setEpochIndicatorTime(final Long epochIndicatorTime) {
		this.epochIndicatorTime = epochIndicatorTime;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {

		final Long newEpochIndicatorTime
				= (Long) properties.get(PROP_NAME_EPOCH_INDICATOR_TIME);
		if (newEpochIndicatorTime != null) {
			setEpochIndicatorTime(newEpochIndicatorTime);
		}
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DateTimeStampArgumentHandler clone() throws CloneNotSupportedException {
		return (DateTimeStampArgumentHandler) super.clone();
	}

	@Override
	public Date parse(final ByteBuffer input) throws OSCParseException {
		return TimeTag64ArgumentHandler.INSTANCE.parse(input).toDate(epochIndicatorTime);
	}

	@Override
	public void serialize(final BytesReceiver output, final Date value) throws OSCSerializeException {
		TimeTag64ArgumentHandler.INSTANCE.serialize(output, OSCTimeTag64.valueOf(value));
	}
}
