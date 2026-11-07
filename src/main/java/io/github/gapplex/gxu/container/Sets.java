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
package io.github.gapplex.gxu.container;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntFunction;

public class Sets {
    @SuppressWarnings("rawtypes")
    private static final ReflectUtils<Set> reflectUtils = new ReflectUtils<>(new CtorClassValue<>(Set.class));
    
    private Sets(){}

    @SafeVarargs
    public static <T> Set<T> of(T @NotNull ... args){
        Objects.requireNonNull(args, "args");
        if (args.length == 0){
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(CollectionAndArrayUtils.addAllAndNotNull(newLinkedHashSet(args.length), args));
    }

    @Contract("_ -> new")
    public static <T> @NotNull HashSet<T> newHashSet(int expectedSize){
        return new HashSet<>(CapacityUtils.capacity(expectedSize));
    }

    @Contract("_ -> new")
    public static <T> @NotNull LinkedHashSet<T> newLinkedHashSet(int expectedSize){
        return new LinkedHashSet<>(CapacityUtils.capacity(expectedSize));
    }

    @Contract(value = "_, _ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(IntFunction<Set> create, boolean immutable){
        return new SetBuilder(Objects.requireNonNull(create, "create"), immutable);
    }

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(IntFunction<Set> create){
        return setOf(Objects.requireNonNull(create, "create"), false);
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(Class<? extends Set> create, boolean immutable){
        return new SetBuilder(reflectUtils.createReflectFunc(Objects.requireNonNull(create, "create")), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(Class<? extends Set> create){
        return setOf(Objects.requireNonNull(create, "create"), false);
    }

    public static final class SetBuilder {
        @SuppressWarnings("rawtypes")
        private final IntFunction<Set> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        private SetBuilder(IntFunction<Set> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <T> Set<T> of(T @NotNull ... args) {
            return finish(CollectionAndArrayUtils.addAllAndNotNull(newSetInstance(Objects.requireNonNull(args, "args").length), args));
        }

        @SuppressWarnings("unchecked")
        private <T> @NotNull Set<T> newSetInstance(int expectedSize) {
            Set<T> set = (Set<T>) create.apply(expectedSize);
            if (set == null) {
                throw new CreateInstanceFailedException(
                        "create returned null for expectedSize=" + expectedSize);
            }
            return set;
        }

        private <T> Set<T> finish(Set<T> set){
            return immutable ? Collections.unmodifiableSet(set) : set;
        }
    }
}
