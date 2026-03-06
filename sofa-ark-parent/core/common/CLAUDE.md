# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-common`
**Package**: `com.alipay.sofa.ark.common`

This module provides common utilities, logging, and shared functionality used across all SOFAArk modules.

## Purpose

- Utility classes for file operations, strings, assertions
- Logging infrastructure
- Common constants and helper methods

## Key Packages

### `common.util`
Utility classes:
- `StringUtils` - String manipulation utilities
- `FileUtils` - File I/O operations
- `AssertUtils` - Assertion helpers
- `BizIdentityUtils` - Business module identity parsing (`name:version` format)
- `ClassUtils` - Class loading utilities
- `EnvironmentUtils` - Environment variable handling

### `common.log`
Logging infrastructure:
- `ArkLogger` - Logger wrapper
- `ArkLoggerFactory` - Logger factory for creating Ark loggers

## Dependencies

- `sofa-ark-exception` - Exception definitions
- `log-sofa-boot-starter` - SOFA logging framework
- `slf4j-api` - Logging facade

## Used By

Almost all SOFAArk modules depend on this common utilities module:
- `sofa-ark-api`
- `sofa-ark-container`
- `sofa-ark-archive`
- `sofa-ark-maven-plugin`
- `sofa-ark-plugin-maven-plugin`