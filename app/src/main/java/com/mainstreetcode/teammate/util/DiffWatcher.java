/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.util;

import com.tunjid.androidbootstrap.functions.Consumer;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class DiffWatcher<T> {

    private final AtomicReference<PublishSubject<T>> ref = new AtomicReference<>();
    private final Consumer<T> consumer;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public DiffWatcher(Consumer<T> consumer) {
        this.consumer = consumer;
        watch();
    }

    public void push(T item) { ref.get().onNext(item); }

    public void stop() { disposable.clear(); }

    public void restart() {
        stop();
        watch();
    }

    private void watch() {
        ref.set(PublishSubject.create());
        disposable.add(ref.get().distinctUntilChanged().subscribe(consumer::accept, ErrorHandler.EMPTY));
    }
}
