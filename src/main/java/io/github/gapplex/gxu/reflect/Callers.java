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
import java.util.function.Consumer;

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

    private final Consumer<Throwable> diagnosticHandler;

    private final ClassValue<ConcurrentHashMap<String, Boolean>> skipCache =
            new ClassValue<>() {
                @Override
                protected ConcurrentHashMap<String, Boolean> computeValue(@NotNull Class<?> type) {
                    return new ConcurrentHashMap<>();
                }
            };

    public Callers(Class<?>... trusted) {
        this(null, trusted);
    }

    public Callers(@Nullable Consumer<Throwable> diagnosticHandler, Class<?>... trusted) {
        Objects.requireNonNull(trusted, "trusted");

        Set<Class<?>> set = Sets.newHashSet(trusted.length + 1);
        for (int i = 0; i < trusted.length; i++) {
            set.add(Objects.requireNonNull(trusted[i], "Null at trusted[" + i + "]"));
        }
        set.add(Callers.class);
        this.trusted = Set.copyOf(set);
        this.diagnosticHandler = diagnosticHandler;
    }

    @SkipCallerScan
    public boolean isCalledFrom(@NotNull Class<?> expected) {
        return Boolean.TRUE.equals(WALKER.walk(frames -> frames
                .filter(this::shouldRetainFrame)
                .findFirst()
                .map(f -> f.getDeclaringClass() == expected)
                .orElse(false)));
    }

    @Nullable
    @SkipCallerScan
    public StackTraceElement getCaller() {
        return WALKER.walk(frames -> frames
                .filter(this::shouldRetainFrame)
                .findFirst()
                .map(StackWalker.StackFrame::toStackTraceElement)
                .orElse(null));
    }

    @Nullable
    @SkipCallerScan
    public Class<?> getCallerClass() {
        return WALKER.walk(frames -> frames
                .filter(this::shouldRetainFrame)
                .findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass)
                .orElse(null));
    }

    private boolean shouldRetainFrame(StackWalker.StackFrame frame) {
        try {
            Class<?> declaring = frame.getDeclaringClass();
            if (!trusted.contains(declaring)) {
                return true;
            }
            String name = frame.getMethodName();
            String key = name + frame.getDescriptor();
            ConcurrentHashMap<String, Boolean> cache = skipCache.get(declaring);

            Boolean cachedSkip = cache.get(key);
            if (cachedSkip == null) {
                boolean computedSkip = doCheckSkip(declaring, name, frame.getMethodType().parameterArray());
                Boolean existing = cache.putIfAbsent(key, computedSkip);
                cachedSkip = existing != null ? existing : computedSkip;
            }
            return !cachedSkip;
        } catch (RuntimeException | LinkageError e) {
            if (diagnosticHandler != null) {
                try {
                    diagnosticHandler.accept(e);
                } catch (Throwable ignored) {}
            }
            return true;
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

        if (exec instanceof Method && ((Method) exec).isBridge()) {
            return hasSameNameAndParamsAnnotatedMethod(declaring, name, params);
        }
        return false;
    }

    private boolean hasSameNameAndParamsAnnotatedMethod(@NotNull Class<?> declaring, String name, Class<?>[] params) {
        for (Method m : declaring.getDeclaredMethods()) {
            if (m.isBridge()) {
                continue;
            }
            if (!m.getName().equals(name)) {
                continue;
            }

            Class<?>[] mParams = m.getParameterTypes();
            if (mParams.length != params.length) {
                continue;
            }

            boolean paramsMatch = true;
            for (int i = 0; i < mParams.length; i++) {
                if (!params[i].isAssignableFrom(mParams[i])) {
                    paramsMatch = false;
                    break;
                }
            }

            if (paramsMatch && m.isAnnotationPresent(SkipCallerScan.class)) {
                return true;
            }
        }
        return false;
    }
}