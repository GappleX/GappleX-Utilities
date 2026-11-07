# GappleX-Utilities (GXU)

GappleX-Utilities 是一个轻量级、零外部依赖（Zero Dependencies）的 Java 底层工具库。它极致压榨性能，提供工业级的安全防护与优雅的 API 设计。

## 环境要求

- **Java 11 及以上**
- 构建：Maven 3.6+
- 开发环境使用 JDK 17 + GraalVM 验证，编译目标为 Java 11
- 整体包体积极小（约 120KB），无任何第三方运行时强依赖

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.gapplex</groupId>
    <artifactId>gxu</artifactId>
    <version>0.8.1</version>
</dependency>
```

### 2. 使用指南

---

## 容器工厂 (container)

提供类似 Guava 但**更具现代化泛型推断**和**容量精算**的 `Maps`、`Lists`、`Sets`、`Deques` 及 `MapModifier`。

### 核心设计：延迟泛型推断
所有 `Builder` 都是无状态且不可变的。你可以实例化一个 `Builder` 模具，无限次地铸造出**完全不同泛型**的集合，完美解决泛型被过早固定死的问题。

```java
// 实例化一次，复用无数次，线程绝对安全
Lists.ListBuilder lb = Lists.listOf(ArrayList::new);
List<String> list1 = lb.of("A", "B");
List<Integer> list2 = lb.of(1, 2, 3);

// 兼容传统面向对象写法（内部使用 ClassValue + getConstructor 缓存反射，性能极佳）
Lists.ListBuilder lb2 = Lists.listOf(ArrayList.class);
```

### 三大核心特性：
1. **精确的容量预分配**：针对 `ArrayList` / `HashMap` / `HashSet` 等，内部自动调用 `CapacityUtils.capacity` 计算真实容量，**彻底消灭无意义的扩容操作**。
2. **支持 Lambda 与 Class 双模式**：传 `Class` 极简易用，传 `IntFunction` 极致灵活（如 `ArrayList::new` 或 `i -> new TreeMap<>()`）。
3. **填补 JDK 空白**：`Deques` 提供了 JDK 原生的 `Deque` 所缺失的 `Deque.of()` 和 `UnmodifiableDeque` 实现。

> **注：容量说明**
> `Maps` 和 `Sets` 的 `IntFunction` 入参是**容量**（内部会做 `CapacityUtils.capacity` 换算）。
> `Lists` 和 `Deques` 的入参是**元素数量**（如 `ArrayList` 的 `int` 构造参数本身即预期元素数量）。

---

## 集合与数组辅助 (CollectionAndArrayUtils)

提供了极其严格的 **Fail-Fast** 校验机制，保证集合操作的**原子性**（绝不产生半添加的脏数据）。

- **`addAllAndNotNull`**：先遍历校验数组中是否包含 `null`，只有全部合法才会批量加入集合。如果遇到 `null` 直接抛出异常，目标集合保持原样。
- **原生数组遍历**：提供针对 `int[]`、`long[]`、`double[]` 等基本类型数组的 `foreach` 方法，接受带索引的函数式接口（如 `IntIntCsm`），避免自动装箱性能损耗。

---

## JSON 解析 (json)

**从 0.8.0 开始，废弃了所有 `org.json` 风格的 `JsonObject` / `JsonArray` / `JsonUtils` 树形模型。**
抛弃了高维护成本、弱类型的四不像中间态，`parse` 包回归最纯粹、极速的解析器，只产出原生 `Map` 和 `List`。

### 极速解析
```java
// 解析为原生的 LinkedHashMap，保持字段顺序
Map<String, Object> rawMap = new JsonParser(json).parseObject();

// 解析为原生的 ArrayList
List<Object> rawList = new JsonParser(json).parseArray();
```

> **迁移提示**：`io.github.gapplex.gxu.json.object` 包下的 `JsonObject`、`JsonArray`、`Json` 以及 JSON 注解均已标记为 `@Deprecated(since="0.8.0")`，将在后续版本中移除。

---

## Unsafe 底层增强 (unsafe)

`unsafe` 包**自身不获取、也不持有** `sun.misc.Unsafe`（防止被当成提权攻击的“白手套”），而是依赖外部传入 `IUnsafe` 接口实现，做到彻底的能力隔离。

### 1. 极速序列化与反序列化 (`Unsafeties`)
利用 `Unsafe` 直接读写内存偏移量，配合 `ClassValue` 字段缓存，吞吐量远超传统反射。

```java
Unsafeties u = Unsafeties.getInstance(myUnsafeImpl);

// 将对象拆解为原始 Map
BeanMap beanMap = u.beanToMap(myObject);

// 将原生 Map 极速反序列化为对象
User user = u.deserialize(User.class, rawMap);
```

### 2. 严格区分普通 Map 与 Bean (`BeanMap` & `BeanMapWrapper`)
为了不在类型转换上产生歧义（例如 JSON 对象到底该转成业务 Bean 还是普通字典），GXU 引入了强语义的 `BeanMap`。
`BeanMapWrapper` 会依据目标类的字段泛型，深度递归扫描原生 `Map`，**精准地为需要转成 Bean 的子结构贴上 `BeanMap` 标签**。

```java
// 1. 解析出纯 Map
Map<String, Object> rawMap = new JsonParser(json).parseObject();

// 2. 根据 User 结构贴标签（普通字典字段原样保留，Bean 字段包装为 BeanMap）
Map<String, Object> taggedMap = BeanMapWrapper.wrap(User.class, rawMap);

// 3. 极速反序列化
User user = Unsafeties.getInstance(unsafe).deserialize(User.class, taggedMap);
```

### 3. 字段与数组内存级直读直写
提供 `get` / `put` 及 `getStatic` / `putStatic` 方法，直接通过 Unsafe 偏移量读写对象的实例字段和静态字段。
同时支持直接对 `int[]`、`long[]`、`Object[]` 等原生数组进行 `getArrayElement` / `putArrayElement` 操作，完全绕过边界反射，达到极致性能。

---

## 反射与调用者安全 (reflect)

`Fields` 提供了纯反射读写。`Callers` 则提供了**防伪造、防反射篡改的栈帧扫描能力**。

### Callers 调用链安全防御
传统的 `StackWalker` 经常被攻击者在自己的恶意方法上加个注解就伪造了调用链。GXU 的 `Callers` 引入了多重校验：

1. **白名单机制 (`trusted`)**：只有被显式加入白名单的类，其带有 `@SkipCallerScan` 注解的方法才会被真正跳过。
2. **纯不可变性**：内部 `trusted` 集合使用 `Set.copyOf` 深度冻结，攻击者就算反射拿到了也无法往里塞类。
3. **模块系统隔离**：库自带 `module-info.class` 且**无 `opens` 声明**。外部若尝试 `setAccessible(true)` 篡改，JVM 将直接抛出异常拦截。
4. **Fail-Closed 异常兜底**：当反射检查出现异常时，宁可直接暴露内部帧，也绝不放过任何可能的隐藏恶意调用。

```java
// 只有框架类 (MyFrameworkClass) 内部的 @SkipCallerScan 才会生效
Callers callers = new Callers(MyFrameworkClass.class);
boolean isSafe = callers.isCalledFrom(MyFrameworkClass.class);
```

---

## 其他工具

- **`MapModifier`**：优雅的链式 Map 组装器，支持 `putIfNotNull`，一键生成可变或不可变副本。
- **`CapacityUtils`**：开放 `capacity` 与 `inverseCapacity`，帮助用户自定义集合时也能做到完美的容量预分配。
- **`Maths`**：提供 `toShortExact`、`toByteExact` 和 `toLongExact`。在各种数值格式转换（如 JSON 数字溢出、BigDecimal 截断）中，严格检测边界，防止“静默溢出”。

---

## 构建

```bash
git clone https://github.com/GappleX/GappleX-Utilities.git
cd GappleX-Utilities
mvn clean install
```

## 许可证

[Apache License 2.0](LICENSE)

---

### 升级说明 (0.2.0 -> 0.8.0 核心破坏性变更)

1. **彻底删除** `JsonUtils`、`JsonObject`、`JsonArray` 以及对应的注解映射（迁移至 `JsonParser` 纯解析）。
2. **彻底分离职责**：JSON 解析器只负责出 `Map/List`；`Unsafeties` 和 `BeanMapWrapper` 负责把 `Map` 组装成强类型的 Java Bean。
3. 如果你在旧代码里使用了 `Var` 和 `CloseException`，请注意它们已在 0.8.0 被标记为废弃（由于并发竞态和异常机制存在设计缺陷，不推荐生产使用）。