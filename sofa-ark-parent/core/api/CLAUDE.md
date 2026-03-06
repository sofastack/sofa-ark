# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-api`
**Package**: `com.alipay.sofa.ark.api`

This module provides the public API for SOFAArk operations. It is the main entry point for external code to interact with the Ark container runtime.

## Purpose

- Public API for managing Ark business modules (Biz)
- Configuration management through `ArkConfigs`
- Response models for API operations

## Key Classes

### `ArkClient`
Main API for runtime operations on business modules:
- `installBiz(File bizFile)` - Install a new business module
- `uninstallBiz(String bizName, String bizVersion)` - Remove a business module
- `switchBiz(String bizName, String bizVersion)` - Activate a specific biz version
- `checkBiz()` - Query installed business modules
- `installPlugin(PluginOperation)` - Install a plugin dynamically
- `invocationReplay(String version, Replay replay)` - Invoke code with specific biz version context

### `ArkConfigs`
Configuration management:
- `getStringValue(String key)` - Get configuration value
- `setSystemProperty(String key, String value)` - Set system properties

### `ClientResponse`
Response wrapper for API operations with status code and message.

### `ResponseCode`
Enum defining response codes: `SUCCESS`, `FAILED`, `REPEAT_BIZ`, `NOT_FOUND_BIZ`, `ILLEGAL_STATE_BIZ`

## Dependencies

- `sofa-ark-spi` - Service Provider Interfaces
- `sofa-ark-common` - Common utilities

## Used By

- `sofa-ark-container` - Implements the services behind this API
- `config-ark-plugin` - Uses API for dynamic module management
- Application code interacting with Ark runtime