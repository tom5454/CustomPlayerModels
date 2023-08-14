/*
 * Copyright (C) 2014-2017, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.tom.cpmoscc.external.com.illposed.osc.messageselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.tom.cpmoscc.external.com.illposed.osc.MessageSelector;
import com.tom.cpmoscc.external.com.illposed.osc.OSCMessageEvent;

/**
 * Checks whether an OSC <i>Address Pattern</i> matches a given wildcard expression,
 * as described in the OSC 1.0 protocol specification.
 * For details, see the "OSC Message Dispatching and Pattern Matching" section
 * in <a href="http://opensoundcontrol.org/spec-1_0">the OSC 1.0 specification
 * </a>.
 * This also supports the path-traversal wildcard "//",
 * as specified in OSC 1.1 (borrowed from XPath).
 *
 * <p>
 * A coarse history of the code in the function
 * {@link OSCPatternAddressMessageSelector#matches(String, String)},
 * from the origin to JavaOSC:
 * </p>
 * <ol>
 * <li>
 *   <b>robust glob pattern matcher</b><br>
 *   language: <i>C</i><br>
 *   author: <i>ozan s. yigit/dec 1994</i><br>
 *   matching code license: <i>public domain</i><br>
 *   <a href="http://www.cse.yorku.ca/~oz/glob.bun">source location</a><br>
 * </li>
 * <li>
 *   <b>Open SoundControl kit</b><br>
 *   language: <i>C++</i><br>
 *   matching code license: <i>public domain</i><br>
 *   library license: <i>LGPL 2.1+</i><br>
 *   <a href="http://archive.cnmat.berkeley.edu/OpenSoundControl/src/OSC-Kit/OSC-pattern-match.c">
 *   source location</a><br>
 * </li>
 * <li>
 *   <b>LibLO</b><br>
 *   language: <i>C++</i><br>
 *   library license: <i>LGPL 2.1+</i><br>
 *   <a href="https://sourceforge.net/p/liblo/git/ci/master/tree/src/pattern_match.c">
 *   source location</a><br>
 * </li>
 * <li>
 *   <b>JavaOSC</b><br>
 *   language: <i>Java</i><br>
 *   matching code license: <i>public domain</i><br>
 *   library license: <i>BSD 3-Clause</i><br>
 * </li>
 * </ol>
 */
public class OSCPatternAddressMessageSelector implements MessageSelector {

	private final List<String> patternParts;

	/**
	 * Creates a selector that may take a simple address or a wildcard as matching criteria.
	 *
	 * @param selector either a fixed address like "/sc/mixer/volume",
	 *   or a selector pattern (a mix between wildcards and regex)
	 *   like "/??/mixer/*", see {@link OSCPatternAddressMessageSelector the class comment}
	 *   for more details
	 */
	public OSCPatternAddressMessageSelector(final String selector) {
		this.patternParts = splitIntoParts(selector);
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof OSCPatternAddressMessageSelector) {
			final OSCPatternAddressMessageSelector otherSelector
					= (OSCPatternAddressMessageSelector) other;
			equal = this.patternParts.equals(otherSelector.patternParts);
		}

		return equal;
	}

	@Override
	public int hashCode() {
		return this.patternParts.hashCode();
	}

	@Override
	public boolean isInfoRequired() {
		return false;
	}

	@Override
	public boolean matches(final OSCMessageEvent messageEvent) {

		final List<String> messageAddressParts = splitIntoParts(messageEvent.getMessage().getAddress());
		return matches(patternParts, 0, messageAddressParts, 0);
	}

	/**
	 * Splits an OSC message address or address selector pattern into parts that are convenient
	 * during the matching process.
	 * @param addressOrPattern to be split into parts, e.g.: "/hello/", "/hello//world//"
	 * @return the given address or pattern split into parts: {"hello"}, {"hello, "", "world", ""}
	 */
	private static List<String> splitIntoParts(final String addressOrPattern) {

		final List<String> parts
				= new ArrayList<>(Arrays.asList(addressOrPattern.split("/", -1)));
		if (addressOrPattern.startsWith("/")) {
			// as "/hello" gets split into {"", "hello"}, we remove the first empty entry,
			// so we end up with {"hello"}
			parts.remove(0);
		}
		if (addressOrPattern.endsWith("/")) {
			// as "hello/" gets split into {"hello", ""}, we also remove the last empty entry,
			// so we end up with {"hello"}
			parts.remove(parts.size() - 1);
		}
		return Collections.unmodifiableList(parts);
	}

	/**
	 * Tries to match an OSC <i>Address Pattern</i> to a selector,
	 * both already divided into their parts.
	 * @param patternParts all the parts of the pattern
	 * @param patternPartIndex index/pointer to the current part of the pattern we are looking at
	 * @param messageAddressParts all the parts of the address
	 * @param addressPartIndex index/pointer to the current part of the address we are looking at
	 * @return true if the address matches, false otherwise
	 */
	private static boolean matches(
			final List<String> patternParts,
			final int patternPartIndex,
			final List<String> messageAddressParts,
			final int addressPartIndex)
	{
		int ppi = patternPartIndex;
		int api = addressPartIndex;
		while (ppi < patternParts.size()) {
			// There might be some path-traversal wildcards (PTW) "//" in the pattern.
			// "//" in the pattern translates to an empty String ("") in the pattern parts.
			// We skip all consecutive "//"s at the current pattern position.
			boolean pathTraverser = false;
			while ((ppi < patternParts.size()) && patternParts.get(ppi).isEmpty()) {
				ppi++;
				pathTraverser = true;
			}
			// ppi is now at the end, or at the first non-PTW part
			if (pathTraverser) {
				if (ppi == patternParts.size()) {
					// trailing PTW matches the whole rest of the address
					return true;
				}
				while (api < messageAddressParts.size()) {
					if (matches(messageAddressParts.get(api), patternParts.get(ppi))
							&& matches(patternParts, ppi + 1, messageAddressParts, api + 1))
					{
						return true;
					}
					api++;
				}
				// end of address parts reached, but there are still non-PTW pattern parts
				// left
				return false;
			} else {
				if ((ppi == patternParts.size()) != (api == messageAddressParts.size())) {
					// end of pattern, no trailing PTW, but there are still address parts left
					// OR
					// end of address, but there are still non-PTW pattern parts left
					return false;
				}
				if (!matches(messageAddressParts.get(api), patternParts.get(ppi))) {
					return false;
				}
				api++;
			}
			ppi++;
		}

		return (api == messageAddressParts.size());
	}

	// Public API
	/**
	 * Tries to match an OSC <i>Address Pattern</i> part to a part of
	 * a selector.
	 * This code was copied and adapted from LibLo,
	 * and is licensed under the Public Domain.
	 * For more details see:
	 * {@link OSCPatternAddressMessageSelector the class comment}.
	 * @param str address part
	 * @param p pattern part
	 * @return true if the address part matches, false otherwise
	 */
	@SuppressWarnings("WeakerAccess")
	public static boolean matches(final String str, final String p) {

		boolean match;
		char c;

		int si = 0;
		int pi = 0;
		while (pi < p.length()) {
			if ((si == str.length()) && (p.charAt(pi) != '*')) {
				return false;
			}

			c = p.charAt(pi++);
			boolean negate;
			switch (c) {
				case '*':
					while ((pi < p.length()) && (p.charAt(pi) == '*') && (p.charAt(pi) != '/')) {
						pi++;
					}

					if (pi == p.length()) {
						return true;
					}

//					if (p.charAt(pi) != '?' && p.charAt(pi) != '[' && p.charAt(pi) != '\\')
					if ((p.charAt(pi) != '?') && (p.charAt(pi) != '[') && (p.charAt(pi) != '{')) {
						while ((si < str.length()) && (p.charAt(pi) != str.charAt(si))) {
							si++;
						}
					}

					while (si < str.length()) {
						if (matches(str.substring(si), p.substring(pi))) {
							return true;
						}
						si++;
					}
					return false;

				case '?':
					if (si < str.length()) {
						break;
					}
					return false;

				/*
				 * set specification is inclusive, that is [a-z] is a, z and
				 * everything in between. this means [z-a] may be interpreted
				 * as a set that contains z, a and nothing in between.
				 */
				case '[':
					if (p.charAt(pi) == '!') {
						negate = true;
						pi++;
					} else {
						negate = false;
					}

					match = false;

					while (!match && (pi < p.length())) {
						c = p.charAt(pi++);
						if (pi == p.length()) {
							return false;
						}
						if (p.charAt(pi) == '-') {
							// c-c
							pi++;
							if (pi == p.length()) {
								return false;
							}
							if (p.charAt(pi) != ']') {
								if ((str.charAt(si) == c) || (str.charAt(si) == p.charAt(pi))
										|| ((str.charAt(si) > c) && (str.charAt(si) < p.charAt(pi))))
								{
									match = true;
								}
							} else {
								// c-]
								if (str.charAt(si) >= c) {
									match = true;
								}
								break;
							}
						} else {
							// cc or c]
							if (c == str.charAt(si)) {
								match = true;
							}
							if (p.charAt(pi) != ']') {
								if (p.charAt(pi) == str.charAt(si)) {
									match = true;
								}
							} else {
								break;
							}
						}
					}

					if (negate == match) {
						return false;
					}
					// if there is a match, skip past the cSet and continue on
					while ((pi < p.length()) && (p.charAt(pi) != ']')) {
						pi++;
					}
					if (pi++ == p.length()) {
						// oops!
						return false;
					}
					break;

				// {aString,bString,cString}
				case '{':
					// p.charAt(pi) is now first character in the {brace list}

					// to back-track
					final int place = si;
					// to forward-track
					int remainder = pi;

					// find the end of the brace list
					while ((remainder < p.length()) && (p.charAt(remainder) != '}')) {
						remainder++;
					}
					if (remainder == p.length()) {
						// oops!
						return false;
					}
					remainder++;

					c = p.charAt(pi++);
					while (pi <= p.length()) {
						if (c == ',') {
							if (matches(str.substring(si), p.substring(remainder))) {
								return true;
							} else {
								// back-track on test string
								si = place;
								// continue testing,
								// skip comma
								if (pi++ == p.length()) {
									// oops!
									return false;
								}
							}
						} else if (c == '}') {
							// continue normal pattern matching
							if ((pi == p.length()) && (si == str.length())) {
								return true;
							}
							// "si" is incremented again at the end of the loop
							si--;
							break;
						} else if (c == str.charAt(si)) {
							si++;
							if ((si == str.length()) && (remainder < p.length())) {
								return false;
							}
						} else {
							// skip to next comma
							si = place;
							while ((pi < p.length()) && (p.charAt(pi) != ',')
									&& (p.charAt(pi) != '}'))
							{
								pi++;
							}
							if (pi < p.length()) {
								if (p.charAt(pi) == ',') {
									pi++;
								} else if (p.charAt(pi) == '}') {
									return false;
								}
							}
						}
						c = p.charAt(pi++);
					}
					break;

				/*
				 * Not part of OSC pattern matching
					case '\\':
						if (p.charAt(pi)) {
							c = p.charAt(pi)++;
						}
				 */
				default:
					if (c != str.charAt(si)) {
						return false;
					}
					break;
			}
			si++;
		}

		return (si == str.length());
	}
}
