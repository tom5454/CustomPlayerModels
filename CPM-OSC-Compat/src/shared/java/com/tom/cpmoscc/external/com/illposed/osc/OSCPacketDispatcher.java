/*
 * Copyright (C) 2003-2017, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.tom.cpmoscc.external.com.illposed.osc.argument.OSCTimeTag64;
import com.tom.cpmoscc.external.com.illposed.osc.argument.handler.StringArgumentHandler;

/**
 * Dispatches {@link OSCPacket}s to registered listeners (<i>Method</i>s).
 */
public class OSCPacketDispatcher implements OSCPacketListener {

	// Public API
	/**
	 * Completely arbitrary number of arguments,
	 * required only due to library internal technical reasons.
	 * It should be possible to get around this limitation,
	 * by refactoring the code.
	 * XXX refactor-out this arbitrary number
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int MAX_ARGUMENTS = 64;
	private static final int DEFAULT_CORE_THREADS = 3;
	private final BytesReceiver argumentTypesOutput;
	private final OSCSerializer serializer;
	private final Charset typeTagsCharset;
	private final List<SelectiveMessageListener> selectiveMessageListeners;
	private final List<OSCBadDataListener> badDataListeners;
	private boolean metaInfoRequired;
	/**
	 * Whether to disregard bundle time-stamps for dispatch-scheduling.
	 */
	private boolean alwaysDispatchingImmediately;
	private final ScheduledExecutorService dispatchScheduler;

	public static class DaemonThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(final Runnable runnable) {

			final Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	}

	private static final class SelectiveMessageListener {

		private final MessageSelector selector;
		private final OSCMessageListener listener;

		public SelectiveMessageListener(
				final MessageSelector selector,
				final OSCMessageListener listener)
		{
			this.selector = selector;
			this.listener = listener;
		}

		public MessageSelector getSelector() {
			return selector;
		}

		public OSCMessageListener getListener() {
			return listener;
		}

		@Override
		public boolean equals(final Object other) {

			boolean equal = false;
			if (other instanceof SelectiveMessageListener) {
				final SelectiveMessageListener otherSelector = (SelectiveMessageListener) other;
				equal = this.selector.equals(otherSelector.selector)
						&& this.listener.equals(otherSelector.listener);
			}

			return equal;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = (89 * hash) + this.selector.hashCode();
			hash = (89 * hash) + this.listener.hashCode();
			return hash;
		}
	}

	private static class NullOSCSerializer extends OSCSerializer {

		NullOSCSerializer() {
			super(
					Collections.emptyList(),
					Collections.emptyMap(),
					null);
		}

		@Override
		public void writeOnlyTypeTags(final List<?> arguments) {
			throw new IllegalStateException(
					"You need to either dispatch only packets containing meta-info, "
					+ "or supply a serialization factory to the dispatcher");
		}
	}

	private static class NullOSCSerializerBuilder extends OSCSerializerAndParserBuilder {
		@Override
		public OSCSerializer buildSerializer(final BytesReceiver output) {
			return new NullOSCSerializer();
		}
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCPacketDispatcher(
			final OSCSerializerAndParserBuilder serializerBuilder,
			final ScheduledExecutorService dispatchScheduler)
	{
		final OSCSerializerAndParserBuilder nonNullSerializerBuilder;
		final ByteBuffer argumentTypesBuffer;
		if (serializerBuilder == null) {
			argumentTypesBuffer = ByteBuffer.allocate(0);
			nonNullSerializerBuilder = new NullOSCSerializerBuilder();
		} else {
			argumentTypesBuffer = ByteBuffer.allocate(MAX_ARGUMENTS);
			nonNullSerializerBuilder = serializerBuilder;
		}
		this.argumentTypesOutput = new BufferBytesReceiver(argumentTypesBuffer);
		this.serializer = nonNullSerializerBuilder.buildSerializer(argumentTypesOutput);
		final Map<String, Object> serializationProperties
				= nonNullSerializerBuilder.getProperties();
		final Charset propertiesCharset
				= (Charset) serializationProperties.get(StringArgumentHandler.PROP_NAME_CHARSET);
		this.typeTagsCharset = (propertiesCharset == null)
				? Charset.defaultCharset()
				: propertiesCharset;
		this.selectiveMessageListeners = new ArrayList<>();
		this.badDataListeners = new ArrayList<>();
		this.metaInfoRequired = false;
		this.alwaysDispatchingImmediately = false;
		this.dispatchScheduler = dispatchScheduler;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCPacketDispatcher(final OSCSerializerAndParserBuilder serializerBuilder) {
		this(serializerBuilder, createDefaultDispatchScheduler());
	}

	public OSCPacketDispatcher() {
		this(null);
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static ScheduledExecutorService createDefaultDispatchScheduler() {
		return Executors.newScheduledThreadPool(DEFAULT_CORE_THREADS, new DaemonThreadFactory());
	}

	/**
	 * Set whether to disregard bundle time-stamps for dispatch-scheduling.
	 * @param alwaysDispatchingImmediately if {@code true}, all bundles will be
	 *   dispatched immediately
	 */
	public void setAlwaysDispatchingImmediately(final boolean alwaysDispatchingImmediately) {
		this.alwaysDispatchingImmediately = alwaysDispatchingImmediately;
	}

	// Public API
	/**
	 * Indicates whether we disregard bundle time-stamps for dispatch-scheduling.
	 * @return {@code true}, if all bundles are dispatched immediately
	 */
	@SuppressWarnings("WeakerAccess")
	public boolean isAlwaysDispatchingImmediately() {
		return alwaysDispatchingImmediately;
	}

	// Public API
	/**
	 * Indicates whether we need outgoing messages to have meta-info attached.
	 * @return {@code true}, if at least one of the registered listeners requires message meta-info
	 */
	@SuppressWarnings("WeakerAccess")
	public boolean isMetaInfoRequired() {
		return metaInfoRequired;
	}

	/**
	 * Adds a listener (<i>Method</i> in OSC speak) that will be notified
	 * of incoming messages that match the selector.
	 * Registered listeners will be notified of selected messages in the order
	 * they were added to the dispatcher.
	 * A listener can be registered multiple times,
	 * and will consequently be notified as many times as it was added.
	 * @param messageSelector selects which messages will be forwarded to the listener
	 * @param listener receives messages accepted by the selector
	 */
	public void addListener(
			final MessageSelector messageSelector,
			final OSCMessageListener listener)
	{
		selectiveMessageListeners.add(new SelectiveMessageListener(messageSelector, listener));
		if (messageSelector.isInfoRequired()) {
			metaInfoRequired = true;
		}
	}

	// Public API
	/**
	 * Removes a listener (<i>Method</i> in OSC speak), which will no longer
	 * be notified of incoming messages.
	 * Removes only the first occurrence of the selector and listener pair.
	 * @param messageSelector has to match the registered pair to be removed
	 * @param listener will no longer receive messages accepted by the selector
	 */
	@SuppressWarnings("WeakerAccess")
	public void removeListener(
			final MessageSelector messageSelector,
			final OSCMessageListener listener)
	{
		selectiveMessageListeners.remove(new SelectiveMessageListener(messageSelector, listener));
		if (metaInfoRequired) {
			// re-evaluate whether meta info is still required
			metaInfoRequired = selectiveMessageListeners.stream()
					.anyMatch(selMsgListener -> selMsgListener.getSelector().isInfoRequired());
		}
	}

	// Public API
	/**
	 * Adds a listener that will be notified of incoming bad/unrecognized data.
	 * @param listener will receive chunks of unrecognized data
	 */
	@SuppressWarnings("WeakerAccess")
	public void addBadDataListener(final OSCBadDataListener listener) {
		badDataListeners.add(listener);
	}

	// Public API
	/**
	 * Removes a listener that is notified of incoming bad/unrecognized data.
	 * @param listener will no longer receive chunks of unrecognized data
	 */
	@SuppressWarnings("unused")
	public void removeBadDataListener(final OSCBadDataListener listener) {
		badDataListeners.remove(listener);
	}

	@Override
	public void handleBadData(final OSCBadDataEvent event) {

		for (final OSCBadDataListener listener : badDataListeners) {
			listener.badDataReceived(event);
		}
	}

	/**
	 * The work-horse of {@link #handlePacket(OSCPacketEvent)}.
	 * @param source the origin of the packet, usually an instance of
	 *   {@link com.illposed.osc.transport.udp.OSCPortIn}
	 * @param packet to be dispatched
	 * @param timeStamp the associated time-stamp
	 */
	private void dispatchPacket(final Object source, final OSCPacket packet, final OSCTimeTag64 timeStamp) {
		if (packet instanceof OSCBundle) {
			dispatchBundle(source, (OSCBundle) packet);
		} else {
			dispatchMessageNow(new OSCMessageEvent(source, timeStamp, (OSCMessage) packet));
		}
	}

	/**
	 * Dispatches the packet immediately if it is a simple message,
	 * otherwise (it being a bundle), its dispatch might get delayed,
	 * depending on whether it has an associated {@link OSCBundle#getTimestamp() time-stamp},
	 * and whether we are {@link #isAlwaysDispatchingImmediately() always dispatching immediately}.
	 * @param event the packet to be dispatched plus meta-data
	 */
	@Override
	public void handlePacket(final OSCPacketEvent event) {
		dispatchPacket(event.getSource(), event.getPacket(), OSCTimeTag64.IMMEDIATE);
	}

	/**
	 * Data-container and (delayed) dispatcher for a bundle
	 * that is supposed to be dispatched at a later point.
	 */
	private class BundleDispatcher implements Runnable {

		private final Object source;
		private final OSCBundle bundle;

		BundleDispatcher(final Object source, final OSCBundle bundle) {
			this.source = source;
			this.bundle = bundle;
		}

		@Override
		public void run() {
			dispatchBundleNow(source, bundle);
		}
	}

	private long calculateDelayFromNow(final OSCTimeTag64 timeStamp) {
		return timeStamp.toDate(null).getTime() - System.currentTimeMillis();
	}

	/**
	 * Dispatches the bundle, either immediately or at the time indicated by its associated time-stamp,
	 * depending on whether it has an associated {@link OSCBundle#getTimestamp() time-stamp},
	 * and whether we are {@link #isAlwaysDispatchingImmediately() always dispatching immediately}.
	 * @param source the origin of the packet, usually an instance of
	 *   {@link com.illposed.osc.transport.udp.OSCPortIn}
	 * @param bundle the bundle to be dispatched
	 */
	private void dispatchBundle(final Object source, final OSCBundle bundle) {
		final OSCTimeTag64 timeStamp = bundle.getTimestamp();
		if (isAlwaysDispatchingImmediately() || timeStamp.isImmediate()) {
			dispatchBundleNow(source, bundle);
		} else {
			// NOTE This scheduling accuracy is only to at most the accuracy of the
			//   default system clock, and thus might not be enough in some use-cases.
			//   It can never be more accurate then 1ms, and on many systems will be ~ 10ms.
			final long delayMs = calculateDelayFromNow(timeStamp);
			dispatchScheduler.schedule(
					new BundleDispatcher(source, bundle),
					delayMs,
					TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Immediately dispatches a bundle to the registered listeners.
	 * This means contained messages are dispatched immediately,
	 * but contained sub-bundles might still be dispatched later,
	 * depending on whether they have an associated {@link OSCBundle#getTimestamp() time-stamp},
	 * and whether we are {@link #isAlwaysDispatchingImmediately() always dispatching immediately}.
	 * @param source the origin of the packet, usually an instance of
	 *   {@link com.illposed.osc.transport.udp.OSCPortIn}
	 * @param bundle the bundle to be dispatched immediately
	 */
	private void dispatchBundleNow(final Object source, final OSCBundle bundle) {
		final OSCTimeTag64 timeStamp = bundle.getTimestamp();
		final List<OSCPacket> packets = bundle.getPackets();
		for (final OSCPacket packet : packets) {
			dispatchPacket(source, packet, timeStamp);
		}
	}

	private CharSequence generateTypeTagsString(final List<?> arguments) {

		try {
			serializer.writeOnlyTypeTags(arguments);
		} catch (final OSCSerializeException ex) {
			throw new IllegalArgumentException(
					"Failed generating Arguments Type Tag string while dispatching",
					ex);
		}

		return new String(argumentTypesOutput.toByteArray(), typeTagsCharset);
	}

	private void ensureMetaInfo(final OSCMessage message) {

		if (isMetaInfoRequired() && !message.isInfoSet()) {
			final CharSequence generateTypeTagsString
					= generateTypeTagsString(message.getArguments());
			final OSCMessageInfo messageInfo = new OSCMessageInfo(generateTypeTagsString);
			message.setInfo(messageInfo);
		}
	}

	/**
	 * Immediately dispatches a message to the registered listeners,
	 * after ensuring it has meta-info set.
	 * @param event the message to dispatch plus event meta-data
	 */
	private void dispatchMessageNow(final OSCMessageEvent event) {

		ensureMetaInfo(event.getMessage());

		for (final SelectiveMessageListener selectiveMessageListener : selectiveMessageListeners) {
			if (selectiveMessageListener.getSelector().matches(event)) {
				selectiveMessageListener.getListener().acceptMessage(event);
			}
		}
	}
}
