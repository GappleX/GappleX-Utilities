# GappleX-Utilities

## 环境要求

- Java 8 及以上
- 构建：Maven 3.6+
- 开发环境使用 JDK 17 + GraalVM 验证，编译目标为 Java 8

## 快速开始

### 1.添加依赖

```xml
<dependency>
    <groupId>io.github.gapplex</groupId>
    <artifactId>gxu</artifactId>
    <version>0.2.0</version>
</dependency>
```

### 2.使用

GaplleX-Utilities提供了类似Guava的工具类，如`Maps` `Lists` `Sets` `Deques`，以及 `MapModifier`。

#### Maps

1. **of**:

   `Maps`中包含了类似Java9+的`Map.of`。它们的用法相同，但是`Maps.of`使用`LinkedHashMap`，相较于`Map.of`，`Maps.of`是有序的。
2. **mapOf**：

   `Maps`提供了针对特殊`Map`类型的`of`.`mapOf`有两个重载，`Class<? extends Map>` 和 `IntFunction<Map>`。前者适用于动态类型的`Map`创建，但是局限性较大，只支持反射创建的`(int) or ()`构造。后者则提供了更大的灵活性，您可以利用这一点做到`EnumMap`或`TreeMap`的入参。

    `mapOf`返回一个`Maps.MapBuilder`,您可以使用其内部的`of`或`ofEntries`创建`Map`.它是可复用的。您可以在`mapOf`方法的第二个参数输入`boolean`控制是否不可变，`true`返回不可变，单参数的`mapOf`返回可变（等价于第二个参数传`false`）

   >注：`IntFunction<Map>`的`Int`输入是容量而非元素数量。针对特殊`Map`,您可以使用`CapacityUtils`中的`inverseCapacity`来逆向`IntFunction`的输入为预期的元素数量
3. **ofEntries**:

    `Maps`提供了ofEntries用于创建无数对的不可变Map.它接受`Map.Entry<? extends K, ? extends V> ...`。您可以使用`Maps.entry`快速创建`Map.Entry`。

   ```java
   Map<String, Integer> map = Maps.ofEntries(
       Maps.entry("a", 1),
       Maps.entry("b", 2),
       Maps.entry("c", 3)
   );
   ```

   重复 key 或 null key/value 会抛 `IllegalArgumentException` / `NullPointerException`。

4. **newHashMap / newLinkedHashMap**：

   按预期元素数量创建可变 `HashMap` / `LinkedHashMap`，内部自动做 `CapacityUtils.capacity` 转换，避免扩容。

   ```java
   Map<String, Integer> m = Maps.newHashMap(16);
   ```

5. **unmodifiableMap**：

   对已有 `Map` 包一层只读视图，等价于 `Collections.unmodifiableMap`，但会拒绝 null 参数。

6. **modifierToAnyMap**：

   配合 `MapModifier` 使用，把链式构造的结果传给任意 `Map` 工厂函数。详见 MapModifier 一节。

### Lists

`Lists` 对应 `Maps` 的用法，提供不可变列表创建和可选的反射/函数式 builder。

1. **of**：

   ```java
   List<String> list = Lists.of("a", "b", "c");
   ```

   返回 `Collections.unmodifiableList` 包装的 `ArrayList`。空参数时返回 `Collections.emptyList()`。

2. **listOf**：

   与 `Maps.mapOf` 同构，接受 `Class<? extends List>` 或 `IntFunction<List>`，返回可复用的 `ListBuilder`。第二个 `boolean` 参数控制是否不可变，单参数版本默认可变。

   ```java
   List<String> list = Lists.listOf(ArrayList.class).of("a", "b");
   List<String> immutable = Lists.listOf(LinkedList.class, true).of("x", "y");
   ```

   > 注：`IntFunction<List>` 的 `Int` 输入是容量。`ArrayList` 的 `int` 构造参数本身就是"预期元素数量"，所以直接传入即可，无需 `CapacityUtils` 转换。

### Sets

和 `Lists` 对称，额外提供 `newHashSet` / `newLinkedHashSet` 快捷方法。

1. **of**：

   ```java
   Set<String> set = Sets.of("a", "b", "c");
   ```

   返回 `Collections.unmodifiableSet` 包装的 `LinkedHashSet`，保持插入顺序。空参数时返回 `Collections.emptySet()`。

2. **setOf**：

   与 `Maps.mapOf` / `Lists.listOf` 同构，接受 `Class<? extends Set>` 或 `IntFunction<Set>`，返回可复用的 `SetBuilder`。

3. **newHashSet / newLinkedHashSet**：

   ```java
   Set<String> a = Sets.newHashSet(16);
   Set<String> b = Sets.newLinkedHashSet(16);
   ```

   `SetBuilder` 内部对 `CapacityUtils.capacity` 做了转换，两个快捷方法同样如此。

### Deques

`Deques` 提供 `Deque` 的构建，以及一个自己的 `UnmodifiableDeque` 实现。

1. **of**：

   ```java
   Deque<String> deque = Deques.of("a", "b", "c");
   ```

   返回 `UnmodifiableDeque`，底层是 `ArrayDeque`。空参数时返回共享的 `EMPTY` 单例。

2. **dequeOf**：

   与 `Maps.mapOf` 同构，接受 `Class<? extends Deque>` 或 `IntFunction<Deque>`，返回可复用的 `DequeBuilder`。

   > 注：`IntFunction<Deque>` 的 `Int` 输入是**元素数量**，不是容量。因为 `ArrayDeque` 的 `int` 构造参数本身就是元素数量。这一点和 `Maps` / `Sets` 不同，见下文 CapacityUtils。

3. **unmodifiableDeque**：

   ```java
   Deque<String> view = Deques.unmodifiableDeque(someDeque);
   ```

   把一个可变 `Deque` 包成只读视图。所有写操作抛 `UnsupportedOperationException`，`iterator().remove()` 和 `removeIf` 也会被拦截。

   > 注意：`UnmodifiableDeque.equals` / `hashCode` 直接转发给底层 `Deque`。如果底层是 `ArrayDeque`，它沿用 `Object` 的引用相等语义，因此两个内容相同的 `UnmodifiableDeque` 并不相等。需要内容比较时请自行遍历。

### MapModifier

`MapModifier` 是一个链式的 `Map` 构造器，适合“逐步组装、可读性优先”的场景。

```java
Map<String, Object> map = new MapModifier<String, Object>()
    .put("name", "alice")
    .putIfNotNull("email", maybeEmail)
    .putAll(otherMap)
    .toImmutableMap();
```

| 方法 | 说明 |
| --- | --- |
| `put(k, v)` | 无条件写入 |
| `putIfNotNull(k, v)` | key 或 value 任一为 null 时跳过 |
| `putAll(map)` | 批量写入，null 参数抛 `NullPointerException` |
| `clear()` | 清空 |
| `size()` | 当前大小 |
| `toMap()` | 返回可变 `LinkedHashMap` 副本 |
| `toImmutableMap()` | 返回不可修改副本 |

默认内部使用 `LinkedHashMap`，也可以用 `new MapModifier<>(existingMap)` 包装一个已有的 map，此时 `toMap` 会返回其副本。

配合 `Maps.modifierToAnyMap` 可以把构造结果直接交给任意 `Map` 工厂：

```java
TreeMap<String, Integer> treeMap = Maps.modifierToAnyMap(
    new MapModifier<String, Integer>().put("b", 2).put("a", 1),
    TreeMap::new
);
```

### CapacityUtils

集合工厂内部用它做“元素数量 → 初始容量”的换算，直接暴露给调用方以支持自定义 `IntFunction`。

1. **capacity(int expectedSize)**：

   把预期元素数量换算成 `HashMap` / `HashSet` 这类以负载因子 0.75 扩容的集合的初始容量。

   ```java
   CapacityUtils.capacity(0);   // 1
   CapacityUtils.capacity(3);   // 4
   CapacityUtils.capacity(16);  // 21
   ```

   负数抛 `IllegalArgumentException`。

2. **inverseCapacity(int capacity)**：

   `capacity` 的逆运算：从初始容量反推元素数量。用于把 `Maps.mapOf(IntFunction<Map>)` 的入参（容量）还原为业务侧更直观的元素数量。

   ```java
   CapacityUtils.inverseCapacity(21);  // 16
   CapacityUtils.inverseCapacity(4);   // 3
   ```

   如果传入的容量不是 `capacity` 的合法输出值，抛 `IllegalArgumentException`。

> 注意：`Maps` 和 `Sets` 的 `IntFunction` 入参已经是容量（内部会调 `CapacityUtils.capacity`），而 `Lists` 和 `Deques` 的入参是元素数量（不做转换）。需要跨类型统一时，用 `inverseCapacity` 显式还原。

## 构建

```bash
git clone https://github.com/GappleX/GappleX-Utilities.git
cd GappleX-Utilities
mvn clean install
```

## 许可证

[Apache License 2.0](LICENSE)