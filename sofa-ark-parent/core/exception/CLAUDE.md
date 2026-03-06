# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-exception`
**Package**: `com.alipay.sofa.ark.exception`

This module defines all exception types used in the SOFAArk project.

## Purpose

- Centralized exception definitions
- Consistent error handling across the project

## Key Classes

### `ArkException`
Base exception class for SOFAArk. Extends `RuntimeException`.

### `ArkRuntimeException`
Runtime exception for non-recoverable errors during Ark container operation.

## Usage

Throw these exceptions when:
- Ark container fails to start
- Plugin or Biz fails to load/start
- Configuration errors
- ClassLoader errors

```java
throw new ArkRuntimeException("Failed to start Ark container");
```

## Used By

All SOFAArk modules use these exception types for consistent error handling.