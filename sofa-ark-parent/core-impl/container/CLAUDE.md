# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-container`
**Package**: `com.alipay.sofa.ark.container`

This is the core implementation of the Ark Container - the runtime that manages the entire SOFAArk lifecycle including plugins and business modules.

## Purpose

- Ark container startup and shutdown
- Manage plugins and business modules lifecycle
- Provide classloader isolation
- Service registry and dependency injection
- Telnet command server for runtime management

## Key Classes

### Container Entry
- `ArkContainer` - Main entry point, manages container lifecycle
  - `main(String[] args)` - Static entry for JAR execution
  - `start()` - Start the container
  - `stop()` - Stop the container

### Pipeline Stages (`pipeline/`)
Startup stages executed in order:
1. `HandleArchiveStage` - Parse archives
2. `RegisterServiceStage` - Register core services
3. `ExtensionLoaderStage` - Load SPI extensions
4. `DeployPluginStage` - Start plugins
5. `DeployBizStage` - Start business modules
6. `FinishStartupStage` - Complete startup

StandardPipeline.java:47-60 defines the startup sequence.

### Service Implementations (`service/`)
- `ArkServiceContainer` - Guice-based service container
- `biz.BizManagerServiceImpl` - Biz lifecycle management
- `biz.BizFactoryServiceImpl` - Create Biz instances
- `plugin.PluginManagerServiceImpl` - Plugin management
- `plugin.PluginFactoryServiceImpl` - Create Plugin instances
- `classloader.ClassLoaderServiceImpl` - Classloader management
- `classloader.BizClassLoader` - Business module classloader
- `classloader.PluginClassLoader` - Plugin classloader
- `injection.InjectionServiceImpl` - Dependency injection
- `event.EventAdminServiceImpl` - Event dispatching

### Session/Command (`session/`)
- `StandardTelnetServerImpl` - Telnet server for runtime commands
- `NettyTelnetServer` - Netty-based telnet server
- Command handlers for: biz, plugin, info queries

## ClassLoader Architecture

```
Bootstrap ClassLoader
       ↓
Ark Container ClassLoader (loads sofa-ark-all)
       ↓
PluginClassLoader (bidirectional delegation between plugins)
       ↓
BizClassLoader (delegates to plugins, isolated from other biz)
```

## Dependencies

- `sofa-ark-spi` - Service interfaces
- `sofa-ark-api` - API layer
- `sofa-ark-archive` - Archive loading
- `sofa-ark-common` - Utilities
- `guice` - Dependency injection

## Used By

- `sofa-ark-all` - Aggregates this module
- Application at runtime (via `java -jar`)