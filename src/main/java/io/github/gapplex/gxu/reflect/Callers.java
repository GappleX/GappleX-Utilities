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
package io.github.gapplex.gxu.reflect;

import io.github.gapplex.gxu.container.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Determines the "real" caller of a method, transparently skipping frames that
 * belong to the framework itself, i.e. frames whose method/constructor is
 * annotated with {@link SkipCallerScan}.
 * <p>
 * Only classes explicitly registered as {@code trusted} may have their
 * {@link SkipCallerScan} frames skipped. This prevents user code from
 * silently hiding its own frames simply by adding the annotation.
 */
public final class Callers {

    private static final StackWalker WALKER = StackWalker.getInstance(
            StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final Set<Class<?>> trusted;

    /**
     * Per-class cache: {@code methodName + descriptor} -> whether that frame
     * must be treated as a "skip" (internal framework) frame.
     */
    private final ClassValue<ConcurrentHashMap<String, Boolean>> skipCache =
            new ClassValue<>() {
                @Override
                protected ConcurrentHashMap<String, Boolean> computeValue(@NotNull Class<?> type) {
                    return new ConcurrentHashMap<>();
                }
            };

    /**
     * @param trusted classes whose {@link SkipCallerScan}-annotated
     *                methods/constructors should be skipped when locating the
     *                caller. {@code null} entries are ignored.
     */
    public Callers(Class<?>... trusted) {
        Objects.requireNonNull(trusted, "trusted");

        Set<Class<?>> set = Sets.newHashSet(trusted.length + 1);
        for (int i = 0; i < trusted.length; i++) set.add(Objects.requireNonNull(trusted[i], "Null at trusted[" + i + "]"));
        set.add(Callers.class);
        this.trusted = Set.copyOf(set);
    }

    /**
     * @return {@code true} if the nearest non-internal caller belongs to
     *         {@code expected}
     */
    @SkipCallerScan
    public boolean isCalledFrom(@NotNull Class<?> expected) {
        return Boolean.TRUE.equals(WALKER.walk(frames -> frames
                .filter(this::isNotSkipFrame)
                .findFirst()
                .map(f -> f.getDeclaringClass() == expected)
                .orElse(false)));
    }

    /**
     * @return the nearest non-internal caller frame, or {@code null}
     */
    @Nullable
    @SkipCallerScan
    public StackTraceElement getCaller() {
        return WALKER.walk(frames -> frames
                .filter(this::isNotSkipFrame)
                .findFirst()
                .map(StackWalker.StackFrame::toStackTraceElement)
                .orElse(null));
    }

    /**
     * @return the class of the nearest non-internal caller, or {@code null}
     */
    @Nullable
    @SkipCallerScan
    public Class<?> getCallerClass() {
        return WALKER.walk(frames -> frames
                .filter(this::isNotSkipFrame)
                .findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass)
                .orElse(null));
    }

    private boolean isNotSkipFrame(StackWalker.StackFrame frame) {
        try {
            Class<?> declaring = frame.getDeclaringClass();
            // Fast path: an untrusted class can never be skipped, so there is
            // no point in doing any reflection or touching the cache.
            if (!trusted.contains(declaring)) {
                return true;
            }
            String name = frame.getMethodName();
            String key = name + frame.getDescriptor();
            ConcurrentHashMap<String, Boolean> cache = skipCache.get(declaring);
            boolean skip = cache.computeIfAbsent(key, ignored ->
                    doCheckSkip(declaring, name, frame.getMethodType().parameterArray()));
            return !skip;
        } catch (RuntimeException | LinkageError e) {
            // If the frame cannot be inspected reliably, treat it as internal
            // so that framework internals are not exposed to the caller.
            return false;
        }
    }

    private boolean doCheckSkip(Class<?> declaring, String name, Class<?>[] params) {
        Executable exec;
        try {
            exec = "<init>".equals(name)
                    ? declaring.getDeclaredConstructor(params)
                    : declaring.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException | SecurityException e) {
            return false;
        }

        if (exec.isAnnotationPresent(SkipCallerScan.class)) {
            return true;
        }

        // Bridge methods generated for generics do not carry the original
        // annotations, so fall back to a same-name lookup.
        return exec instanceof Method
                && ((Method) exec).isBridge()
                && hasSameNameAnnotatedMethod(declaring, name);
    }

    private boolean hasSameNameAnnotatedMethod(Class<?> declaring, String name) {
        for (Method m : declaring.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.isAnnotationPresent(SkipCallerScan.class)) {
                return true;
            }
        }
        return false;
    }
}