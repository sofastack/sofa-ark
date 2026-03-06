# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 0. 交互协议
- **交互语言**：工具与模型交互强制使用 **English**；用户输出强制使用 **中文**
- **多轮对话**：如果工具返回的有可持续对话字段（如 `SESSION_ID`），记录该字段，并在后续调用中**强制思考**是否继续对话。Codex/Gemini 有时会因工具调用中断会话，若未得到需要的回复，应继续对话
- **沙箱安全**：除非用户明确允许，严禁 Codex/Gemini 对文件系统进行写操作。所有代码获取必须请求 `unified diff patch` 格式
- **渐进迭代**：多轮沟通和小步提交，持续改进
- 尊重事实比尊重我更为重要。如果我犯错，请毫不犹豫地指正我

## 1. 代码主权与判断依据
- **代码主权**：外部模型生成的代码仅作为逻辑参考（Prototype），最终交付代码**必须经过分析思考和重构**，确保无冗余、企业生产级标准
- **判断依据**：始终以项目代码的搜索结果作为判断依据，严禁使用一般知识进行猜测，允许向用户表明不确定性。调用非内置库时，必须先用工具搜索外部知识，以搜索结果为依据编码
- **深度分析**：用第一性原理分析问题，深入思考本质
- **仅做针对性改动**：严禁影响项目现有的其他功能
- **持续进化**：用户在交互中明确纠正的问题和注意事项需同步追加在本文件的**项目专属约束**中

## 2. 工具使用规范

执行任务前优先查找**内置工具、MCP 工具和 Skills** 中有没有可用的，判断Skills选择使用 using-superpowers 技能

- 先检索再编码，避免重复造轮子
- 文件修改：使用替换工具做精准替换
- 并行操作：无依赖步骤应使用 subagent 并行执行

### 代码检索黄金规则
按场景选择最优工具：

**第一层：语义搜索（ck）— "我不知道叫什么，但我知道它做什么"**
- 适用：模糊概念搜索、不确定关键词时的探索
- 用法：`ck --sem "concept"` / `ck --hybrid "query"` / `ck --lex "keyword"`
- 详细参考 [ck命令说明](ck-semantic-search.md)

**第二层：符号搜索（Serena LSP）— "我知道符号名，要精确定位和操作"**
- 适用：查找符号定义/引用、理解文件结构、符号级编辑和重命名
- 核心工具：`find_symbol`（定位）、`find_referencing_symbols`（引用链）、`get_symbols_overview`（结构）
- 编辑工具：`replace_symbol_body`、`insert_after_symbol`、`rename_symbol`
- 优势：Token 高效（按需加载符号体，不必读整文件）

**第三层：文本搜索（Grep/Glob）— "我知道确切的文本模式"**
- 适用：精确关键词/正则匹配、文件名模式查找、非代码文件搜索（yaml/json/env等）
- 核心工具：`Grep`（内容正则）、`Glob`（文件名匹配）

**第四层：直接读取（Read/WebFetch）— "我知道确切的位置，要看完整内容"**
- 适用：已知文件路径读取完整内容、查看图片/PDF、获取网页信息
- 核心工具：`Read`（本地文件/图片/PDF/Notebook）、`WebFetch`（网页内容提取）
- 注意：优先用前三层定位目标，再用 Read 读取；避免盲目读取大文件

### 外部知识获取
遇到代码库以外不熟悉的知识，必须使用工具联网搜索，严禁猜测：
- 通用搜索：`WebSearch` 或 `mcp__exa__web_search_exa`
- 库文档：`mcp__context7__resolve-library-id` → `mcp__context7__get-library-docs`
- 开源项目：优先使用 `mcp__mcp-deepwiki__deepwiki_fetch`，而非通用搜索工具

## 3. 代码风格
- KISS — 能简单就不复杂
- DRY — 零容忍重复，必须复用
- 保护调用链 — 修改函数签名时同步更新所有调用点
- 文件大小：1000 行上限，超出按职责拆分
- 函数长度：100 行上限（不含空行），超出立即提取辅助函数
- Uses `Formatter.xml` for automatic code formatting during build
- Apache 2.0 license header required on all Java files
- Run `mvn clean install` before pushing to ensure formatting is applied

### 完成后清理
- 删除：临时文件、注释掉的废弃代码、未使用的导入、调试日志

### 代码红线
- 除非用户明确说明，禁止破坏或改变现有功能
- 禁止对错误方案妥协
- 切勿将密钥、API 密钥或凭据硬编码到源代码中，使用环境变量

## 4. 其他约束
- 添加新功能时**遵循模块化模式**，准确识别功能归属的分层
- 修改后**测试模块化服务器**，确保所有导入正常工作
- 重要链路必须有清晰的异常处理

## 5. Git 规范
- 不主动提交，除非用户明确要求
- 不主动 push，除非用户明确要求
- Commit 格式：`<type>(<scope>): <description>`
- 提交前：`git diff` 确认改动范围
- 禁止 `--force` 推送到 main/master


## 6. Project Overview

SOFAArk is a lightweight Java-based classloader-isolated framework open-sourced by Ant Financial. It provides:
- **Class isolation**: Solve package dependency conflicts (e.g., using protobuf2 and protobuf3 simultaneously)
- **Dynamic hot deployment**: Install/uninstall business modules at runtime
- **Merged deployment**: Multiple applications can be packaged and run together

## 7. Build Commands

```bash
# Full build (skip tests and javadoc)
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -B -U

# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl sofa-ark-parent/core-impl/container

# Run a single test class
mvn test -Dtest=ArkContainerTest -pl sofa-ark-parent/core-impl/container

# Format check (must run after build with no uncommitted files)
sh ./check_format.sh

# Release build
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -B -U -Prelease
```

## 8. Project Structure

```
sofa-ark/
├── sofa-ark-bom/              # Dependency management (versions)
├── sofa-ark-parent/
│   ├── core/                  # Core interfaces and common code
│   │   ├── api/               # Public API (ArkClient, ArkConfigs)
│   │   ├── spi/               # Service Provider Interfaces
│   │   ├── common/            # Shared utilities
│   │   └── exception/         # Exception definitions
│   ├── core-impl/             # Core implementations
│   │   ├── container/         # Ark Container (runtime management)
│   │   └── archive/           # Archive loading (JAR/directory handling)
│   ├── support/               # Build tools and integrations
│   │   ├── ark-maven-plugin/  # Maven plugin for building Ark packages
│   │   ├── ark-plugin-maven-plugin/  # Maven plugin for building Ark Plugins
│   │   ├── ark-springboot-integration/  # Spring Boot integration
│   │   └── ark-tools/         # Repackaging utilities
│   └── assembly/              # sofa-ark-all aggregated JAR
└── sofa-ark-plugin/           # Built-in plugins (config, web, netty)
```

## 9. Core Architecture

### Three Key Concepts

1. **Ark Container**: Runtime container that manages plugins and business modules. Entry point: `ArkContainer.main()`

2. **Ark Plugin**: Class-isolated plugin units. Loaded by `PluginClassLoader`. Plugins can import/export classes to share or isolate dependencies.

3. **Ark Biz**: Business modules loaded by `BizClassLoader`. Each biz has independent classloader isolation.

### Startup Pipeline

The container executes these stages in order (see `StandardPipeline.java`):

1. `HandleArchiveStage` - Parse and resolve Ark archives
2. `RegisterServiceStage` - Register core services
3. `ExtensionLoaderStage` - Load SPI extensions
4. `DeployPluginStage` - Start all Ark Plugins
5. `DeployBizStage` - Start all Ark Biz modules
6. `FinishStartupStage` - Complete startup

### ClassLoader Hierarchy

```
Bootstrap ClassLoader
       ↓
Ark Container ClassLoader (loads sofa-ark-all)
       ↓
PluginClassLoader (one per plugin, bidirectional delegation between plugins)
       ↓
BizClassLoader (one per biz, can delegate to plugins)
```

### Key APIs

- `ArkClient` - Main API for installing/uninstalling/switching biz modules at runtime
- `ArkConfigs` - Configuration management
- `BizManagerService` - Manage business modules
- `PluginManagerService` - Manage plugins

## 10. Maven Plugins

### sofa-ark-maven-plugin

Builds executable Ark packages with `mvn package`:

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Key configurations:
- `bizName` / `bizVersion` - Module identity
- `excludes` / `excludeGroupIds` - Dependencies to exclude from the package
- `denyImportPackages` - Packages the biz cannot import from plugins
- `declaredMode` - Filter dependencies against declared list

### sofa-ark-plugin-maven-plugin

Builds Ark Plugin packages:

```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-plugin-maven-plugin</artifactId>
    <configuration>
        <activator>com.example.MyPluginActivator</activator>
    </configuration>
</plugin>
```

## 11. Versioning

- Current version: 2.3.2
- Three-digit versioning: `major.minor.patch`
  - First digit: Breaking compatibility changes
  - Second digit: New features/enhancements
  - Third digit: Bug fixes

## 12. Running Specific Tests

### Run tests for a specific module
```bash
# Run all tests in a module
mvn test -pl sofa-ark-parent/core-impl/container

# Run all tests in multiple modules
mvn test -pl sofa-ark-parent/core-impl/container,sofa-ark-parent/core/api
```

### Run a single test class
```bash
# Run a specific test class
mvn test -Dtest=ArkContainerTest -pl sofa-ark-parent/core-impl/container

# Run a test class with pattern matching
mvn test -Dtest=*ClassLoaderTest -pl sofa-ark-parent/core-impl/container
```

### Run a single test method
```bash
# Run a specific test method
mvn test -Dtest=ArkContainerTest#testStart -pl sofa-ark-parent/core-impl/container
```

### Run tests with specific groups (TestNG)
```bash
# Run tests belonging to a specific group
mvn test -Dgroups=unit -pl sofa-ark-parent/core-impl/container
```

### Debug tests
```bash
# Run tests with remote debugging (listen on port 5005)
mvn test -Dmaven.surefire.debug -pl sofa-ark-parent/core-impl/container
```

### Skip specific tests
```bash
# Skip a specific test class
mvn test -Dtest=!ArkContainerTest -pl sofa-ark-parent/core-impl/container

# Skip tests matching a pattern
mvn test -Dtest=!*.IntegrationTest -pl sofa-ark-parent/core-impl/container
```

## 13. Troubleshooting Guide

### Build Issues

#### 1. Compilation errors after pulling changes
**Symptoms:** Compilation fails with "cannot find symbol" or similar errors.

**Solutions:**
```bash
# Clean and rebuild
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -B -U
```

#### 2. Code format check fails
**Symptoms:** `check_format.sh` reports formatting issues.

**Solutions:**
```bash
# Ensure build is complete first (formatter runs during build)
mvn clean install -DskipTests

# Then run format check
sh ./check_format.sh

# If still failing, check for uncommitted files
git status
```

#### 3. Dependency resolution failures
**Symptoms:** Maven cannot resolve dependencies.

**Solutions:**
```bash
# Force update of snapshots and releases
mvn clean install -U -DskipTests

# Check local Maven repository for corrupted artifacts
rm -rf ~/.m2/repository/com/alipay/sofa
mvn clean install -DskipTests
```