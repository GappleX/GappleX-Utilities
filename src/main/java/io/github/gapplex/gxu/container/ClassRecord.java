package io.github.gapplex.gxu.container;

import java.lang.reflect.Constructor;

public final class ClassRecord<T>{
    private final Constructor<? extends T> ctor;
    private final boolean canSetSize;
    public ClassRecord(Constructor<? extends T> ctor, boolean canSetSize){
        this.ctor = ctor;
        this.canSetSize = canSetSize;
    }

    public Constructor<? extends T> getCtor(){
        return ctor;
    }

    public boolean canSetSize(){
        return canSetSize;
    }
}
