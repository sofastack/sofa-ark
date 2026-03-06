# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

**Artifact ID**: `sofa-ark-common-springboot`
**Package**: `com.alipay.sofa.ark.springboot`

This module provides common Spring Boot integration code shared across different Spring Boot versions.

## Purpose

- Conditional annotations for Ark-enabled features
- Spring Boot version detection

## Key Classes

### `condition.ConditionalOnArkEnabled`
Conditional annotation to enable features only when Ark is active:
```java
@ConditionalOnArkEnabled
@Bean
public MyBean myBean() { ... }
```

### `condition.OnArkEnabled`
Condition implementation that checks if Ark container is running.

### `condition.ConditionalOnSpringBootVersion`
Conditional annotation based on Spring Boot version:
```java
@ConditionalOnSpringBootVersion("2.x")
@Bean
public MyBean myBean() { ... }
```

### `condition.OnSpringBootVersion`
Condition implementation that matches specific Spring Boot versions.

## Dependencies

- `sofa-ark-spi` - Service interfaces
- Spring Boot (provided scope)

## Used By

- `ark-compatible-springboot1` - Spring Boot 1.x compatibility
- `ark-compatible-springboot2` - Spring Boot 2.x compatibility
- `ark-springboot-starter` - Main starter