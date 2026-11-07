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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldsTest {

    @Test
    void getAndSetInstanceField() {
        Bean bean = new Bean("a");
        assertEquals("a", Fields.getField(bean, "name", String.class));
        Fields.setField(bean, "name", "b");
        assertEquals("b", bean.name());
        assertEquals("b", Fields.getField(bean, "name"));
    }

    @Test
    void getAndSetInheritedPrivateField() {
        Child child = new Child();
        assertEquals(1, Fields.getField(child, "hidden", int.class));
        Fields.setField(child, "hidden", 9);
        assertEquals(9, Fields.getField(child, "hidden", Integer.class));
    }

    @Test
    void getAndSetStaticField() {
        int original = StaticHolder.counter;
        try {
            assertEquals(original, Fields.getStaticField(StaticHolder.class, "counter", int.class));
            Fields.setStaticField(StaticHolder.class, "counter", original + 3);
            assertEquals(original + 3, StaticHolder.counter);
            assertEquals(original + 3, Fields.getStaticField(StaticHolder.class, "counter"));
        } finally {
            StaticHolder.counter = original;
        }
    }

    @Test
    void findFieldWalksInterfaces() {
        Field field = Fields.findField(Impl.class, "FLAG");
        assertEquals("FLAG", field.getName());
        assertEquals(true, Fields.getStaticField(Impl.class, "FLAG", boolean.class));
    }

    @Test
    void rejectsWrongKindAndType() {
        Bean bean = new Bean("x");
        ReflectException missing = assertThrows(ReflectException.class,
                () -> Fields.getField(bean, "nope"));
        assertEquals(ReflectException.Reason.FIELD_NOT_FOUND, missing.getReason());

        ReflectException staticOnInstance = assertThrows(ReflectException.class,
                () -> Fields.getField(bean, "SHARED"));
        assertEquals(ReflectException.Reason.STATIC_FIELD_NOT_SUPPORTED, staticOnInstance.getReason());

        ReflectException instanceOnStatic = assertThrows(ReflectException.class,
                () -> Fields.getStaticField(Bean.class, "name"));
        assertEquals(ReflectException.Reason.INSTANCE_FIELD_NOT_SUPPORTED, instanceOnStatic.getReason());

        ReflectException type = assertThrows(ReflectException.class,
                () -> Fields.getField(bean, "name", Integer.class));
        assertEquals(ReflectException.Reason.TYPE_MISMATCH, type.getReason());

        ReflectException primitiveNull = assertThrows(ReflectException.class,
                () -> Fields.setField(new Child(), "hidden", null));
        assertEquals(ReflectException.Reason.NULL_FOR_PRIMITIVE, primitiveNull.getReason());
    }

    @Test
    void findFieldReturnsNullWhenAbsent() {
        assertNull(Fields.findField(Bean.class, "missing"));
        Field hidden = Fields.findField(Child.class, "hidden");
        assertEquals("hidden", hidden.getName());
        assertEquals(Parent.class, hidden.getDeclaringClass());
    }

    static class Bean {
        private static final String SHARED = "s";
        private String name;

        Bean(String name) {
            this.name = name;
        }

        String name() {
            return name;
        }
    }

    static class Parent {
        private int hidden = 1;
    }

    static class Child extends Parent {
    }

    static class StaticHolder {
        private static int counter = 7;
    }

    interface Marker {
        boolean FLAG = true;
    }

    static class Impl implements Marker {
    }
}
