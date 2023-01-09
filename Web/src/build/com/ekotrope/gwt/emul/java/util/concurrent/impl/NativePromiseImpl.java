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

import java.util.function.BiConsumer;

/**
 *
 */
final class NativePromiseImpl<V> implements Promise<V> {

  final JsPromise jsPromise;
  private JsPromise.Resolver resolver;
  private JsPromise.Rejector rejector;

  NativePromiseImpl() {
    jsPromise = new JsPromise((resolve, reject) -> {
      resolver = resolve;
      rejector = reject;
    });
  }

  NativePromiseImpl(JsPromise promise) {
    assert promise != null;
    this.jsPromise = promise;
  }

  @Override
  public void resolve(V value) {
    resolver.resolve(value);
  }

  @Override
  public void reject(Throwable reason) {
    assert reason != null;
    rejector.reject(reason);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void then(BiConsumer<? super V, ? super Throwable> callback) {
    assert callback != null;
    jsPromise.then(
        value -> callback.accept((V) value, null),
        reason -> callback.accept(null, (Throwable) reason));
  }

  @Override
  public void then(Runnable callback) {
    assert callback != null;
    JsPromise.OnSettledCallback func = value -> callback.run();
    jsPromise.then(func, func);
  }
}
