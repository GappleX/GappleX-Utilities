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

public class Lists {
    @SuppressWarnings("rawtypes")
    private static final ReflectUtils<List> reflectUtils = new ReflectUtils<>(new CtorClassValue<>(List.class));

    private Lists(){}

    @SafeVarargs
    public static <T> List<T> of(T @NotNull ... args){
        Objects.requireNonNull(args, "args");
        if (args.length == 0){
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(CollectionAndArrayUtils.addAllAndNotNull(new ArrayList<>(args.length), args));
    }

    @Contract(value = "_, _ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(IntFunction<List> create, boolean immutable){
        return new ListBuilder(Objects.requireNonNull(create, "create"), immutable);
    }

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(IntFunction<List> create){
        return listOf(Objects.requireNonNull(create, "create"), false);
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(Class<? extends List> create, boolean immutable){
        return new ListBuilder(reflectUtils.createReflectFunc(Objects.requireNonNull(create, "create")), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(Class<? extends List> create){
        return listOf(Objects.requireNonNull(create, "create"), false);
    }

    public static final class ListBuilder {
        @SuppressWarnings("rawtypes")
        private final IntFunction<List> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        private ListBuilder(IntFunction<List> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <T> List<T> of(T @NotNull ... args) {
            return finish(CollectionAndArrayUtils.addAllAndNotNull(newListInstance(Objects.requireNonNull(args, "args").length), args));
        }

        @SuppressWarnings("unchecked")
        private <T> @NotNull List<T> newListInstance(int expectedSize) {
            List<T> list = (List<T>) create.apply(expectedSize);
            if (list == null) {
                throw new CreateInstanceFailedException(
                        "create returned null for expectedSize=" + expectedSize);
            }
            return list;
        }

        private <T> List<T> finish(List<T> list){
            return immutable ? Collections.unmodifiableList(list) : list;
        }
    }
}
