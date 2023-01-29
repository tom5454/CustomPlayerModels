package com.atlassian.sourcemap;

import java.io.IOException;

/**
 * Code based on Google Closure Compiler https://code.google.com/p/closure-compiler
 */
class InternalUtil
{
	private static final char[] HEX_CHARS = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	/**
	 * Escapes the given string to a double quoted (") JavaScript/JSON string
	 */
	static String escapeString(String s) {
		return escapeString(s, '"',  "\\\"", "\'", "\\\\");
	}

	/** Helper to escape JavaScript string as well as regular expression */
	static String escapeString(String s, char quote,
			String doublequoteEscape,
			String singlequoteEscape,
			String backslashEscape) {
		StringBuilder sb = new StringBuilder(s.length() + 2);
		sb.append(quote);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			case '\\': sb.append(backslashEscape); break;
			case '\"': sb.append(doublequoteEscape); break;
			case '\'': sb.append(singlequoteEscape); break;
			case '>':                       // Break --> into --\> or ]]> into ]]\>
				if (i >= 2 &&
				((s.charAt(i - 1) == '-' && s.charAt(i - 2) == '-') ||
						(s.charAt(i - 1) == ']' && s.charAt(i - 2) == ']'))) {
					sb.append("\\>");
				} else {
					sb.append(c);
				}
				break;
			case '<':
				// Break </script into <\/script
				final String END_SCRIPT = "/script";

				// Break <!-- into <\!--
				final String START_COMMENT = "!--";

				if (s.regionMatches(true, i + 1, END_SCRIPT, 0,
						END_SCRIPT.length())) {
					sb.append("<\\");
				} else if (s.regionMatches(false, i + 1, START_COMMENT, 0,
						START_COMMENT.length())) {
					sb.append("<\\");
				} else {
					sb.append(c);
				}
				break;
			default:
				// No charsetEncoder provided - pass straight Latin characters
				// through, and escape the rest.  Doing the explicit character
				// check is measurably faster than using the CharsetEncoder.
				if (c > 0x1f && c <= 0x7f) {
					sb.append(c);
				} else {
					// Other characters can be misinterpreted by some JS parsers,
					// or perhaps mangled by proxies along the way,
					// so we play it safe and Unicode escape them.
					appendCharAsHex(sb, c);
				}
			}
		}
		sb.append(quote);
		return sb.toString();
	}

	static String join(Iterable<String> list, String delimiter)
	{
		StringBuilder buff = new StringBuilder();
		boolean isFirst = true;
		for (String s : list) {
			if (isFirst) isFirst = false;
			else buff.append(delimiter);
			buff.append(s);
		}
		return buff.toString();
	}

	/**
	 * @see #appendHexJavaScriptRepresentation(Appendable, int)
	 */
	@SuppressWarnings("cast")
	private static void appendCharAsHex(
			StringBuilder sb, char c) {
		try {
			appendHexJavaScriptRepresentation(sb, c);
		} catch (IOException ex) { throw new RuntimeException(ex); }
	}

	/**
	 * Returns a JavaScript representation of the character in a hex escaped
	 * format.
	 * @param out The buffer to which the hex representation should be appended.
	 * @param codePoint The code point to append.
	 */
	private static void appendHexJavaScriptRepresentation(Appendable out, int codePoint) throws IOException {
		if (Character.isSupplementaryCodePoint(codePoint)) {
			// Handle supplementary Unicode values which are not representable in
			// JavaScript.  We deal with these by escaping them as two 4B sequences
			// so that they will round-trip properly when sent from Java to JavaScript
			// and back.
			char[] surrogates = Character.toChars(codePoint);
			appendHexJavaScriptRepresentation(out, surrogates[0]);
			appendHexJavaScriptRepresentation(out, surrogates[1]);
			return;
		}
		out.append("\\u")
		.append(HEX_CHARS[(codePoint >>> 12) & 0xf])
		.append(HEX_CHARS[(codePoint >>> 8) & 0xf])
		.append(HEX_CHARS[(codePoint >>> 4) & 0xf])
		.append(HEX_CHARS[codePoint & 0xf]);
	}
}
