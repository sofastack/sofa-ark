# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-spi`
**Package**: `com.alipay.sofa.ark.spi`

This module defines the Service Provider Interfaces (SPI) for SOFAArk. It contains all the core interfaces, models, and extension points that allow customization and integration with the Ark container.

## Purpose

- Define core interfaces for Ark container services
- Define data models for Biz, Plugin, and Archive
- Provide extension points for custom implementations
- Define event system for lifecycle notifications

## Key Packages

### `spi.model`
Core data models:
- `Biz` / `BizInfo` / `BizState` / `BizConfig` / `BizOperation` - Business module model
- `Plugin` / `PluginContext` / `PluginConfig` / `PluginOperation` - Plugin model

### `spi.service`
Service interfaces:
- `biz.BizManagerService` - Manage business modules lifecycle
- `biz.BizFactoryService` - Create business module instances
- `plugin.PluginManagerService` - Manage plugins
- `plugin.PluginFactoryService` - Create plugin instances
- `classloader.ClassLoaderService` - Manage classloaders
- `event.EventAdminService` - Event publishing/subscribing
- `injection.InjectionService` - Dependency injection
- `extension.ArkServiceLoader` - SPI extension loading

### `spi.archive`
Archive interfaces:
- `Archive` - Base archive interface
- `BizArchive` - Business module archive
- `PluginArchive` - Plugin archive
- `ExecutableArchive` - Executable Ark archive
- `ContainerArchive` - Container archive

### `spi.pipeline`
Startup pipeline interfaces:
- `Pipeline` - Pipeline that processes startup stages
- `PipelineStage` - Individual stage in the startup process
- `PipelineContext` - Context passed between stages

### `spi.event`
Event types for lifecycle notifications:
- `biz.*` - Business module events (Before/After Install, Uninstall, Start, Stop, Switch)
- `plugin.*` - Plugin events

### `spi.constant.Constants`
Constant definitions used throughout SOFAArk.

## Extension Points

Implement these interfaces to extend SOFAArk:
1. `PipelineStage` - Add custom startup stages
2. `PluginActivator` - Custom plugin activation logic
3. `AddBizToStaticDeployHook` - Hook for adding biz during static deployment
4. `EmbeddedServerService` - Custom embedded server implementation

## Dependencies

- `sofa-ark-exception` - Exception definitions

## Used By

- `sofa-ark-api` - API layer sits on top of SPI
- `sofa-ark-container` - Implements all SPI services
- `sofa-ark-archive` - Implements archive interfaces
- All other SOFAArk modules depend on SPI