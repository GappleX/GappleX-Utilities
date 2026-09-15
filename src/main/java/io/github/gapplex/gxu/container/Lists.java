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
        if (args.length == 0){
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>(args.length);
        Collections.addAll(list, args);
        return Collections.unmodifiableList(list);
    }

    @Contract(value = "_, _ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(IntFunction<List> create, boolean immutable){
        return new ListBuilder(create, immutable);
    }

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(IntFunction<List> create){
        return listOf(create, false);
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(Class<? extends List> create, boolean immutable){
        return new ListBuilder(reflectUtils.createReflectFunc(create), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull ListBuilder listOf(Class<? extends List> create){
        return listOf(create, false);
    }

    public static final class ListBuilder {
        @SuppressWarnings("rawtypes")
        private final IntFunction<List> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        public ListBuilder(IntFunction<List> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <T> List<T> of(T @NotNull ... args) {
            List<T> list = newListInstance(args.length);
            list.addAll(Arrays.asList(args));
            return finish(list);
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
