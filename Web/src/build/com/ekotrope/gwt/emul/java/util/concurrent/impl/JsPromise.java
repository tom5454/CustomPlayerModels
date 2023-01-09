/*
 * Copyright 2016 Google Inc.
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
package java.util.concurrent.impl;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * ECMA 6 Promise API.
 * See
 * <a href="https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Promise">
 * MDN Promise documentation</a>.
 */
@JsType(isNative = true, name = "Promise", namespace = JsPackage.GLOBAL)
final class JsPromise {

  public static native JsPromise all(JsPromise[] promises);

  public static native JsPromise race(JsPromise[] promises);

  public static native JsPromise reject(Object reason);

  public static native JsPromise resolve(Object value);

  @JsConstructor
  public JsPromise(Executor executor) { }

  /*
   * Method has no return value for simplicity because the return value of the method,
   * onFulfilled and onRejected (OnSettledCallback) are not used.
   */
  // TODO: $entry ?
  public native void then(OnSettledCallback onFulfilled,
      OnSettledCallback onRejected);

  @FunctionalInterface
  @JsFunction
  interface Executor {
    void executor(Resolver resolve, Rejector reject);
  }

  @JsFunction
  interface Resolver {
    void resolve(Object value);
  }

  @JsFunction
  interface Rejector {
    void reject(Object reason);
  }

  /*
   * Single interface for onFulfilled and onRejected callbacks are used for simplicity.
   */
  @FunctionalInterface
  @JsFunction
  interface OnSettledCallback {
    void onSettled(Object value);
  }
}
