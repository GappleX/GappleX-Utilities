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
        if (args.length == 0){
            return Collections.emptySet();
        }

        Set<T> set = newLinkedHashSet(args.length);
        Collections.addAll(set, args);
        return Collections.unmodifiableSet(set);
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
        return new SetBuilder(create, immutable);
    }

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(IntFunction<Set> create){
        return setOf(create, false);
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(Class<? extends Set> create, boolean immutable){
        return new SetBuilder(reflectUtils.createReflectFunc(create), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull SetBuilder setOf(Class<? extends Set> create){
        return setOf(create, false);
    }

    public static final class SetBuilder {
        @SuppressWarnings("rawtypes")
        private final IntFunction<Set> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        public SetBuilder(IntFunction<Set> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <T> Set<T> of(T @NotNull ... args) {
            Set<T> set = newSetInstance(args.length);
            Collections.addAll(set, args);
            return finish(set);
        }

        @SuppressWarnings("unchecked")
        private <T> @NotNull Set<T> newSetInstance(int expectedSize) {
            Set<T> set = (Set<T>) create.apply(CapacityUtils.capacity(expectedSize));
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
