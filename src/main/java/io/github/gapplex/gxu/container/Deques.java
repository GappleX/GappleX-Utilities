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
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class Deques {
    @SuppressWarnings("rawtypes")
    private static final ReflectUtils<Deque> reflectUtils = new ReflectUtils<>(new CtorClassValue<>(Deque.class));
    
    private static final Deque<?> EMPTY = new UnmodifiableDeque<>(new ArrayDeque<>(8));

    private Deques(){}

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <E> Deque<E> of(E @NotNull ... args){
        Objects.requireNonNull(args, "args");
        if (args.length == 0){
            return (Deque<E>) EMPTY;
        }
        return unmodifiableDeque(CollectionAndArrayUtils.addAllAndNotNull(new ArrayDeque<>(args.length), args));
    }

    @Contract(value = "_, _ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull DequeBuilder dequeOf(IntFunction<Deque> create, boolean immutable){
        return new DequeBuilder(Objects.requireNonNull(create, "create"), immutable);
    }

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("rawtypes")
    public static @NotNull DequeBuilder dequeOf(IntFunction<Deque> create){
        return dequeOf(Objects.requireNonNull(create, "create"), false);
    }

    @Contract("_, _ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull DequeBuilder dequeOf(Class<? extends Deque> create, boolean immutable){
        return new DequeBuilder(reflectUtils.createReflectFunc(Objects.requireNonNull(create, "create")), immutable);
    }

    @Contract("_ -> new")
    @SuppressWarnings("rawtypes")
    public static @NotNull DequeBuilder dequeOf(Class<? extends Deque> create){
        return dequeOf(Objects.requireNonNull(create, "create"), false);
    }

    public static final class DequeBuilder {
        @SuppressWarnings("rawtypes")
        private final IntFunction<Deque> create;
        private final boolean immutable;

        @SuppressWarnings("rawtypes")
        private DequeBuilder(IntFunction<Deque> create, boolean immutable) {
            this.create = Objects.requireNonNull(create, "create");
            this.immutable = immutable;
        }

        @SafeVarargs
        public final <T> Deque<T> of(T @NotNull ... args) {
            Objects.requireNonNull(args, "args");
            return finish(CollectionAndArrayUtils.addAllAndNotNull(newDequeInstance(args.length), args));
        }

        @SuppressWarnings("unchecked")
        private <T> @NotNull Deque<T> newDequeInstance(int expectedSize) {
            Deque<T> deque = (Deque<T>) create.apply(expectedSize);
            if (deque == null) {
                throw new CreateInstanceFailedException(
                        "create returned null for expectedSize=" + expectedSize);
            }
            return deque;
        }

        private <T> Deque<T> finish(Deque<T> deque){
            return immutable ? unmodifiableDeque(deque) : deque;
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static <E> @NotNull Deque<E> unmodifiableDeque(Deque<E> deque){
        return new UnmodifiableDeque<>(Objects.requireNonNull(deque, "deque"));
    }

    private static final class UnmodifiableDeque<E> implements Deque<E> {
        private static final UnsupportedOperationException UOE = new UnsupportedOperationException("Unmodifiable Deque");
        private final Deque<E> d;

        private UnmodifiableDeque(Deque<E> deque){
            d = Objects.requireNonNull(deque, "deque");
        }

        @Override
        public void addFirst(E e) {
            throw UOE;
        }

        @Override
        public void addLast(E e) {
            throw UOE;
        }

        @Override
        public boolean offerFirst(E e) {
            throw UOE;
        }

        @Override
        public boolean offerLast(E e) {
            throw UOE;
        }

        @Override
        public E removeFirst() {
            throw UOE;
        }

        @Override
        public E removeLast() {
            throw UOE;
        }

        @Override
        public E pollFirst() {
            throw UOE;
        }

        @Override
        public E pollLast() {
            throw UOE;
        }

        @Override
        public E getFirst() {
            return d.getFirst();
        }

        @Override
        public E getLast() {
            return d.getLast();
        }

        @Override
        public E peekFirst() {
            return d.peekFirst();
        }

        @Override
        public E peekLast() {
            return d.peekLast();
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            throw UOE;
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            throw UOE;
        }

        @Override
        public boolean add(E e) {
            throw UOE;
        }

        @Override
        public boolean offer(E e) {
            throw UOE;
        }

        @Override
        public E remove() {
            throw UOE;
        }

        @Override
        public E poll() {
            throw UOE;
        }

        @Override
        public E element() {
            return d.element();
        }

        @Override
        public E peek() {
            return d.peek();
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            throw UOE;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            throw UOE;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw UOE;
        }

        @Override
        public void clear() {
            throw UOE;
        }

        @Override
        public void push(E e) {
            throw UOE;
        }

        @Override
        public E pop() {
            throw UOE;
        }

        @Override
        public boolean remove(Object o) {
            throw UOE;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return d.containsAll(c);
        }

        @Override
        public boolean contains(Object o) {
            return d.contains(o);
        }

        @Override
        public int size() {
            return d.size();
        }

        @Override
        public boolean isEmpty() {
            return d.isEmpty();
        }

        @Contract(" -> new")
        @Override
        public @NotNull Iterator<E> iterator() {
            return unmodifiableIterator(d.iterator());
        }

        @Contract(pure = true)
        @Override
        public Object @NotNull [] toArray() {
            return d.toArray();
        }

        @Override
        public <T> T @NotNull [] toArray(T @NotNull [] a) {
            return d.toArray(a);
        }

        @Contract(" -> new")
        @Override
        public @NotNull Iterator<E> descendingIterator() {
            return unmodifiableIterator(d.descendingIterator());
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super E> filter) {
            throw UOE;
        }

        @Override
        public boolean equals(Object o) {
            return d.equals(o);
        }

        @Override
        public int hashCode() {
            return d.hashCode();
        }

        @Contract(" -> new")
        @Override
        public @NotNull Spliterator<E> spliterator() {
            return Spliterators.spliterator(
                    unmodifiableIterator(d.iterator()),
                    d.size(),
                    d.spliterator().characteristics()
            );
        }

        @Override
        public String toString(){
            return d.toString();
        }
    }

    @Contract(value = "_ -> new", pure = true)
    private static <E> @NotNull Iterator<E> unmodifiableIterator(Iterator<E> it) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw UnmodifiableDeque.UOE;
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                it.forEachRemaining(action);
            }
        };
    }
}
