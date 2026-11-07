/*
 * Copyright (C) 2026 GappleX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.gapplex.gxu;

import io.github.gapplex.gxu.container.Lists;
import io.github.gapplex.gxu.reflect.Callers;
import io.github.gapplex.gxu.reflect.SkipCallerScan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Deprecated(since="0.8.0")
public class Var<T> implements AutoCloseable {

    private final AtomicReference<T> ref;
    private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> upstream = new CopyOnWriteArrayList<>();
    private volatile boolean closed;

    public Var(T value) {
        this.ref = new AtomicReference<>(value);
    }

    public T get() {
        return ref.get();
    }

    public void set(T value) {
        T old = ref.getAndSet(value);
        fireIfChanged(old, value);
    }

    public boolean compareAndSet(T expect, T update) {
        if (ref.compareAndSet(expect, update)) {
            fireIfChanged(expect, update);
            return true;
        }
        return false;
    }

    public T getAndSet(T value) {
        T old = ref.getAndSet(value);
        fireIfChanged(old, value);
        return old;
    }

    public T updateAndGet(UnaryOperator<T> f) {
        T prev, next;
        do {
            prev = ref.get();
            next = f.apply(prev);
        } while (!ref.compareAndSet(prev, next));
        fireIfChanged(prev, next);
        return next;
    }

    public Runnable subscribe(Consumer<T> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public <R> Var<R> map(Function<T, R> f) {
        Var<R> out = new Var<>(f.apply(get()));
        out.attach(subscribe(v -> out.set(f.apply(v))));
        return out;
    }

    public static <A, B, R> Var<R> combine(Var<A> a, Var<B> b, BiFunction<A, B, R> f) {
        Var<R> out = new Var<>(f.apply(a.get(), b.get()));
        out.attach(a.subscribe(x -> out.set(f.apply(x, b.get()))));
        out.attach(b.subscribe(x -> out.set(f.apply(a.get(), x))));
        return out;
    }

    @Override
    public void close() {
        closed = true;
        List<Throwable> failures = new ArrayList<>();
        for (Runnable r : upstream) {
            try {
                r.run();
            } catch (Throwable t) {
                failures.add(t);
            }
        }
        upstream.clear();
        listeners.clear();
        if (!failures.isEmpty()) {
            throw new CloseException("Var.close(): " + failures.size() + " upstream cancel(s) failed", failures);
        }
    }

    public static class CloseException extends RuntimeException {
        private CloseException(String message, List<? extends Throwable> throwableList) {
            super(message);
            if (throwableList.size() == 1) {
                initCause(throwableList.get(0));
                return;
            }
            for (Throwable t : throwableList) {
                addSuppressed(t);
            }
        }

        public boolean hasMultipleFailures() {
            return getCause() == null;
        }

        public ArrayList<Throwable> getFailures() {
            ArrayList<Throwable> out = new ArrayList<>();
            Throwable c = getCause();
            if (c != null) out.add(c);
            Collections.addAll(out, getSuppressed());
            return out;
        }

        @Override
        @SkipCallerScan
        public synchronized CloseException initCause(Throwable t) {
            Callers c = new Callers(CloseException.class);
            if (!c.isCalledFrom(CloseException.class)) {
                StackTraceElement caller = c.getCaller();
                throw new UnsupportedOperationException(
                        "Unauthorized caller: " + (caller == null ? "<unknown>" : caller.getClassName()));
            }
            super.initCause(t);
            return this;
        }
    }

    private void fireIfChanged(T oldValue, T newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (Consumer<T> l : listeners) l.accept(newValue);
        }
    }

    private void attach(Runnable cancel) {
        if (closed) {
            cancel.run();
        } else {
            upstream.add(cancel);
        }
    }
}