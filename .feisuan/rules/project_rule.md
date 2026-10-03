
# 开发规范指南 (NeoForge Minecraft Mod)

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、基础环境信息

- **工作目录**：`D:\Games\Minecraft\My Mods\Winter falls\winter_falls-mc1.21.1-neoforge`
- **操作系统**：Windows 10
- **代码作者**：1
- **当前时间**：2026-05-25

## 二、技术栈与构建工具

- **主框架**：NeoForge (Minecraft 1.21.1)
- **语言版本**：Java 21 (JDK 21.0.2)
- **构建工具**：Gradle (使用 ModDev 插件 `net.neoforged.moddev`)
- **核心依赖**：
  - `curse.maven:tide` (前置依赖)
  - `curse.maven:cloth-config` (前置依赖)
  - `curse.maven:jei` (前置依赖)
  - `curse.maven:jade` (本地运行时依赖)
  - `curse.maven:kubejs` (本地运行时依赖)
  - `curse.maven:rhino` (本地运行时依赖)
  - `mixin` (用于代码注入)

## 三、目录结构规范

项目必须严格遵循以下目录树结构，包名统一基于 `com.chinaex123.winter_falls`：

```text
winter_falls-mc1.21.1-neoforge
├── .github
│   └── workflows
├── gradle
│   └── wrapper
└── src
    └── main
        ├── java
        │   └── com
        │       └── chinaex123
        │           └── winter_falls
        │               ├── client          # 客户端特定逻辑 (渲染、GUI等)
        │               ├── config          # 配置文件管理
        │               ├── event           # 事件监听与注册
        │               ├── mixin           # Mixin 注入逻辑
        │               └── util            # 通用工具类
        ├── resources
        │   └── assets
        │       └── winter_falls
        │           └── lang                # 语言文件
        └── templates
            └── META-INF                    # Mod 元数据模板
```

### 包职责说明

| 包路径 | 职责说明 | 开发约束 |
|--------|----------|----------|
| **client** | 处理仅客户端运行的逻辑，如模型加载、粒子效果、HUD渲染 | 禁止包含服务端逻辑；需通过 `DistExecutor` 或条件加载确保安全性 |
| **config** | 管理 `Config` 类及配置文件读取/写入 | 使用 Cloth Config 库进行配置界面展示 |
| **event** | 注册 NeoForge 事件监听器 (`@Mod.EventBusSubscriber`) | 避免在事件回调中进行耗时操作，防止阻塞主线程 |
| **mixin** | 混合注入代码，修改原版 Minecraft 逻辑 | 保持注入点最小化；避免破坏游戏稳定性；需符合 NeoForge 安全规范 |
| **util** | 静态工具方法、辅助类 | 保持无状态；避免过度耦合 |

## 四、安全与性能规范

### 输入与数据校验

- **配置校验**：使用 `@Config` 注解定义配置项，并设置合理的默认值与范围限制。
- **资源校验**：加载 Assets (JSON/模型/纹理) 时，必须处理 `FileNotFoundException` 或 `IOException`，避免模组崩溃。

### 事务与并发

- **线程安全**：
  - 所有涉及 Minecraft 主线程（渲染线程）的操作必须在主线程执行。
  - 后台线程操作数据后，如需更新 UI 或世界状态，需使用 `Minecraft.getInstance().execute(() -> ...)` 或 NeoForge 提供的线程调度工具。
- **事件处理**：事件监听器应尽量轻量，避免阻塞事件总线。

### 依赖管理

- **前置依赖**：在 `mods.toml` 中正确声明 `required-dependency` 或 `optional-dependency`。
- **版本兼容**：明确指定依赖的版本范围，避免版本冲突。

## 五、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `WinterFallsClient`   |
| 方法/变量  | lowerCamelCase       | `registerParticles()` |
| 常量       | UPPER_SNAKE_CASE     | `MAX_PARTICLE_COUNT`  |
| 包名       | 全小写               | `com.chinaex123.winter_falls` |

### 注释规范

- **语言要求**：所有类、方法、字段必须添加 **中文** 注释。
- **Javadoc**：公共 API 和方法需包含标准的 Javadoc 注释，说明参数、返回值及异常。
- **Mixin 注释**：在 `@Mixin` 和 `@Inject` 等注解旁简要说明注入目的。

### 实体与资源命名

- **Mod ID**：统一使用 `winter_falls`。
- **资源路径**：遵循 Minecraft 资源路径规范，如 `winter_falls:item/my_item`。
- **语言文件**：键名使用小写加下划线，如 `item.winter_falls.my_item.name`。

## 六、扩展性与日志规范

### 接口优先原则

- 虽然 Minecraft 模组开发多为具体类实现，但在设计复杂系统（如自定义注册表、事件总线）时，建议定义接口以增强可测试性和扩展性。

### 日志记录

- **使用 Mod 日志**：优先使用 NeoForge 提供的 `FMLLog` 或 `MinecraftForge.EVENT_BUS` 相关的日志机制。
- **避免 System.out**：禁止使用 `System.out.println` 或 `e.printStackTrace()`，应使用 `log.warn()` 或 `log.error()`。
- **日志级别**：
  - `DEBUG`：用于开发调试，发布版本中应关闭或减少输出。
  - `WARN`：用于非致命错误，如资源加载失败。
  - `ERROR`：用于致命错误，如模组初始化失败。

## 七、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂，避免过度设计             |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如路径遍历、注入攻击等   |
| **Minecraft** | 遵循 Minecraft 模组开发最佳实践，确保兼容性与稳定性 |
