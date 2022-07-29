/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.user.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Helper class, which, during startup, asserts that the browser's current
 * rendering mode is one of the values allowed by the
 * {@value #PROPERTY_DOCUMENT_COMPATMODE}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Quirks_mode">Quirks Mode</a>
 */
public class DocumentModeAsserter implements EntryPoint {

	/**
	 * Interface to provide {@value #PROPERTY_DOCUMENT_COMPATMODE} configuration
	 * property value.
	 */
	public interface DocumentModeProperty {
		String[] getAllowedDocumentModes();

		Severity getDocumentModeSeverity();
	}

	/**
	 * Determine the severity of the runtime {@literal $doc.compatMode} check:
	 */
	public static enum Severity {
		/**
		 * Receive an error message at runtime.
		 */
		ERROR,

		/**
		 * No runtime check.
		 */
		IGNORE,

		/**
		 * Receive a warning in Development Mode.
		 */
		WARN;
	}

	@Override
	public void onModuleLoad() {}
}
