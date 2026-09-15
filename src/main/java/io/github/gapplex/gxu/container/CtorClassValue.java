package io.github.gapplex.gxu.container;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class CtorClassValue<T> extends ClassValue<ClassRecord<T>> {
    private final Class<T> targetType;

    public CtorClassValue(Class<T> targetType) {
        this.targetType = Objects.requireNonNull(targetType, "targetType");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ClassRecord<T> computeValue(@NotNull Class<?> type) {
        if (!targetType.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "type '" + type.getName() + "' is not a " + targetType.getName());
        }
        Class<? extends T> clazz = (Class<? extends T>) type;

        Constructor<? extends T> ctor;
        boolean canSetSize;
        try {
            ctor = clazz.getConstructor(int.class);
            canSetSize = true;
        } catch (NoSuchMethodException e) {
            try {
                ctor = clazz.getConstructor();
                canSetSize = false;
            } catch (NoSuchMethodException e1){
                throw new GetConstructorFailedException(
                        "Get '" + clazz.getName() + "' constructor failed: "
                                + "no public (int) or () constructor", e1);
            }
        }
        return new ClassRecord<>(ctor, canSetSize);
    }
}
