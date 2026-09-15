package io.github.gapplex.gxu.container;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.IntFunction;

public class ReflectUtils<T> {
    private final CtorClassValue<T> ctors;
    public ReflectUtils(CtorClassValue<T> ctors){
        this.ctors = ctors;
    }

    public IntFunction<T> createReflectFunc(Class<? extends T> clazz){
        Objects.requireNonNull(clazz, "clazz");

        ClassRecord<T> classRecord;
        try {
            classRecord = ctors.get(clazz);
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof RuntimeException ) {
                throw (RuntimeException) cause;
            }
            throw new GetConstructorFailedException(
                    "Get '" + clazz.getName() + "' constructor failed", cause);
        }
        return expectedSize -> {
            try {
                if (classRecord.canSetSize()){
                    return classRecord.getCtor().newInstance(expectedSize);
                } else {
                    return classRecord.getCtor().newInstance();
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String detail = cause != null ? cause.getMessage() : null;
                String message = "Create '" + clazz.getName() + "' instance failed"
                        + (detail != null ? ", cause:" + detail : "");
                throw new CreateInstanceFailedException(message, cause != null ? cause : e);
            } catch (Exception e){
                String detail = e.getMessage();
                String message = "Create '" + clazz.getName() + "' instance failed"
                        + (detail != null ? ", cause:" + detail : "");
                throw new CreateInstanceFailedException(message, e);
            }
        };
    }
}
